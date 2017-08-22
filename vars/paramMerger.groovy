def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
    
    def utilities = new org.swiftotter.Functions()
    
    def defaultParams = [
        string(name: 'DEV_SSH_PORT', defaultValue: '22', description: 'SSH port for the DEV server.'),
        string(name: 'PROD_SSH_PORT', defaultValue: '22', description: 'SSH port for the PROD server.'),
        
        string(name: 'OUTPUT_FILE', defaultValue: 'build.tar.gz', description: 'Output file name (deployment, downloading from AWS)'),
        string(name: 'BUILD_FILE', defaultValue: 'build' + env.BUILD_NUMBER + '.tar.gz', description: 'Artifact file name'),
        string(name: 'S3_DEST_BUCKET', defaultValue: 'swiftotter-builds', description: 'The AWS S3 bucket to send the build artifacts to.'),

        booleanParam(name: 'SKIP_TEST', defaultValue: false, description: 'Whether or not to skip test runner.'),
        booleanParam(name: 'SKIP_DEV_DEPLOY', defaultValue: false, description: 'Whether or not to skip DEV deployment.'),
        booleanParam(name: 'SKIP_PROD_DEPLOY', defaultValue: false, description: 'Whether or not to skip PROD deployment.'),

        string(name: 'TEST_NODE', defaultValue: 'test', description: 'Node to run tests on. Ideally, this is always on.'),
        string(name: 'BUILD_NODE', defaultValue: 'm2-build', description: 'Node on which to build. This is usually best as a larger AWS instance, and can be started on demand.'),
        string(name: 'DEPLOY_NODE', defaultValue: 'deploy', description: 'Node from which to deploy. Ideally, this is always on and assigned a static IP address.'),
    ]
    
    println "Before merging parameters"
    
    return utilities.mergeParameters(config.parameters ?: [], defaultParams)
}
