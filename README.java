 Ah d'accord ! Dans la méthode handleRequest, vous n'avez que le paramètre HttpServletRequest. Voici comment récupérer le body directement depuis la request :/**
 * Log les informations détaillées de la requête
 */
private void logRequestInfo(HttpServletRequest request) {
    logger.info("=== DÉBUT DE LA REQUÊTE ===");
    logger.info("Méthode: {}", request.getMethod());
    logger.info("URI: {}", request.getRequestURI());
    logger.info("URL complète: {}", request.getRequestURL());
    logger.info("Query String: {}", request.getQueryString());
    logger.info("Remote Address: {}", request.getRemoteAddr());
    logger.info("User Agent: {}", request.getHeader("User-Agent"));
    
    // Log des headers
    logger.info("=== HEADERS ===");
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String headerName = headerNames.nextElement();
        String headerValue = request.getHeader(headerName);
        // Masquer les headers sensibles
        if (headerName.toLowerCase().contains("authorization") || 
            headerName.toLowerCase().contains("cookie")) {
            headerValue = "***MASKED***";
        }
        logger.info("{}: {}", headerName, headerValue);
    }
    
    // Log des paramètres
    if (!request.getParameterMap().isEmpty()) {
        logger.info("=== PARAMÈTRES ===");
        request.getParameterMap().forEach((key, values) -> {
            logger.info("{}: {}", key, String.join(", ", values));
        });
    }
    
    // Log du body pour les requêtes POST/PUT/PATCH
    if ("POST".equalsIgnoreCase(request.getMethod()) || 
        "PUT".equalsIgnoreCase(request.getMethod()) || 
        "PATCH".equalsIgnoreCase(request.getMethod())) {
        
        String requestBody = getRequestBody(request);
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            logger.info("=== BODY DE LA REQUÊTE ===");
            // Limiter la taille du log pour éviter les logs trop volumineux
            if (requestBody.length() > 1000) {
                logger.info("Body (tronqué): {}", requestBody.substring(0, 1000) + "... [TRONQUÉ]");
                logger.info("Taille totale du body: {} caractères", requestBody.length());
            } else {
                logger.info("Body: {}", requestBody);
            }
        }
    }
    
    logger.info("=== FIN DES INFORMATIONS DE LA REQUÊTE ===");
}

/**
 * Récupère le body de la requête HTTP
 */
private String getRequestBody(HttpServletRequest request) {
    try (BufferedReader reader = request.getReader()) {
        return reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
        logger.warn("Erreur lors de la lecture du body de la requête: {}", e.getMessage());
        return null;
    }
}Et dans votre méthode handleRequest, appelez simplement :@RequestMapping("/**")
public ResponseEntity<?> handleRequest(HttpServletRequest request) {
    // Log des informations de la requête
    logRequestInfo(request);
    
    X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    
    // ... reste du code
}Les imports nécessaires :import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.stream.Collectors;Cette approche lit le body directement depuis l'InputStream de la requête HTTP, ce qui fonctionne pour toutes les méthodes HTTP qui peuvent avoir un body (POST, PUT, PATCH, etc.).
