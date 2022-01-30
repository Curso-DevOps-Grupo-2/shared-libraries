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
     prueba()
    // gitDiff()
    // nexusDownload()
    // runJar()
}
def prueba(){
    env.STAGE = "prueba"
    stage("$env.STAGE"){

        sh "echo '${GIT_BRANCH}'"
        sh "echo '${GIT_URL}'"


        def repoUrl = env.GIT_BRANCH
        def key = repoUrl.split('/')
                
        echo "The projectKey is: ${key}"        
        def scope  ='minor'

        def version = sh (
            script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
        ).trim()

        def latestVersion = version
        def (major, minor, patch) = latestVersion.tokenize('.').collect { it.toInteger() }
        def nextVersion
        switch (scope) {
            case 'major':
                nextVersion = "${major + 1}.0.0"
                break
            case 'minor':
                nextVersion = "${major}.${minor + 1}.0"
                break
            case 'patch':
                nextVersion = "${major}.${minor}.${patch + 1}"
                break
        }
        echo "The nextVersion is: ${nextVersion}"    

        
    }
}

def gitDiff(){
    env.STAGE = "Stage 1: git diff"
    stage("$env.STAGE"){
        sh "echo 'git diff'"
        sh "git diff origin/main...'${GIT_BRANCH}'"
    }
}
def nexusDownload(){
    env.STAGE = "Stage 2: nexus download"
    stage("$env.STAGE"){
        sh "echo 'download from nexus'"
        def version = sh (
            script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
        ).trim()
        def URL = "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/$version/DevOpsUsach2020-${version}.jar"
        sh "curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD $URL -O"
    }
}
def runJar(){
    env.STAGE = "Stage 3: run project"
    stage("$env.STAGE"){
        
        def version = sh (
            script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
        ).trim()
        sh "echo '${version}'"
        sh "java -jar DevOpsUsach2020-${version}.jar &"                      
        sh "sleep 20"
        sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
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
