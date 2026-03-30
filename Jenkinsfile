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
                sh 'mvn test -B'
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
        }
        
        failure {
            echo '❌ Build or tests failed!'
        }
    }
}
