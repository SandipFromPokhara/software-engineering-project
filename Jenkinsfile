pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
    }

    environment {
        DB_HOST = '127.0.0.1'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
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
                git branch: 'feature-dev', url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Build & Test') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'DB_CREDENTIALS',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                )]) {
                    bat """
                    mvn clean package -DDB_USER=%DB_USER% -DDB_PASSWORD=%DB_PASSWORD% -DDB_HOST=%DB_HOST% -DDB_PORT=%DB_PORT% -DDB_NAME=%DB_NAME%
                    """
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

        stage('Build & Push Docker Image') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'Docker_Hub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                    REM --- Debug environment ---
                    echo DOCKER_USER=%DOCKER_USER%
                    echo Length of DOCKER_PASS=%DOCKER_PASS%

                    REM --- Docker Login with retry ---
                    set RETRIES=3
                    :LOGIN_RETRY
                    echo Logging in to Docker...
                    echo "%DOCKER_PASS%" | docker login -u "%DOCKER_USER%" --password-stdin
                    if %ERRORLEVEL% neq 0 (
                        set /a RETRIES-=1
                        if %RETRIES% gtr 0 (
                            echo Retry Docker login...
                            goto LOGIN_RETRY
                        )
                        echo Docker login failed
                        exit /b 1
                    )

                    REM --- Build Docker Image ---
                    docker build --pull -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% .

                    REM --- Verify image exists ---
                    docker images

                    REM --- Push Docker Image with build number tag ---
                    docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG%

                    REM --- Tag as latest and push ---
                    docker tag %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG% %DOCKERHUB_REPO%:latest
                    docker push %DOCKERHUB_REPO%:latest
                    """
                }
            }
        }
    }
}