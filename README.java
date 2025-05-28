 public Map<String, Object> getBankResponseByUrl(String url) {
    if (url == null) {
        return Collections.emptyMap();
    }
    
    Map<String, Object> providers = getProvidersSection();
    
    // Utilisation des Streams Java 8 pour une approche plus fonctionnelle
    return providers.entrySet().stream()
        .filter(entry -> matchesProviderPattern(url, entry.getKey()))
        .findFirst()
        .map(entry -> {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) entry.getValue();
                logger.debug("Réponse trouvée pour le pattern: {} dans l'URL: {}", 
                           entry.getKey(), url);
                return response;
            }
            return Collections.<String, Object>emptyMap();
        })
        .orElse(null);
}
