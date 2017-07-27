def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()
     
    println env.BRANCH_NAME
    println BRANCH_NAME
    
    println config.toString()
    println config.devDeployTargets.toString()
    println config.prodDeployTargets.toString()
    
    if (env.BRANCH_NAME != "master") {
        println "About to run tests:"
        if (!SKIP_TEST) {
            println "Running tests..."
            test(TEST_NODE)
        }
    } else {
        milestone 1
        build(BUILD_NODE)
        milestone 2
        println SKIP_DEV_DEPLOY.toString()
        if (SKIP_DEV_DEPLOY != true && config.devDeployTargets) {
            println "Deploying to DEV"
            deploy(DEPLOY_NODE, 'dev', config.devDeployTargets)
        }
        milestone 3
        def approval = input(message: 'OK to push to production?', ok: 'Yes, push to production.')
        milestone 4
        if (SKIP_PROD_DEPLOY != true && config.prodDeployTargets) {
            println "Deploying to PROD"
            deploy(DEPLOY_NODE, 'prod', config.prodDeployTargets)   
        }
    }    
}
