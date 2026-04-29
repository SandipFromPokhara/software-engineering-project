pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    tools {
        maven 'MAVEN_HOME'
    }

    environment {

        SONARQUBE_SERVER = 'SonarQubeServer'
        SONAR_TOKEN = credentials('sonar-token')

        DB_HOST = '127.0.0.1'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
        DB_CREDENTIALS_ID = 'db-credentials'
        DOCKERHUB_CREDENTIALS_ID = 'docker_Id'
        DOCKERHUB_REPO = 'dinal1999/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-sinhala', url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile'
                    } else {
                        bat 'mvn clean compile'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    withCredentials([usernamePassword(
                            credentialsId: DB_CREDENTIALS_ID,
                            usernameVariable: 'DB_USER',
                            passwordVariable: 'DB_PASSWORD'
                    )]) {
                        if (isUnix()) {
                            sh 'mvn test -DDB_USER=$DB_USER -DDB_PASSWORD=$DB_PASSWORD -DDB_HOST=$DB_HOST -DDB_PORT=$DB_PORT -DDB_NAME=$DB_NAME'
                        } else {
                            bat 'mvn test -DDB_USER=%DB_USER% -DDB_PASSWORD=%DB_PASSWORD% -DDB_HOST=%DB_HOST% -DDB_PORT=%DB_PORT% -DDB_NAME=%DB_NAME%'
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }

        stage('Code Coverage') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn jacoco:report'
                    } else {
                        bat 'mvn jacoco:report'
                    }
                }
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(SONARQUBE_SERVER) {
                    script {
                        if (isUnix()) {
                            sh 'mvn clean verify sonar:sonar'
                        } else {
                            bat 'mvn clean verify sonar:sonar'
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    if (isUnix()) {
                        sh '''
                            docker build --pull -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
                            docker images
                            '''
                    } else {
                        bat """
                        REM --- Build Docker image with build number tag ---
                        docker build --pull -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% .
        
                        REM --- Verify image exists ---
                        docker images
                    """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    withCredentials([usernamePassword(
                            credentialsId: DOCKERHUB_CREDENTIALS_ID,
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASS'

                    )]) {
                        if (isUnix()) {
                            sh '''
                                echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                                
                                docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}
                                
                                docker tag ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ${DOCKERHUB_REPO}:latest
                        
                                docker push ${DOCKERHUB_REPO}:latest
                                
                                docker image rm ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}
                                docker image prune -f
                                '''
                        } else {
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
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            jacoco()
            cleanWs()
        }
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}