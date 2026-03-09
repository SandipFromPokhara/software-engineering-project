pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = '127.0.0.1'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
        DB_CREDENTIALS_ID = 'DB_CREDENTIALS'
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-dev', url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Test') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: DB_CREDENTIALS_ID,
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                )]) {
                    bat 'mvn test -DDB_USER=%DB_USER% -DDB_PASSWORD=%DB_PASSWORD% -DDB_HOST=%DB_HOST% -DDB_PORT=%DB_PORT% -DDB_NAME=%DB_NAME%'
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn package -DskipTests'
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
                bat """
                    REM --- Build Docker image with build number tag ---
                    docker build --pull -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% .
    
                    REM --- Verify image exists ---
                    docker images
                """
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: DOCKERHUB_CREDENTIALS_ID,
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'

                )]) {
                    bat """
                        REM --- Login to Docker Hub ---
                        echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin
                        
                        REM --- Push image with build number tag ---
                        echo Pushing Docker image %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%...
                        docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%
                       
                        REM --- Tag as latest and push ---
                        docker tag %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% %DOCKERHUB_REPO%:latest
                        docker push %DOCKERHUB_REPO%:latest
                        
                        REM --- Cleanup local images to save disk space ---
                        docker image rm %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%
                        docker image prune -f
                    """
                }
            }
        }
    }
}