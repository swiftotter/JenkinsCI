def call(String nodeName = 'deploy', String environment, ArrayList targets) {
    def utilities = new org.swiftotter.Deploy()
    
    targets.each {
        utilities.deployWithDetails(nodeName, environment, S3_DEST_BUCKET, env.JOB_NAME, env.BUILD_NUMBER, OUTPUT_FILE, params.MAGENTO_VERSION, it);
    }
}
