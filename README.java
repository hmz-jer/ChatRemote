    import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class SystemPropertiesManager {
    private static final Logger LOGGER = Logger.getLogger(SystemPropertiesManager.class.getName());
    
    // Constantes pour les noms de propriétés
    private static final String PROXY_HOST_PROPERTY = "http.proxyHost";
    private static final String PROXY_PORT_PROPERTY = "http.proxyPort";
    private static final String SYSTEM_PROXY_PROPERTY = "java.net.useSystemProxies";
    
    // Patterns de validation
    private static final Pattern HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+$");
    private static final Pattern PORT_PATTERN = Pattern.compile("^\\d{1,5}$");
    
    public static void setProxyProperties(InetSocketAddress addr) {
        if (addr == null) {
            LOGGER.warning("Tentative de définition de propriétés proxy avec une adresse null");
            return;
        }

        String host = addr.getHostName();
        String port = Integer.toString(addr.getPort());

        if (isValidHostName(host) && isValidPort(port)) {
            setValidatedProperty(PROXY_HOST_PROPERTY, host);
            setValidatedProperty(PROXY_PORT_PROPERTY, port);
        } else {
            LOGGER.severe("Tentative de définition de propriétés proxy avec des valeurs invalides");
            throw new SecurityException("Valeurs de proxy invalides");
        }
    }
    
    public static void setSystemProxyEnabled(boolean enabled) {
        setValidatedProperty(SYSTEM_PROXY_PROPERTY, Boolean.toString(enabled));
    }
    
    private static void setValidatedProperty(String key, String value) {
        // Vérification des clés autorisées
        if (!isAllowedPropertyKey(key)) {
            LOGGER.severe("Tentative de modification d'une propriété système non autorisée: " + key);
            throw new SecurityException("Propriété système non autorisée");
        }
        
        // Validation supplémentaire selon le type de propriété
        if (PROXY_HOST_PROPERTY.equals(key)) {
            if (!isValidHostName(value)) {
                throw new SecurityException("Nom d'hôte proxy invalide");
            }
        } else if (PROXY_PORT_PROPERTY.equals(key)) {
            if (!isValidPort(value)) {
                throw new SecurityException("Port proxy invalide");
            }
        }
        
        // Application de la propriété après validation
        try {
            System.setProperty(key, value);
        } catch (SecurityException e) {
            LOGGER.severe("Erreur lors de la définition de la propriété système: " + e.getMessage());
            throw e;
        }
    }
    
    private static boolean isAllowedPropertyKey(String key) {
        return PROXY_HOST_PROPERTY.equals(key) ||
               PROXY_PORT_PROPERTY.equals(key) ||
               SYSTEM_PROXY_PROPERTY.equals(key);
    }
    
    private static boolean isValidHostName(String host) {
        return host != null && HOST_PATTERN.matcher(host).matches();
    }
    
    private static boolean isValidPort(String port) {
        if (port != null && PORT_PATTERN.matcher(port).matches()) {
            int portNum = Integer.parseInt(port);
            return portNum > 0 && portNum <= 65535;
        }
        return false;
    }
}
