pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = 'localhost'
        DB_PORT = '3307' // Free port to avoid conflicts
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
                withCredentials([
                        string(credentialsId: 'DB_USER', variable: 'DB_USER'),
                        string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
                ]) {
                    powershell """
                    # Remove any existing container
                    docker rm -f ${env.CONTAINER_NAME} -ErrorAction SilentlyContinue

                    # Start MariaDB container
                    docker run -d --name ${env.CONTAINER_NAME} `
                        -e MYSQL_ROOT_PASSWORD=%DB_PASSWORD% `
                        -e MYSQL_DATABASE=${env.DB_NAME} `
                        -p ${env.DB_PORT}:3306 `
                        mariadb:10.11

                    Write-Host "Waiting for MariaDB to be ready..."
                    \$ready = \$false
                    while (-not \$ready) {
                        Start-Sleep -Seconds 2
                        \$status = docker exec ${env.CONTAINER_NAME} mysqladmin ping -uroot -p%DB_PASSWORD% 2>&1
                        if (\$status -match 'mysqld is alive') { \$ready = \$true }
                    }
                    Write-Host "MariaDB is ready."

                    # Create test user
                    docker exec ${env.CONTAINER_NAME} mysql -uroot -p%DB_PASSWORD% -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'%' IDENTIFIED BY '%DB_PASSWORD%'; GRANT ALL PRIVILEGES ON ${env.DB_NAME}.* TO '%DB_USER%'@'%'; FLUSH PRIVILEGES;"
                    Write-Host "Test DB user created successfully."
                    """
                }
            }
        }

        stage('Build & Test') {
            steps {
                withCredentials([
                        string(credentialsId: 'DB_USER', variable: 'DB_USER'),
                        string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD')
                ]) {
                    script {
                        bat """
                        mvn clean verify -Djava.awt.headless=true ^
                            -DDB_USER=%DB_USER% ^
                            -DDB_PASSWORD=%DB_PASSWORD% ^
                            -DDB_HOST=${env.DB_HOST} ^
                            -DDB_PORT=${env.DB_PORT} ^
                            -DDB_NAME=${env.DB_NAME} -X
                        """
                    }
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
                bat "docker rm -f ${env.CONTAINER_NAME} || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:latest || exit 0"
            }
        }
    }
}