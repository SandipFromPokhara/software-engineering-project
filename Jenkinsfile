pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = '127.0.0.1'
        DB_PORT = '3307'
        DB_NAME = 'notevault_test_db'
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
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
                withCredentials([usernamePassword(
                        credentialsId: 'DB_CREDENTIALS',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                )]) {
                    powershell """
                    docker rm -f test-mariadb -ErrorAction SilentlyContinue
        
                    docker run -d --name test-mariadb `
                        -e MYSQL_ROOT_PASSWORD=%DB_PASSWORD% `
                        -e MYSQL_DATABASE=notevault_test_db `
                        -p 3307:3306 `
                        mariadb:10.11
        
                    Write-Host "Waiting for MariaDB to be ready..."
                    \$ready = \$false
                    while (-not \$ready) {
                        Start-Sleep -Seconds 2
                        \$status = docker exec test-mariadb mysqladmin ping -uroot -p%DB_PASSWORD% 2>&1
                        if (\$status -match 'mysqld is alive') { \$ready = \$true }
                    }
                    Write-Host "MariaDB is ready."
        
                    docker exec test-mariadb mysql -uroot -p%DB_PASSWORD% -e `
                        "CREATE USER IF NOT EXISTS '%DB_USER%'@'%' IDENTIFIED BY '%DB_PASSWORD%'; `
                         GRANT ALL PRIVILEGES ON notevault_test_db.* TO '%DB_USER%'@'%'; `
                         FLUSH PRIVILEGES;"
                         
                    docker exec test-mariadb mysql -uroot -p%DB_PASSWORD% -e `
                        "CREATE USER IF NOT EXISTS '%DB_USER%'@'172.17.0.1' IDENTIFIED BY '%DB_PASSWORD%'; `
                         GRANT ALL PRIVILEGES ON notevault_test_db.* TO '%DB_USER%'@'172.17.0.1'; `
                         FLUSH PRIVILEGES;"
                    Write-Host "Test DB user created successfully."
                    """
                }
            }
        }

        stage('Build & Test') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'DB_CREDENTIALS',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                )]) {
                    script {
                        bat """
                        mvn clean verify -Djava.awt.headless=true ^
                            -DDB_USER=%DB_USER% ^
                            -DDB_PASSWORD=%DB_PASSWORD% ^
                            -DDB_HOST=%DB_HOST% ^
                            -DDB_PORT=%DB_PORT% ^
                            -DDB_NAME=%DB_NAME%
                        """
                    }
                }
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
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
                bat "docker build -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% ."
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                        docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%
                        docker tag %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% %DOCKERHUB_REPO%:latest
                        docker push %DOCKERHUB_REPO%:latest
                    """
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