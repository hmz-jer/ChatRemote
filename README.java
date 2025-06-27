  Voici comment ajouter le certificat au truststore selon votre situation :1. Récupérer le certificat du serveurMéthode 1 : Via openssl# Récupérer le certificat
echo | openssl s_client -connect votreserveur.com:443 -servername votreserveur.com 2>/dev/null | openssl x509 -out serveur.crt

# Ou avec plus de détails
openssl s_client -connect votreserveur.com:443 -showcerts </dev/null 2>/dev/null | openssl x509 -outform PEM > serveur.crtMéthode 2 : Via votre navigateurAllez sur https://votreserveur.comClic sur le cadenas → Certificat → Détails → Exporter2. Ajouter au truststore JavaAu truststore système Java :# Localiser le truststore Java
find $JAVA_HOME -name "cacerts" 2>/dev/null

# Ajouter le certificat (mot de passe par défaut: changeit)
keytool -import -alias serveur-alias -file serveur.crt -keystore $JAVA_HOME/lib/security/cacerts -storepass changeitÀ un truststore personnalisé :# Créer un nouveau truststore
keytool -import -alias serveur-alias -file serveur.crt -keystore mon-truststore.jks -storepass monpassword

# Ou ajouter à un existant
keytool -import -alias serveur-alias -file serveur.crt -keystore truststore-existant.jks -storepass password3. Configurer votre applicationVia paramètres JVM :java -Djavax.net.ssl.trustStore=/path/to/mon-truststore.jks \
     -Djavax.net.ssl.trustStorePassword=monpassword \
     -jar votre-app.jarDans application.properties (Spring Boot) :server.ssl.trust-store=/path/to/mon-truststore.jks
server.ssl.trust-store-password=monpassword
server.ssl.trust-store-type=JKSProgrammatiquement :System.setProperty("javax.net.ssl.trustStore", "/path/to/mon-truststore.jks");
System.setProperty("javax.net.ssl.trustStorePassword", "monpassword");4. Vérifier l'ajout# Lister les certificats dans le truststore
keytool -list -keystore mon-truststore.jks -storepass monpassword

# Vérifier un alias spécifique
keytool -list -alias serveur-alias -keystore mon-truststore.jks -storepass monpassword5. Tester# Test curl avec le nouveau truststore (si converti en PEM)
keytool -exportcert -alias serveur-alias -keystore mon-truststore.jks -file test.pem -rfc -storepass monpassword
curl --cacert test.pem https://votreserveur.comQuelle méthode préférez-vous ? Truststore système ou personnalisé ?
