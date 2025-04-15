// Ajoutez ce bloc dans votre fichier build.gradle

plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0' // Ajustez la version selon votre projet
    id 'io.spring.dependency-management' version '1.1.4'
    // ... vos autres plugins
}

// ... Vos autres configurations

// Définition d'un groupe de tâches pour la distribution
def distribGroup = 'Distribution'

// Tâche pour créer l'arborescence de l'intégration
task createIntegrationStructure(type: Copy, group: distribGroup, description: 'Crée l\'arborescence d\'intégration') {
    // Crée les dossiers nécessaires
    doFirst {
        delete "${buildDir}/integration"
        mkdir "${buildDir}/integration"
        mkdir "${buildDir}/integration/bin"
        mkdir "${buildDir}/integration/conf"
        mkdir "${buildDir}/integration/etc"
        mkdir "${buildDir}/integration/logs"
        mkdir "${buildDir}/integration/script"
    }
    
    // Copier le JAR principal dans le dossier bin
    from("${buildDir}/libs") {
        include "*.jar"
        rename { String fileName ->
            "ibcproxy.jar"
        }
        into "bin"
    }
    
    // Copier le fichier application.yml depuis le projet
    from("src/main/resources") {
        include "application.yml"
        into "etc"
    }
    
    // Copier le fichier logback.xml depuis le projet
    from("src/main/resources") {
        include "logback.xml"
        into "conf"
    }
    
    // Copier le fichier jbcproxy.cfg depuis le projet
    from("src/main/resources/config") {
        include "jbcproxy.cfg"
        into "conf"
    }
    
    // Copier le script manage.sh depuis le projet
    from("src/main/resources/scripts") {
        include "manage.sh"
        into "script"
    }
    
    // Rendre le script manage.sh exécutable
    doLast {
        exec {
            workingDir = file("${buildDir}/integration/script")
            commandLine = ['chmod', '+x', 'manage.sh']
        }
    }
    
    into "${buildDir}/integration"
}

// Tâche pour créer l'archive tar.gz
task distrib(type: Tar, dependsOn: [bootJar, createIntegrationStructure], group: distribGroup, description: 'Crée l\'archive de distribution tar.gz') {
    archiveFileName = "ibcproxy-${version}.tar.gz" 
    compression = Compression.GZIP
    
    from "${buildDir}/integration"
    into "ibcproxy-${version}"
    
    doLast {
        println "Archive de distribution créée : ${archiveFile.get().asFile.path}"
    }
}

// Ajout de la tâche distrib à la tâche build
build.dependsOn distrib
