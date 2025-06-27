 Oui, excellente idée ! Tester avec curl va vous aider à identifier le problème. Voici comment procéder selon le format de votre truststore :Avec un truststore JKS1. Convertir le JKS en PEM d'abord :keytool -exportcert -keystore truststore.jks -alias votre_alias -file cert.pem -rfc
# Ou pour exporter tous les certificats
keytool -list -keystore truststore.jks -v2. Test curl avec le certificat PEM :curl -v --cacert cert.pem https://votreserveur.com/votre-endpointAvec un truststore PEM directementcurl -v --cacert truststore.pem https://votreserveur.com/votre-endpointAvec un certificat p12/pfx# Convertir d'abord en PEM
openssl pkcs12 -in truststore.p12 -out truststore.pem -nodes

# Puis tester
curl -v --cacert truststore.pem https://votreserveur.com/votre-endpointTests de diagnostic1. Test sans validation SSL (pour voir si c'est bien un problème de certificat) :curl -k -v https://votreserveur.com/votre-endpoint2. Test avec informations détaillées :curl -v --cacert cert.pem https://votreserveur.com/votre-endpoint 2>&1 | grep -E "(SSL|TLS|certificate|subject|issuer)"3. Voir les certificats du serveur :openssl s_client -connect votreserveur.com:443 -showcertsExemple de test POSTcurl -v --cacert cert.pem \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}' \
  https://votreserveur.com/votre-endpointQuel format a votre truststore ? Et pouvez-vous partager l'URL que vous testez (sans données sensibles) ?
