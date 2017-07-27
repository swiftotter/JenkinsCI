def call(String nodeName = 'test', String composerAuth = 'unset') {
    node (nodeName) {
        withEnv(["COMPOSER_AUTH=" + composerAuth]) {
            milestone 1
            def testPath = env.BUILD_NUMBER

            stage ('\u26CF Checkout') {
                checkout scm
            }

            stage ('Testing') {
                sh 'rm -rf scripts/'
                sh 'git clone https://github.com/SwiftOtter/Magento1CI.git scripts'

                sh 'sudo chmod --recursive +x scripts/'
                sh 'scripts/test.sh'+' -i '+env.BUILD_NUMBER+' -c '+env.CRYPT_KEY+' -t '+env.TABLE_PREFIX+' -b '+env.BUCKET+' -p . -f '+env.S3_FILE
            }

            stage('\u2795 Pushing Artifacts') {
                archive "output/*"
                junit "output/*.xml"
            }
        }

        updateGithubCommitStatus(currentBuild);
    }

    milestone 2
}

def getRepoURL() {
    sh "git config --get remote.origin.url > .git/remote-url"
    return readFile(".git/remote-url").trim()
}

def getCommitSha() {
    sh "git rev-parse HEAD > .git/current-commit"
    return readFile(".git/current-commit").trim()
}

def updateGithubCommitStatus(build) {
  // workaround https://issues.jenkins-ci.org/browse/JENKINS-38674
  repoUrl = getRepoURL()
  commitSha = getCommitSha()

  step([
    $class: 'GitHubCommitStatusSetter',
    reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
    commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
    errorHandlers: [[$class: 'ShallowAnyErrorHandler']],
    statusResultSource: [
      $class: 'ConditionalStatusResultSource',
      results: [
        [$class: 'BetterThanOrEqualBuildResult', result: 'SUCCESS', state: 'SUCCESS', message: build.description],
        [$class: 'BetterThanOrEqualBuildResult', result: 'FAILURE', state: 'FAILURE', message: build.description],
        [$class: 'AnyBuildResult', state: 'FAILURE', message: 'Loophole']
      ]
    ]
  ])
}
