    import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Arrays;

public class URLValidator {
    // Liste des protocoles autorisés
    private static final List<String> ALLOWED_PROTOCOLS = Arrays.asList(
        "http", "https", "ftp", "ldap"
    );
    
    // Pattern pour valider le format de l'URL incluant tous les protocoles autorisés
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp|ldap)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"
    );
    
    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL);
            return false;
        }

        try {
            // Création de l'URL pour validation
            URL urlObj = new URL(url);
            
            // Vérification du protocole
            String protocol = urlObj.getProtocol().toLowerCase();
            if (!ALLOWED_PROTOCOLS.contains(protocol)) {
                LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                    LoggerConstants.CRL_MALFORMED_URL);
                return false;
            }

            // Vérification du format global
            if (!URL_PATTERN.matcher(url).matches()) {
                LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                    LoggerConstants.CRL_MALFORMED_URL);
                return false;
            }

            return true;

        } catch (MalformedURLException e) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL);
            return false;
        }
    }
}
