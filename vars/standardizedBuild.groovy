def call(String nodeName = 'build') {
    node(nodeName) {
        stage ('\u26CF Checkout') {
            sh 'mkdir -p checkout'
            
            dir ('checkout') {
                checkout scm
            }
        }

        stage ('\u267B Build') {
            withCredentials([string(credentialsId: params.PACKAGIST, variable: 'COMPOSER_AUTH')]) {
                withEnv(["COMPOSER_AUTH=" + COMPOSER_AUTH]) {
                    sh 'rm -rf scripts'
                    sh 'git clone https://github.com/SwiftOtter/MagentoCI.git scripts'
                    sh 'mkdir -p checkout/scripts.d'
                    sh 'ls -alh'
                    sh 'cp --recursive --backup --force "checkout/scripts.d/." scripts'
                    sh 'sudo chmod --recursive +x scripts/'

                    env.PATH = "./scripts:${env.PATH}"
                    sh 'build.sh --magentoVersion ' + params.MAGENTO_VERSION + ' --theme ' + params.THEME + ' --buildId ' + BUILD_NUMBER
                }
            }
        }


        stage('\u2795 Pushing Artifacts') {
            sh 'aws s3 cp ' + OUTPUT_FILE + ' s3://' + params.S3_DEST_BUCKET + '/jobs/' + JOB_NAME + '/' + BUILD_NUMBER + '/' + OUTPUT_FILE
            currentBuild.result = "SUCCESS"
        }
    }
}
