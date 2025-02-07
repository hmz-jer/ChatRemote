
 plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.vaadin' version '24.5.11'
    id 'com.github.node-gradle.node' version '7.0.1'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation platform('com.vaadin:vaadin-bom:24.5.11')
    implementation 'com.vaadin:vaadin'
    implementation 'com.vaadin:vaadin-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

node {
    download = false // Ne pas télécharger Node
    version = '20.11.1' // Spécifiez la version que vous avez en local
}

vaadin {
    optimizeBundle = false
    pnpmEnable = true
}

tasks.named('test') {
    useJUnitPlatform()
}

bootRun {
    systemProperties = [
        'vaadin.devmode.webpack.options': '--port 0'
    ]
}m
