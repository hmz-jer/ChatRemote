 import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingUtils {
    private static boolean printOutEvents = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtils.class);
    
    public static void setPrintEvents(boolean value) {
        printOutEvents = value;
    }
    
    public static void loggingEvent(RSLogger logger, LogEvent event, String... args) {
        if (logger != null) {
            // Sanitize tous les arguments
            String[] sanitizedArgs = sanitizeArgs(args);
            
            // Log avec le logger spécifique
            logger.log(event, sanitizedArgs);
            
            // Log additionnel si printOutEvents est activé
            if (printOutEvents && event != null) {
                String sanitizedEventLabel = sanitizeString(event.getLabel());
                String sanitizedArgsStr = formatArgs(sanitizedArgs);
                LOGGER.info("{}: {}", sanitizedEventLabel, sanitizedArgsStr);
            }
        }
    }
    
    public static void loggingEvent(RSLogger logger, LogEvent event, 
            String args1, String args2) {
        if (logger != null) {
            // Sanitize les arguments individuels
            String sanitizedArgs1 = sanitizeString(args1);
            String sanitizedArgs2 = sanitizeString(args2);
            
            // Log avec le logger spécifique
            logger.log(event, sanitizedArgs1, sanitizedArgs2);
            
            // Log additionnel si printOutEvents est activé
            if (printOutEvents && event != null) {
                String sanitizedEventLabel = sanitizeString(event.getLabel());
                LOGGER.info("{}: {} {}", 
                    sanitizedEventLabel, 
                    sanitizedArgs1,
                    sanitizedArgs2);
            }
        }
    }
    
    private static String[] sanitizeArgs(String... args) {
        if (args == null) {
            return new String[0];
        }
        
        return java.util.Arrays.stream(args)
            .map(LoggingUtils::sanitizeString)
            .toArray(String[]::new);
    }
    
    private static String sanitizeString(String input) {
        if (input == null) {
            return "null";
        }
        
        // Supprimer les caractères de contrôle
        String sanitized = input.replaceAll("[\\r\\n\\t]", " ");
        
        // Échapper les caractères spéciaux
        sanitized = StringEscapeUtils.escapeJava(sanitized);
        
        // Limiter la longueur
        return truncateIfNeeded(sanitized);
    }
    
    private static String formatArgs(String[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return "[" + String.join(", ", args) + "]";
    }
    
    private static String truncateIfNeeded(String input) {
        final int MAX_LENGTH = 1000;
        if (input != null && input.length() > MAX_LENGTH) {
            return input.substring(0, MAX_LENGTH) + "...";
        }
        return input;
    }
}
