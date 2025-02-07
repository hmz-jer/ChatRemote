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
    implementation ('com.vaadin:vaadin-spring-boot-starter') {
        exclude group: 'com.vaadin', module: 'vaadin-dev-server'
    }
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.vaadin:flow-server-production-mode'
    
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

vaadin {
    optimizeBundle = true
    productionMode = true
}

// Configuration spécifique pour le mode production
bootRun {
    systemProperties = [
        'vaadin.productionMode': 'true'
    ]
}
