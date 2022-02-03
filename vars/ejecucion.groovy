import utilities.*

def call(){
 pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('nexus-user')
        NEXUS_PASSWORD     = credentials('nexus-pass')
    }
     parameters {
            string  name: 'stages', description: 'Ingrese los stages para ejecutar', trim: true
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{                        
                        sh "env"

                        def versionUtils = new version.versionUtils();
                        def nextVersion = versionUtils.getNextVersion('minor')
                        def currentVersion = versionUtils.getCurrentVersion()
                        if (env.GIT_BRANCH.contains("feature") || env.GIT_BRANCH.contains("develop")) {
                            figlet  "C. INTEGRATION"
                            ci_pipeline.call(params.stages, nextVersion, currentVersion)
                        }
                        else if (env.GIT_BRANCH.contains("release")) {
                            figlet  "C. DELIVERY"
                            cd_pipeline.call(params.stages, nextVersion, currentVersion)
                        }
                        else{
                            sh "echo 'Rama no identificada para ejecutar un pipeline.'"
                        }
                        
                    }
                }
            }
        }
    }
}
