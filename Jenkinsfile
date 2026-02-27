pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'docker_jenkins'     // Jenkins Docker Hub credentials ID
        DOCKERHUB_REPO = 'swostikalama/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'edit-test',
                    url: 'git@github.com:SandipFromPokhara/software-engineering-project.git',
                    credentialsId: 'private'
            }
        }

        stage('Run Tests') {
            steps {
                // Use single Jenkins credential 'sep1' for DB username and password
                withCredentials([usernamePassword(credentialsId: 'sep1', usernameVariable: 'DB_USER', passwordVariable: 'DB_PASSWORD')]) {
                    sh 'mvn clean test -DDB_USER=$DB_USER -DDB_PASSWORD=$DB_PASSWORD'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report -Djava.awt.headless=true'
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

        stage('Build Docker Image (amd64)') {
            steps {
                script {
                    // Ensure Buildx is enabled for cross-platform builds
                    sh """
                        docker buildx create --use || true
                        docker buildx build --platform linux/amd64 -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
                    """
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        sh "docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                        sh "docker tag ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ${DOCKERHUB_REPO}:latest"
                        sh "docker push ${DOCKERHUB_REPO}:latest"
                    }
                }
            }
        }

        stage('Cleanup Docker Images') {
            steps {
                script {
                    sh "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true"
                    sh "docker rmi ${DOCKERHUB_REPO}:latest || true"
                }
            }
        }
    }
}