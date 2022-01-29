def call(){
  
 pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('nexus-user')
        NEXUS_PASSWORD     = credentials('nexus-pass')
    }
     parameters {
            choice  name: 'compileTool', choices: ['Gradle', 'Maven'], description: 'Seleccione el empaquetador maven/gradle'
            string  name: 'stages', description: 'Ingrese los stages para ejecutar', trim: true
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                        // params.compileTool
                        sh "env"
                        env.STAGE  = ""
                        switch(params.compileTool)
                        {
                            case 'Maven':
                                figlet  "Maven"
                                // def ejecucion = load 'maven.groovy'
                                // ejecucion.call()
                                maven.call(params.stages)
                            break;
                            case 'Gradle':
                                figlet  "Gradle"
                                // def ejecucion = load 'gradle.groovy'
                                // ejecucion.call()
                                gradle.call(params.stages)
                            break;
                        }
                    }
                }
                post{
                    success{
                        slackSend color: 'good', message: "[Mentor Devops] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                    }
                    failure{
                        slackSend color: 'danger', message: "[Mentor Devops] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${env.STAGE}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-slack'
                    }
                }
            }
        }
    }
}