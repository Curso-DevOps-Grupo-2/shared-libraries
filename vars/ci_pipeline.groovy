import utilities.*

def call(stages){
    def stagesList = stages.split(';')
    def listStagesOrder = [
        'compile': 'compile',
        'test': 'test',
        'packageJar': 'packageJar',
        'sonar': 'sonar',
        'nexusUpload': 'nexusUpload',
    ]

    def arrayUtils = new array.arrayExtentions();
    def stagesArray = []
        stagesArray = arrayUtils.searchKeyInArray(stages, ";", listStagesOrder)

    if (stagesArray.isEmpty()) {
        echo 'El pipeline se ejecutará completo'
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
    compile()
    test()
    packageJar()
    sonar()
    nexusUpload()
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

            def repoUrl = env.GIT_URL
            def repoName = repoUrl.split('/')                                        

            sh "mvn sonar:sonar -Dsonar.projectName='${repoName[4]}'-'${GIT_BRANCH}'-'${BUILD_DISPLAY_NAME}' -Dsonar.projectKey=test"
        }
    }
}
def nexusUpload(){
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

return this;
