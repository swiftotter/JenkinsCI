def call(String nodeName = 'test', String composerAuth = 'unset') {
    def utilities = new org.swiftotter.TestFunctions()
    
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

        test.updateGithubCommitStatus(currentBuild);
    }

    milestone 2
}
