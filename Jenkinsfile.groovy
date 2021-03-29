    post {
        cleanup {
            script {
                if (env.imageFullName) {
                    sh "docker rmi $imageFullName"
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
