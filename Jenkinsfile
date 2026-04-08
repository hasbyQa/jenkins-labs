#!/usr/bin/env groovy

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        // API tests are fast — 15 minutes is more than enough
        timeout(time: 15, unit: 'MINUTES')
    }

    triggers {
        // Trigger a build automatically on every GitHub push
        githubPush()
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Compiling project..."
                sh 'mvn clean compile -B -q'
                echo "Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "Running API test suite against fakestoreapi.com..."
                // catchError keeps the pipeline running so reports are always published
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'mvn test -B'
                }
            }
        }

        stage('Reports') {
            steps {
                echo "Publishing test reports..."

                // Publish JUnit results and capture counts for use in notifications
                script {
                    def results = junit testResults: 'target/surefire-reports/**/*.xml',
                                        allowEmptyResults: true
                    env.TOTAL_TESTS   = "${results.totalCount}"
                    env.FAILED_TESTS  = "${results.failCount}"
                    env.SKIPPED_TESTS = "${results.skipCount}"
                    env.PASSED_TESTS  = "${results.totalCount - results.failCount - results.skipCount}"
                    env.PASS_RATE     = results.totalCount > 0
                        ? "${(int)(((results.totalCount - results.failCount - results.skipCount) / results.totalCount) * 100)}"
                        : "0"
                    echo "Tests: total=${env.TOTAL_TESTS} passed=${env.PASSED_TESTS} failed=${env.FAILED_TESTS}"
                }

                // Publish Allure report — requires Allure Commandline configured in
                // Manage Jenkins > Tools > Allure Commandline > name: allure
                allure([
                    commandline      : 'allure',
                    results          : [[path: 'target/allure-results']],
                    reportBuildPolicy: 'ALWAYS',
                    includeProperties: false
                ])

                // Keep test result files for later review
                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**',
                                  allowEmptyArchive: true

                echo "Reports published!"
            }
        }
    }

    post {
        // cleanup always runs last — workspace is wiped after notifications fire
        cleanup {
            echo "Cleaning workspace..."
            cleanWs()
        }

        success {
            script {
                def commitShort = (env.GIT_COMMIT ?: 'unknown').take(10)

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "✅ BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
    .card { background: #ffffff; border-radius: 8px; max-width: 620px; margin: auto;
            box-shadow: 0 2px 8px rgba(0,0,0,0.12); overflow: hidden; }
    .banner { background: #2e7d32; padding: 24px 28px; color: #fff; }
    .banner h1 { margin: 0 0 4px; font-size: 22px; }
    .banner p  { margin: 0; font-size: 13px; opacity: 0.85; }
    .badge { display: inline-block; background: #a5d6a7; color: #1b5e20;
             border-radius: 4px; padding: 2px 10px; font-weight: bold; font-size: 13px; }
    .body  { padding: 24px 28px; }
    table  { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    th     { text-align: left; color: #555; font-size: 12px; text-transform: uppercase;
             letter-spacing: 0.5px; padding: 6px 0; border-bottom: 1px solid #e0e0e0; }
    td     { padding: 8px 0; font-size: 14px; color: #333; border-bottom: 1px solid #f0f0f0; }
    td.label { color: #777; width: 38%; }
    .stats { background: #f9fbe7; border-radius: 6px; padding: 16px 20px; margin-bottom: 20px; }
    .stats-grid { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 12px; text-align: center; }
    .stat-val  { font-size: 26px; font-weight: bold; color: #2e7d32; }
    .stat-fail { color: #c62828; }
    .stat-lbl  { font-size: 11px; color: #777; text-transform: uppercase; margin-top: 2px; }
    .buttons   { text-align: center; margin-top: 4px; }
    .btn { display: inline-block; padding: 10px 20px; border-radius: 5px; text-decoration: none;
           font-size: 13px; font-weight: bold; margin: 4px; }
    .btn-primary { background: #2e7d32; color: #fff; }
    .btn-outline { background: #fff; color: #2e7d32; border: 1px solid #2e7d32; }
    .footer { text-align: center; font-size: 11px; color: #aaa; padding: 14px; border-top: 1px solid #eee; }
  </style>
</head>
<body>
<div class="card">
  <div class="banner">
    <h1>&#9989; Build Passed</h1>
    <p>${JOB_NAME} &nbsp;|&nbsp; #${BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">SUCCESS</span></p>
  </div>
  <div class="body">
    <table>
      <tr><th colspan="2">Build Details</th></tr>
      <tr><td class="label">Branch</td><td>${env.GIT_BRANCH ?: 'unknown'}</td></tr>
      <tr><td class="label">Commit</td><td>${commitShort}</td></tr>
      <tr><td class="label">Build URL</td><td><a href="${BUILD_URL}">${BUILD_URL}</a></td></tr>
    </table>
    <div class="stats">
      <div class="stats-grid">
        <div><div class="stat-val">${env.TOTAL_TESTS ?: '0'}</div><div class="stat-lbl">Total</div></div>
        <div><div class="stat-val">${env.PASSED_TESTS ?: '0'}</div><div class="stat-lbl">Passed</div></div>
        <div><div class="stat-val stat-fail">${env.FAILED_TESTS ?: '0'}</div><div class="stat-lbl">Failed</div></div>
        <div><div class="stat-val">${env.PASS_RATE ?: '0'}%</div><div class="stat-lbl">Pass Rate</div></div>
      </div>
    </div>
    <div class="buttons">
      <a class="btn btn-primary" href="${BUILD_URL}allure/">&#128202; Allure Report</a>
      <a class="btn btn-outline" href="${BUILD_URL}testReport/">&#9989; JUnit Results</a>
      <a class="btn btn-outline" href="${BUILD_URL}console/">&#128196; Console</a>
    </div>
  </div>
  <div class="footer">Jenkins CI &bull; ${JOB_NAME}</div>
</div>
</body>
</html>""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "username": "Jenkins CI",
                            "icon_emoji": ":white_check_mark:",
                            "attachments": [
                              {
                                "color": "#2e7d32",
                                "blocks": [
                                  {
                                    "type": "header",
                                    "text": {"type": "plain_text", "text": ":white_check_mark:  Build Passed \u2014 Fake Store API Tests", "emoji": true}
                                  },
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                      {"type": "mrkdwn", "text": "*Build:*\n<'"${BUILD_URL}"'|#'"${BUILD_NUMBER}"'>"},
                                      {"type": "mrkdwn", "text": "*Branch:*\n`'"${GIT_BRANCH}"'`"},
                                      {"type": "mrkdwn", "text": "*Status:*\n:large_green_circle:  SUCCESS"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Total Tests*\n'"${TOTAL_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Passed :white_check_mark:*\n'"${PASSED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Failed :x:*\n'"${FAILED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Pass Rate*\n'"${PASS_RATE}"'%"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "actions",
                                    "elements": [
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":bar_chart: Allure Report", "emoji": true},
                                        "url": "'"${BUILD_URL}"'allure/",
                                        "style": "primary"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":memo: Test Results", "emoji": true},
                                        "url": "'"${BUILD_URL}"'testReport/"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":jenkins: Build Logs", "emoji": true},
                                        "url": "'"${BUILD_URL}"'console/"
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        failure {
            script {
                def commitShort = (env.GIT_COMMIT ?: 'unknown').take(10)

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "❌ BUILD FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
    .card { background: #ffffff; border-radius: 8px; max-width: 620px; margin: auto;
            box-shadow: 0 2px 8px rgba(0,0,0,0.12); overflow: hidden; }
    .banner { background: #b71c1c; padding: 24px 28px; color: #fff; }
    .banner h1 { margin: 0 0 4px; font-size: 22px; }
    .banner p  { margin: 0; font-size: 13px; opacity: 0.85; }
    .badge { display: inline-block; background: #ef9a9a; color: #7f0000;
             border-radius: 4px; padding: 2px 10px; font-weight: bold; font-size: 13px; }
    .body  { padding: 24px 28px; }
    table  { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    th     { text-align: left; color: #555; font-size: 12px; text-transform: uppercase;
             letter-spacing: 0.5px; padding: 6px 0; border-bottom: 1px solid #e0e0e0; }
    td     { padding: 8px 0; font-size: 14px; color: #333; border-bottom: 1px solid #f0f0f0; }
    td.label { color: #777; width: 38%; }
    .stats { background: #ffebee; border-radius: 6px; padding: 16px 20px; margin-bottom: 20px; }
    .stats-grid { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 12px; text-align: center; }
    .stat-val  { font-size: 26px; font-weight: bold; color: #b71c1c; }
    .stat-pass { color: #2e7d32; }
    .stat-lbl  { font-size: 11px; color: #777; text-transform: uppercase; margin-top: 2px; }
    .buttons   { text-align: center; margin-top: 4px; }
    .btn { display: inline-block; padding: 10px 20px; border-radius: 5px; text-decoration: none;
           font-size: 13px; font-weight: bold; margin: 4px; }
    .btn-primary { background: #b71c1c; color: #fff; }
    .btn-outline { background: #fff; color: #b71c1c; border: 1px solid #b71c1c; }
    .footer { text-align: center; font-size: 11px; color: #aaa; padding: 14px; border-top: 1px solid #eee; }
  </style>
</head>
<body>
<div class="card">
  <div class="banner">
    <h1>&#10060; Build Failed</h1>
    <p>${JOB_NAME} &nbsp;|&nbsp; #${BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">FAILURE</span></p>
  </div>
  <div class="body">
    <table>
      <tr><th colspan="2">Build Details</th></tr>
      <tr><td class="label">Branch</td><td>${env.GIT_BRANCH ?: 'unknown'}</td></tr>
      <tr><td class="label">Commit</td><td>${commitShort}</td></tr>
      <tr><td class="label">Build URL</td><td><a href="${BUILD_URL}">${BUILD_URL}</a></td></tr>
    </table>
    <div class="stats">
      <div class="stats-grid">
        <div><div class="stat-val">${env.TOTAL_TESTS ?: '0'}</div><div class="stat-lbl">Total</div></div>
        <div><div class="stat-val stat-pass">${env.PASSED_TESTS ?: '0'}</div><div class="stat-lbl">Passed</div></div>
        <div><div class="stat-val">${env.FAILED_TESTS ?: '0'}</div><div class="stat-lbl">Failed</div></div>
        <div><div class="stat-val">${env.PASS_RATE ?: '0'}%</div><div class="stat-lbl">Pass Rate</div></div>
      </div>
    </div>
    <div class="buttons">
      <a class="btn btn-primary" href="${BUILD_URL}allure/">&#128202; Allure Report</a>
      <a class="btn btn-outline" href="${BUILD_URL}testReport/">&#10060; JUnit Results</a>
      <a class="btn btn-outline" href="${BUILD_URL}console/">&#128196; Console</a>
    </div>
  </div>
  <div class="footer">Jenkins CI &bull; ${JOB_NAME}</div>
</div>
</body>
</html>""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "username": "Jenkins CI",
                            "icon_emoji": ":x:",
                            "attachments": [
                              {
                                "color": "#b71c1c",
                                "blocks": [
                                  {
                                    "type": "header",
                                    "text": {"type": "plain_text", "text": ":x:  Build Failed \u2014 Fake Store API Tests", "emoji": true}
                                  },
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                      {"type": "mrkdwn", "text": "*Build:*\n<'"${BUILD_URL}"'|#'"${BUILD_NUMBER}"'>"},
                                      {"type": "mrkdwn", "text": "*Branch:*\n`'"${GIT_BRANCH}"'`"},
                                      {"type": "mrkdwn", "text": "*Status:*\n:red_circle:  FAILURE"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Total Tests*\n'"${TOTAL_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Passed :white_check_mark:*\n'"${PASSED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Failed :x:*\n'"${FAILED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Pass Rate*\n'"${PASS_RATE}"'%"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "actions",
                                    "elements": [
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":bar_chart: Allure Report", "emoji": true},
                                        "url": "'"${BUILD_URL}"'allure/",
                                        "style": "danger"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":memo: Test Results", "emoji": true},
                                        "url": "'"${BUILD_URL}"'testReport/"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":jenkins: Build Logs", "emoji": true},
                                        "url": "'"${BUILD_URL}"'console/"
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }

        unstable {
            script {
                def commitShort = (env.GIT_COMMIT ?: 'unknown').take(10)

                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "⚠️ BUILD UNSTABLE: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 20px; }
    .card { background: #ffffff; border-radius: 8px; max-width: 620px; margin: auto;
            box-shadow: 0 2px 8px rgba(0,0,0,0.12); overflow: hidden; }
    .banner { background: #e65100; padding: 24px 28px; color: #fff; }
    .banner h1 { margin: 0 0 4px; font-size: 22px; }
    .banner p  { margin: 0; font-size: 13px; opacity: 0.85; }
    .badge { display: inline-block; background: #ffcc80; color: #bf360c;
             border-radius: 4px; padding: 2px 10px; font-weight: bold; font-size: 13px; }
    .body  { padding: 24px 28px; }
    table  { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
    th     { text-align: left; color: #555; font-size: 12px; text-transform: uppercase;
             letter-spacing: 0.5px; padding: 6px 0; border-bottom: 1px solid #e0e0e0; }
    td     { padding: 8px 0; font-size: 14px; color: #333; border-bottom: 1px solid #f0f0f0; }
    td.label { color: #777; width: 38%; }
    .stats { background: #fff8e1; border-radius: 6px; padding: 16px 20px; margin-bottom: 20px; }
    .stats-grid { display: grid; grid-template-columns: 1fr 1fr 1fr 1fr; gap: 12px; text-align: center; }
    .stat-val  { font-size: 26px; font-weight: bold; color: #e65100; }
    .stat-pass { color: #2e7d32; }
    .stat-fail { color: #c62828; }
    .stat-lbl  { font-size: 11px; color: #777; text-transform: uppercase; margin-top: 2px; }
    .buttons   { text-align: center; margin-top: 4px; }
    .btn { display: inline-block; padding: 10px 20px; border-radius: 5px; text-decoration: none;
           font-size: 13px; font-weight: bold; margin: 4px; }
    .btn-primary { background: #e65100; color: #fff; }
    .btn-outline { background: #fff; color: #e65100; border: 1px solid #e65100; }
    .footer { text-align: center; font-size: 11px; color: #aaa; padding: 14px; border-top: 1px solid #eee; }
  </style>
</head>
<body>
<div class="card">
  <div class="banner">
    <h1>&#9888; Build Unstable</h1>
    <p>${JOB_NAME} &nbsp;|&nbsp; #${BUILD_NUMBER} &nbsp;|&nbsp; <span class="badge">UNSTABLE</span></p>
  </div>
  <div class="body">
    <table>
      <tr><th colspan="2">Build Details</th></tr>
      <tr><td class="label">Branch</td><td>${env.GIT_BRANCH ?: 'unknown'}</td></tr>
      <tr><td class="label">Commit</td><td>${commitShort}</td></tr>
      <tr><td class="label">Build URL</td><td><a href="${BUILD_URL}">${BUILD_URL}</a></td></tr>
    </table>
    <div class="stats">
      <div class="stats-grid">
        <div><div class="stat-val">${env.TOTAL_TESTS ?: '0'}</div><div class="stat-lbl">Total</div></div>
        <div><div class="stat-val stat-pass">${env.PASSED_TESTS ?: '0'}</div><div class="stat-lbl">Passed</div></div>
        <div><div class="stat-val stat-fail">${env.FAILED_TESTS ?: '0'}</div><div class="stat-lbl">Failed</div></div>
        <div><div class="stat-val">${env.PASS_RATE ?: '0'}%</div><div class="stat-lbl">Pass Rate</div></div>
      </div>
    </div>
    <div class="buttons">
      <a class="btn btn-primary" href="${BUILD_URL}allure/">&#128202; Allure Report</a>
      <a class="btn btn-outline" href="${BUILD_URL}testReport/">&#9888; JUnit Results</a>
      <a class="btn btn-outline" href="${BUILD_URL}console/">&#128196; Console</a>
    </div>
  </div>
  <div class="footer">Jenkins CI &bull; ${JOB_NAME}</div>
</div>
</body>
</html>""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        replyTo: '$DEFAULT_REPLYTO',
                        from: '$DEFAULT_FROM',
                        mimeType: 'text/html'
                    )
                    echo "Email notification sent successfully"
                } catch (Exception e) {
                    echo "Email notification failed: ${e.message}"
                }

                // ── Slack ────────────────────────────────────────────────────
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK')]) {
                        sh '''
                        curl -s -X POST "${SLACK_WEBHOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "username": "Jenkins CI",
                            "icon_emoji": ":warning:",
                            "attachments": [
                              {
                                "color": "#e65100",
                                "blocks": [
                                  {
                                    "type": "header",
                                    "text": {"type": "plain_text", "text": ":warning:  Build Unstable \u2014 Fake Store API Tests", "emoji": true}
                                  },
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                      {"type": "mrkdwn", "text": "*Build:*\n<'"${BUILD_URL}"'|#'"${BUILD_NUMBER}"'>"},
                                      {"type": "mrkdwn", "text": "*Branch:*\n`'"${GIT_BRANCH}"'`"},
                                      {"type": "mrkdwn", "text": "*Status:*\n:large_yellow_circle:  UNSTABLE"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "section",
                                    "fields": [
                                      {"type": "mrkdwn", "text": "*Total Tests*\n'"${TOTAL_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Passed :white_check_mark:*\n'"${PASSED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Failed :x:*\n'"${FAILED_TESTS}"'"},
                                      {"type": "mrkdwn", "text": "*Pass Rate*\n'"${PASS_RATE}"'%"}
                                    ]
                                  },
                                  {"type": "divider"},
                                  {
                                    "type": "actions",
                                    "elements": [
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":bar_chart: Allure Report", "emoji": true},
                                        "url": "'"${BUILD_URL}"'allure/",
                                        "style": "danger"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":memo: Test Results", "emoji": true},
                                        "url": "'"${BUILD_URL}"'testReport/"
                                      },
                                      {
                                        "type": "button",
                                        "text": {"type": "plain_text", "text": ":jenkins: Build Logs", "emoji": true},
                                        "url": "'"${BUILD_URL}"'console/"
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "Slack notification sent successfully"
                } catch (Exception e) {
                    echo "Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
