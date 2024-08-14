  package com.example.omc.config;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.Entry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapInMemoryConfig {

    @Bean
    public InMemoryDirectoryServer ldapServer() throws Exception {
        // Configuration du serveur LDAP en mémoire
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
        config.addAdditionalBindCredentials("cn=admin,dc=example,dc=com", "adminpassword");

        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        ds.startListening();

        // Ajout d'un utilisateur de test
        ds.add(new Entry("dn: uid=user,ou=users,dc=example,dc=com",
                "objectClass: inetOrgPerson",
                "uid: user",
                "sn: User",
                "cn: Test User",
                "userPassword: {SSHA}2ZKPs8Xr4Xlm5oYPyfnRWQ=="));  // mot de passe "azerty" encodé en SSHA

        return ds;
    }
}
