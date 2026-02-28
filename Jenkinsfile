//pipeline {
//    agent any
//
//    tools {
//        maven 'MAVEN_HOME'
//        jdk 'JDK21'
//    }
//
//    environment {
//        JAVA_HOME = tool 'JDK21'
//        PATH = "${env.JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"
//
//        DOCKERHUB_CREDENTIALS_ID = 'docker-jenkins'
//        DOCKERHUB_REPO = 'swostikalama/notevault'
//        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
//        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
//        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"
//    }
//
//    stages {
//
//        stage('Checkout') {
//            steps {
//                git branch: 'edit-test',
//                    url: 'git@github.com:SandipFromPokhara/software-engineering-project.git',
//                    credentialsId: 'private'
//            }
//        }
//
//        stage('Verify Java & Docker') {
//            steps {
//                sh 'java -version'
//                sh 'docker --version'
//                sh 'which docker'
//            }
//        }
//
//        stage('Run Tests') {
//            steps {
//                withCredentials([
//                    usernamePassword(
//                        credentialsId: 'sep1',
//                        usernameVariable: 'DB_USER',
//                        passwordVariable: 'DB_PASSWORD'
//                    )
//                ]) {
//                    sh '''
//                        mvn clean test \
//                        -DDB_USER=$DB_USER \
//                        -DDB_PASSWORD=$DB_PASSWORD
//                    '''
//
//                }
//            }
//        }
//
//        stage('Code Coverage') {
//            steps {
//                sh 'mvn jacoco:report'
//            }
//        }
//
//        stage('Publish Test Results') {
//            steps {
//                junit '**/target/surefire-reports/*.xml'
//            }
//        }
//
//        stage('Publish Coverage Report') {
//            steps {
//                jacoco()
//            }
//        }
//
//        stage('Build Docker Image (AMD64)') {
//            steps {
//                sh '''
//                    docker build \
//                        --platform linux/amd64 \
//                        -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
//                '''
//            }
//        }
//
//        stage('Push Docker Image to Docker Hub') {
//            steps {
//                withCredentials([
//                    usernamePassword(
//                        credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
//                        usernameVariable: 'DOCKER_USER',
//                        passwordVariable: 'DOCKER_PASS'
//                    )
//                ]) {
//                    sh '''
//                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
//
//                        # Push versioned tag
//                        docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}
//
//                        # Tag and push latest
//                        docker tag ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ${DOCKERHUB_REPO}:latest
//                        docker push ${DOCKERHUB_REPO}:latest
//                    '''
//                }
//            }
//        }
//
//
//
//        stage('Cleanup Docker Images') {
//            steps {
//                sh "docker rmi ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} || true"
//                sh "docker rmi ${DOCKERHUB_REPO}:latest || true"
//            }
//        }
//    }
//}

pipeline {
    agent any

    tools {
        maven 'MAVEN_HOME'
        jdk 'JDK21'
    }

    environment {
        JAVA_HOME = tool 'JDK21'
        PATH = "${env.JAVA_HOME}/bin:/opt/homebrew/bin:/usr/local/bin:${env.PATH}"

        DOCKERHUB_CREDENTIALS_ID = 'docker-jenkins'
        DOCKERHUB_REPO = 'swostikalama/notevault'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"
        BUILD_DATE = "${new Date().format('yyyy-MM-dd')}"
        JAVA_TOOL_OPTIONS = "-Dprism.order=sw -Djava.awt.headless=true"

        // DB defaults
        DB_HOST = 'localhost'
        DB_PORT = '3306'
        DB_NAME = 'notevault_db'
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
                sh 'docker --version || echo "Docker not installed"'
            }
        }

        stage('Start MariaDB Container') {
            steps {
                script {
                    def dockerExists = sh(script: 'which docker', returnStatus: true) == 0
                    if (dockerExists) {
                        sh '''
                            docker run -d --name test-db \
                            -e MARIADB_ROOT_PASSWORD=root \
                            -e MARIADB_DATABASE=${DB_NAME} \
                            -p ${DB_PORT}:3306 \
                            mariadb:latest || true
                        '''
                        sh 'sleep 15' // wait for DB to be ready
                    } else {
                        echo "Docker not available, using local DB"
                    }
                }
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
                    sh '''
                        mvn clean test \
                        -DDB_USER=$DB_USER \
                        -DDB_PASSWORD=$DB_PASSWORD \
                        -DDB_HOST=${DB_HOST} \
                        -DDB_PORT=${DB_PORT} \
                        -DDB_NAME=${DB_NAME}
                    '''
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
                sh '''
                    docker build \
                        --platform linux/amd64,linux/arm64 \
                        -t ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} .
                '''
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin

                        # Push versioned tag
                        docker push ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}

                        # Tag and push latest
                        docker tag ${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG} ${DOCKERHUB_REPO}:latest
                        docker push ${DOCKERHUB_REPO}:latest
                    '''
                }
            }
        }

    post {
        always {
            echo "Build finished, cleanup done"
        }
    }
}