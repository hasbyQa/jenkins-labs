pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    triggers {
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                echo "📦 Checking out code from Git..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "🔨 Building project with Maven..."
                sh 'mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                echo "🧪 Running tests..."
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
                }
            }
            post {
                always {
                    echo "📊 Publishing test results..."
                    junit testResults: 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Allure Report') {
            steps {
                script {
                    echo "📈 Generating Allure report..."
                    sh 'mvn allure:report -B -DskipTests || true'
                    
                    // Check if report was generated
                    if (fileExists('target/site/allure-report/index.html')) {
                        echo "✅ Allure report generated successfully"
                    } else {
                        echo "⚠️ Allure report not found - tests may have failed"
                    }
                    
                    // Archive all artifacts
                    archiveArtifacts artifacts: 'target/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        always {
            script {
                echo "═══════════════════════════════════════════════════════════"
                echo "�� Cleaning up workspace..."
                cleanWs(deleteDirs: true)
            }
        }
        
        success {
            script {
                echo "✅ Build Successful!"
                
                // Jenkins Allure plugin will automatically display reports
                // Access via: Jenkins UI → Job → Allure Report link
                
                // Slack notification
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
                        sh '''
                        curl -X POST "${SLACK_URL}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#36a64f",
                                "title": "✅ BUILD PASSED",
                                "title_link": "'"${BUILD_URL}"'",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "✅ SUCCESS", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Build", "url": "'"${BUILD_URL}"'", "style": "primary"},
                                  {"type": "button", "text": "View Allure Report", "url": "'"${BUILD_URL}"'allure/", "style": "good"}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent"
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
                
                // Email notification
                try {
                    emailext(
                        subject: "✅ BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """BUILD SUCCESSFULLY COMPLETED ✅

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ✅ SUCCESS
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST RESULTS:
  ✅ All tests completed successfully!
  
DETAILED REPORTS:
  📊 Allure Test Report: ${env.BUILD_URL}allure/
  📈 JUnit Results: ${env.BUILD_URL}testReport/
  🔍 Console Output: ${env.BUILD_URL}console/

═══════════════════════════════════════════════════════════

BUILD ARTIFACTS:
  • Test Results: Available in Jenkins UI
  • Detailed Test Logs: Check Allure Report in Jenkins
  • Build Duration: ${currentBuild.durationString}

═══════════════════════════════════════════════════════════

For more details, visit: ${env.BUILD_URL}

Thank you!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "✅ Email notification sent"
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
        
        failure {
            script {
                echo "❌ Build Failed!"
                
                // Slack notification
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
                        sh '''
                        curl -X POST "${SLACK_URL}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#ff0000",
                                "title": "❌ BUILD FAILED",
                                "title_link": "'"${BUILD_URL}"'console/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "❌ FAILURE", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Console", "url": "'"${BUILD_URL}"'console/", "style": "danger"},
                                  {"type": "button", "text": "View Tests", "url": "'"${BUILD_URL}"'testReport/"}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent"
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
                
                // Email notification
                try {
                    emailext(
                        subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """BUILD FAILED ❌

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ❌ FAILURE
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

FAILURE ANALYSIS:
  1. Review the Console Output for error messages
  2. Check Test Results for failed tests
  3. Review Allure Report for detailed logs and screenshots

═══════════════════════════════════════════════════════════

ACTIONS:
  • View Console: ${env.BUILD_URL}console/
  • Check Tests: ${env.BUILD_URL}testReport/
  • View Allure: ${env.BUILD_URL}allure/
  • Build Details: ${env.BUILD_URL}

═══════════════════════════════════════════════════════════

For more details, visit: ${env.BUILD_URL}

Need help? Review the console output for error details!""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "✅ Email notification sent"
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
        
        unstable {
            script {
                echo "⚠️ Build is Unstable!"
                
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
                        sh '''
                        curl -X POST "${SLACK_URL}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins",
                            "icon_emoji": ":jenkins:",
                            "attachments": [
                              {
                                "color": "#ff9800",
                                "title": "⚠️ BUILD UNSTABLE",
                                "title_link": "'"${BUILD_URL}"'testReport/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "⚠️ UNSTABLE", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "Review Tests", "url": "'"${BUILD_URL}"'testReport/", "style": "warning"},
                                  {"type": "button", "text": "View Allure", "url": "'"${BUILD_URL}"'allure/"}
                                ]
                              }
                            ]
                          }'
                        '''
                    }
                    echo "✅ Slack notification sent"
                } catch (Exception e) {
                    echo "⚠️ Slack notification failed: ${e.message}"
                }
            }
        }
    }
}
