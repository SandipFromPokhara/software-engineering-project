pipeline {
    agent any

    environment {
        JAVA_HOME = "/opt/homebrew/opt/openjdk"
        PATH = "/usr/local/bin:/opt/homebrew/bin:/opt/homebrew/sbin:${env.JAVA_HOME}/bin:${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'docker-jenkins'
        DOCKERHUB_REPO = 'swostikalama/notevault'
        DOCKER_IMAGE_TAG = 'latest'
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }


    tools {
        maven 'MAVEN_HOME'
    }

    stages {

        stage('Check Docker') {
            steps {
                sh 'docker --version'
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'edit-test',
                    url: 'git@github.com:SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -Djavafx.platform=mac'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Djavafx.platform=mac'
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report -Djavafx.platform=mac'
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

        /* -------------------------
           SEPARATE DOCKER STAGES
           ------------------------- */

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build \
                        -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
                '''
            }
        }

        stage('Push Docker Image to docker_jenkins') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Cleanup Docker Images') {
            steps {
                sh '''
                    docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true
                    docker rmi ${DOCKERHUB_REPO}:latest || true
                '''
            }
        }
    }
}
