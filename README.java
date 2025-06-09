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
                
                // Ne traiter que les références locales (pas HTTP ni internes pures)
                if (isLocalFileReference(ref)) {
                    // Éviter les références circulaires
                    if (processedRefs.contains(ref)) {
                        throw new RuntimeException("Référence circulaire détectée: " + ref);
                    }
                    
                    processedRefs.add(ref);
                    
                    // Résoudre la référence
                    String filePath = ref.contains("#") ? ref.substring(0, ref.indexOf("#")) : ref;
                    String fragment = ref.contains("#") ? ref.substring(ref.indexOf("#") + 1) : null;
                    
                    String resolvedPath = resolvePath(basePath, filePath);
                    JsonNode referencedSchema = loadSchema(resolvedPath);
                    
                    // Résoudre le fragment JSON Pointer si présent
                    if (fragment != null && !fragment.isEmpty()) {
                        referencedSchema = resolveJsonPointer(referencedSchema, fragment);
                    }
                    
                    // Récursivement aplatir le schéma référencé
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

    private JsonNode resolveJsonPointer(JsonNode root, String pointer) {
        if (pointer == null || pointer.isEmpty()) {
            return root;
        }
        
        // Enlever le premier '/' si présent
        if (pointer.startsWith("/")) {
            pointer = pointer.substring(1);
        }
        
        JsonNode current = root;
        String[] parts = pointer.split("/");
        
        for (String part : parts) {
            if (current == null) {
                throw new RuntimeException("Fragment JSON Pointer non trouvé: " + pointer);
            }
            
            // Décoder les caractères échappés dans JSON Pointer
            part = part.replace("~1", "/").replace("~0", "~");
            
            if (current.isObject()) {
                current = current.get(part);
            } else if (current.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Index de tableau invalide dans JSON Pointer: " + part);
                }
            } else {
                throw new RuntimeException("Impossible de naviguer dans le JSON Pointer: " + pointer);
            }
        }
        
        if (current == null) {
            throw new RuntimeException("Fragment JSON Pointer non trouvé: " + pointer);
        }
        
        return current;
    }

    private boolean isLocalFileReference(String ref) {
        return ref != null && 
               !ref.startsWith("http://") && 
               !ref.startsWith("https://") && 
               !ref.startsWith("#") &&
               (ref.contains(".json") || ref.contains(".schema"));
    }

    private String resolvePath(String basePath, String ref) {
        // Extraire la partie fichier si il y a un fragment JSON Pointer
        String filePath = ref;
        if (ref.contains("#")) {
            filePath = ref.substring(0, ref.indexOf("#"));
        }
        
        // Normaliser le chemin de référence
        if (filePath.startsWith("./")) {
            filePath = filePath.substring(2);
        }
        
        // Gérer les chemins qui remontent avec ../
        String resolvedPath = basePath;
        while (filePath.startsWith("../")) {
            filePath = filePath.substring(3);
            // Remonter d'un niveau dans le chemin de base
            if (resolvedPath.endsWith("/")) {
                resolvedPath = resolvedPath.substring(0, resolvedPath.length() - 1);
            }
            int lastSlash = resolvedPath.lastIndexOf('/');
            if (lastSlash > 0) {
                resolvedPath = resolvedPath.substring(0, lastSlash + 1);
            } else {
                resolvedPath = "";
            }
        }
        
        // Combiner le chemin résolu avec la référence
        if (resolvedPath.isEmpty()) {
            return "schemas/" + filePath;
        } else if (resolvedPath.endsWith("/")) {
            return resolvedPath + filePath;
        } else {
            return resolvedPath + "/" + filePath;
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
