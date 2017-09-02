def overriddenParams = [
    string(name: 'S3_DEST_BUCKET', defaultValue: 'your-s3-bucket', description: 'The AWS S3 bucket to send the build artifacts to.'),

    //-- BEGIN: This section is for automated testing.
    string(name: 'CRYPT_KEY', defaultValue: 'your-crypt-key', description: 'Name of Crypt Key Credential'),
    string(name: 'TABLE_PREFIX', defaultValue: '', description: 'Table prefix for DB'),
    string(name: 'S3_DB_BUCKET', defaultValue: 'your-s3-seed-db-bucket', description: 'S3 bucket that holds the DB upload'),
    string(name: 'S3_DB_FILE', defaultValue: 'swiftotter-db.gz', description: 'File in S3 bucket containing development DB dump'),
    string(name: 'SKIP_TEST', defaultValue: '1', description: 'Whether or not to skip tests'),
    //-- END
    
    //-- BEING: this section is for Magento-specific values.
    string(name: 'THEME', defaultValue: 'your-theme-name', description: 'Name of primary project theme'),
    string(name: 'MAGENTO_VERSION', defaultValue: '2.1', description: 'Current version of Magento'),
    string(name: 'PACKAGIST', defaultValue: 'your-packagist-composer-crednetials', description: 'Packagist / composer configuration credential name')
    //-- END
]

def mergedParams = paramMerger {
    parameters = overriddenParams
}

properties([parameters(mergedParams), pipelineTriggers([])])

// To deploy to multiple targets, add more lines below.
// This will likely mean that the overriddenParams variable above needs to be updated.
def inputDevDeployTargets = [
//    [ sshUser: 'deploy', sshHost: '', sshPort: '22', sshPath: '', sshKey: '' ]
]
def inputProdDeployTargets = [
    [ sshUser: 'deploy', sshHost: '', sshPort: '22', sshPath: '', sshKey: '' ]
]

// This is what runs the build / deploy pipeline
pipelinePlugin {
    standardized = true
    devDeployTargets = inputDevDeployTargets
    prodDeployTargets = inputProdDeployTargets
}
