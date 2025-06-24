package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
public class BankResponsesConfig {

    private static final Logger logger = LoggerFactory.getLogger(BankResponsesConfig.class);

    @Value("${mock-vop.responses.config-file:file:/opt/mock-client-vop/conf/bank-responses.yml}")
    private String configFilePath;

    @Value("${mock-vop.responses.auto-reload-enabled:true}")
    private boolean autoReloadEnabled;

    @Value("${mock-vop.responses.auto-reload-interval-ms:30000}")
    private long autoReloadIntervalMs;

    private Map<String, Object> responsesConfig = new HashMap<>();
    private FileTime lastModifiedTime = null;
    private String actualFilePath;

    @PostConstruct
    public void init() {
        // Déterminer le chemin réel du fichier
        actualFilePath = configFilePath.replace("file:", "");
        
        logger.info("Configuration des réponses bancaires:");
        logger.info("- Fichier: {}", actualFilePath);
        logger.info("- Rechargement automatique: {}", autoReloadEnabled);
        logger.info("- Intervalle de vérification: {}ms", autoReloadIntervalMs);
        
        // Chargement initial
        loadResponses();
    }

    /**
     * Vérifie et recharge le fichier de configuration si modifié
     */
    @Scheduled(fixedDelayString = "${mock-vop.responses.auto-reload-interval-ms:30000}")
    public void checkAndReloadIfModified() {
        if (!autoReloadEnabled) {
            return;
        }

        try {
            Path configPath = Paths.get(actualFilePath);
            
            if (!Files.exists(configPath)) {
                logger.warn("Fichier de configuration introuvable: {}", actualFilePath);
                return;
            }

            FileTime currentModifiedTime = Files.getLastModifiedTime(configPath);
            
            // Vérifier si le fichier a été modifié
            if (lastModifiedTime == null || currentModifiedTime.compareTo(lastModifiedTime) > 0) {
                logger.info("Modification détectée du fichier de configuration, rechargement...");
                loadResponses();
            }
            
        } catch (IOException e) {
            logger.error("Erreur lors de la vérification de modification du fichier", e);
        }
    }

    /**
     * Force le rechargement du fichier de configuration
     */
    public void forceReload() {
        logger.info("Rechargement forcé du fichier de configuration...");
        loadResponses();
    }

    /**
     * Charge ou recharge le fichier de configuration
     */
    public void loadResponses() {
        try {
            Path configPath = Paths.get(actualFilePath);
            
            if (!Files.exists(configPath)) {
                logger.warn("Fichier de configuration introuvable: {}", actualFilePath);
                return;
            }

            // Lire le fichier
            try (InputStream inputStream = Files.newInputStream(configPath)) {
                Yaml yaml = new Yaml();
                Object loaded = yaml.load(inputStream);
                
                if (loaded instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> config = (Map<String, Object>) loaded;
                    responsesConfig = config;
                    
                    // Mettre à jour le timestamp de dernière modification
                    lastModifiedTime = Files.getLastModifiedTime(configPath);
                    
                    logger.info("Configuration rechargée avec succès depuis: {}", actualFilePath);
                    logConfigurationSummary();
                } else {
                    logger.error("Le fichier ne contient pas une structure YAML valide");
                }
            }
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement du fichier de configuration: {}", actualFilePath, e);
        }
    }

    /**
     * Affiche un résumé de la configuration chargée
     */
    private void logConfigurationSummary() {
        Map<String, Object> responses = getResponsesSection();
        Map<String, Object> providers = getProvidersSection();
        
        logger.info("Configuration chargée:");
        logger.info("- Réponse par défaut: {}", responses.containsKey("default") ? "configurée" : "non configurée");
        logger.info("- Nombre de providers: {}", providers.size());
        logger.info("- Providers configurés: {}", providers.keySet());
    }

    // ... autres méthodes existantes (getResponsesSection, getProvidersSection, etc.)

    @Bean
    public Map<String, Object> bankResponsesConfiguration() {
        return responsesConfig;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getResponsesSection() {
        Object responses = responsesConfig.getOrDefault("responses", new HashMap<>());
        if (responses instanceof Map) {
            return (Map<String, Object>) responses;
        }
        return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getProvidersSection() {
        Map<String, Object> responses = getResponsesSection();
        Object providers = responses.getOrDefault("providers", new HashMap<>());
        if (providers instanceof Map) {
            return (Map<String, Object>) providers;
        }
        return new HashMap<>();
    }

    // ... autres méthodes existantes
}
