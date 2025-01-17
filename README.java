# Créer le keystore client en spécifiant explicitement PKCS12
keytool -keystore kafka.client.keystore.jks -alias client -validity 365 -genkey -keyalg RSA -storepass azerty -keypass azerty -dname "CN=client-test-local" -storetype PKCS12

# Créer la demande de signature
keytool -keystore kafka.client.keystore.jks -alias client -certreq -file client.csr -storepass azerty -keypass azerty -storetype PKCS12

# Signer avec le CA
openssl x509 -req -CA ca.crt -CAkey ca.key -in client.csr -out client.crt -days 365 -CAcreateserial -passin pass:azerty

# Importer le certificat CA dans le keystore client
keytool -keystore kafka.client.keystore.jks -alias CARoot -import -file ca.crt -storepass azerty -noprompt -storetype PKCS12

# Importer le certificat client signé
keytool -keystore kafka.client.keystore.jks -alias client -import -file client.crt -storepass azerty -noprompt -storetype PKCS12
