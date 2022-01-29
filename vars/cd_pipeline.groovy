/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/

import utilities.*

def call(stages){
    def stagesList = stages.split(';')
    sh "echo ${stagesList}"


}


def sBuild(){
    env.STAGE = "Paso 1: Build  Test"
    stage("$env.STAGE "){
    // stage("Paso 1: Build - Test"){
        sh "gradle clean build"
    }
}

def sSonar(){
    env.STAGE = "Paso 2: Sonar - Análisis Estático"
    stage("$env.STAGE "){
    // stage("Paso 2: Sonar - Análisis Estático"){
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
             sh 'chmod +x gradlew'
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }
}

def sCurl(){
    env.STAGE = "Paso 3: Curl Springboot Gradle sleep 40"
    stage("$env.STAGE "){
    // stage("Paso 3: Curl Springboot Gradle sleep 40"){
        sh "gradle bootRun&"
        sh "sleep 40 - curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}

def sUNexus(){
     env.STAGE = "Paso 4: Subir Nexus"
    stage("$env.STAGE "){
    // stage("Paso 4: Subir Nexus"){
        nexusPublisher nexusInstanceId: 'nexus',
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
    env.STAGE = "Paso 5: Descargar Nexus"
    stage("$env.STAGE "){
    // stage("Paso 5: Descargar Nexus"){
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexucito:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}

def  sUArt(){
    env.STAGE = "Paso 6: Levantar Artefacto Jar"
    stage("$env.STAGE "){
    // stage("Paso 6: Levantar Artefacto Jar"){
        // sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
        sh 'java -jar DevOpsUsach2020-0.0.1.jar &'
    }
}

def sTest(){
    env.STAGE = "Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "
    stage("$env.STAGE "){
    // stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        sh "sleep 20 - curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}   

return this;