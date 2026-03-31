pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
    }

    triggers {
        githubPush()
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
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        
        success {
            echo '✅ Build and tests passed!'
            emailext(
                subject: "✅ Build #${BUILD_NUMBER} PASSED - ${JOB_NAME}",
                body: """
                    Build #${BUILD_NUMBER} PASSED
                    
                    Job: ${JOB_NAME}
                    Status: SUCCESS
                    Branch: ${GIT_BRANCH}
                    Commit: ${GIT_COMMIT}
                    
                    Details: ${BUILD_URL}
                    Console Output: ${BUILD_URL}console
                """,
                to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                mimeType: 'text/plain'
            )
        }
        
        failure {
            echo '❌ Build or tests failed!'
            emailext(
                subject: "❌ Build #${BUILD_NUMBER} FAILED - ${JOB_NAME}",
                body: """
                    Build #${BUILD_NUMBER} FAILED
                    
                    Job: ${JOB_NAME}
                    Status: FAILURE
                    Branch: ${GIT_BRANCH}
                    Commit: ${GIT_COMMIT}
                    
                    Details: ${BUILD_URL}
                    Console Output: ${BUILD_URL}console
                    
                    Please check the logs and fix the issues.
                """,
                to: 'hasbiyallah.umutoniwabo@amalitechtraining.org',
                mimeType: 'text/plain'
            )
        }
    }
}
