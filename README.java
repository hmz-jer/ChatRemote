package com.example.jsonschemaflattener.service;

import com.example.jsonschemaflattener.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${schema.input.default-file}")
    private String defaultInputFile;

    @Value("${schema.input.base-path}")
    private String inputBasePath;

    @Value("${schema.output.default-file}")
    private String defaultOutputFile;

    @Value("${schema.output.base-path}")
    private String outputBasePath;

    public void flattenAndValidateSchema() throws Exception {
        flattenAndValidateSchema(defaultInputFile, defaultOutputFile);
    }

    public void flattenAndValidateSchema(String inputSchemaFile, String outputFileName) throws Exception {
        // 1. Charger le schéma principal
        String inputPath = inputBasePath + "/" + inputSchemaFile;
        JsonNode schema = loadSchema(inputPath);
        
        // 2. Résoudre toutes les références $ref (avec le schéma racine pour les références internes)
        JsonNode flattenedSchema = flattenSchema(schema, inputBasePath + "/", new HashSet<>(), schema);
        
        // 3. Créer le dossier output s'il n'existe pas
        Path outputDir = Paths.get(outputBasePath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        // 4. Écrire le schéma aplati
        Path outputFile = outputDir.resolve(outputFileName);
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), flattenedSchema);
        
        // 5. Valider le schéma généré
        validator.validateSchema(flattenedSchema);
        
        System.out.println("Schéma aplati généré: " + outputFile.toAbsolutePath());
    }

    private JsonNode loadSchema(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return objectMapper.readTree(resource.getInputStream());
    }



    private JsonNode flattenSchema(JsonNode node, String basePath, Set<String> processedRefs) throws IOException {
        return flattenSchema(node, basePath, processedRefs, null);
    }

    private JsonNode flattenSchema(JsonNode node, String basePath, Set<String> processedRefs, JsonNode rootSchema) throws IOException {
        if (node == null) {
            return node;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            
            // Vérifier s'il y a une référence $ref
            if (objectNode.has("$ref")) {
                String ref = objectNode.get("$ref").asText();
                
                // Nettoyer la référence des caractères indésirables
                ref = cleanString(ref);
                
                // Ne traiter que les références locales
                if (isLocalFileReference(ref)) {
                    // Éviter les références circulaires
                    if (processedRefs.contains(ref)) {
                        throw new RuntimeException("Référence circulaire détectée: " + ref);
                    }
                    
                    processedRefs.add(ref);
                    JsonNode referencedSchema;
                    JsonNode recursiveRootSchema = rootSchema;
                    String newBasePath = basePath;
                    
                    // Gérer les références internes (#/$defs/...)
                    if (ref.startsWith("#/$defs/")) {
                        if (rootSchema == null) {
                            throw new RuntimeException("Impossible de résoudre une référence interne sans schéma racine: " + ref);
                        }
                        // Résoudre dans le schéma racine
                        referencedSchema = resolveJsonPointer(rootSchema, ref.substring(1)); // enlever le #
                    } else {
                        // Gérer les références de fichiers externes
                        String filePath = ref.contains("#") ? ref.substring(0, ref.indexOf("#")) : ref;
                        String fragment = ref.contains("#") ? ref.substring(ref.indexOf("#") + 1) : null;
                        
                        // Nettoyer le fragment s'il existe
                        if (fragment != null) {
                            fragment = cleanString(fragment);
                        }
                        
                        String resolvedPath = resolvePath(basePath, filePath);
                        JsonNode externalSchema = loadSchema(resolvedPath);
                        
                        // Résoudre le fragment JSON Pointer si présent
                        if (fragment != null && !fragment.isEmpty()) {
                            referencedSchema = resolveJsonPointer(externalSchema, fragment);
                        } else {
                            referencedSchema = externalSchema;
                        }
                        
                        // Pour les fichiers externes, utiliser le nouveau schéma comme racine pour ses propres références internes
                        recursiveRootSchema = externalSchema;
                        newBasePath = getBasePath(resolvedPath);
                    }
                    
                    // Récursivement aplatir le schéma référencé
                    JsonNode flattenedRef = flattenSchema(referencedSchema, newBasePath, new HashSet<>(processedRefs), recursiveRootSchema);
                    
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
                result.set(fieldName, flattenSchema(fieldValue, basePath, processedRefs, rootSchema));
            }
            return result;
            
        } else if (node.isArray()) {
            // Traiter récursivement tous les éléments du tableau
            for (int i = 0; i < node.size(); i++) {
                JsonNode arrayElement = node.get(i);
                JsonNode flattenedElement = flattenSchema(arrayElement, basePath, processedRefs, rootSchema);
                ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, flattenedElement);
            }
        }
        
        return node;
    }

    private JsonNode resolveJsonPointer(JsonNode root, String pointer) {
        if (pointer == null || pointer.isEmpty()) {
            return root;
        }
        
        // Nettoyer le pointeur
        pointer = cleanString(pointer);
        
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
            
            // Nettoyer chaque partie et décoder les caractères échappés dans JSON Pointer
            part = cleanString(part).replace("~1", "/").replace("~0", "~");
            
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
               (ref.contains(".json") || ref.contains(".schema") || ref.startsWith("#/$defs/"));
    }

    private String resolvePath(String basePath, String ref) {
        // Extraire la partie fichier si il y a un fragment JSON Pointer
        String filePath = ref;
        if (ref.contains("#")) {
            filePath = ref.substring(0, ref.indexOf("#"));
        }
        
        // Nettoyer les caractères indésirables
        filePath = cleanString(filePath);
        
        // Normaliser le chemin de référence
        if (filePath.startsWith("./")) {
            filePath = filePath.substring(2);
        }
        
        // Gérer les chemins qui remontent avec ../
        String resolvedPath = cleanString(basePath);
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
        String finalPath;
        if (resolvedPath.isEmpty()) {
            finalPath = inputBasePath + "/" + filePath;
        } else if (resolvedPath.endsWith("/")) {
            finalPath = resolvedPath + filePath;
        } else {
            finalPath = resolvedPath + "/" + filePath;
        }
        
        // Nettoyer le chemin final
        return cleanString(finalPath);
    }

    private String getBasePath(String fullPath) {
        // Nettoyer le chemin complet
        fullPath = cleanString(fullPath);
        
        int lastSlashIndex = fullPath.lastIndexOf('/');
        if (lastSlashIndex > 0) {
            return fullPath.substring(0, lastSlashIndex + 1);
        }
        return "";
    }

    private String cleanString(String input) {
        if (input == null) {
            return "";
        }
        // Supprimer les caractères de contrôle indésirables
        return input.trim().replaceAll("[\r\n\t\u0000-\u001F\u007F]", "");
    }
}
