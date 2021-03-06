def call(){
  
 pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('nexus-user')
        NEXUS_PASSWORD     = credentials('nexus-pass')
    }
     parameters {
            choice  name: 'pipelineType', choices: ['ci', 'cd'], description: 'Seleccione el tipo de Pipeline'
            string  name: 'stages', description: 'Ingrese los stages para ejecutar', trim: true
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{                        
                        sh "env"
                        env.STAGE  = ""
                        switch(params.pipelineType)
                        {
                            case 'ci':
                                figlet  "C. INTEGRATION"                                
                                ci_pipeline.call(params.stages)
                            break;
                            case 'cd':
                                figlet  "C. DELIVERY"                                
                                cd_pipeline.call(params.stages)
                            break;
                        }
                    }
                }
            }
        }
    }
}
