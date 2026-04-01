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
                echo "📥 Checking out code from GitHub..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "🔨 Building project..."
                sh 'mvn clean compile -B -q'
                echo "✅ Build completed successfully"
            }
        }

        stage('Test') {
            steps {
                echo "🧪 Running test suite..."
                sh '''
                    set +e
                    SELENIUM_TIMEOUT=60 mvn test -B -Dmaven.surefire.timeout=1200
                    TEST_RESULT=$?
                    set -e
                    exit $TEST_RESULT
                '''
            }
            post {
                always {
                    echo "📊 Recording test results..."
                    junit testResults: 'target/surefire-reports/**/*.xml', 
                          allowEmptyResults: true,
                          skipPublishingChecks: true
                }
            }
        }

        stage('Allure Report') {
            steps {
                script {
                    echo "📈 Generating Allure report..."
                    sh '''
                        mvn allure:report -B -DskipTests=true -q || {
                            echo "⚠️ Allure report generation had issues, continuing..."
                            exit 0
                        }
                    '''
                    
                    if (fileExists('target/site/allure-report/index.html')) {
                        echo "✅ Allure report generated successfully"
                    } else {
                        echo "⚠️ Allure report not found, creating placeholder..."
                        sh '''
                            mkdir -p target/site/allure-report
                            echo "<html><body><h1>Build #${BUILD_NUMBER}</h1><p>Report generation in progress...</p></body></html>" > target/site/allure-report/index.html
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            echo "🧹 Archiving artifacts and reports..."
            
            // Archive test results
            archiveArtifacts artifacts: 'target/surefire-reports/**/*.xml,target/allure-results/**',
                              allowEmptyArchive: true,
                              onlyIfSuccessful: false
            
            // Publish Allure report using Jenkins plugin
            step([
                $class: 'io.qameta.allure.jenkins.steps.PublishAllureStep',
                includeProperties: false,
                jdk: '',
                results: [[path: 'target/allure-results']]
            ])
            
            echo "✅ Reports published to Jenkins"
        }
        
        success {
            script {
                echo "✅ Build Successful!"
                
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
                                "title_link": "'"${BUILD_URL}"'allure/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "✅ SUCCESS", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Allure Report", "url": "'"${BUILD_URL}"'allure/", "style": "primary"},
                                  {"type": "button", "text": "Console Log", "url": "'"${BUILD_URL}"'console/"}
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
                
                try {
                    emailext(
                        subject: "✅ BUILD PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
BUILD SUCCESSFULLY COMPLETED ✅

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ✅ SUCCESS
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST & REPORT LINKS:
  📊 Allure Report: ${env.BUILD_URL}allure/
  📈 JUnit Results: ${env.BUILD_URL}testReport/
  🔍 Console Output: ${env.BUILD_URL}console/

═══════════════════════════════════════════════════════════

BUILD DURATION: ${currentBuild.durationString}

For more details, visit: ${env.BUILD_URL}

Thank you!
""",
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
                echo "⚠️ Build Unstable - Some tests failed!"
                
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
                                "color": "#FFA500",
                                "title": "⚠️ BUILD UNSTABLE",
                                "title_link": "'"${BUILD_URL}"'allure/",
                                "fields": [
                                  {"title": "Job", "value": "'"${JOB_NAME}"'", "short": true},
                                  {"title": "Build", "value": "#'"${BUILD_NUMBER}"'", "short": true},
                                  {"title": "Branch", "value": "'"${GIT_BRANCH}"'", "short": true},
                                  {"title": "Status", "value": "⚠️ UNSTABLE (Tests Failed)", "short": true}
                                ],
                                "actions": [
                                  {"type": "button", "text": "View Allure Report", "url": "'"${BUILD_URL}"'allure/", "style": "primary"},
                                  {"type": "button", "text": "Test Results", "url": "'"${BUILD_URL}"'testReport/"}
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
                
                try {
                    emailext(
                        subject: "⚠️ BUILD UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
BUILD UNSTABLE - TESTS FAILED ⚠️

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ⚠️ UNSTABLE
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

TEST & REPORT LINKS:
  📊 Allure Report: ${env.BUILD_URL}allure/
  📈 JUnit Results: ${env.BUILD_URL}testReport/
  🔍 Console Output: ${env.BUILD_URL}console/

═══════════════════════════════════════════════════════════

FAILURE ANALYSIS:
  1. Review the Allure Report for detailed test failures
  2. Check Test Results for specific test errors
  3. Review Console Output for debug information

ACTION REQUIRED:
  Please fix the failing tests!

═══════════════════════════════════════════════════════════

For more details, visit: ${env.BUILD_URL}
""",
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
                                  {"type": "button", "text": "View Console", "url": "'"${BUILD_URL}"'console/", "style": "danger"}
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
                
                try {
                    emailext(
                        subject: "❌ BUILD FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                        body: """
BUILD FAILED ❌

═══════════════════════════════════════════════════════════

JOB DETAILS:
  • Job Name: ${env.JOB_NAME}
  • Build Number: #${env.BUILD_NUMBER}
  • Status: ❌ FAILURE
  • Branch: ${env.GIT_BRANCH}
  • Commit: ${env.GIT_COMMIT}

═══════════════════════════════════════════════════════════

FAILURE ANALYSIS:
  1. Build or compilation failed
  2. Check Console Output for error details
  3. Review compilation logs

ACTION REQUIRED:
  Please fix the build errors!

═══════════════════════════════════════════════════════════

LINKS:
  🔍 Console Output: ${env.BUILD_URL}console/
  📋 Build Details: ${env.BUILD_URL}

For more details, visit: ${env.BUILD_URL}
""",
                        to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                        mimeType: 'text/plain'
                    )
                    echo "✅ Email notification sent"
                } catch (Exception e) {
                    echo "⚠️ Email failed: ${e.message}"
                }
            }
        }
        
        cleanup {
            cleanWs(deleteDirs: true, patterns: [[pattern: '**', type: 'INCLUDE']])
        }
    }
}
