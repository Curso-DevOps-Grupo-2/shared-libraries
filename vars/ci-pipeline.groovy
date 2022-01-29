/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/

import utilities.*

def call(stages){
    // def stagesList = stages.split(';')
    // sh "echo ${stagesList}"
    //Escribir directamente el código del stage, sin agregarle otra clausula de Jenkins.
    // sBuild()
    // sSonar()
    // sCurlSpring()
    // sUNexus()
    // sDNexus()
    // sTestJar()
    // sCurlJar()

    def listStagesOrder = [
        'build': 'sBuild',
        'sonar': 'sSonar',
        'run_spring_curl': 'sCurlSpring',
        'upload_nexus': 'sUNexus',
        'download_nexus': 'sDNexus',
        'run_jar': 'sTestJar',
        'curl_jar': 'sCurlJar'
    ]

    // stagesList.each{
    //     if(it == "build"){
    //         sBuild()
    //     }else{
    //         if(it == "sonar"){
    //             sSonar()
    //         }else{
    //             sh "echo 'Caso else'"
    //         }

    //     }
    // }

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
    sBuild()
    sSonar()
    sCurlSpring()
    sUNexus()
    sDNexus()
    sTestJar()
    sCurlJar()
}
def sBuild(){
    env.STAGE = "Paso 1: Build  Test"
    stage("$env.STAGE "){
        sh "echo 'Build  Test!'"
        sh "gradle clean build"
        // code
    }
}
def sSonar(){
    env.STAGE = "Paso 2: Sonar - Análisis Estático"
    stage("$env.STAGE "){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube3') {
            sh "echo 'Calling sonar by ID!'"
            // Run Maven on a Unix agent to execute Sonar.
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}
def sCurlSpring(){
    env.STAGE = "Paso 3: Curl Springboot Gradle sleep 20"
    stage("$env.STAGE "){
        sh "gradle bootRun&"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}
def sUNexus(){
    env.STAGE = "Paso 4: Subir Nexus"
    stage("$env.STAGE "){
        nexusPublisher nexusInstanceId: 'nexus3',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: '.jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
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
def sDNexus(){
     stage("Paso 5: Descargar Nexus"){
        env.STAGE = env.STAGE_NAME
        sh 'curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus3:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }

}

def sTestJar(){
    stage("Paso 6: Levantar Artefacto Jar"){
        env.STAGE = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}
def sCurlJar(){
    stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        env.STAGE = env.STAGE_NAME
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

return this;