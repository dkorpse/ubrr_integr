def packageName   = 'ubrr_integr'
def registryName  = 'nexus.dev.ubrr.ru'
def imageFullName = ''
pipeline {
    options {
        gitLabConnection('OTP Gitlab')
        buildDiscarder logRotator(numToKeepStr: '5')
    }
    post {
        cleanup {
            script {
                if (imageFullName) {
                    sh "docker rmi " + imageFullName
                }
            }
            cleanWs()
        }
        regression {
            script {
                if (!failureSent) {
                    emailext subject: '$DEFAULT_SUBJECT',
                        body: '$DEFAULT_CONTENT',
                        recipientProviders: [developers()],
                        replyTo: '$DEFAULT_REPLYTO',
                        to: '$DEFAULT_RECIPIENTS',
                        attachLog: true,
                        compressLog: true
                    failureSent = true
                }
            }
        }
        failure {
            script {
                if (!failureSent) {
                    emailext subject: '$DEFAULT_SUBJECT',
                        body: '$DEFAULT_CONTENT',
                        recipientProviders: [developers()],
                        replyTo: '$DEFAULT_REPLYTO',
                        to: '$DEFAULT_RECIPIENTS',
                        attachLog: true,
                        compressLog: true
                    failureSent = true
                }
            }
        }
        fixed {
            emailext subject: '$DEFAULT_SUBJECT',
                body: '$DEFAULT_CONTENT',
                recipientProviders: [developers()],
                replyTo: '$DEFAULT_REPLYTO',
                to: '$DEFAULT_RECIPIENTS',
                attachLog: false
        }
    }
    agent any
    stages {
        stage('Prepare parameters') {
            steps {
                gitlabCommitStatus(STAGE_NAME) {
                    echo "Opa!"
                }
            }
        }
        stage('Compile project') {
            agent {
                docker { 
                    image registryName + '/dockerhub/maven:3.3-jdk-8'
                    reuseNode true
                    args '-v ${workspace}:/build -v /etc/hosts:/etc/hosts:ro'
                }
            }
            steps {
                gitlabCommitStatus(STAGE_NAME) {
                    sh "cd /build"
                    sh "mvn clean install"
                }
            }
        }
        stage('Build docker image') {
            steps {
                gitlabCommitStatus(STAGE_NAME) {
                        imageFullName = registryName + '/' + packageName + ":0.0.1"
                        dockerImage = docker.build(imageFullName)
                    }
                }
            }
        }
    }
}
