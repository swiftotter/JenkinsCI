def call(String nodeName = 'build') {
    node(nodeName) {
        stage ('\u26CF Checkout') {
            checkout scm
            println 'aws s3 cp ' + OUTPUT_FILE + ' s3://' + S3_DEST_BUCKET + '/jobs/' + JOB_NAME + '/' + BUILD_NUMBER + '/' + OUTPUT_FILE
        }

        stage ('\u267B Build') {
            withCredentials([string(credentialsId: PACKAGIST, variable: 'COMPOSER_AUTH')]) {
                withEnv(["COMPOSER_AUTH=" + COMPOSER_AUTH]) {
                    sh 'rm -rf scripts'
                    sh 'git clone https://github.com/SwiftOtter/Magento1CI.git scripts'
                    sh 'mkdir -p scripts.d'
                    sh 'ls -alh'
                    sh 'cp --recursive --backup --force "scripts.d/." scripts'
                    sh 'sudo chmod --recursive +x scripts/'

                    env.PATH = "./scripts:${env.PATH}"
                    sh 'build.sh'
                }
            }
        }


        stage('\u2795 Pushing Artifacts') {
            sh 'aws s3 cp ' + OUTPUT_FILE + ' s3://' + S3_DEST_BUCKET + '/jobs/' + JOB_NAME + '/' + BUILD_NUMBER + '/' + OUTPUT_FILE
            currentBuild.result = "SUCCESS"
        }
    }
}
