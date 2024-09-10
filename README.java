  import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.filter.EqualsFilter;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;

public class LdapUniqueMemberTest {

    public static void main(String[] args) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:389");
        contextSource.setBase("dc=interne,dc=cartes,dc=com");
        contextSource.setUserDn("cn=admin,dc=interne,dc=cartes,dc=com");
        contextSource.setPassword("admin_password");
        
        try {
            contextSource.afterPropertiesSet();

            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);

            // Recherche du cn=admin dans ou=acc
            String searchBase = "ou=acc";
            EqualsFilter filter = new EqualsFilter("cn", "admin");
            
            ldapTemplate.search(searchBase, filter.encode(), (attrs) -> {
                System.out.println("CN trouvé : " + attrs.getDn());
                Attribute uniqueMembers = attrs.get("uniqueMember");
                if (uniqueMembers != null) {
                    NamingEnumeration<?> values = uniqueMembers.getAll();
                    while (values.hasMore()) {
                        System.out.println("UniqueMember : " + values.next());
                    }
                } else {
                    System.out.println("Aucun uniqueMember trouvé.");
                }
                return null;
            });

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche LDAP : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
