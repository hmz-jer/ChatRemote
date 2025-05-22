 # Correction de la méthode getBankResponseByUrl

Le problème dans votre méthode `getBankResponseByUrl` est que la méthode `contains()` trouve une correspondance partielle même quand ce n'est pas exact. Par exemple, "12345" contient "1234", donc les deux retournent la même réponse.

Voici la correction de la méthode:

```java
public Map<String, Object> getBankResponseByUrl(String url) {
    Map<String, Object> providers = getProvidersSection();
    
    if (url == null) {
        return null;
    }
    
    // Chercher une correspondance exacte avec les patterns d'URL
    for (Map.Entry<String, Object> entry : providers.entrySet()) {
        String providerPattern = entry.getKey();
        
        // Vérifier si l'URL correspond exactement au pattern
        if (matchesProviderPattern(url, providerPattern)) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) entry.getValue();
                logger.debug("Réponse trouvée pour le pattern: {} dans l'URL: {}", providerPattern, url);
                return response;
            }
        }
    }
    
    return null;
}

/**
 * Vérifie si une URL correspond à un pattern de provider de manière exacte
 */
private boolean matchesProviderPattern(String url, String providerPattern) {
    // Cas 1: Pattern exact dans l'URL (par exemple "/api/provider/1234/")
    if (url.contains("/provider/" + providerPattern + "/") || 
        url.endsWith("/provider/" + providerPattern)) {
        return true;
    }
    
    // Cas 2: Pattern comme segment complet dans l'URL
    String[] urlSegments = url.split("/");
    for (String segment : urlSegments) {
        if (segment.equals(providerPattern)) {
            return true;
        }
    }
    
    // Cas 3: Pattern avec délimiteurs pour éviter les correspondances partielles
    // Par exemple, chercher "1234" mais pas dans "12345"
    String regex = "\\b" + Pattern.quote(providerPattern) + "\\b";
    Pattern pattern = Pattern.compile(regex);
    return pattern.matcher(url).find();
}
```

## Alternative plus robuste avec support de patterns regex

Si vous voulez une solution encore plus flexible qui supporte les expressions régulières:

```java
public Map<String, Object> getBankResponseByUrl(String url) {
    Map<String, Object> providers = getProvidersSection();
    
    if (url == null) {
        return null;
    }
    
    logger.debug("Recherche de correspondance pour l'URL: {}", url);
    
    // Trier les patterns par longueur décroissante pour prioriser les correspondances les plus spécifiques
    List<Map.Entry<String, Object>> sortedProviders = providers.entrySet().stream()
            .sorted((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()))
            .collect(Collectors.toList());
    
    for (Map.Entry<String, Object> entry : sortedProviders) {
        String providerPattern = entry.getKey();
        
        if (matchesProviderPattern(url, providerPattern)) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> response = (Map<String, Object>) entry.getValue();
                logger.debug("Réponse trouvée pour le pattern: {} dans l'URL: {}", providerPattern, url);
                return response;
            }
        }
    }
    
    logger.debug("Aucune correspondance trouvée pour l'URL: {}", url);
    return null;
}

/**
 * Vérifie si une URL correspond à un pattern de provider
 * Support plusieurs types de correspondances:
 * 1. Correspondance exacte dans le chemin (/api/provider/1234/)  
 * 2. Pattern regex si le pattern commence par "regex:"
 * 3. Correspondance de segment complet
 */
private boolean matchesProviderPattern(String url, String providerPattern) {
    try {
        // Cas 1: Pattern regex explicite
        if (providerPattern.startsWith("regex:")) {
            String regex = providerPattern.substring(6); // Enlever "regex:"
            Pattern pattern = Pattern.compile(regex);
            boolean matches = pattern.matcher(url).find();
            logger.debug("Test regex '{}' sur '{}': {}", regex, url, matches);
            return matches;
        }
        
        // Cas 2: Pattern exact dans le chemin provider
        String exactPathPattern1 = "/provider/" + providerPattern + "/";
        String exactPathPattern2 = "/provider/" + providerPattern;
        
        if (url.contains(exactPathPattern1) || url.endsWith(exactPathPattern2)) {
            logger.debug("Correspondance exacte trouvée pour le pattern: {}", providerPattern);
            return true;
        }
        
        // Cas 3: Correspondance de segment complet avec délimiteurs
        String regexPattern = "(?:^|/)provider/" + Pattern.quote(providerPattern) + "(?:/|$)";
        Pattern pattern = Pattern.compile(regexPattern);
        boolean matches = pattern.matcher(url).find();
        logger.debug("Test de segment complet '{}' sur '{}': {}", regexPattern, url, matches);
        return matches;
        
    } catch (Exception e) {
        logger.error("Erreur lors de la vérification du pattern '{}' pour l'URL '{}': {}", 
                    providerPattern, url, e.getMessage());
        return false;
    }
}
```

## Configuration YAML mise à jour

Voici comment vous pouvez configurer votre fichier `bank-responses.yml` pour éviter les conflits:

```yaml
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

  # Réponses par patterns d'URL - ORDRE IMPORTANT: du plus spécifique au moins spécifique
  providers:
    # Pattern exact pour éviter les conflits (12345 ne correspond pas à 1234)
    "12345":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success", 
          "providerId": "12345",
          "message": "Réponse spécifique pour le provider 12345",
          "timestamp": "${timestamp}"
        }
    
    "1234":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "providerId": "1234", 
          "message": "Réponse spécifique pour le provider 1234",
          "timestamp": "${timestamp}"
        }
    
    # Utilisation de regex pour des patterns plus complexes
    "regex:provider/\\d{5}":
      status: 200
      headers:
        Content-Type: application/json
      body: |
        {
          "status": "success",
          "message": "Provider avec 5 chiffres détecté",
          "timestamp": "${timestamp}"
        }
        
    # Pattern pour Natixis
    "natixis":
      status: 200
      headers:
        Content-Type: application/json
        X-Bank: "Natixis"
      body: |
        {
          "status": "success",
          "bank": "Natixis",
          "message": "Connexion établie avec Natixis",
          "timestamp": "${timestamp}"
        }
```

## Test de la correction

Voici comment tester que la correction fonctionne:

```java
// Test unitaire pour vérifier la correction
@Test
public void testProviderPatternMatching() {
    BankResponsesConfig config = new BankResponsesConfig();
    
    // Test avec URL contenant 1234
    String url1 = "/api/provider/1234/status";
    Map<String, Object> response1 = config.getBankResponseByUrl(url1);
    // Doit retourner la réponse pour 1234
    
    // Test avec URL contenant 12345
    String url2 = "/api/provider/12345/status";
    Map<String, Object> response2 = config.getBankResponseByUrl(url2);
    // Doit retourner la réponse pour 12345, PAS celle de 1234
    
    // Les réponses doivent être différentes
    assertNotEquals(response1, response2);
}
```

## Avantages de la correction

1. **Correspondance exacte**: "1234" ne correspond plus à "12345"
2. **Priorisation**: Les patterns les plus longs sont testés en premier
3. **Flexibilité**: Support des expressions régulières pour des patterns complexes
4. **Logging amélioré**: Meilleur débogage des correspondances
5. **Robustesse**: Gestion des erreurs et validation des patterns

Cette correction résoudra le problème où "/api/provider/12345/" retournait la même réponse que "/api/provider/1234/".
