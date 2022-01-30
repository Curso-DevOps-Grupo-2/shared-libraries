import utilities.*

def call(stages){
    def stagesList = stages.split(';')
    def listStagesOrder = [
        // 'gitDiff': 'gitDiff',
        'nexusDownload': 'nexusDownload',
        'runJar': 'runJar',
        'prueba' : 'prueba'
    ]

    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
        stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stagesArray.isEmpty()) {
        echo 'El pipeline CD se ejecutará completo'
        allStages()
    } else {
        echo 'Stages a ejecutar :' + stages
        stagesArray.each{ stageFunction ->//variable as param
            echo 'Ejecutando ' + stageFunction
            "${stageFunction}"()
        }
    }

}
def allStages(){
    // nexusDownload()
    // runJar()
    prueba()
}
// def gitDiff(){
//     env.STAGE = "Stage 1: git diff"
//     stage("$env.STAGE"){
//         sh "echo 'git diff'"
//         sh "git diff '${GIT_BRANCH}'...main"
//     }
// }

def prueba(){
 env.STAGE = "Stage prueba: prueba"
    stage("$env.STAGE"){

         def script_output = 
        sh(returnStdout: true, script: "echo hola")
        script_output = script_output.trim()
        VAR_NAME1 = script_output
        println "VAR_NAME1 is ${VAR_NAME1}"


        def output = sh(returnStdout: true, script: """
         #!/bin/bash
        set -e
        set +x
        VAR_NAME=10
        echo \$VAR_NAME
            """)
            output = output.trim()
            VAR_NAME = output
            echo "VAR_NAME is ${VAR_NAME}"        
    }

}


def nexusDownload(){
    env.STAGE = "Stage 2: nexus download"
    stage("$env.STAGE"){
        sh "echo 'download from nexus'"
        sh "export PVERSION=`mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'`"
        sh "curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD 'http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/{$PVERSION}/DevOpsUsach2020-{$PVERSION}.jar' -O"
    }
}
def runJar(){
    env.STAGE = "Stage 3: run project"
    stage("$env.STAGE"){
        steps {
            sh "java -jar DevOpsUsach2020-'{$PVERSION}'.jar &"
            sh "sleep 20"
            sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
        }
    }
}
// def mergeMaster(){
//     env.STAGE = "Stage 4: merge master"
//     stage("$env.STAGE"){
//         sh "git push origin/main"
//     }
// }
// def mergeDevelop(){
//     env.STAGE = "Stage 5: merge develop"
//     stage("$env.STAGE"){
//     }
// }
// def tagMaster(){
//     env.STAGE = "Stage 6: tag master"
//     stage("$env.STAGE")
// }

return this;
