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
                sh 'docker --version'
                sh 'which docker'
            }
        }

        stage('Run Tests') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'sep1',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    sh """
                        mvn clean test \
                        -DDB_USER=$DB_USER \
                        -DDB_PASSWORD=$DB_PASSWORD
                    """
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'mvn jacoco:report'
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
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
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
                sh "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true"
                sh "docker rmi ${DOCKERHUB_REPO}:latest || true"
            }
        }
    }
}
