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
                # Remove any existing container
                docker rm -f ${env.CONTAINER_NAME} -ErrorAction SilentlyContinue
        
                # Run MariaDB container
                docker run -d --name ${env.CONTAINER_NAME} `
                    -e MYSQL_ROOT_PASSWORD=root `
                    -e MYSQL_DATABASE=${env.DB_NAME} `
                    -p 3306:3306 `
                    mariadb:10.11
        
                Write-Host "Waiting for MariaDB to be ready..."
        
                # Wait for MariaDB to accept connections
                do {
                    Start-Sleep -Seconds 2
                    \$status = docker exec ${env.CONTAINER_NAME} mysqladmin ping -u root -proot 2>&1
                } while (\$status -notmatch 'mysqld is alive')
        
                Write-Host "MariaDB is ready."
        
                # Create CI user with full privileges
                docker exec ${env.CONTAINER_NAME} mariadb -u root -proot -e `
                    "CREATE USER IF NOT EXISTS '${env.DB_USER}'@'%' IDENTIFIED BY '${env.DB_PASSWORD}'; `
                     GRANT ALL PRIVILEGES ON ${env.DB_NAME}.* TO '${env.DB_USER}'@'%'; `
                     FLUSH PRIVILEGES;"
        
                Write-Host "CI user ${env.DB_USER} is ready."
                """
            }
        }

        stage('Build, Test & Coverage') {
            steps {
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
                bat "docker rm -f %CONTAINER_NAME% || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || exit 0"
                bat "docker rmi ${DOCKERHUB_REPO}:latest || exit 0"
            }
        }
    }
}