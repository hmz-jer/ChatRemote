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
        // Supprimer la partie fragment (#/...) si présente
        if (ref.contains("#")) {
            ref = ref.substring(0, ref.indexOf("#"));
        }
        
        // Traiter les chemins relatifs
        if (ref.startsWith("./")) {
            ref = ref.substring(2);
        } else if (ref.startsWith("../")) {
            // Remonter d'un niveau dans le basePath
            String[] basePathParts = basePath.split("/");
            StringBuilder newBasePath = new StringBuilder();
            
            int levelsUp = 0;
            String tempRef = ref;
            while (tempRef.startsWith("../")) {
                levelsUp++;
                tempRef = tempRef.substring(3);
            }
            
            // Reconstruire le chemin de base en remontant
            for (int i = 0; i < basePathParts.length - levelsUp - 1; i++) {
                if (!basePathParts[i].isEmpty()) {
                    newBasePath.append(basePathParts[i]).append("/");
                }
            }
            
            ref = tempRef;
            basePath = newBasePath.toString();
        }
        
        // Combiner le chemin de base avec la référence
        if (basePath.endsWith("/")) {
            return basePath + ref;
        } else {
            return basePath.isEmpty() ? ref : basePath + "/" + ref;
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
