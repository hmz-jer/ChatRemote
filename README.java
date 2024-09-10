  import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.AndFilter;

public class LdapConnectionTest {

    public static void main(String[] args) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:389");
        contextSource.setBase("dc=interne,dc=cartes,dc=com");
        contextSource.setUserDn("cn=admin,dc=interne,dc=cartes,dc=com");
        contextSource.setPassword("admin_password");
        
        try {
            contextSource.afterPropertiesSet();

            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

            // Test de connexion
            boolean authenticated = ldapTemplate.authenticate("", "(uid=admin)", "admin_password");
            
            if (authenticated) {
                System.out.println("Connexion LDAP réussie !");
                
                // Recherche d'informations sur l'utilisateur
                AndFilter filter = new AndFilter();
                filter.and(new EqualsFilter("uid", "admin"));
                
                ldapTemplate.search("", filter.encode(), (attrs) -> {
                    System.out.println("Informations de l'utilisateur :");
                    System.out.println("DN : " + attrs.getDn());
                    System.out.println("CN : " + attrs.get("cn"));
                    System.out.println("UID : " + attrs.get("uid"));
                    return null;
                });
            } else {
                System.out.println("Échec de l'authentification LDAP.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion LDAP : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
