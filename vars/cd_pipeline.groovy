import utilities.*

def call(stages, nextVersion, currentVersion){
    def stagesList = stages.split(';')
    def listStagesOrder = [
        'gitDiff': 'gitDiff',
        'nexusDownload': 'nexusDownload',
        'runJar': 'runJar',
        'mergeMaster': 'mergeMaster',
        'mergeDevelop': 'mergeDevelop',
        'tagMaster': 'tagMaster'
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
            if(stageFunction.matches("gitDiff")) {
                "${stageFunction}"()
            }
            else if(stageFunction.matches("nexusDownload") || stageFunction.matches("runJar")){
                "${stageFunction}"(currentVersion)
            }
            else {
                "${stageFunction}"(nextVersion)
            }
        }
    }

}
def allStages(){
    prueba()
    gitDiff()
    nexusDownload()
    runJar()
}

def gitDiff(){
    env.STAGE = "Stage 1: git diff"
    stage("$env.STAGE"){
        sh "echo 'git diff'"
        sh "git diff origin/main...'${GIT_BRANCH}'"
    }
}
def nexusDownload(version){
    env.STAGE = "Stage 2: nexus download"
    stage("$env.STAGE"){
        sh "echo 'download from nexus'"
        def URL = "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/$version/DevOpsUsach2020-${version}.jar"
        sh "curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD '$URL' -O"
    }
}
def runJar(version){
    env.STAGE = "Stage 3: run project"
    stage("$env.STAGE"){
        sh "echo '${version}'"
        sh "java -jar DevOpsUsach2020-${version}.jar &"
        sh "sleep 20"
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}
def mergeMaster(version){
    env.STAGE = "Stage 4: merge master"
    stage("$env.STAGE"){
        sh "git checkout main"
        sh "git merge release-v${version}"
        sh "git commit -am \"merge branch release-v${version}\""
        sh "git push"
    }
}
def mergeDevelop(version){
    env.STAGE = "Stage 5: merge develop"
    stage("$env.STAGE"){
        sh "git checkout develop"
        sh "git merge release-v${version}"
        sh "git commit -am \"merge branch release-v${version}\""
        sh "git push"
    }
}
def tagMaster(version){
    env.STAGE = "Stage 6: tag master"
    stage("$env.STAGE") {
        sh "git checkout main"
        sh "git tag -a ${version} -m \"Jenkins CD version: ${version}\""
        sh "git push"
    }
}

return this;
