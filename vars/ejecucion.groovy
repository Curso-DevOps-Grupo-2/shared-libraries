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
                            env.PIPELINE_TYPE = "CI"
                            ci_pipeline.call(params.stages, nextVersion, currentVersion)
                        }
                        else if (env.GIT_BRANCH.contains("release")) {
                            figlet  "C. DELIVERY"
                            env.PIPELINE_TYPE = "CD"
                            cd_pipeline.call(params.stages, nextVersion, currentVersion)
                        }
                        else{
                            sh "echo 'Rama no identificada para ejecutar un pipeline.'"
                        }
                        
                    }
                }
                post{
                    success{
                        slackSend color: 'good', message: "[Grupo 2] [Pipeline ${env.PIPELINE_TYPE}] [${env.GIT_BRANCH}] Ejecucion Exitosa 🎉", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-token'
                    }
                    failure{
                        slackSend color: 'danger', message: "[Grupo 2] [Pipeline ${env.PIPELINE_TYPE}] [${env.GIT_BRANCH}] Ejecucion Fallida 😢", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack-token'
                    }
                }
            }
        }
    }
}
