def packageName        = 'ubrr-integr'
def imageName          = 'docker-host.dev.ubrr.ru/' + packageName
// System variables
def imageVersion       = ''
def failureSent        = false
pipeline {
    options {
        buildDiscarder logRotator(numToKeepStr: '5')
    }
    triggers{
        bitbucketPush()
    }
    post {
        cleanup {
            script {
                if (imageVersion) {
                    sh "docker rmi " + imageName + ":" + imageVersion + " || true"
                }
                sh "docker rmi " + imageName + ":latest || true"
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
        stage('Prepare params') {
            steps {
                script {
                    runUser = sh(script: "whoami", returnStdout: true).trim()
                }
            }
        }
        stage('Compile project') {
            agent {
                docker { 
                    image 'nexus.dev.ubrr.ru/dockerhub/maven:3.3-jdk-8'
                    reuseNode true
                    args "--entrypoint='' -v /etc/hosts:/etc/hosts:ro"
                }
            }
            steps {
                    sh "mvn -P ubrr clean install"
                    script {
                        imageVersion = sh(script: "mvn help:evaluate -Dexpression=project.version -q -DforceStdout", returnStdout: true).trim()
                    }
            }
        }
        stage('Build docker image') {
            steps {
                sh "docker pull nexus.dev.ubrr.ru/dockerhub/openjdk:11.0.2"
                sh "docker tag nexus.dev.ubrr.ru/dockerhub/openjdk:11.0.2 openjdk:11.0.2"
                script {
                    dockerImage = docker.build(imageName + ':' + imageVersion)
                    docker.withRegistry( 'https://docker-host.dev.ubrr.ru', 'nexus-push' ) {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }
    }
}
