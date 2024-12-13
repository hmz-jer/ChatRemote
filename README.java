    import java.net.URL;
import javax.naming.ldap.LdapName;
import javax.naming.InvalidNameException;
import java.util.regex.Pattern;

public class LDAPURLValidator {
    // Pattern pour URL LDAP valide
    private static final Pattern LDAP_URL_PATTERN = Pattern.compile(
        "^ldap://[a-zA-Z0-9.-]+(?::\\d{1,5})?/(?:[a-zA-Z0-9-]+=[^,]*,)*[a-zA-Z0-9-]+=[^,]*$"
    );
    
    // Liste des attributs autorisés dans le DN
    private static final List<String> ALLOWED_DN_ATTRIBUTES = Arrays.asList(
        "cn", "dc", "ou", "o", "c"
    );
    
    public static X509CRL downloadCRL(String crlURL) throws MalformedURLException, 
            CertificateException, CRLException {
            
        if (!isValidLDAPUrl(crlURL)) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL);
            throw new MalformedURLException("Invalid LDAP URL format");
        }
        
        try {
            // Extraction du DN depuis l'URL LDAP
            String dn = extractDNFromURL(crlURL);
            if (!isValidDN(dn)) {
                throw new MalformedURLException("Invalid DN in LDAP URL");
            }
            
            // Téléchargement du CRL via LDAP
            if (crlURL.startsWith("ldap://")) {
                return downloadCRLFromLDAP(crlURL);
            }
            
            return null;
            
        } catch (InvalidNameException e) {
            LoggerTools.loggingEvent(RSLOG, LoggerConstants.EVENT_CRL_MALFORMED_URL, 
                LoggerConstants.CRL_MALFORMED_URL);
            throw new MalformedURLException("Invalid LDAP name: " + e.getMessage());
        }
    }
    
    private static boolean isValidLDAPUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // Vérification du format de base avec le pattern
        return LDAP_URL_PATTERN.matcher(url).matches();
    }
    
    private static String extractDNFromURL(String url) {
        // Extraire la partie après ldap://host:port/
        int dnStart = url.indexOf("/", 7);
        return dnStart != -1 ? url.substring(dnStart + 1) : "";
    }
    
    private static boolean isValidDN(String dn) throws InvalidNameException {
        if (dn == null || dn.isEmpty()) {
            return false;
        }
        
        // Validation du DN avec LdapName
        LdapName ldapName = new LdapName(dn);
        
        // Vérification de chaque RDN
        return ldapName.getRdns().stream()
            .allMatch(rdn -> {
                String type = rdn.getType().toLowerCase();
                String value = rdn.getValue().toString();
                
                // Vérifier que le type d'attribut est autorisé
                if (!ALLOWED_DN_ATTRIBUTES.contains(type)) {
                    return false;
                }
                
                // Vérifier que la valeur ne contient pas de caractères dangereux
                return !containsDangerousCharacters(value);
            });
    }
    
    private static boolean containsDangerousCharacters(String value) {
        // Liste des caractères dangereux pour LDAP
        return value.contains("*") || value.contains("(") || value.contains(")") ||
               value.contains("\\") || value.contains("\u0000");
    }
}
