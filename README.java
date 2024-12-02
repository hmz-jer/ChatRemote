    // build.gradle
plugins {
    id 'java'
    id 'maven-publish'
}

def getCurrentVersionFromNexus() {
    def nexusUrl = "http://votre-nexus/repository"
    def artifactPath = "votre/groupe/artifact"
    
    def connection = new URL("${nexusUrl}/${artifactPath}/maven-metadata.xml").openConnection()
    connection.requestMethod = 'GET'
    
    if (connection.responseCode == 200) {
        def metadata = new XmlSlurper().parse(connection.inputStream)
        def latestVersion = metadata.versioning.latest.text()
        return latestVersion ?: "1.0.0" // Version par défaut si aucune version trouvée
    }
    
    println "Impossible de récupérer la version depuis Nexus. Utilisation de la version par défaut."
    return "1.0.0"
}

def incrementVersion(String currentVersion, boolean isRelease) {
    def (major, minor, patch) = currentVersion.tokenize('.')
                                            .collect { it.toInteger() }
    
    if (isRelease) {
        minor++
        patch = 0
    } else {
        patch++
    }
    
    return "${major}.${minor}.${patch}"
}

task updateVersion {
    doLast {
        def isRelease = project.hasProperty('release') ? 
            project.property('release').toBoolean() : false
            
        def currentVersion = getCurrentVersionFromNexus()
        def newVersion = incrementVersion(currentVersion, isRelease)
        
        project.version = newVersion
        
        println "Version actuelle: ${currentVersion}"
        println "Nouvelle version: ${newVersion}"
        
        // Optionnel: Sauvegarde la nouvelle version dans un fichier properties
        file("version.properties").text = "version=${newVersion}"
    }
}

// Configure la version initiale du projet
project.version = getCurrentVersionFromNexus()

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
        }
    }
    
    repositories {
        maven {
            url = "http://votre-nexus/repository"
        }
    }
}
