@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private BankResponsesConfig bankResponsesConfig;

    /**
     * Force le rechargement du fichier bank-responses.yml
     */
    @PostMapping("/responses/reload")
    public ResponseEntity<Map<String, Object>> reloadBankResponses() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            bankResponsesConfig.forceReload();
            
            // Obtenir un résumé de la nouvelle configuration
            Map<String, Object> providers = bankResponsesConfig.getProvidersSection();
            
            result.put("success", true);
            result.put("message", "Configuration des réponses rechargée avec succès");
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            result.put("providersCount", providers.size());
            result.put("providersConfigured", providers.keySet());
            
        } catch (Exception e) {
            logger.error("Erreur lors du rechargement de la configuration", e);
            result.put("success", false);
            result.put("message", "Erreur lors du rechargement: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Obtient le statut de la configuration actuelle
     */
    @GetMapping("/responses/status")
    public ResponseEntity<Map<String, Object>> getBankResponsesStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            Path configPath = Paths.get(bankResponsesConfig.getActualFilePath());
            
            status.put("configFile", bankResponsesConfig.getActualFilePath());
            status.put("fileExists", Files.exists(configPath));
            status.put("autoReloadEnabled", bankResponsesConfig.isAutoReloadEnabled());
            
            if (Files.exists(configPath)) {
                status.put("lastModified", Files.getLastModifiedTime(configPath).toString());
                status.put("fileSize", Files.size(configPath));
            }
            
            Map<String, Object> providers = bankResponsesConfig.getProvidersSection();
            status.put("providersCount", providers.size());
            status.put("providersConfigured", providers.keySet());
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du statut", e);
            status.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Retourne le contenu actuel de la configuration (sans mots de passe)
     */
    @GetMapping("/responses/preview")
    public ResponseEntity<Map<String, Object>> previewBankResponses() {
        Map<String, Object> preview = new HashMap<>();
        
        Map<String, Object> responses = bankResponsesConfig.getResponsesSection();
        Map<String, Object> providers = bankResponsesConfig.getProvidersSection();
        
        preview.put("defaultConfigured", responses.containsKey("default"));
        preview.put("providers", providers.keySet());
        
        // Afficher un aperçu des réponses (sans les corps complets)
        Map<String, Object> providersSummary = new HashMap<>();
        providers.forEach((key, value) -> {
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> providerConfig = (Map<String, Object>) value;
                Map<String, Object> summary = new HashMap<>();
                summary.put("status", providerConfig.get("status"));
                summary.put("hasHeaders", providerConfig.containsKey("headers"));
                summary.put("hasBody", providerConfig.containsKey("body"));
                providersSummary.put(key, summary);
            }
        });
        preview.put("providersSummary", providersSummary);
        
        return ResponseEntity.ok(preview);
    }
}
