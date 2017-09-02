package org.swiftotter

    def deployWithDetails(
        String inputNodeName,
        String inputEnvironment,
        String inputS3BucketName,
        String inputBuildName,
        String inputBuildNumber,
        String inputOutputFile,
        String inputMagentoVersion,
        Map details
    ) {
        this.deployBuild(
            inputNodeName,
            inputEnvironment,
            inputS3BucketName,
            inputBuildName,
            inputBuildNumber,
            details.sshUser,
            details.sshHost,
            details.sshPort,
            details.sshKey,
            details.sshPath,
            inputOutputFile,
            inputMagentoVersion
        )
    }

    def deployBuild(
        String nodeName,
        String environmentName,
        String s3BucketName, 
        String buildName,
        String buildNumber,
        String sshUser,
        String sshHost,
        String sshPort,
        String sshKey,
        String sshPath,
        String outputFile,
        String magentoVersion
    ) {
        buildFile = this.downloadArtifactFromS3Bucket(nodeName, s3BucketName, buildName, buildNumber, outputFile)

        this.pushArtifactToDeployServer(nodeName, sshUser, sshHost, sshPort, sshKey, sshPath, buildFile, buildNumber)
        this.deployArtifactOnServer(nodeName, sshUser, sshHost, sshPort, sshKey, sshPath, buildFile, buildNumber, magentoVersion)
    }

    def downloadArtifactFromS3Bucket(String nodeName, String s3BucketName, String buildName, String buildNumber, String outputFile) {
        int index = buildName.lastIndexOf('/');
        def jobName = buildName;
        if (index > -1) {
            jobName = buildName.substring(0, index)
        }

        def buildFile = jobName + '-' + buildNumber.toString() + '.tar.gz'

        node (nodeName) {
            sh 'rm -f *.tar.gz'
            sh 'aws s3 cp s3://' + s3BucketName + '/jobs/' + buildName + '/' + buildNumber + '/' + outputFile + ' ' + buildFile
        }

        println("Build File: " + buildFile)

        return buildFile
    }

    def pushArtifactToDeployServer(String nodeName = 'deploy', String sshUser, String sshHost, String sshPort, String sshKey, String sshPath, String buildFile, String buildNumber) {
        def userHost = sshUser + '@' + sshHost
        def releaseFolder = 'releases/build-' + buildNumber

        this.executeInNode(nodeName, sshKey) { SSH_KEY ->
            sh 'ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no -p ' + sshPort + ' ' + userHost + ' \'mkdir -p ' + sshPath + '\''
        }
        
        this.executeInNode(nodeName, sshKey) { SSH_KEY ->
            sh 'scp -i ${SSH_KEY} -P ' + sshPort + ' -o StrictHostKeyChecking=no -o \'CompressionLevel 9\' -o \'IPQoS throughput\' ' + buildFile + ' ' + userHost + ':' + sshPath + '/releases/' + buildFile
        }
    }

    def deployArtifactOnServer(String nodeName = 'deploy', String sshUser, String sshHost, String sshPort, String sshKey, String sshPath, String buildFile, String buildNumber, magentoVersion) {
        def userHost = sshUser + '@' + sshHost
        def releaseFolder = 'releases/build-' + buildNumber

        this.executeInNode(nodeName, sshKey) { SSH_KEY ->
            sh 'ssh -i ${SSH_KEY} -o StrictHostKeyChecking=no -p ' + sshPort + ' ' + userHost + ' << EOF\n' +
                'mkdir -p ' + sshPath + '\n' + // try to create the path if it doesn't exist
                'cd ' + sshPath + '\n' +
                'mkdir -p ' + releaseFolder + '\n' +
                'tar --extract --gzip --mode 777 --touch --no-overwrite-dir --file releases/' + buildFile + ' --directory ' + sshPath + '/' + releaseFolder + '\n' +
                './'+releaseFolder+'/scripts/deploy.sh --build ' + buildNumber + ' --magentoVersion ' + magentoVersion + '\n' +
                'EOF'
        }
    }

    def executeInNode(String nodeName = 'deploy', String sshKey, Closure whatToDo) {
        node (nodeName) {
            timeout(time: 15, unit: 'MINUTES') {
                withCredentials([file(credentialsId: sshKey, variable: 'SSH_KEY')]) {
                    sh 'chmod 600 ${SSH_KEY}'
                    whatToDo(SSH_KEY)
                }
            }
        }
    }
