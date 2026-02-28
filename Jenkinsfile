pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = '127.0.0.1'
        DB_PORT = '3307'
        DB_NAME = 'notevault_test_db'
        DB_USER = 'ciuser'
        DB_PASSWORD = 'ciuserpass'
        BCRYPT_COST = '12'
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        DOCKERHUB_REPO = 'sandipranjit/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-dev',
                        url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Start Test DB') {
            steps {
                script {
                    // Remove old container if exists
                    bat 'docker rm -f test-mariadb || exit 0'

                    // Start fresh MariaDB container
                    bat """
                    docker run -d --name test-mariadb ^
                        -e MYSQL_ROOT_PASSWORD=rootpass ^
                        -e MYSQL_DATABASE=%DB_NAME% ^
                        -p %DB_PORT%:3306 ^
                        mariadb:10.11
                    """

                    // Wait for DB to be ready (max 30 tries)
                    bat """
                    powershell -NoProfile -Command ^
                        "$ready=$false; $tries=0; ^
                        while (-not $ready -and $tries -lt 30) { ^
                            Start-Sleep -Seconds 2; ^
                            try { docker exec test-mariadb mysqladmin ping -uroot -prootpass | Out-Null; $ready=$true } ^
                            catch { $ready=$false }; ^
                            $tries++ ^
                        }; ^
                        if (-not $ready) { Write-Host 'MariaDB did not start in time'; exit 1 }"
                    """

                    // Create CI user in the DB
                    bat """
                    docker exec test-mariadb mysql -uroot -prootpass -e ^
                        "CREATE USER IF NOT EXISTS '%DB_USER%'@'%' IDENTIFIED BY '%DB_PASSWORD%'; ^
                        GRANT ALL PRIVILEGES ON %DB_NAME%.* TO '%DB_USER%'@'%'; ^
                        FLUSH PRIVILEGES;"
                    """
                }
            }
        }

        stage('Build & Test') {
            steps {
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
                bat "docker rmi %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% || exit 0"
                bat "docker rmi %DOCKERHUB_REPO%:latest || exit 0"
            }
        }
    }
}