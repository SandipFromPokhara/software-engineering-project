pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK21'
    }

    environment {
        PATH = "${env.PATH}:/usr/local/bin:/Applications/Docker.app/Contents/Resources/bin"
        DB_HOST = '127.0.0.1'
        DB_PORT = '3307'
        DB_NAME = 'notevault_db'
        DB_CREDENTIALS_ID = 'DB_CREDENTIALS'
        DOCKERHUB_CREDENTIALS_ID = 'DockerHub_ID'
        DOCKERHUB_REPO = '218468/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
        DB_CONTAINER_NAME = 'notevault-db'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'feature-create', url: 'https://github.com/SandipFromPokhara/software-engineering-project.git'
            }
        }

        stage('Start Test DB') {
            steps {
                withCredentials([usernamePassword(credentialsId: DB_CREDENTIALS_ID, usernameVariable: 'DB_USER', passwordVariable: 'DB_PASSWORD')]) {
                    sh '''
                        docker rm -f $DB_CONTAINER_NAME >/dev/null 2>&1 || true
                        docker run -d --name $DB_CONTAINER_NAME -p $DB_PORT:3306 \
                          -e MARIADB_DATABASE=notevault_db \
                          -e MARIADB_USER=notevaultUser \
                          -e MARIADB_PASSWORD=password123 \
                          -e MARIADB_ROOT_PASSWORD=password123 \
                          mariadb:11.3

                        for i in {1..30}; do
                          docker exec $DB_CONTAINER_NAME mariadb -u$DB_USER -p$DB_PASSWORD -e "select 1" && break
                          sleep 2
                        done
                    '''
                }
            }
        }

        stage('Build & Test (with Coverage)') {
            steps {
                withCredentials([usernamePassword(credentialsId: DB_CREDENTIALS_ID, usernameVariable: 'DB_USER', passwordVariable: 'DB_PASSWORD')]) {
                    sh '''
                        mvn clean verify \
                        -DDB_USER=$DB_USER \
                        -DDB_PASSWORD=$DB_PASSWORD \
                        -DDB_HOST=$DB_HOST \
                        -DDB_PORT=$DB_PORT \
                        -DDB_NAME=$DB_NAME
                    '''
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=notevault \
                        -Dsonar.host.url=http://localhost:9000
                    '''
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
                publishHTML target: [
                        reportDir  : 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName : 'JaCoCo Coverage Report',
                        keepAll    : true
                ]
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build --pull -t $DOCKERHUB_REPO:$DOCKER_IMAGE_TAG .
                    docker images | head
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: DOCKERHUB_CREDENTIALS_ID, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $DOCKERHUB_REPO:$DOCKER_IMAGE_TAG
                        docker tag $DOCKERHUB_REPO:$DOCKER_IMAGE_TAG $DOCKERHUB_REPO:latest
                        docker push $DOCKERHUB_REPO:latest
                        docker image rm $DOCKERHUB_REPO:$DOCKER_IMAGE_TAG || true
                        docker image prune -f || true
                    '''
                }
            }
        }
    }

    post {
        always {
            sh 'docker rm -f $DB_CONTAINER_NAME >/dev/null 2>&1 || true'
        }
    }
}