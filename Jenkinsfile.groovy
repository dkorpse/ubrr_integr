def packageName   = 'ubrr_integr'
def registryName  = 'nexus.dev.ubrr.ru'
def imageFullName = ''
def failureSent   = false
pipeline {
    options {
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
                    echo "Opa!"
            }
        }
        stage('Compile project') {
            agent {
                docker { 
                    image registryName + '/dockerhub/maven:3.3-jdk-8'
                    reuseNode true
                    args "--entrypoint='' -v /etc/hosts:/etc/hosts:ro"
                }
            }
            steps {
                    sh "mvn -Pubrr clean install"
            }
        }
        stage('Build docker image') {
            steps {
                    script {
                        imageFullName = registryName + '/' + packageName + ":0.0.1"
                        dockerImage = docker.build(imageFullName)
                    }
            }
        }
    }
}
