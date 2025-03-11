
Je vais créer une solution qui permet d'ajouter le nouveau fonctionnement "Émetteur" tout en préservant l'existant. Après avoir examiné le code actuel dans vos captures d'écran, voici mon approche:

1. D'abord, créons une interface commune pour les gestionnaires de notifications:

```java
package com.ser2s.ws_simulator.server.service.notification;

import org.springframework.http.ResponseEntity;
import java.util.Map;

public interface NotificationHandler {
    /**
     * Vérifie si ce handler peut traiter cette notification
     */
    boolean canHandle(String path, Map<String, Object> body);
    
    /**
     * Traite la notification et génère une réponse
     */
    ResponseEntity<?> handleNotification(String path, Map<String, Object> body);
}
```

2. Maintenant, créons une implémentation pour le nouveau traitement Émetteur:

```java
package com.ser2s.ws_simulator.server.service.notification;

import com.ser2s.ws_simulator.server.service.specification.NotificationSpecification;
import com.ser2s.ws_simulator.server.service.specification.SpecificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmetteurNotificationHandler implements NotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmetteurNotificationHandler.class);
    
    @Autowired
    private SpecificationManager specificationManager;
    
    @Override
    public boolean canHandle(String path, Map<String, Object> body) {
        Optional<NotificationSpecification> spec = specificationManager.lookupSpecification(path);
        if (!spec.isPresent()) {
            return false;
        }
        
        // Vérifier si le type est "emetteur"
        return "emetteur".equalsIgnoreCase((String) spec.get().getAdditionalProperty("type"));
    }
    
    @Override
    public ResponseEntity<?> handleNotification(String path, Map<String, Object> body) {
        LOGGER.info("Handling notification with Emetteur handler: {}", path);
        
        Optional<NotificationSpecification> spec = specificationManager.lookupSpecification(path);
        if (!spec.isPresent()) {
            LOGGER.warn("No specification found for path: {}", path);
            return buildErrorResponse("No specification found");
        }
        
        NotificationSpecification specification = spec.get();
        
        // Récupérer la configuration CSV
        Map<String, Object> csvConfig = (Map<String, Object>) specification.getAdditionalProperty("csvConfig");
        if (csvConfig == null || !Boolean.TRUE.equals(csvConfig.get("enabled"))) {
            LOGGER.warn("CSV configuration not enabled for path: {}", path);
            return buildErrorResponse("CSV configuration not enabled");
        }
        
        // Charger les scénarios depuis le fichier CSV
        String filePath = (String) csvConfig.get("filePath");
        String identifier = (String) csvConfig.get("scenarioIdentifier");
        String defaultScenarioId = (String) csvConfig.get("defaultScenarioId");
        String tempoColumn = (String) csvConfig.get("tempoColumn");
        
        if (filePath == null || identifier == null) {
            LOGGER.warn("Missing required CSV configuration for path: {}", path);
            return buildErrorResponse("Invalid CSV configuration");
        }
        
        // Trouver le scénario correspondant
        try {
            List<Map<String, String>> scenarios = loadCsvScenarios(filePath);
            
            // Récupérer la valeur de l'identifiant dans la requête
            Object identifierValue = body.get(identifier);
            if (identifierValue == null) {
                LOGGER.warn("Identifier {} not found in request", identifier);
                return useDefaultScenario(scenarios, defaultScenarioId, tempoColumn);
            }
            
            // Chercher un scénario correspondant
            Optional<Map<String, String>> matchingScenario = scenarios.stream()
                    .filter(scenario -> identifierValue.toString().equals(scenario.get(identifier + "Expected")))
                    .findFirst();
            
            if (matchingScenario.isPresent()) {
                return buildResponse(matchingScenario.get(), body, tempoColumn);
            } else {
                // Utiliser le scénario par défaut si défini
                return useDefaultScenario(scenarios, defaultScenarioId, tempoColumn);
            }
            
        } catch (IOException e) {
            LOGGER.error("Error reading CSV file: {}", filePath, e);
            return buildErrorResponse("Error processing CSV file");
        }
    }
    
    private ResponseEntity<?> useDefaultScenario(List<Map<String, String>> scenarios, 
                                              String defaultScenarioId, 
                                              String tempoColumn) {
        if (defaultScenarioId == null) {
            LOGGER.warn("No default scenario ID configured and no matching scenario found");
            return buildErrorResponse("No matching scenario");
        }
        
        Optional<Map<String, String>> defaultScenario = scenarios.stream()
                .filter(scenario -> defaultScenarioId.equals(scenario.get("ScenarioId")))
                .findFirst();
        
        if (defaultScenario.isPresent()) {
            return buildResponse(defaultScenario.get(), new HashMap<>(), tempoColumn);
        } else {
            LOGGER.warn("Default scenario {} not found", defaultScenarioId);
            return buildErrorResponse("Default scenario not found");
        }
    }
    
    private ResponseEntity<?> buildResponse(Map<String, String> scenario, 
                                         Map<String, Object> request,
                                         String tempoColumn) {
        Map<String, Object> response = new HashMap<>();
        
        // Extraire toutes les colonnes qui ne sont pas des critères de correspondance
        // (celles qui ne se terminent pas par "Expected")
        scenario.forEach((key, value) -> {
            if (!key.endsWith("Expected") && !"ScenarioId".equals(key)) {
                // Traiter les valeurs spéciales
                if ("NULL".equals(value)) {
                    // Ignorer
                } else if ("ECHO".equals(value)) {
                    // Copier la valeur depuis la requête
                    String requestKey = key.replace("Expected", "");
                    if (request.containsKey(requestKey)) {
                        response.put(key, request.get(requestKey));
                    }
                } else if ("non renseigné".equals(value)) {
                    // Champ vide
                    response.put(key, "");
                } else {
                    // Valeur normale
                    response.put(key, value);
                }
            }
        });
        
        // Appliquer la temporisation si configurée
        if (tempoColumn != null && scenario.containsKey(tempoColumn)) {
            String tempoValue = scenario.get(tempoColumn);
            if (tempoValue != null && !tempoValue.isEmpty()) {
                try {
                    long delay = Long.parseLong(tempoValue);
                    if (delay > 0) {
                        LOGGER.info("Applying delay of {} ms", delay);
                        Thread.sleep(delay);
                    }
                } catch (NumberFormatException | InterruptedException e) {
                    LOGGER.warn("Error applying delay: {}", e.getMessage());
                }
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<?> buildErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("RspnCode", "99");
        response.put("RspnRsn", "9999");
        response.put("error", message);
        return ResponseEntity.badRequest().body(response);
    }
    
    private List<Map<String, String>> loadCsvScenarios(String filePath) throws IOException {
        List<Map<String, String>> scenarios = new java.util.ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Lire l'en-tête
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("CSV file is empty");
            }
            
            String[] headers = headerLine.split(",");
            
            // Lire les lignes de données
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignorer les lignes vides ou les commentaires
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }
                
                String[] values = line.split(",");
                Map<String, String> scenario = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    scenario.put(headers[i].trim(), values[i].trim());
                }
                
                scenarios.add(scenario);
            }
        }
        
        return scenarios;
    }
}
```

3. Maintenant, modifions le `RoutingNotificationManagerImpl` existant pour utiliser notre nouveau handler:

```java
package com.ser2s.ws_simulator.server.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RoutingNotificationManagerImpl implements RoutingNotificationManager {

    // ... code existant ...
    
    @Autowired
    private List<NotificationHandler> notificationHandlers;
    
    @Override
    public NotificationResponse routeNotifications(String path, Map<String, Object> body) {
        // Tenter d'utiliser les nouveaux handlers d'abord
        for (NotificationHandler handler : notificationHandlers) {
            if (handler.canHandle(path, body)) {
                ResponseEntity<?> response = handler.handleNotification(path, body);
                // Convertir ResponseEntity en NotificationResponse 
                // (peut nécessiter une adaptation selon votre modèle de données)
                return convertToNotificationResponse(response);
            }
        }
        
        // Si aucun nouveau handler ne peut traiter, utiliser le code existant
        // ... code existant de routeNotifications ...
    }
    
    private NotificationResponse convertToNotificationResponse(ResponseEntity<?> responseEntity) {
        // Implémentation de la conversion selon votre modèle
        // Exemple simplifié:
        NotificationSpecification dummySpec = null; // à adapter selon votre contexte
        ResponseEntity<?> responseEntity = (ResponseEntity<?>) responseEntity.getBody();
        return new NotificationResponse(dummySpec, responseEntity);
    }
    
    // ... reste du code existant ...
}
```

4. Enfin, assurons-nous que `SpecificationManager` peut gérer les propriétés additionnelles:

```java
package com.ser2s.ws_simulator.server.service.specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SpecificationManager {

    // ... code existant ...
    
    public Optional<NotificationSpecification> lookupSpecification(String path) {
        // ... code existant pour charger la spécification ...
        
        // Pour assurer que les propriétés additionnelles comme "type" et "csvConfig"
        // sont préservées lors de la désérialisation
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        try {
            // Charger le fichier JSON comme une Map générique
            Map<String, Object> rawSpec = mapper.readValue(specFile.toFile(), Map.class);
            
            // Créer la spécification normalement
            NotificationSpecification spec = mapper.convertValue(rawSpec, NotificationSpecification.class);
            
            // Ajouter toutes les propriétés qui ne font pas partie du modèle standard
            for (Map.Entry<String, Object> entry : rawSpec.entrySet()) {
                if (!isStandardProperty(entry.getKey())) {
                    spec.addAdditionalProperty(entry.getKey(), entry.getValue());
                }
            }
            
            return Optional.of(spec);
        } catch (IOException e) {
            logger.error("Error loading specification for path: {}", path, e);
            return Optional.empty();
        }
    }
    
    private boolean isStandardProperty(String key) {
        // Liste des propriétés standard du modèle NotificationSpecification
        return Arrays.asList("path", "notification", "ack").contains(key);
    }
    
    // ... reste du code existant ...
}
```

5. Modifions légèrement le modèle `NotificationSpecification` pour supporter les propriétés additionnelles:

```java
package com.ser2s.ws_simulator.server.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NotificationSpecification {
    
    private String path;
    private Map<String, NotificationField> notification;
    private Map<String, String> ack;
    
    // Stockage pour les propriétés additionnelles
    private Map<String, Object> additionalProperties = new HashMap<>();
    
    @JsonAnySetter
    public void addAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
    
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }
    
    public Object getAdditionalProperty(String name) {
        return this.additionalProperties.get(name);
    }
}
```

Cette approche permet:

1. De conserver tout le code existant intact
2. D'ajouter une couche d'abstraction avec l'interface `NotificationHandler`
3. D'implémenter le nouveau comportement "Émetteur" dans une classe dédiée
4. De modifier le routeur de notifications pour essayer d'abord les nouveaux handlers avant de revenir au comportement existant
5. D'étendre le modèle de spécification pour supporter des propriétés additionnelles comme "type" et "csvConfig"

L'avantage majeur est que cette solution ne nécessite aucune modification des fichiers de spécification existants et assure une rétrocompatibilité totale.
