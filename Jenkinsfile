pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        // Use your preconfigured local DB for testing
        DB_HOST = '127.0.0.1'
        DB_PORT = '3307'
        DB_NAME = 'notevault_test_db'
        DB_USER = 'localuser'            // your local DB username
        DB_PASSWORD = 'localpass'        // your local DB password
        BCRYPT_COST = '12'
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-dev',
                        url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Build & Test') {
            steps {
                bat """
                mvn clean verify -Djava.awt.headless=true ^
                    -DDB_USER=%DB_USER% ^
                    -DDB_PASSWORD=%DB_PASSWORD% ^
                    -DDB_HOST=%DB_HOST% ^
                    -DDB_PORT=%DB_PORT% ^
                    -DDB_NAME=%DB_NAME%
                """
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
            }
        }

        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% ."
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                    docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                    docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%
                    docker tag %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% %DOCKERHUB_REPO%:latest
                    docker push %DOCKERHUB_REPO%:latest
                    """
                }
            }
        }
    }
}