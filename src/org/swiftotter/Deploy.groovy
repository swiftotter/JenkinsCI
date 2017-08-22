package org.swiftotter

def deployWithDetails(
    String nodeName = 'deploy',
    String environment,
    String s3BucketName,
    String buildName,
    String buildNumber,
    String outputFile,
    String magentoVersion,
    Map details
) {
    println nodeName
    deploy(nodeName,
           environment,
           s3BucketName,
           buildName,
           buildNumber,
           details.sshUser,
           details.sshHost,
           details.sshKey,
           details.sshPath,
           outputFile,
           magentoVersion
    )
}

def deploy(
    String nodeName = 'deploy',
    String environment,
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
    buildFile = downloadArtifactFromS3Bucket(nodeName, s3BucketName, buildName, buildNumber, outputFile)

    pushArtifactToDeployServer(nodeName, sshUser, sshHost, sshPort, sshKey, sshPath, buildFile, buildNumber)
    deployArtifactOnServer(nodeName, sshUser, sshHost, sshPort, sshKey, sshPath, buildFile, buildNumber, magentoVersion)
}

def downloadArtifactFromS3Bucket(String nodeName, String s3BucketName, String buildName, String buildNumber, String outputFile) {
    int index = buildName.lastIndexOf('/');
    def jobName = buildName;
    if (index > -1) {
        jobName = buildName.substring(0, index)
    }
    
    println("Job Name: " + jobName);
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

    executeInNode(nodeName, sshKey) { SSH_KEY ->
        sh 'scp -i ${SSH_KEY} -p ' + sshPort + ' -o StrictHostKeyChecking=no -o \'CompressionLevel 9\' -o \'IPQoS throughput\' -c arcfour ' + buildFile + ' ' + userHost + ':' + sshPath + '/releases/' + buildFile
    }
}

def deployArtifactOnServer(String nodeName = 'deploy', String sshUser, String sshHost, String sshPort, String sshKey, String sshPath, String buildFile, String buildNumber, magentoVersion) {
    def userHost = sshUser + '@' + sshHost
    def releaseFolder = 'releases/build-' + buildNumber

    executeInNode(nodeName, sshKey) { SSH_KEY ->
        println SSH_KEY
        sh 'ssh -i ${SSH_KEY} -p ' + sshPort + ' + userHost + ' << EOF\n' +
            'cd ' + sshPath + '\n' +
            'mkdir -p ' + releaseFolder + '\n' +
            'tar --extract --gzip --mode 777 --touch --no-overwrite-dir --file releases/' + buildFile + ' -C ' + sshPath + '/' + releaseFolder + '\n' +
            './'+releaseFolder+'/scripts/deploy.sh --build=' + buildNumber + ' --magentoVersion=' + magentoVersion + '\n' +
            'EOF'
    }
}

def executeInNode(String nodeName = 'deploy', String sshKey, Closure whatToDo) {
    node (nodeName) {
        timeout(time: 15, unit: 'MINUTES') {
            println sshKey
            withCredentials([file(credentialsId: sshKey, variable: 'SSH_KEY')]) {
                println SSH_KEY
                sh 'chmod 600 ${SSH_KEY}'
                whatToDo(SSH_KEY)
            }
        }
    }
}
