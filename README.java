 Pour améliorer la visibilité de votre application lors du démarrage, vous pouvez personnaliser les logs pour afficher clairement le port, l'IP et les endpoints API disponibles. Voici comment configurer cela:

## 1. Création d'une classe pour le log au démarrage

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class ApplicationStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    @Autowired
    private Environment env;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = env.getProperty("server.port", "8443");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String protocol = env.getProperty("server.ssl.enabled", "true").equals("true") ? "https" : "http";
        
        logger.info("╔═══════════════════════════════════════════════════════════════════════════╗");
        logger.info("║                                                                          ║");
        logger.info("║                    MOCK-CLIENT-VOP DÉMARRÉ AVEC SUCCÈS                   ║");
        logger.info("║                                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════════════════════╣");
        
        // Afficher toutes les interfaces réseau et adresses IP
        try {
            logger.info("║ Interfaces réseau disponibles:                                         ║");
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            
            for (NetworkInterface netint : Collections.list(nets)) {
                if (!netint.isLoopback() && netint.isUp()) {
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                        String ip = inetAddress.getHostAddress();
                        // Filtrer les adresses IPv6
                        if (!ip.contains(":")) {
                            String accessUrl = protocol + "://" + ip + ":" + port + contextPath;
                            logger.info("║ • Interface: " + netint.getDisplayName());
                            logger.info("║     Adresse IP: " + ip);
                            logger.info("║     URL d'accès: " + accessUrl);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des interfaces réseau", e);
        }

        // Afficher les endpoints API disponibles
        logger.info("╠══════════════════════════════════════════════════════════════════════════╣");
        logger.info("║ Endpoints API disponibles:                                               ║");
        
        requestMappingHandlerMapping.getHandlerMethods().forEach((requestMappingInfo, handlerMethod) -> {
            if (requestMappingInfo.getPatternsCondition() != null) {
                requestMappingInfo.getPatternsCondition().getPatterns().forEach(pattern -> {
                    requestMappingInfo.getMethodsCondition().getMethods().forEach(method -> {
                        String endpoint = method + " " + pattern;
                        logger.info("║ • " + endpoint);
                    });
                });
            }
        });
        
        logger.info("╠══════════════════════════════════════════════════════════════════════════╣");
        logger.info("║ Pour valider un certificat QWAC, utilisez:                               ║");
        logger.info("║ curl -v --cert client.crt --key client.key --cacert ca.crt               ║");
        logger.info("║      https://[ADRESSE_IP]:" + port + "/api/status                             ║");
        logger.info("╚══════════════════════════════════════════════════════════════════════════╝");
    }
}
```

## 2. Modification du script de démarrage

Modifiez votre script `mock-vop.sh` pour afficher le logo et l'information de base quand le service démarre:

```bash
# Fonction pour démarrer l'application (dans mock-vop.sh)
start_app() {
  # [code existant...]

  # Démarrer l'application
  echo "╔═══════════════════════════════════════════════════════╗"
  echo "║                                                        ║"
  echo "║          DÉMARRAGE DU MOCK-CLIENT-VOP                 ║"
  echo "║                                                        ║"
  echo "╚═══════════════════════════════════════════════════════╝"
  
  nohup java $JAVA_OPTS \
    -Dspring.config.additional-location=file:$INSTALL_DIR/config/ \
    -Dlogging.file.name=$LOG_FILE \
    -jar "$JAR_FILE" > "$INSTALL_DIR/logs/startup.log" 2>&1 &

  # Sauvegarder le PID
  echo $! > "$PID_FILE"
  echo "Application démarrée avec le PID $(cat $PID_FILE)"

  # Vérifier que l'application a bien démarré
  sleep 5
  if ps -p $(cat "$PID_FILE") > /dev/null; then
    echo "L'application a démarré avec succès"
    
    # Afficher les informations de configuration
    SERVER_IP=$(grep "server.address" $INSTALL_DIR/config/application.yml | awk '{print $2}' || echo "0.0.0.0")
    SERVER_PORT=$(grep "server.port" $INSTALL_DIR/config/application.yml | awk '{print $2}' || echo "8443")
    echo ""
    echo "Informations de connexion:"
    echo "- Port: $SERVER_PORT"
    echo "- Interface: $SERVER_IP"
    echo "- API pour vérification de statut: https://$SERVER_IP:$SERVER_PORT/api/status"
    echo ""
    echo "Pour valider un certificat QWAC, utilisez:"
    echo "curl -v --cert client.crt --key client.key --cacert ca.crt https://$SERVER_IP:$SERVER_PORT/api/status"
    echo ""
    echo "Les logs détaillés sont disponibles dans $LOG_FILE"
    echo "Pour voir les logs en temps réel: $0 logs -f"
    
    return 0
  else
    echo "L'application n'a pas démarré correctement. Consultez les logs pour plus d'informations."
    echo "Contenu du fichier startup.log:"
    cat "$INSTALL_DIR/logs/startup.log"
    return 1
  fi
}
```

## 3. Ajout d'un endpoint d'informations système

Créez un contrôleur qui fournit des informations système pour faciliter le diagnostic:

```java
package com.example.mockclientvop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system")
public class SystemInfoController {

    @Autowired
    private Environment env;
    
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    
    @Value("${server.port:8443}")
    private String serverPort;
    
    @Value("${server.address:0.0.0.0}")
    private String serverAddress;
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Informations générales
        info.put("application", "mock-client-VOP");
        info.put("version", env.getProperty("application.version", "1.0.0"));
        info.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        // Informations serveur
        Map<String, Object> serverInfo = new HashMap<>();
        serverInfo.put("port", serverPort);
        serverInfo.put("address", serverAddress);
        serverInfo.put("hostname", getHostName());
        serverInfo.put("ssl", env.getProperty("server.ssl.enabled", "true"));
        
        info.put("server", serverInfo);
        
        // Informations sur les endpoints
        Set<String> endpoints = requestMappingHandlerMapping.getHandlerMethods().keySet().stream()
                .filter(mapping -> mapping.getPatternsCondition() != null)
                .flatMap(mapping -> mapping.getPatternsCondition().getPatterns().stream()
                        .map(pattern -> mapping.getMethodsCondition().getMethods().stream()
                                .map(method -> method + " " + pattern)
                                .collect(Collectors.joining(", "))))
                .collect(Collectors.toSet());
        
        info.put("endpoints", endpoints);
        
        // Informations de certificat
        Map<String, Object> certInfo = new HashMap<>();
        certInfo.put("truststore", env.getProperty("server.ssl.trust-store", ""));
        certInfo.put("keystore", env.getProperty("server.ssl.key-store", ""));
        certInfo.put("clientAuth", env.getProperty("server.ssl.client-auth", "need"));
        
        info.put("certificate", certInfo);
        
        return ResponseEntity.ok(info);
    }
    
    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
```

## 4. Ajout d'une fonctionnalité de test au script

Ajoutez une option pour tester rapidement si le serveur fonctionne:

```bash
# Ajouter cette fonction dans mock-vop.sh
test_server() {
  if [ ! -f "$PID_FILE" ]; then
    echo "Le serveur n'est pas en cours d'exécution."
    return 1
  fi
  
  SERVER_IP=$(grep "server.address" $INSTALL_DIR/config/application.yml | awk '{print $2}' || echo "0.0.0.0")
  if [ "$SERVER_IP" = "0.0.0.0" ]; then
    # Si le serveur écoute sur toutes les interfaces, utiliser localhost pour le test
    SERVER_IP="localhost"
  fi
  
  SERVER_PORT=$(grep "server.port" $INSTALL_DIR/config/application.yml | awk '{print $2}' || echo "8443")
  
  echo "Test de connexion au serveur https://$SERVER_IP:$SERVER_PORT/api/system/info..."
  curl -k -s https://$SERVER_IP:$SERVER_PORT/api/system/info | jq . || echo "Err
