# Générer la clé privée du CA
openssl req -new -x509 -keyout ca.key -out ca.crt -days 365 -subj "/CN=ca.test.com" -passin pass:azerty -passout pass:azerty

# Créer le keystore du serveur
keytool -keystore kafka.server.keystore.jks -alias server -validity 365 -genkey -keyalg RSA -storepass azerty -keypass azerty -dname "CN=localhost" -storetype pkcs12

# Créer une demande de signature de certificat (CSR)
keytool -keystore kafka.server.keystore.jks -alias server -certreq -file server.csr -storepass azerty -keypass azerty

# Signer le certificat serveur avec le CA
openssl x509 -req -CA ca.crt -CAkey ca.key -in server.csr -out server.crt -days 365 -CAcreateserial -passin pass:azerty

# Importer le certificat CA dans le keystore serveur
keytool -keystore kafka.server.keystore.jks -alias CARoot -import -file ca.crt -storepass azerty -noprompt

# Importer le certificat serveur signé dans le keystore serveur
keytool -keystore kafka.server.keystore.jks -alias server -import -file server.crt -storepass azerty -noprompt

# Créer le truststore et importer le certificat CA
keytool -keystore kafka.server.truststore.jks -alias CARoot -import -file ca.crt -storepass azerty -noprompt -storetype pkcs12

# Créer le keystore client
keytool -keystore kafka.client.keystore.jks -alias client -validity 365 -genkey -keyalg RSA -storepass azerty -keypass azerty -dname "CN=client.test.com" -storetype pkcs12

# Créer une demande de signature de certificat pour le client
keytool -keystore kafka.client.keystore.jks -alias client -certreq -file client.csr -storepass azerty -keypass azerty

# Signer le certificat client avec le CA
openssl x509 -req -CA ca.crt -CAkey ca.key -in client.csr -out client.crt -days 365 -CAcreateserial -passin pass:azerty

# Importer le certificat CA dans le keystore client
keytool -keystore kafka.client.keystore.jks -alias CARoot -import -file ca.crt -storepass azerty -noprompt

# Importer le certificat client signé dans le keystore client
keytool -keystore kafka.client.keystore.jks -alias client -import -file client.crt -storepass azerty -noprompt

# Créer le truststore client et importer le certificat CA
keytool -keystore kafka.client.truststore.jks -alias CARoot -import -file ca.crt -storepass azerty -noprompt -storetype pkcs12
