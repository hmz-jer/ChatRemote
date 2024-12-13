    import org.apache.commons.text.StringEscapeUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureCertificateLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureCertificateLogger.class);
    
    public static void logCertificateVerification(List<String> autRevokedList, 
                                                List<String> sigRevokedList,
                                                List<String> encRevokedList) {
        try {
            // Sécurisation des données avant logging
            String sanitizedAutList = sanitizeList(autRevokedList);
            String sanitizedSigList = sanitizeList(sigRevokedList);
            String sanitizedEncList = sanitizeList(encRevokedList);

            // Logging sécurisé des listes de certificats
            LOGGER.info("Auth [{}]", sanitizedAutList);
            LOGGER.info("Sig  [{}]", sanitizedSigList);
            LOGGER.info("Enc  [{}]", sanitizedEncList);

            // Vérification du résultat
            if (autRevokedList.isEmpty() && sigRevokedList.isEmpty()) {
                logTestResult(false);
            } else {
                logTestResult(true);
            }
            
        } catch (Exception e) {
            LOGGER.error("Erreur lors du logging des certificats: {}", 
                sanitizeMessage(e.getMessage()));
        }
    }
    
    private static void logTestResult(boolean passed) {
        if (passed) {
            LOGGER.info(">>> Test PASSED!");
            LOGGER.info(">>> At least one revoked certificate found.");
        } else {
            LOGGER.info(">>> Test FAILED!");
            LOGGER.info(">>> Revoked certificate not found!");
        }
    }
    
    private static String sanitizeList(List<String> list) {
        if (list == null) {
            return "null";
        }
        
        return list.stream()
            .map(SecureCertificateLogger::sanitizeMessage)
            .collect(Collectors.joining(", "));
    }
    
    private static String sanitizeMessage(String message) {
        if (message == null) {
            return "null";
        }
        
        // Suppression des caractères de contrôle et échappement
        String sanitized = message.replaceAll("[\\r\\n\\t]", " ");
        sanitized = StringEscapeUtils.escapeJava(sanitized);
        
        // Limitation de la longueur pour éviter les logs trop longs
        return truncateIfNeeded(sanitized);
    }
    
    private static String truncateIfNeeded(String input) {
        final int MAX_LENGTH = 1000;
        if (input.length() > MAX_LENGTH) {
            return input.substring(0, MAX_LENGTH) + "...";
        }
        return input;
    }
}
