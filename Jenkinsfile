pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        githubPush()
    }

    environment {
        BUILD_DETAILS = "Build #${BUILD_NUMBER} - ${GIT_BRANCH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile -B'
            }
        }

        stage('Test') {
            steps {
                sh 'SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200'
            }
        }

        stage('Report') {
            steps {
                junit 'target/surefire-reports/**/*.xml'
                archiveArtifacts artifacts: 'target/**', allowEmptyArchive: true
                
                // Generate Allure reports
                script {
                    sh 'mvn allure:report || true'
                }
                
                // Publish Allure Report to Jenkins
                publishHTML([
                    reportDir: 'target/site/allure-report',
                    reportFiles: 'index.html',
                    reportName: 'Allure Test Report',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ])
                
                echo '✅ Test reports generated successfully!'
            }
        }
    }

    post {
        always {
            script {
                // Archive test results
                junit 'target/surefire-reports/**/*.xml', allowEmptyResults: true
                archiveArtifacts artifacts: 'target/site/allure-report/**', allowEmptyArchive: true
            }
            cleanWs()
        }
        
        success {
            script {
                echo '✅ Build and tests passed!'
                
                // Get test summary
                def testSummary = readFile('target/surefire-reports/TEST-*.xml') ?: 'No test data'
                
                // Send Email Notification - SUCCESS
                try {
                    mail(
                        subject: "✅ BUILD PASSED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """
BUILD SUCCESSFULLY COMPLETED ✅

═══════════════════════════════════════════════════════════════

🎯 JOB DETAILS:
   Job Name: ${JOB_NAME}
   Build Number: #${BUILD_NUMBER}
   Status: ✅ SUCCESS
   Branch: ${GIT_BRANCH}
   Commit: ${GIT_COMMIT}

═══════════════════════════════════════════════════════════════

✅ TEST RESULTS:
   All tests passed successfully!
   Duration: ${currentBuild.durationString}

═══════════════════════════════════════════════════════════════

📊 DETAILED REPORTS AVAILABLE:
   
   1. Allure Test Report:
      ${BUILD_URL}Allure_Test_Report/
      
   2. JUnit Test Results:
      ${BUILD_URL}testReport/
      
   3. Build Console Output:
      ${BUILD_URL}console/
      
   4. Artifacts & Logs:
      ${BUILD_URL}artifact/

═══════════════════════════════════════════════════════════════

🔗 BUILD LINK: ${BUILD_URL}

Thank you!
""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        from: 'jenkins@localhost',
                        charset: 'UTF-8'
                    )
                    echo '✅ Email notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Email notification error: ${e.message}"
                }
                
                // Send Slack Notification - SUCCESS
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_HOOK')]) {
                        sh '''
                        curl -X POST "${SLACK_HOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins CI/CD",
                            "icon_emoji": ":green_heart:",
                            "attachments": [
                              {
                                "color": "#36a64f",
                                "title": "✅ BUILD PASSED",
                                "title_link": "'"${BUILD_URL}"'",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "✅ SUCCESS", "short": true},
                                  {"title": "Reports", "value": "<'"${BUILD_URL}"'testReport|JUnit> | <'"${BUILD_URL}"'Allure_Test_Report|Allure>", "short": false}
                                ],
                                "footer": "Jenkins",
                                "ts": '"$(date +%s)"'
                              }
                            ]
                          }'
                        '''
                    }
                    echo '✅ Slack notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Slack notification error: ${e.message}"
                }
            }
        }
        
        failure {
            script {
                echo '❌ Build or tests failed!'
                
                // Send Email Notification - FAILURE
                try {
                    mail(
                        subject: "❌ BUILD FAILED: ${JOB_NAME} #${BUILD_NUMBER}",
                        body: """
BUILD FAILED ❌

═══════════════════════════════════════════════════════════════

🎯 JOB DETAILS:
   Job Name: ${JOB_NAME}
   Build Number: #${BUILD_NUMBER}
   Status: ❌ FAILURE
   Branch: ${GIT_BRANCH}
   Commit: ${GIT_COMMIT}

═══════════════════════════════════════════════════════════════

❌ TEST RESULTS:
   Some tests failed or build encountered errors!
   Duration: ${currentBuild.durationString}

═══════════════════════════════════════════════════════════════

📊 DETAILED REPORTS & DIAGNOSTIC INFORMATION:
   
   1. Allure Test Report (with failed test details):
      ${BUILD_URL}Allure_Test_Report/
      
   2. JUnit Test Results:
      ${BUILD_URL}testReport/
      
   3. Build Console Output (error messages):
      ${BUILD_URL}console/
      
   4. Complete Build Artifacts:
      ${BUILD_URL}artifact/

═══════════════════════════════════════════════════════════════

🔍 TROUBLESHOOTING STEPS:

   Step 1: Check Console Output
      Look for error stack traces and failure messages
      
   Step 2: Review Failed Tests in Allure
      Click on failed test to see detailed logs
      
   Step 3: Check Test Logs
      Each test has associated logs in Allure Report
      
   Step 4: Fix Issues and Commit
      Once fixed, push changes to trigger new build

═══════════════════════════════════════════════════════════════

🔗 BUILD LINK: ${BUILD_URL}

Need help? Review the error messages in console output!
""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        from: 'jenkins@localhost',
                        charset: 'UTF-8'
                    )
                    echo '✅ Email notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Email notification error: ${e.message}"
                }
                
                // Send Slack Notification - FAILURE
                try {
                    withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_HOOK')]) {
                        sh '''
                        curl -X POST "${SLACK_HOOK}" \
                          -H 'Content-Type: application/json' \
                          -d '{
                            "channel": "#builds",
                            "username": "Jenkins CI/CD",
                            "icon_emoji": ":red_circle:",
                            "attachments": [
                              {
                                "color": "#ff0000",
                                "title": "❌ BUILD FAILED",
                                "title_link": "'"${BUILD_URL}"'",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "❌ FAILURE", "short": true},
                                  {"title": "Reports", "value": "<'"${BUILD_URL}"'testReport|JUnit> | <'"${BUILD_URL}"'Allure_Test_Report|Allure>", "short": false},
                                  {"title": "Action", "value": "Click title link to view details and console output", "short": false}
                                ],
                                "footer": "Jenkins",
                                "ts": '"$(date +%s)"'
                              }
                            ]
                          }'
                        '''
                    }
                    echo '✅ Slack notification sent successfully'
                } catch (Exception e) {
                    echo "⚠️ Slack notification error: ${e.message}"
                }
            }
        }
    }
}
