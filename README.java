 
Je vais vous montrer un exemple complet avec la génération des certificats et le code Java.

1. Premièrement, créons les certificats nécessaires :

```bash
# 1. Création du keystore serveur
keytool -genkeypair -alias serverkey \
    -keyalg RSA -keysize 2048 \
    -validity 365 \
    -dname "CN=localhost,OU=Dev,O=MyCompany,L=Paris,C=FR" \
    -keystore server.keystore \
    -storepass server123 \
    -keypass server123

# 2. Export du certificat serveur
keytool -export -alias serverkey \
    -keystore server.keystore \
    -file server.cer \
    -storepass server123

# 3. Création du truststore client avec le certificat serveur
keytool -import -alias serverkey \
    -file server.cer \
    -keystore client.truststore \
    -storepass client123 \
    -noprompt
```

Maintenant, créons le code serveur et client :

```java
// Server.java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Server {
    public static void main(String[] args) {
        // Configuration SSL
        System.setProperty("server.ssl.key-store", "server.keystore");
        System.setProperty("server.ssl.key-store-password", "server123");
        System.setProperty("server.ssl.key-store-type", "JKS");
        System.setProperty("server.ssl.key-alias", "serverkey");
        System.setProperty("server.port", "8443");
        
        SpringApplication.run(Server.class, args);
    }
}

@RestController
class ServerController {
    @GetMapping("/api/test")
    public String test() {
        return "Connexion SSL réussie!";
    }
}
```

```java
// Client.java
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

public class Client {
    public static void main(String[] args) {
        // Configuration SSL
        System.setProperty("javax.net.ssl.trustStore", "client.truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "client123");

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://localhost:8443/api/test");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("Réponse du serveur: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

Configuration Maven (pom.xml) :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>ssl-demo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
            <version>4.5.13</version>
        </dependency>
    </dependencies>
</project>
```

Configuration application.properties :

```properties
server.ssl.enabled=true
server.ssl.key-store=server.keystore
server.ssl.key-store-password=server123
server.ssl.key-store-type=JKS
server.ssl.key-alias=serverkey
server.port=8443
```

Étapes pour exécuter :

1. Créer un nouveau dossier pour le projet
2. Copier les fichiers pom.xml et application.properties
3. Créer les certificats avec les commandes keytool
4. Créer les classes Server.java et Client.java
5. Compiler et exécuter :

```bash
# Terminal 1 - Démarrer le serveur
mvn spring-boot:run

# Terminal 2 - Exécuter le client
mvn exec:java -Dexec.mainClass="com.example.Client"
```

Points importants à noter :

1. Les chemins des certificats doivent être corrects dans le code
2. Les mots de passe doivent correspondre à ceux utilisés lors de la création
3. Le hostname dans le certificat (CN) doit correspondre à celui utilisé dans l'URL
4. Pour un environnement de production :
   - Utilisez des certificats signés par une autorité reconnue
   - Stockez les mots de passe de manière sécurisée
   - Configurez des ciphers forts
   - Désactivez les protocoles SSL/TLS obsolètes

Vous voulez des précisions sur une partie particulière ?
