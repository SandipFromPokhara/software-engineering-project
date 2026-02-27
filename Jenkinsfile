pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
        jdk 'JDK21'
    }

    environment {
        JAVA_HOME = tool 'JDK21'
        PATH = "${env.JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"

        DOCKERHUB_CREDENTIALS_ID = 'docker_jenkins'
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

        stage('Verify Java & Docker') {
            steps {
                sh 'java -version'
                sh 'which docker'
                sh 'docker --version'
            }
        }

        stage('Run Tests') {
            steps {
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
                    sh """
                        docker buildx create --use || true
                        docker buildx build --platform linux/amd64 \
                            -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} \
                            --push .
                    """
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh """
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker tag ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ${DOCKERHUB_REPO}:latest
                        docker push ${DOCKERHUB_REPO}:latest
                    """
                }
            }
        }

        stage('Cleanup Docker Images') {
            steps {
                sh "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true"
                sh "docker rmi ${DOCKERHUB_REPO}:latest || true"
            }
        }
    }
}
