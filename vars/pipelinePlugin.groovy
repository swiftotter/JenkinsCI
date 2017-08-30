def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
     
    println env.BRANCH_NAME
    println BRANCH_NAME
    
    if (!config.primaryBranch) {
        primaryBranch = "master"   
    } else {
        primaryBranch  = config.primaryBranch   
    }
    
    if (env.BRANCH_NAME != primaryBranch) {
        println "About to run tests:"
        if (!binding.variables['SKIP_TEST'] || params.SKIP_TEST != true) {
            println "Running tests..."
            test(params.TEST_NODE)
        }
    } else {
        milestone 1
        println 'STANDARDIZED: ' + config.standardized
        if (!config.standardized || config.standardized != true) {
            build(params.BUILD_NODE)
        } else {
            standardizedBuild(params.BUILD_NODE)   
        }
        milestone 2
        println params.SKIP_DEV_DEPLOY.toString()
        if (params.SKIP_DEV_DEPLOY != true && config.devDeployTargets) {
            println "Deploying to DEV"
            deploy(DEPLOY_NODE, 'dev', config.devDeployTargets)
        }
        milestone 3
        def approval = input(message: 'OK to push to production?', ok: 'Yes, push to production.')
        milestone 4
        if (params.SKIP_PROD_DEPLOY != true && config.prodDeployTargets) {
            println "Deploying to PROD"
            deploy(params.DEPLOY_NODE, 'prod', config.prodDeployTargets)   
        }
    }    
}
