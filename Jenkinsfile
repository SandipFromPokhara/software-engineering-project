
pipeline {
    agent any


    environment {
        PATH = "/usr/local/bin:$PATH"
        DOCKERHUB_CREDENTIALS_ID = 'docker-jenkins'
        DOCKERHUB_REPO = 'swostikalama/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
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

        stage('Checkout') {
            steps {
                git branch: 'edit-test',
                url: 'git@github.com:SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test'
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

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}", ".")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push('latest')
                    }
                }
            }
        }

        stage('Cleanup Docker Images') {
            steps {
                script {
                    // Remove local images to save disk space
                    sh "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true"
                    sh "docker rmi ${DOCKERHUB_REPO}:latest || true"
                }
            }
        }
    }
}
