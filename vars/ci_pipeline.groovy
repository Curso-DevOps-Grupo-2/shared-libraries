import utilities.*

def call(stages, version){
    def stagesList = stages.split(';')
    def listStagesOrder = [
        'compile': 'compile',
        'test': 'test',
        'packageJar': 'packageJar',
        'sonar': 'sonar',
        'nexusUpload': 'nexusUpload',
        'gitCreateRelease': 'gitCreateRelease'
    ]

    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
        stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stagesArray.isEmpty()) {
        echo 'El pipeline se ejecutará completo'
        allStages(version)
    } else {
        echo 'Stages a ejecutar :' + stages
        stagesArray.each{ stageFunction ->//variable as param
            echo 'Ejecutando ' + stageFunction
            if (stageFunction.matches("nexusUpload") || stageFunction.matches("gitCreateRelease")) {
                "${stageFunction}"(version)
            }
            else {
                "${stageFunction}"()
            }
        }
    }

}
def allStages(version){
    compile()
    test()
    packageJar()
    sonar()
    nexusUpload(version)
    if (env.GIT_BRANCH.contains("develop")) {
        gitCreateRelease()
    }
}
def compile(){
    env.STAGE = "Stage 1: Compile"
    stage("$env.STAGE "){
        sh "echo 'compile'"
        sh "mvn compile"
    }
}
def test(){
    env.STAGE = "Stage 2: unit tests"
    stage("$env.STAGE "){
        sh "echo 'tests'"
        sh "mvn test"
    }
}
def packageJar(){
    env.STAGE = "Stage 3: Package"
    stage("$env.STAGE "){
        sh "mvn package"
    }
}
def sonar(){
    env.STAGE = "Stage 4: Sonarqube analysis"
    stage("$env.STAGE "){
        withSonarQubeEnv('sonarqube') {
            sh "echo 'Calling sonar'"
            sh "mvn sonar:sonar -Dsonar.projectName=ms-iclab-'${GIT_BRANCH}'-'${BUILD_DISPLAY_NAME}' -Dsonar.projectKey=test"
        }
    }
}
def nexusUpload(version){
    env.STAGE = "Stage 5: Nexus Upload"
    stage("$env.STAGE "){
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [
                        classifier: '',
                        extension: '',
                        filePath: 'build/DevOpsUsach2020-0.0.1.jar'
                    ]
                ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }
}
def gitCreateRelease(version) {
    sh "git checkout -b release-v${version}"
    sh "git push origin/release-v${version}"
}

return this;
