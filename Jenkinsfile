pipeline {
    agent any

    environment {
        PATH = "/usr/local/bin:$PATH"
        DOCKERHUB_CREDENTIALS_ID = 'docker-jenkins'
        DOCKERHUB_REPO = 'swostikalama/jenkins_temp'
        DOCKER_IMAGE_TAG = 'latest'
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
                git branch: 'main',
                    url: 'https://github.com/Swostika-Lama/Jenkins_Temp.git'
            }
        }

        stage('Build') {
            steps {
                dir('Temperature') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Test') {
            steps {
                dir('Temperature') {
                    sh 'mvn test'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                dir('Temperature') {
                    sh 'mvn jacoco:report'
                }
            }
        }

        stage('Publish Test Results') {
            steps {
                junit 'Temperature/target/surefire-reports/*.xml'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }



        stage('Build Docker Image') {
            steps {
                dir('Temperature') {
                    sh '''
                        docker build \
                            -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
                    '''
                }
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
