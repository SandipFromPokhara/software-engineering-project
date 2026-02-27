pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = 'localhost'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
        // Path to Docker CLI on Windows
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-dev', url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Start Test DB') {
            steps {
                withCredentials([
                        string(credentialsId: 'DB_USER', variable: 'DB_USER'),
                        string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
                ]) {
                    bat """
                    docker run -d --name test-mariadb ^
                     -e MYSQL_ROOT_PASSWORD=root ^
                     -e MYSQL_DATABASE=%DB_NAME% ^
                     -e MYSQL_USER=%DB_USER% ^
                     -e MYSQL_PASSWORD=%DB_PASSWORD% ^
                     -p 3306:3306 ^
                     mariadb:10.11
                    """

                    echo "Waiting for MariaDB to start..."
                    bat 'ping 127.0.0.1 -n 20 > nul'
                }
            }
        }

        stage('Build, Test & Coverage') {
            steps {
                withCredentials([
                        string(credentialsId: 'DB_USER', variable: 'DB_USER'),
                        string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
                ]) {
                    script {
                        def mvnCmd = 'mvn clean verify -Djava.awt.headless=true'
                        if (env.SKIP_DB_TESTS == 'true') {
                            echo "Skipping DB-dependent tests..."
                            mvnCmd += ' -DskipITs=true'
                        } else {
                            mvnCmd += " -DDB_USER=%DB_USER% -DDB_PASSWORD=%DB_PASSWORD% -DDB_HOST=%DB_HOST% -DDB_PORT=%DB_PORT% -DDB_NAME=%DB_NAME%"
                        }
                        bat mvnCmd
                    }
                }
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
                    echo "Building Docker image ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}"
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}", ".")
                }
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                        echo "Pushing image ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} to Docker Hub"
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push()
                        docker.image("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}").push('latest')
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Cleaning up test DB and Docker images..."
                bat "docker rm -f test-mariadb || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:latest || exit 0"
            }
        }
    }
}