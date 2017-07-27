def call(String nodeName = 'test', String composerAuth = 'unset') {
    println "Beginning test runner"
    def testFunctions = new org.swiftotter.TestFunctions()
    
    node (nodeName) {
        withEnv(["COMPOSER_AUTH=" + composerAuth]) {
            stage ('\u26CF Checkout') {
                checkout scm
            }

            stage ('Testing') {
                sh 'rm -rf scripts/'
                sh 'git clone https://github.com/SwiftOtter/Magento1CI.git scripts'

                sh 'sudo chmod --recursive +x scripts/'
                withCredentials(CRYPT_KEY, 'CRYPT_KEY') {
                    sh 'scripts/test.sh'+' -i '+env.BUILD_NUMBER+' -c '+CRYPT_KEY+' -t '+TABLE_PREFIX+' -b '+S3_DB_BUCKET+' -p . -f '+S3_DB_FILE
                }
            }

            stage('\u2795 Pushing Artifacts') {
                archive "output/*"
                junit "output/*.xml"
            }
        }

        testFunctions.updateGithubCommitStatus(currentBuild);
    }
}
