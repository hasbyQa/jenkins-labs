#!/usr/bin/env groovy

// ── Read test counts stored by the Reports stage ─────────────────────────────
// Counts are set as env vars by the junit() step return value in Reports stage,
// so they are always available in post blocks without sandbox restrictions.
def getTestSummary() {
    return [
        total  : (env.TOTAL_TESTS  ?: '0').toInteger(),
        passed : (env.PASSED_TESTS ?: '0').toInteger(),
        failed : (env.FAILED_TESTS ?: '0').toInteger(),
        skipped: (env.SKIPPED_TESTS ?: '0').toInteger()
    ]
}

pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 45, unit: 'MINUTES')
    }

    triggers {
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
                echo "Building project with clean compilation..."
                sh 'mvn clean compile -B -q'
                echo "Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "Running test suite..."
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
                }
            }
        }

        stage('Reports') {
            steps {
                echo "Publishing test reports..."

                // Publish JUnit results and capture counts into env vars for post blocks.
                // junit() returns a TestResultSummary — storing here avoids sandbox issues
                // with currentBuild.testResultAction in post blocks.
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
                    echo "Tests: total=${env.TOTAL_TESTS} passed=${env.PASSED_TESTS} failed=${env.FAILED_TESTS} skipped=${env.SKIPPED_TESTS}"
                }

                // Publish Allure report using the Jenkins Allure plugin.
                // Requires: Manage Jenkins → Tools → Allure Commandline → name: "allure"
                // Report is served at ${BUILD_URL}allure/
                allure([
                    commandline        : 'allure',
                    results            : [[path: 'target/allure-results']],
                    reportBuildPolicy  : 'ALWAYS',
                    includeProperties  : false
                ])

                archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml, target/allure-results/**',
                                  allowEmptyArchive: true

                echo "Reports published!"
            }
        }
    }

    post {
        // cleanup runs last — after success/failure/unstable — so getTestSummary()
        // can still read surefire XMLs from the workspace before it is wiped
        cleanup {
            echo "Cleaning workspace..."
            cleanWs()
        }

        success {
            script {
                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD SUCCESSFULLY COMPLETED

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : SUCCESS
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total    : ${env.TOTAL_TESTS ?: '0'}
  Passed   : ${env.PASSED_TESTS ?: '0'}
  Failed   : ${env.FAILED_TESTS ?: '0'}
  Skipped  : ${env.SKIPPED_TESTS ?: '0'}
  Pass Rate: ${env.PASS_RATE ?: '0'}%

REPORTS:
  Allure Report : ${BUILD_URL}allure/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
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
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {"type": "plain_text", "text": "BUILD PASSED - Swag Labs Tests", "emoji": true}
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nSUCCESS"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'allure/",
                                    "style": "primary"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Jenkins Build", "emoji": true},
                                    "url": "'"${BUILD_URL}"'"
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
                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD FAILED

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : FAILURE
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total    : ${env.TOTAL_TESTS ?: '0'}
  Passed   : ${env.PASSED_TESTS ?: '0'}
  Failed   : ${env.FAILED_TESTS ?: '0'}
  Skipped  : ${env.SKIPPED_TESTS ?: '0'}
  Pass Rate: ${env.PASS_RATE ?: '0'}%


REPORTS:
  Allure Report : ${BUILD_URL}allure/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
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
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {"type": "plain_text", "text": "BUILD FAILED - Swag Labs Tests", "emoji": true}
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nFAILURE"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'allure/",
                                    "style": "danger"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Build Logs", "emoji": true},
                                    "url": "'"${BUILD_URL}"'console"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "Test Results", "emoji": true},
                                    "url": "'"${BUILD_URL}"'testReport"
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
                // ── Email ────────────────────────────────────────────────────
                try {
                    emailext(
                        subject: "BUILD UNSTABLE: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """BUILD UNSTABLE - Some Tests Failed

Job Name   : ${JOB_NAME}
Build      : #${BUILD_NUMBER}
Status     : UNSTABLE
Branch     : ${env.GIT_BRANCH ?: 'unknown'}
Commit     : ${env.GIT_COMMIT ?: 'unknown'}

TEST RESULTS:
  Total    : ${env.TOTAL_TESTS ?: '0'}
  Passed   : ${env.PASSED_TESTS ?: '0'}
  Failed   : ${env.FAILED_TESTS ?: '0'}
  Skipped  : ${env.SKIPPED_TESTS ?: '0'}
  Pass Rate: ${env.PASS_RATE ?: '0'}%


REPORTS:
  Allure Report : ${BUILD_URL}allure/
  JUnit Results : ${BUILD_URL}testReport/
  Console Output: ${BUILD_URL}console/

Full details: ${BUILD_URL}""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
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
                            "channel": "#builds",
                            "username": "Jenkins Bot",
                            "icon_emoji": ":jenkins:",
                            "blocks": [
                              {
                                "type": "header",
                                "text": {"type": "plain_text", "text": "BUILD UNSTABLE - Swag Labs Tests", "emoji": true}
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Job:*\n'"${JOB_NAME}"'"},
                                  {"type": "mrkdwn", "text": "*Build:*\n#'"${BUILD_NUMBER}"'"},
                                  {"type": "mrkdwn", "text": "*Branch:*\n'"${GIT_BRANCH}"'"},
                                  {"type": "mrkdwn", "text": "*Status:*\nUNSTABLE"}
                                ]
                              },
                              {
                                "type": "section",
                                "fields": [
                                  {"type": "mrkdwn", "text": "*Total Tests:*\n'"${TOTAL_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Passed:*\n'"${PASSED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Failed:*\n'"${FAILED_TESTS}"'"},
                                  {"type": "mrkdwn", "text": "*Pass Rate:*\n'"${PASS_RATE}"'%"}
                                ]
                              },
                              {"type": "divider"},
                              {
                                "type": "actions",
                                "elements": [
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Allure Report", "emoji": true},
                                    "url": "'"${BUILD_URL}"'allure/",
                                    "style": "danger"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "View Build Logs", "emoji": true},
                                    "url": "'"${BUILD_URL}"'console"
                                  },
                                  {
                                    "type": "button",
                                    "text": {"type": "plain_text", "text": "Test Results", "emoji": true},
                                    "url": "'"${BUILD_URL}"'testReport"
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
