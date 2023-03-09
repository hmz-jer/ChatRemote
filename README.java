plugins {
    id 'java'
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.7"
    reportsDir = file("$buildDir/reports/jacoco")
    // Exclure certaines classes pour augmenter la couverture de code
    excludes = [
        '**/config/**', // Exclure les classes de configuration
        '**/model/**', // Exclure les classes de modèles de données
        '**/exceptions/**', // Exclure les classes d'exceptions
        '**/utils/**', // Exclure les classes utilitaires
        '**/enums/**', // Exclure les enums
        '**/*Test*', // Exclure les classes de tests
        '**/*TestSuite*', // Exclure les suites de tests
        '**/*Steps*', // Exclure les classes de tests BDD (Behavior Driven Development)
        '**/*Cucumber*', // Exclure les classes Cucumber
    ]
}

test {
    jacoco {
        // Inclure toutes les classes sauf celles exclues par Jacoco
        excludes = jacoco.excludes
        // Ajouter des inclusions si nécessaire
        // includes = ['com.example.mypackage.*']
    }
}

jacocoTestReport {
    // Inclure toutes les classes sauf celles exclues par Jacoco
    additionalSourceDirs.from = sourceSets.main.allSource.srcDirs
    additionalClassDirs.from = files(sourceSets.main.output.classesDirs)
    sourceDirectories.from = files(sourceSets.main.java.srcDirs)
    classDirectories.from = files(sourceSets.main.output.classesDirs)
    // Générer des rapports HTML et XML
    reports {
        xml.enabled = true
        html.enabled = true
    }
}
