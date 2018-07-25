def call(String nodeName = 'test', String composerAuth = 'unset') {
    println "Beginning test runner"
    def testFunctions = new org.swiftotter.TestFunctions()
    
    node (nodeName) {
        withEnv(["COMPOSER_AUTH=" + composerAuth]) {
            stage ('\u26CF Checkout') {
                sh 'mkdir -p checkout'
            
                dir ('checkout') {
                    checkout scm
                }
            }

            stage ('Testing') {
                sh 'rm -rf scripts/'
                sh 'mkdir -p scripts'
                dir ('scripts/') {
                    git credentialsId: 'GitHub-Access', url: 'git@github.com:SwiftOtter/MagentoCI.git'
                }
                
                sh 'mkdir -p checkout/scripts.d'
                sh 'ls -alh'
                sh 'cp --recursive --backup --force "checkout/scripts.d/." scripts'

                sh 'sudo chmod --recursive +x scripts/'
                withCredentials([string(credentialsId: params.PACKAGIST, variable: 'COMPOSER_AUTH')]) {
                    withCredentials([string(credentialsId: params.CRYPT_KEY, variable: 'CRYPT_KEY')]) {
                        tablePrefixArg = ''
                        if (params.TABLE_PREFIX) {
                            tablePrefixArg = '--tablePrefix '+params.TABLE_PREFIX 
                        }
                        println 'Running: sh scripts/test.sh'+' -i '+env.BUILD_NUMBER+' -c '+params.CRYPT_KEY+' -t '+params.TABLE_PREFIX+' -b '+params.S3_DB_BUCKET+' -p . -f '+params.S3_DB_FILE
                        sh 'scripts/test.sh --magentoVersion ' + params.MAGENTO_VERSION + ' --theme ' + params.THEME + ' --buildId ' + BUILD_NUMBER + ' --cryptKey '+params.CRYPT_KEY+' ' + tablePrefixArg + ' --s3Bucket '+params.S3_DB_BUCKET+' --filePath . --dbFile '+params.S3_DB_FILE
                    }
                }
            }

            stage('\u2795 Pushing Artifacts') {
                archiveArtifacts "output/*"
                junit "output/*.xml"
            }
        }

        // testFunctions.updateGithubCommitStatus(currentBuild);
    }
}
