import utilities.*

def call(stages){
    def stagesList = stages.split(';')
    def listStagesOrder = [
        'gitDiff': 'gitDiff',
        'nexusDownload': 'nexusDownload',
        'runJar': 'runJar'
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
    gitDiff()
    nexusDownload()
    runJar()
}
def gitDiff(){
    env.STAGE = "Stage 1: git diff"
    stage("$env.STAGE"){
        sh "echo 'git diff'"
        sh "git diff '${GIT_BRANCH}'...origin/main"
    }
}
def nexusDownload(){
    env.STAGE = "Stage 2: nexus download"
    stage("$env.STAGE"){
        sh "echo 'download from nexus'"
        def version = sh (
            script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
        )
        def URL = "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/'${version}'/DevOpsUsach2020-'${version}'.jar"
        sh "echo '${URL.trim()}'"
        sh "echo '${version}'"
        sh "curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD $URL -O"
    }
}
def runJar(){
    env.STAGE = "Stage 3: run project"
    stage("$env.STAGE"){
        steps {
            def version = sh (
                script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
            )
            sh "java -jar DevOpsUsach2020-$version.jar &"
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
