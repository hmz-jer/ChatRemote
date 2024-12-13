    import java.net.URL;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Arrays;

public class URLValidator {
    // Liste blanche des protocoles autorisés
    private static final List<String> ALLOWED_PROTOCOLS = Arrays.asList("http", "https");
    
    // Pattern pour valider le format de l'URL
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL, "URL is null or empty");
            return false;
        }

        try {
            // Validation basique du format avec regex
            if (!URL_PATTERN.matcher(url).matches()) {
                LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                    LoggerConstants.CRL_MALFORMED_URL, "URL format invalid");
                return false;
            }

            // Création de l'URL et validations supplémentaires
            URL urlObj = new URL(url);
            
            // Vérification du protocole
            if (!ALLOWED_PROTOCOLS.contains(urlObj.getProtocol().toLowerCase())) {
                LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                    LoggerConstants.CRL_MALFORMED_URL, "Protocol not allowed");
                return false;
            }

            // Vérification du host
            if (urlObj.getHost() == null || urlObj.getHost().isEmpty()) {
                LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                    LoggerConstants.CRL_MALFORMED_URL, "Invalid host");
                return false;
            }

            return true;

        } catch (MalformedURLException e) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL, e.getMessage());
            return false;
        }
    }
}
    
