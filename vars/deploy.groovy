def call(String nodeName = 'deploy', String environment, ArrayList targets) {
    def utilities = new org.swiftotter.Deploy()
    
    targets.each {
        println("Deploy input");
        println("Node Name: " + nodeName);
        println("S3 Bucket Name: " + S3_DEST_BUCKET);
        println("Job Name: " + env.JOB_NAME);
        println("Build Number: " + env.BUILD_NUMBER);
        utilities.deployWithDetails(nodeName, environment, S3_DEST_BUCKET, env.JOB_NAME, env.BUILD_NUMBER, OUTPUT_FILE, params.MAGENTO_VERSION, it);
    }
}
