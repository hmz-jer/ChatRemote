package com.example.jsonschemaflattener.service;

import com.example.jsonschemaflattener.util.JsonSchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SchemaFlattenerService.class);

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
        logger.info("=== Début du processus de flattening ===");
        logger.info("Fichier d'entrée: {}", inputSchemaFile);
        logger.info("Fichier de sortie: {}", outputFileName);
        logger.info("Chemin de base: {}", inputBasePath);
        logger.info("Dossier de sortie: {}", outputBasePath);
        
        // 1. Charger le schéma principal
        String inputPath = inputBasePath + "/" + inputSchemaFile;
        logger.info("Chargement du schéma principal depuis: {}", inputPath);
        JsonNode schema = loadSchema(inputPath);
        logger.info("Schéma principal chargé avec succès. Taille: {} caractères", schema.toString().length());
        
        // 2. Résoudre toutes les références $ref (avec le schéma racine pour les références internes)
        logger.info("Début de la résolution des références $ref...");
        JsonNode flattenedSchema = flattenSchema(schema, inputBasePath + "/", new HashSet<>(), schema);
        logger.info("Résolution des références terminée. Taille finale: {} caractères", flattenedSchema.toString().length());
        
        // 3. Créer le dossier output s'il n'existe pas
        Path outputDir = Paths.get(outputBasePath);
        if (!Files.exists(outputDir)) {
            logger.info("Création du dossier de sortie: {}", outputDir.toAbsolutePath());
            Files.createDirectories(outputDir);
        } else {
            logger.debug("Le dossier de sortie existe déjà: {}", outputDir.toAbsolutePath());
        }
        
        // 4. Écrire le schéma aplati
        Path outputFile = outputDir.resolve(outputFileName);
        logger.info("Écriture du schéma aplati vers: {}", outputFile.toAbsolutePath());
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(outputFile.toFile(), flattenedSchema);
        logger.info("Schéma écrit avec succès. Taille du fichier: {} octets", Files.size(outputFile));
        
        // 5. Valider le schéma généré
        logger.info("Début de la validation du schéma...");
        validator.validateSchema(flattenedSchema);
        logger.info("Validation terminée avec succès!");
        
        logger.info("=== Processus terminé avec succès ===");
        System.out.println("Schéma aplati généré: " + outputFile.toAbsolutePath());
    }

    private JsonNode loadSchema(String path) throws IOException {
        logger.debug("Tentative de chargement du fichier: {}", path);
        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                logger.error("Le fichier n'existe pas dans le classpath: {}", path);
                throw new IOException("Fichier non trouvé: " + path);
            }
            
            JsonNode result = objectMapper.readTree(resource.getInputStream());
            logger.debug("Fichier chargé avec succès: {} (taille: {} caractères)", path, result.toString().length());
            return result;
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du fichier: {} - {}", path, e.getMessage());
            throw e;
        }
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
                logger.debug("Référence $ref trouvée: {}", ref);
                
                // Nettoyer la référence des caractères indésirables
                ref = cleanString(ref);
                logger.debug("Référence après nettoyage: {}", ref);
                
                // Ne traiter que les références locales
                if (isLocalFileReference(ref)) {
                    // Éviter les références circulaires
                    if (processedRefs.contains(ref)) {
                        logger.error("Référence circulaire détectée: {}", ref);
                        throw new RuntimeException("Référence circulaire détectée: " + ref);
                    }
                    
                    logger.info("Résolution de la référence: {}", ref);
                    processedRefs.add(ref);
                    JsonNode referencedSchema;
                    JsonNode recursiveRootSchema = rootSchema;
                    String newBasePath = basePath;
                    
                    // Gérer les références internes (#/$defs/...)
                    if (ref.startsWith("#/$defs/")) {
                        logger.debug("Résolution d'une référence interne: {}", ref);
                        if (rootSchema == null) {
                            logger.error("Impossible de résoudre une référence interne sans schéma racine: {}", ref);
                            throw new RuntimeException("Impossible de résoudre une référence interne sans schéma racine: " + ref);
                        }
                        // Résoudre dans le schéma racine
                        referencedSchema = resolveJsonPointer(rootSchema, ref.substring(1)); // enlever le #
                        logger.debug("Référence interne résolue avec succès");
                    } else {
                        // Gérer les références de fichiers externes
                        String filePath = ref.contains("#") ? ref.substring(0, ref.indexOf("#")) : ref;
                        String fragment = ref.contains("#") ? ref.substring(ref.indexOf("#") + 1) : null;
                        
                        logger.debug("Référence externe - filePath: {}, fragment: {}", filePath, fragment);
                        
                        // Nettoyer le fragment s'il existe
                        if (fragment != null) {
                            fragment = cleanString(fragment);
                            logger.debug("Fragment après nettoyage: {}", fragment);
                        }
                        
                        String resolvedPath = resolvePath(basePath, filePath);
                        logger.debug("Chemin résolu: {}", resolvedPath);
                        
                        JsonNode externalSchema = loadSchema(resolvedPath);
                        
                        // Résoudre le fragment JSON Pointer si présent
                        if (fragment != null && !fragment.isEmpty()) {
                            logger.debug("Résolution du fragment JSON Pointer: {}", fragment);
                            referencedSchema = resolveJsonPointer(externalSchema, fragment);
                        } else {
                            referencedSchema = externalSchema;
                        }
                        
                        // Pour les fichiers externes, utiliser le nouveau schéma comme racine pour ses propres références internes
                        recursiveRootSchema = externalSchema;
                        newBasePath = getBasePath(resolvedPath);
                        logger.debug("Nouveau chemin de base: {}", newBasePath);
                    }
                    
                    // Récursivement aplatir le schéma référencé
                    logger.debug("Aplatissement récursif du schéma référencé...");
                    JsonNode flattenedRef = flattenSchema(referencedSchema, newBasePath, new HashSet<>(processedRefs), recursiveRootSchema);
                    
                    processedRefs.remove(ref);
                    logger.debug("Référence {} résolue et aplatie avec succès", ref);
                    return flattenedRef;
                } else {
                    logger.debug("Référence ignorée (non locale): {}", ref);
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
        logger.debug("Résolution JSON Pointer: '{}'", pointer);
        
        if (pointer == null || pointer.isEmpty()) {
            logger.debug("Pointeur vide, retour du nœud racine");
            return root;
        }
        
        // Nettoyer le pointeur
        pointer = cleanString(pointer);
        logger.debug("Pointeur après nettoyage: '{}'", pointer);
        
        // Enlever le premier '/' si présent
        if (pointer.startsWith("/")) {
            pointer = pointer.substring(1);
            logger.debug("Pointeur après suppression du '/': '{}'", pointer);
        }
        
        JsonNode current = root;
        String[] parts = pointer.split("/");
        logger.debug("Parties du pointeur: {}", java.util.Arrays.toString(parts));
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            logger.debug("Traitement de la partie {}: '{}'", i, part);
            
            if (current == null) {
                logger.error("Nœud null atteint lors de la résolution de la partie: '{}'", part);
                throw new RuntimeException("Fragment JSON Pointer non trouvé: " + pointer);
            }
            
            // Nettoyer chaque partie et décoder les caractères échappés dans JSON Pointer
            part = cleanString(part).replace("~1", "/").replace("~0", "~");
            logger.debug("Partie après nettoyage et décodage: '{}'", part);
            
            if (current.isObject()) {
                logger.debug("Navigation dans un objet - recherche de la clé: '{}'", part);
                JsonNode next = current.get(part);
                if (next == null) {
                    logger.error("Clé '{}' non trouvée dans l'objet. Clés disponibles: {}", part, 
                        java.util.stream.StreamSupport.stream(current.fieldNames().spliterator(), false)
                            .collect(java.util.stream.Collectors.toList()));
                }
                current = next;
            } else if (current.isArray()) {
                logger.debug("Navigation dans un tableau - index: '{}'", part);
                try {
                    int index = Integer.parseInt(part);
                    if (index >= current.size()) {
                        logger.error("Index {} hors limites pour un tableau de taille {}", index, current.size());
                    }
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    logger.error("Index de tableau invalide: '{}'", part);
                    throw new RuntimeException("Index de tableau invalide dans JSON Pointer: " + part);
                }
            } else {
                logger.error("Type de nœud inattendu: {}. Impossible de naviguer plus loin.", current.getNodeType());
                throw new RuntimeException("Impossible de naviguer dans le JSON Pointer: " + pointer);
            }
            
            logger.debug("Navigation vers la partie {} réussie", i);
        }
        
        if (current == null) {
            logger.error("Fragment JSON Pointer non trouvé: '{}'", pointer);
            throw new RuntimeException("Fragment JSON Pointer non trouvé: " + pointer);
        }
        
        logger.debug("Résolution JSON Pointer réussie");
        return current;
    }

    private boolean isLocalFileReference(String ref) {
        boolean isLocal = ref != null && 
               !ref.startsWith("http://") && 
               !ref.startsWith("https://") && 
               (ref.contains(".json") || ref.contains(".schema") || ref.startsWith("#/$defs/"));
        
        logger.debug("Vérification référence locale pour '{}': {}", ref, isLocal);
        return isLocal;
    }

    private String resolvePath(String basePath, String ref) {
        logger.debug("Résolution du chemin - basePath: '{}', ref: '{}'", basePath, ref);
        
        // Extraire la partie fichier si il y a un fragment JSON Pointer
        String filePath = ref;
        if (ref.contains("#")) {
            filePath = ref.substring(0, ref.indexOf("#"));
        }
        
        // Nettoyer les caractères indésirables
        filePath = cleanString(filePath);
        logger.debug("FilePath après nettoyage: '{}'", filePath);
        
        // Normaliser le chemin de référence
        if (filePath.startsWith("./")) {
            filePath = filePath.substring(2);
            logger.debug("FilePath après suppression de './': '{}'", filePath);
        }
        
        // Gérer les chemins qui remontent avec ../
        String resolvedPath = cleanString(basePath);
        logger.debug("ResolvedPath initial: '{}'", resolvedPath);
        
        while (filePath.startsWith("../")) {
            filePath = filePath.substring(3);
            logger.debug("FilePath après suppression de '../': '{}'", filePath);
            
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
            logger.debug("ResolvedPath après remontée: '{}'", resolvedPath);
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
        finalPath = cleanString(finalPath);
        logger.debug("Chemin final résolu: '{}'", finalPath);
        return finalPath;
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
        String original = input;
        // Supprimer les caractères de contrôle indésirables
        String cleaned = input.trim().replaceAll("[\r\n\t\u0000-\u001F\u007F]", "");
        
        if (!original.equals(cleaned)) {
            logger.debug("Nettoyage effectué - original: '{}' -> nettoyé: '{}'", original, cleaned);
        }
        
        return cleaned;
    }
}
