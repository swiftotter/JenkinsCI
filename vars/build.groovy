def call(String nodeName = 'build') {
    node(nodeName) {
        stage ('\u26CF Checkout') {
            checkout scm
        }

        stage ('\u267B Build') {
            withCredentials([string(credentialsId: params.PACKAGIST, variable: 'COMPOSER_AUTH')]) {
                withEnv(["COMPOSER_AUTH=" + COMPOSER_AUTH]) {
                    sh 'rm -rf scripts'
                    sh 'mkdir -p scripts'
                    dir ('scripts/') {
                        git credentialsId: 'GitHub-Access', url: 'git@github.com:SwiftOtter/MagentoCI.git'
                    }
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
            sh 'aws s3 cp ' + OUTPUT_FILE + ' s3://' + params.S3_DEST_BUCKET + '/jobs/' + JOB_NAME + '/' + BUILD_NUMBER + '/' + OUTPUT_FILE
            currentBuild.result = "SUCCESS"
        }
    }
}
