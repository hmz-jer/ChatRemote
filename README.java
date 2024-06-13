 apply plugin: 'java'
apply plugin: 'application'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.bouncycastle:bcprov-jdk15on:1.68'
    testImplementation 'junit:junit:4.12'
}

sourceSets {
    server {
        java {
            srcDirs = ['src/main/java/org/stet/server']
        }
        resources {
            srcDirs = ['resources/server']
        }
    }
    client {
        java {
            srcDirs = ['src/main/java/org/stet/client']
        }
        resources {
            srcDirs = ['resources/client']
        }
    }
}

task createServerJar(type: Jar) {
    archiveBaseName.set('server-socket')
    manifest {
        attributes(
            'Main-Class': 'org.stet.server.ServerSocket'
        )
    }
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    from sourceSets.server.output
    from sourceSets.server.resources
}

task createClientJar(type: Jar) {
    archiveBaseName.set('client')
    manifest {
        attributes(
            'Main-Class': 'org.stet.client.Client'
        )
    }
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    from sourceSets.client.output
    from sourceSets.client.resources
}

tasks.build.dependsOn(createServerJar, createClientJar)
