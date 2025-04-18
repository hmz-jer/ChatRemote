echo -n "curl http://localhost:8080/actuator/health" | tr -d '\n' | bash


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
task createIntegrationStructure(group: distribGroup, description: 'Crée l\'arborescence d\'intégration') {
    dependsOn bootJar
    
    doFirst {
        // Nettoyage
        delete layout.buildDirectory.dir("integration")
        
        // Création des dossiers
        def integrationDir = layout.buildDirectory.dir("integration").get().asFile
        def binDir = new File(integrationDir, "bin")
        def confDir = new File(integrationDir, "conf")
        def etcDir = new File(integrationDir, "etc")
        def logsDir = new File(integrationDir, "logs")
        def scriptDir = new File(integrationDir, "script")
        
        binDir.mkdirs()
        confDir.mkdirs()
        etcDir.mkdirs()
        logsDir.mkdirs()
        scriptDir.mkdirs()
        
        // Copie du JAR
        copy {
            from layout.buildDirectory.dir("libs")
            include "*.jar"
            rename { String fileName ->
                "ibcproxy.jar"
            }
            into binDir
        }
        
        // Copie de application.yml
        copy {
            from "src/main/resources"
            include "application.yml"
            into etcDir
        }
        
        // Copie de logback.xml
        copy {
            from "src/main/resources"
            include "logback.xml"
            into confDir
        }
        
        // Copie de jbcproxy.cfg
        copy {
            from "src/main/resources/config"
            include "jbcproxy.cfg"
            into confDir
        }
        
        // Copie du script manage.sh
        copy {
            from "src/main/resources/scripts"
            include "manage.sh"
            into scriptDir
        }
        
        // Rendre le script manage.sh exécutable
        def manageScript = new File(scriptDir, "manage.sh")
        if (manageScript.exists()) {
            manageScript.setExecutable(true, false)
        }
    }
}

// Tâche pour créer l'archive tar.gz
task distrib(type: Tar, group: distribGroup, description: 'Crée l\'archive de distribution tar.gz') {
    dependsOn createIntegrationStructure
    
    archiveFileName = "ibcproxy-${project.version}.tar.gz" 
    compression = Compression.GZIP
    
    from layout.buildDirectory.dir("integration")
    into "ibcproxy-${project.version}"
    
    doLast {
        println "Archive de distribution créée : ${archiveFile.get().asFile.path}"
    }
}

// Ajout de la tâche distrib à la tâche build
tasks.named('build') {
    dependsOn distrib
}
