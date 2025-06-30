 Excellente progression ! Maintenant vous avez résolu le problème SAN, mais il y a un problème de chaîne de certification. Cette erreur signifie que le certificat n'est pas validé par une autorité de certification reconnue dans votre truststore.Solution 1 : Ajouter le certificat au truststoreRécupérer le certificat complet du serveur# Récupérer le certificat du serveur sur port 8065
echo | openssl s_client -connect localhost:8065 -servername localhost 2>/dev/null | openssl x509 -out server-8065.crt

# Ou récupérer toute la chaîne de certificats
openssl s_client -connect localhost:8065 -showcerts </dev/null 2>/dev/null | awk '/-----BEGIN CERTIFICATE-----/,/-----END CERTIFICATE-----/' > fullchain.pemAjouter au truststore Java# Ajouter le certificat server
keytool -import -alias server-8065 -file server-8065.crt -keystore votre-truststore.jks -storepass votre-password -noprompt

# Vérifier l'ajout
keytool -list -alias server-8065 -keystore votre-truststore.jks -storepass votre-passwordSi il y a plusieurs certificats dans la chaîne# Séparer les certificats de la chaîne
split -p "-----BEGIN CERTIFICATE-----" fullchain.pem cert-

# Ajouter chaque certificat
keytool -import -alias server-cert -file cert-aa -keystore votre-truststore.jks -storepass votre-password -noprompt
keytool -import -alias intermediate-cert -file cert-ab -keystore votre-truststore.jks -storepass votre-password -nopromptSolution 2 : Configuration Java pour utiliser le truststoreVia propriétés systèmeSystem.setProperty("javax.net.ssl.trustStore", "/path/to/votre-truststore.jks");
System.setProperty("javax.net.ssl.trustStorePassword", "votre-password");
System.setProperty("javax.net.ssl.trustStoreType", "JKS");Via configuration RestTemplate avec truststore personnalisé@Bean
public RestTemplate restTemplate() throws Exception {
    // Charger le truststore personnalisé
    KeyStore trustStore = KeyStore.getInstance("JKS");
    try (InputStream trustStoreStream = new FileInputStream("/path/to/votre-truststore.jks")) {
        trustStore.load(trustStoreStream, "votre-password".toCharArray());
    }
    
    // Créer le SSLContext avec ce truststore
    SSLContext sslContext = SSLContextBuilder.create()
            .loadTrustMaterial(trustStore, null)
            .build();
    
    SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(
        sslContext,
        NoopHostnameVerifier.INSTANCE  // Garde la désactivation SAN si nécessaire
    );
    
    CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build();
            
    HttpComponentsClientHttpRequestFactory requestFactory = 
        new HttpComponentsClientHttpRequestFactory();
    requestFactory.setHttpClient(httpClient);
    
    return new RestTemplate(requestFactory);
}Solution 3 : Créer un truststore completSi vous partez de zéro# Créer un nouveau truststore avec le certificat serveur
keytool -import -alias server-localhost -file server-8065.crt -keystore new-truststore.jks -storepass password -noprompt

# Tester avec curl
keytool -exportcert -alias server-localhost -keystore new-truststore.jks -file test.pem -rfc -storepass password
curl --cacert test.pem https://localhost:8065/votre-endpointSolution 4 : Diagnostic approfondiVoir exactement quelle chaîne de certification est présentée# Voir tous les certificats présentés par le serveur
openssl s_client -connect localhost:8065 -showcerts

# Voir spécifiquement la chaîne de validation
openssl s_client -connect localhost:8065 -verify_return_errorIdentifier quel certificat racine/intermédiaire manque# Voir l'issuer du certificat serveur
echo | openssl s_client -connect localhost:8065 2>/dev/null | openssl x509 -noout -issuer

# Comparer avec ce qui est dans votre truststore
keytool -list -v -keystore votre-truststore.jks -storepass votre-password | grep "Issuer:"Solution 5 : Test étape par étape# 1. Vérifier que le certificat est bien récupéré
openssl x509 -in server-8065.crt -text -noout

# 2. Vérifier qu'il est bien ajouté au truststore
keytool -list -keystore votre-truststore.jks -storepass votre-password | grep localhost

# 3. Tester la connectivité
curl -v --cacert server-8065.crt https://localhost:8065/

# 4. Tester avec le truststore Java converti
keytool -exportcert -alias server-8065 -keystore votre-truststore.jks -file converted.pem -rfc -storepass votre-password
curl -v --cacert converted.pem https://localhost:8065/Commandes de diagnosticPouvez-vous me donner le résultat de :# Voir ce qui est présenté par le serveur
openssl s_client -connect localhost:8065 -showcerts | head -30

# Voir ce qui est dans votre truststore actuel
keytool -list -keystore votre-truststore.jks -storepass votre-passwordCela m'aidera à identifier exactement quel certificat il faut ajouter !
