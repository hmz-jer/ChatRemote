 plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'com.vaadin' version '24.5.11'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

ext {
    set('vaadinVersion', "24.5.11")
}

dependencies {
    implementation 'com.vaadin:vaadin-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

dependencyManagement {
    imports {
        mavenBom "com.vaadin:vaadin-bom:${vaadinVersion}"
    }
}

tasks.named('test') {
    useJUnitPlatform()
}

// Configuration spécifique pour Vaadin
vaadin {
    optimizeBundle = false
}

// Configuration des tâches
bootRun {
    systemProperty 'vaadin.productionMode', 'false'
}
