package utilities.version

def getCurrentVersion() {
    def version = sh (
        script: "mvn help:evaluate -Dexpression=project.version | grep -e '^[^[]'", returnStdout: true
    ).trim()
    return version
}

def getNextVersion(type_version){
    //*********** Aumentar Version por variable type_version*************************************
    def latestVersion = getCurrentVersion()
    def (major, minor, patch) = latestVersion.tokenize('.').collect { it.toInteger() }
    def nextVersion
    switch (type_version) {
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

    return nextVersion
}
return this;
