 Voici une configuration Gradle qui devrait fonctionner avec PWA en mode offline :

plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1'
    id 'io.spring.dependency-management' version '1.1.4'
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
    implementation 'org.parttio:line-awesome:2.0.0'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.vaadin:flow-server-production-mode'
    
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}

bootRun {
    systemProperties = [
        'vaadin.devmode.webpack.options': '--port 0',
        'vaadin.pwa.offline': 'true',
        'vaadin.compatibilityMode': 'false'
    ]
}

Et nous devons ajouter une classe pour configurer le PWA :

package com.example;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@Push
@Theme("my-theme")
@PWA(
    name = "My Vaadin App",
    shortName = "My App",
    offlinePath="offline.html",
    offlineResources = { "./images/offline.png" },
    enableInstallPrompt = true
)
public class AppShell implements AppShellConfigurator {
}

Ajoutez aussi dans `application.properties` :

```properties
server.port=8080
vaadin.whitelisted-packages=com.example
vaadin.pwa.offline=true
```

Et créez les fichiers :
1. `src/main/resources/META-INF/resources/offline.html` :
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Offline</title>
</head>
<body>
    <div>You are offline. Please check your connection.</div>
</body>
</html>
```

2. `src/main/resources/META-INF/resources/images/offline.png` (une image pour le mode offline)

Pour lancer :
1. `./gradlew clean`
2. `./gradlew build`
3. `./gradlew bootRun`

Cette configuration devrait permettre :
- Le mode offline
- L'installation comme PWA
- Le cache des ressources statiques
- Une page offline personnalisée

Les principales caractéristiques PWA activées sont :
- Service Worker pour le cache
- Manifest pour l'installation
- Page offline personnalisée
- Push notifications (configurées mais nécessitant une implémentation)

Voulez-vous que je détaille certains aspects ou que j'ajoute d'autres fonctionnalités PWA ?
