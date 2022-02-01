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
                        if (env.GIT_BRANCH.startsWith("feature")) {
                            figlet  "C. INTEGRATION"
                            // ci_pipeline.call(params.stages)
                        }
                        if (env.GIT_BRANCH.startsWith("release")) {
                            figlet  "C. DELIVERY"
                            // cd_pipeline.call(params.stages)
                        }
                    }
                }
            }
        }
    }
}
