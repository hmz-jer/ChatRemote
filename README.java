  # Ajout d'une fonctionnalité de réponses personnalisées par banque dans le mock-client-VOP

Pour implémenter cette fonctionnalité qui permet au mock de répondre différemment selon la banque identifiée (à partir de l'URL ou du certificat), voici les étapes à suivre:

## 1. Création du fichier de configuration des réponses

Créez un fichier `bank-responses.yml` dans le dossier `/opt/mock-client-vop/conf/`:

```yaml
# Configuration des réponses personnalisées par banque
responses:
  # Réponses par défaut (utilisées si aucune banque spécifique ne correspond)
  default:
    status: 200
    headers:
      Content-Type: application/json
    body: |
      {
        "status": "success",
        "message": "Réponse par défaut",
        "timestamp": "${timestamp}"
      }

  # Réponses spécifiques par banque, identifiées par leur PSP ID (PSDFR-ACPR-XXXXX)
  banks:
    # BNP Paribas
    "12345":
      status: 200
      headers:
        Content-Type: application/json
        X-BNP-Api-Version: "1.0"
      body: |
        {
          "status": "success",
          "bank": "BNP Paribas",
          "pspId": "PSDFR-ACPR-12345",
          "message": "Connexion établie avec BNP Paribas",
          "timestamp": "${timestamp}",
          "accountDetails": {
            "available": true,
            "endpoint": "/api/bnp/accounts"
          }
        }

    # Natixis
    "15930":
      status: 200
      headers:
        Content-Type: application/json
        X-Natixis-TraceId: "${requestId}"
      body: |
        {
          "status": "success",
          "bank": "Natixis",
          "pspId": "PSDFR-ACPR-15930",
          "message": "Connexion établie avec Natixis",
          "timestamp": "${timestamp}",
          "features": ["payments", "accounts", "balances"]
        }

    # Société Générale
    "24680":
      status: 200
      headers:
        Content-Type: application/json
        X-SG-Channel: "api-psd2"
      body: |
        {
          "status": "success",
          "bank": "Société Générale",
          "pspId": "PSDFR-ACPR-24680",
          "message": "Connexion établie avec Société Générale",
          "timestamp": "${timestamp}",
          "region": "FR",
          "api": {
            "version": "v3",
            "documentation": "https://developer.socgen.com/docs"
          }
        }

  # Réponses spécifiques par URL (pattern matching)
  urls:
    # Correspond à toutes les URLs contenant "credit-mutuel"
    "credit-mutuel":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "bank": "Crédit Mutuel",
          "message": "Connexion établie avec Crédit Mutuel via URL",
          "timestamp": "${timestamp}"
        }

    # Correspond à toutes les URLs contenant "caisse-epargne"
    "caisse-epargne":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "bank": "Caisse d'Epargne",
          "message": "Connexion établie avec Caisse d'Epargne via URL",
          "timestamp": "${timestamp}"
        }
```

## 2. Création de la classe de configuration pour charger les réponses

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class BankResponsesConfig {

    private static final Logger logger = LoggerFactory.getLogger(BankResponsesConfig.class);

    @Value("${mock-vop.bank-responses-file:file:/opt/mock-client-vop/conf/bank-responses.yml}")
    private Resource bankResponsesFile;

    private Map<String, Object> responsesConfig = new HashMap<>();

    @PostConstruct
    public void loadResponses() {
        try {
            if (bankResponsesFile.exists()) {
                Yaml yaml = new Yaml();
                try (InputStream inputStream = bankResponsesFile.getInputStream()) {
                    responsesConfig = yaml.load(inputStream);
                    logger.info("Fichier de configuration des réponses par banque chargé: {}", 
                        bankResponsesFile.getFilename());
                    
                    // Log du nombre de configurations chargées
                    Map<String, Object> responses = getResponsesSection();
                    Map<String, Object> banks = getBanksSection();
                    Map<String, Object> urls = getUrlsSection();
                    
                    logger.info("Configurations chargées: {} banques par ID PSP, {} banques par URL", 
                        banks != null ? banks.size() : 0, 
                        urls != null ? urls.size() : 0);
                }
            } else {
                logger.warn("Fichier de configuration des réponses par banque introuvable: {}", 
                    bankResponsesFile.getFilename());
            }
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du fichier de configuration des réponses par banque", e);
        }
    }

    @Bean
    public Map<String, Object> bankResponsesConfig() {
        return responsesConfig;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getResponsesSection() {
        return (Map<String, Object>) responsesConfig.getOrDefault("responses", new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDefaultResponse() {
        Map<String, Object> responses = getResponsesSection();
        return (Map<String, Object>) responses.getOrDefault("default", new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBanksSection() {
        Map<String, Object> responses = getResponsesSection();
        return (Map<String, Object>) responses.getOrDefault("banks", new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getUrlsSection() {
        Map<String, Object> responses = getResponsesSection();
        return (Map<String, Object>) responses.getOrDefault("urls", new HashMap<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getBankResponseByPspId(String pspId) {
        Map<String, Object> banks = getBanksSection();
        return (Map<String, Object>) banks.getOrDefault(pspId, null);
    }

    public Map<String, Object> getBankResponseByUrl(String url) {
        Map<String, Object> urls = getUrlsSection();
        
        // Chercher une correspondance partielle dans l'URL
        for (Map.Entry<String, Object> entry : urls.entrySet()) {
            if (url != null && url.contains(entry.getKey())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) entry.getValue();
                return response;
            }
        }
        
        return null;
    }
}
```

## 3. Création d'un service pour gérer les réponses personnalisées

```java
package com.example.mockclientvop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.mockclientvop.config.BankResponsesConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class BankResponseService {

    private static final Logger logger = LoggerFactory.getLogger(BankResponseService.class);

    @Autowired
    private BankResponsesConfig bankResponsesConfig;

    public ResponseEntity<String> generateResponse(String pspId, String requestUrl) {
        // Variables pour les réponses dynamiques
        Map<String, String> variables = new HashMap<>();
        variables.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        variables.put("requestId", UUID.randomUUID().toString());
        variables.put("pspId", pspId != null ? pspId : "unknown");
        
        // Chercher une réponse par PSP ID
        Map<String, Object> responseConfig = null;
        String responseSource = null;
        
        if (pspId != null) {
            responseConfig = bankResponsesConfig.getBankResponseByPspId(pspId);
            if (responseConfig != null) {
                responseSource = "PSP ID " + pspId;
            }
        }
        
        // Si aucune réponse trouvée par PSP ID, chercher par URL
        if (responseConfig == null && requestUrl != null) {
            responseConfig = bankResponsesConfig.getBankResponseByUrl(requestUrl);
            if (responseConfig != null) {
                responseSource = "URL pattern";
            }
        }
        
        // Si aucune réponse spécifique trouvée, utiliser la réponse par défaut
        if (responseConfig == null) {
            responseConfig = bankResponsesConfig.getDefaultResponse();
            responseSource = "default";
        }
        
        // Générer la réponse HTTP
        logger.info("Génération d'une réponse personnalisée (source: {})", responseSource);
        
        // Récupérer les éléments de la réponse
        int status = ((Number) responseConfig.getOrDefault("status", 200)).intValue();
        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) responseConfig.getOrDefault("headers", new HashMap<>());
        String body = (String) responseConfig.getOrDefault("body", "{}");
        
        // Remplacer les variables dans le corps de la réponse
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            body = body.replace("${" + variable.getKey() + "}", variable.getValue());
        }
        
        // Construire la réponse HTTP
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach(httpHeaders::add);
        
        return ResponseEntity
                .status(HttpStatus.valueOf(status))
                .headers(httpHeaders)
                .body(body);
    }
}
```

## 4. Modification du contrôleur pour utiliser les réponses personnalisées

```java
package com.example.mockclientvop.controller;

import com.example.mockclientvop.service.BankResponseService;
import com.example.mockclientvop.service.CertificateService;
import com.example.mockclientvop.service.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MockController {

    private static final Logger logger = LoggerFactory.getLogger(MockController.class);
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private RoutingService routingService;
    
    @Autowired
    private BankResponseService bankResponseService;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        String pspIdValue = pspId.orElse(null);
        
        // Générer une réponse personnalisée en fonction du PSP ID et de l'URL
        return bankResponseService.generateResponse(pspIdValue, request.getRequestURI());
    }
    
    @RequestMapping("/**")
    public ResponseEntity<?> handleRequest(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        String pspIdValue = pspId.orElse(null);
        
        // Log des informations de routage
        String targetUrl = routingService.determineTargetUrl(clientCert);
        logger.info("Requête reçue: {} {} - PSP ID: {} - URL cible: {}", 
            request.getMethod(), request.getRequestURI(), pspIdValue, targetUrl);
        
        // Générer une réponse personnalisée en fonction du PSP ID et de l'URL
        return bankResponseService.generateResponse(pspIdValue, request.getRequestURI());
    }
}
```

## 5. Modifier le script mock-vop.sh pour gérer le fichier de réponses

Ajoutez une fonction pour gérer le fichier de réponses personnalisées:

```bash
# Fonction pour gérer les réponses personnalisées
manage_responses() {
  RESPONSES_FILE="$CONF_DIR/bank-responses.yml"
  
  case "$1" in
    "edit")
      # Déterminer l'éditeur à utiliser
      EDITOR=${EDITOR:-vi}
      if command -v nano &> /dev/null; then
        EDITOR="nano"
      fi
      
      # Vérifier si le fichier existe, sinon créer un modèle
      if [ ! -f "$RESPONSES_FILE" ]; then
        echo "Création d'un fichier de réponses par défaut..."
        cat > "$RESPONSES_FILE" << 'EOF'
# Configuration des réponses personnalisées par banque
responses:
  # Réponse par défaut
  default:
    status: 200
    headers:
      Content-Type: application/json
    body: |
      {
        "status": "success",
        "message": "Réponse par défaut",
        "timestamp": "${timestamp}"
      }
  
  # Réponses spécifiques par banque (PSP ID)
  banks:
    # Exemple pour Natixis
    "15930":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "bank": "Natixis",
          "message": "Connexion établie avec Natixis",
          "timestamp": "${timestamp}"
        }
  
  # Réponses par pattern d'URL
  urls:
    # Exemple pour une URL contenant "bnp"
    "bnp":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "bank": "BNP Paribas",
          "message": "Connexion établie avec BNP Paribas via URL",
          "timestamp": "${timestamp}"
        }
EOF
      fi
      
      # Éditer le fichier
      $EDITOR "$RESPONSES_FILE"
      ;;
      
    "list")
      # Afficher le contenu du fichier (sans les corps de réponse pour plus de lisibilité)
      if [ -f "$RESPONSES_FILE" ]; then
        echo "Réponses personnalisées configurées:"
        grep -A 1 -E "banks:|urls:|default:" "$RESPONSES_FILE" | grep -v -E "body:|headers:"
      else
        echo "Aucun fichier de réponses personnalisées trouvé."
      fi
      ;;
      
    "reload")
      # Redémarrer l'application pour recharger les réponses
      if [ -f "$PID_FILE" ]; then
        echo "Redémarrage de l'application pour recharger les réponses personnalisées..."
        stop_app
        sleep 2
        start_app
      else
        echo "L'application n'est pas en cours d'exécution."
      fi
      ;;
      
    *)
      echo "Action non reconnue pour les réponses personnalisées."
      echo "Utilisez: responses edit|list|reload"
      return 1
      ;;
  esac
}

# Dans le case du script principal, ajoutez:
case "$1" in
  # [autres cas existants]
  "responses")
    shift
    manage_responses "$@"
    ;;
  # [autres cas existants]
esac
```

## 6. Mise à jour de application.yml

Ajoutez la configuration pour le fichier de réponses personnalisées:

```yaml
# Dans application.yml
mock-vop:
  bank-responses-file: file:/opt/mock-client-vop/conf/bank-responses.yml
  
  # Autres configurations existantes
  qwac:
    validation:
      enabled: true
      # ...
```

## 7. Utilisation de la fonctionnalité

1. **Éditer le fichier de réponses personnalisées:**
   ```bash
   mock-vop responses edit
   ```

2. **Lister les réponses configurées:**
   ```bash
   mock-vop responses list
   ```

3. **Recharger les réponses après modification:**
   ```bash
   mock-vop responses reload
   ```

4. **Tester une réponse personnalisée:**
   ```bash
   # Test avec certificat Natixis (PSP ID 15930)
   curl -v --cert natixis-qwac.crt --key natixis-private.key \
     --cacert natixis-ca.cert https://10.55.8.12:8443/api/status
   
   # Test avec URL pour BNP
   curl -v --cert client.crt --key client.key \
     --cacert ca.crt https://10.55.8.12:8443/api/bnp/status
   ```

Cette implémentation vous permet de:
1. Configurer des réponses personnalisées par banque dans un fichier YAML
2. Utiliser le PSP ID extrait du certificat pour déterminer la banque
3. Utiliser l'URL comme méthode alternative d'identification de la banque
4. Avoir une réponse par défaut si aucune correspondance n'est trouvée
5. Inclure des variables dynamiques dans les réponses (timestamp, requestId)
6. Gérer facilement les réponses via le script mock-vop.sh

Le système est extensible et vous pouvez facilement ajouter de nouvelles banques ou modifier les réponses existantes sans avoir à redéployer l'application.
