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

        // Démarrage du serveur
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        ds.startListening();

        // Insertion de quelques entrées pour les tests
        ds.add(new Entry("dn: uid=johndoe,ou=users,dc=example,dc=com",
                         "objectClass: inetOrgPerson",
                         "uid: johndoe",
                         "sn: Doe",
                         "cn: John Doe",
                         "userPassword: {SSHA}e1NTSEF9a2Q5YmFtZGpHclRUMnV0eTJxaXJrdz09"));

        return ds;
    }
}.
