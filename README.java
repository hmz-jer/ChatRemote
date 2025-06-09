plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
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
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'org.everit.json:org.everit.json.schema:1.14.1'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
#######################################################
package com.example.jsonschemaflattener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JsonSchemaFlattenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonSchemaFlattenerApplication.class, args);
    }
}
#########################################################
Structure du projet

Voici la structure complète du projet json-schema-flattener :

json-schema-flattener/
├── build.gradle
├── settings.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── jsonschemaflattener/
│       │               ├── JsonSchemaFlattenerApplication.java
│       │               ├── controller/
│       │               │   └── FlattenController.java
│       │               ├── service/
│       │               │   └── SchemaFlattenerService.java
│       │               └── util/
│       │                   └── JsonSchemaValidator.java
│       └── resources/
│           ├── application.properties
│           └── schemas/
│               ├── schema.json
│               └── common/
│                   ├── address.json
│                   ├── country.json
│                   └── preferences.json
└── output/ (créé automatiquement)
    └── schema-resolu.json (généré)

Fonctionnalités implémentées

✅ API REST : Route GET /flatten exposée ✅ Chargement de schéma : Lecture du fichier schema.json depuis src/main/resources/schemas/ ✅ Résolution des $ref : Parcours récursif et remplacement des références locales ✅ Génération de fichier : Création de schema-resolu.json dans le dossier output/ ✅ Validation JSON Schema : Utilisation d'everit-org/json-schema avec Draft-07 ✅ Gestion des erreurs : Retour HTTP 400 en cas d'erreur de validation ✅ Structure propre : Organisation en couches (controller, service, util)
Comment utiliser

    Lancer l'application :

bash

./gradlew bootRun

Appeler l'API :

    bash

    curl http://localhost:8080/flatten

    Réponses possibles :
        Succès : {"status": "ok"}
        Erreur : {"status": "error", "message": "détails de l'erreur"}

Caractéristiques techniques

    Java 17 et Gradle comme demandé
    Jackson ObjectMapper pour le traitement JSON
    Everit JSON Schema pour la validation Draft-07
    Gestion des références circulaires pour éviter les boucles infinies
    Normalisation des chemins pour gérer ./ et chemins relatifs
    Création automatique du dossier output/

Le projet est prêt à être utilisé et inclut des exemples de schémas JSON avec des références imbriquées pour tester le fonctionnement complet.

il va lire les fichier à parir de quelle dossier .
Modifier

D'après le code que j'ai créé, l'application lit les fichiers JSON à partir de deux emplacements différents selon le contexte :
1. Lecture du schéma principal et des références

Dossier source : src/main/resources/schemas/

Dans le service SchemaFlattenerService, la méthode loadSchema() utilise ClassPathResource :

java

private JsonNode loadSchema(String path) throws IOException {
    ClassPathResource resource = new ClassPathResource(path);
    return objectMapper.readTree(resource.getInputStream());
}

Cela signifie que l'application lit les fichiers depuis le classpath, c'est-à-dire depuis src/main/resources/schemas/ dans votre projet source.
2. Écriture du fichier résolu

Dossier de sortie : output/ (à la racine du projet)

java

// 3. Créer le dossier output s'il n'existe pas
Path outputDir = Paths.get("output");
if (!Files.exists(outputDir)) {
    Files.createDirectories(outputDir);
}

// 4. Écrire le schéma aplati
Path outputFile = outputDir.resolve("schema-resolu.json");

Structure complète des fichiers

json-schema-flattener/
├── src/main/resources/schemas/          ← LECTURE (classpath)
│   ├── schema.json
│   └── common/
│       ├── address.json
│       ├── country.json
│       └── preferences.json
└── output/                              ← ÉCRITURE (système de fichiers)
    └── schema-resolu.json

Pourquoi cette approche ?

    Lecture depuis le classpath : Les schémas sources font partie de l'application (packagés dans le JAR)
    Écriture dans le système de fichiers : Le fichier résolu est généré dynamiquement et doit être accessible en dehors du JAR

Si vous voulez que l'application lise les fichiers depuis un dossier spécifique du système de fichiers plutôt que depuis le classpath, je peux modifier le code pour utiliser Files.readString() ou FileInputStream à la place de ClassPathResource.


package com.example.jsonschemaflattener.controller;

import com.example.jsonschemaflattener.service.SchemaFlattenerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FlattenController {

    @Autowired
    private SchemaFlattenerService schemaFlattenerService;

    @GetMapping("/flatten")
    public ResponseEntity<Map<String, String>> flattenSchema() {
        try {
            schemaFlattenerService.flattenAndValidateSchema();
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
########################################
package com.example.jsonschemaflattener.service;

import com.example.jsonschemaflattener.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class SchemaFlattenerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchemaValidator validator = new JsonSchemaValidator();

    public void flattenAndValidateSchema() throws Exception {
        // 1. Charger le schéma principal
        JsonNode schema = loadSchema("schemas/schema.json");
        
        // 2. Résoudre toutes les références $ref
        JsonNode flattenedSchema = flattenSchema(schema, "schemas/");
        
        // 3. Créer le dossier output s'il n'existe pas
        Path outputDir = Paths.get("output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        // 4. Écrire le schéma aplati
        Path outputFile = outputDir.resolve("schema-resolu.json");
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), flattenedSchema);
        
        // 5. Valider le schéma généré
        validator.validateSchema(flattenedSchema);
    }

    private JsonNode loadSchema(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return objectMapper.readTree(resource.getInputStream());
    }

    private JsonNode flattenSchema(JsonNode node, String basePath) throws IOException {
        return flattenSchema(node, basePath, new HashSet<>());
    }

    private JsonNode flattenSchema(JsonNode node, String basePath, Set<String> processedRefs) throws IOException {
        if (node == null) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            
            // Vérifier s'il y a une référence $ref
            if (objectNode.has("$ref")) {
                String ref = objectNode.get("$ref").asText();
                
                // Ne traiter que les références locales (pas HTTP ni internes)
                if (isLocalFileReference(ref)) {
                    // Éviter les références circulaires
                    if (processedRefs.contains(ref)) {
                        throw new RuntimeException("Référence circulaire détectée: " + ref);
                    }
                    
                    processedRefs.add(ref);
                    
                    // Résoudre la référence
                    String resolvedPath = resolvePath(basePath, ref);
                    JsonNode referencedSchema = loadSchema(resolvedPath);
                    
                    // Récursivement aplir le schéma référencé
                    JsonNode flattenedRef = flattenSchema(referencedSchema, getBasePath(resolvedPath), new HashSet<>(processedRefs));
                    
                    processedRefs.remove(ref);
                    return flattenedRef;
                }
            }
            
            // Traiter récursivement tous les champs de l'objet
            ObjectNode result = objectMapper.createObjectNode();
            Iterator<String> fieldNames = objectNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode fieldValue = objectNode.get(fieldName);
                result.set(fieldName, flattenSchema(fieldValue, basePath, processedRefs));
            }
            return result;
            
        } else if (node.isArray()) {
            // Traiter récursivement tous les éléments du tableau
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrayElement = node.get(i);
                JsonNode flattenedElement = flattenSchema(arrayElement, basePath, processedRefs);
                ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, flattenedElement);
            }
        }
        
        return node;
    }

    private boolean isLocalFileReference(String ref) {
        return ref != null && 
               !ref.startsWith("http://") && 
               !ref.startsWith("https://") && 
               !ref.startsWith("#");
    }

    private String resolvePath(String basePath, String ref) {
        // Normaliser le chemin de référence
        if (ref.startsWith("./")) {
            ref = ref.substring(2);
        }
        
        // Combiner le chemin de base avec la référence
        if (basePath.endsWith("/")) {
            return basePath + ref;
        } else {
            return basePath + "/" + ref;
        }
    }

    private String getBasePath(String fullPath) {
        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            return fullPath.substring(0, lastSlashIndex + 1);
        }
        return "";
    }
}
########################################
package com.example.jsonschemaflattener.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JsonSchemaValidator {

    public void validateSchema(JsonNode schemaNode) throws Exception {
        try {
            // Convertir le JsonNode en JSONObject pour everit
            String schemaString = schemaNode.toString();
            InputStream schemaStream = new ByteArrayInputStream(schemaString.getBytes());
            JSONObject jsonSchema = new JSONObject(new JSONTokener(schemaStream));
            
            // Charger et valider le schéma selon Draft-07
            Schema schema = SchemaLoader.builder()
                    .schemaJson(jsonSchema)
                    .draftV7Support()
                    .build()
                    .load()
                    .build();
            
            // Si on arrive ici, le schéma est valide
            System.out.println("Schéma validé avec succès selon JSON Schema Draft-07");
            
        } catch (Exception e) {
            throw new RuntimeException("Validation du schéma échouée: " + e.getMessage(), e);
        }
    }
}
###########################################
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "title": "User Schema",
  "properties": {
    "id": {
      "type": "integer",
      "description": "Unique identifier"
    },
    "name": {
      "type": "string",
      "description": "User name"
    },
    "email": {
      "type": "string",
      "format": "email"
    },
    "address": {
      "$ref": "./common/address.json"
    },
    "preferences": {
      "$ref": "./common/preferences.json"
    }
  },
  "required": ["id", "name", "email"],
  "additionalProperties": false
}
#########################################################
{
  "type": "object",
  "title": "Address Schema",
  "properties": {
    "street": {
      "type": "string"
    },
    "city": {
      "type": "string"
    },
    "zipCode": {
      "type": "string",
      "pattern": "^[0-9]{5}$"
    },
    "country": {
      "$ref": "./country.json"
    }
  },
  "required": ["street", "city", "zipCode"],
  "additionalProperties": false
}
###########################################################
{
  "type": "object",
  "title": "Country Schema",
  "properties": {
    "code": {
      "type": "string",
      "pattern": "^[A-Z]{2}$",
      "description": "ISO country code"
    },
    "name": {
      "type": "string",
      "description": "Country name"
    }
  },
  "required": ["code", "name"],
  "additionalProperties": false
}
###############################################################
server.port=8080
logging.level.com.example.jsonschemaflattener=DEBUG
 ###############################################################
 rootProject.name = 'json-schema-flattener'
