pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_USER = credentials('DB_USER')
        DB_PASSWORD = credentials('DB_PASSWORD')
        DB_HOST = 'localhost'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        CONTAINER_NAME = "test-mariadb-${BUILD_NUMBER}"
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
                powershell """
                docker rm -f ${env.CONTAINER_NAME} -ErrorAction SilentlyContinue

                docker run -d --name ${env.CONTAINER_NAME} `
                    -e MYSQL_ROOT_PASSWORD=${env.DB_PASSWORD} `
                    -e MYSQL_DATABASE=${env.DB_NAME} `
                    -p 3306:3306 `
                    mariadb:10.11

                Write-Host "Waiting for MariaDB to be ready..."
                do {
                    Start-Sleep -Seconds 2
                    \$status = docker exec ${env.CONTAINER_NAME} mysqladmin ping -u root -p${env.DB_PASSWORD} 2>&1
                } while (\$status -notmatch 'mysqld is alive')
                Write-Host "MariaDB is ready."
                """
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    bat """
                    mvn clean verify -Djava.awt.headless=true ^
                        -DDB_USER=${env.DB_USER} ^
                        -DDB_PASSWORD=${env.DB_PASSWORD} ^
                        -DDB_HOST=${env.DB_HOST} ^
                        -DDB_PORT=${env.DB_PORT} ^
                        -DDB_NAME=${env.DB_NAME} -X
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}", ".")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
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
                bat "docker rm -f %CONTAINER_NAME% || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:latest || exit 0"
            }
        }
    }
}