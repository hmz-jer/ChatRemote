# 1. Créer un répertoire pour les certificats
mkdir -p certs
cd certs

# 2. Créer le keystore client
keytool -keystore kafka.client.keystore.jks \
    -alias client \
    -validity 365 \
    -genkey \
    -keyalg RSA \
    -storepass azerty \
    -keypass azerty \
    -dname "CN=client-test-local" \
    -storetype PKCS12

# 3. Créer la demande de signature de certificat (CSR)
keytool -keystore kafka.client.keystore.jks \
    -alias client \
    -certreq \
    -file client.csr \
    -storepass azerty \
    -keypass azerty \
    -storetype PKCS12

# 4. Signer le certificat client avec le CA
# (assurez-vous que ca.crt et ca.key sont présents)
openssl x509 -req \
    -CA ca.crt \
    -CAkey ca.key \
    -in client.csr \
    -out client.crt \
    -days 365 \
    -CAcreateserial \
    -passin pass:azerty

# 5. Importer le certificat CA dans le keystore client
keytool -keystore kafka.client.keystore.jks \
    -alias CARoot \
    -import \
    -file ca.crt \
    -storepass azerty \
    -noprompt \
    -storetype PKCS12

# 6. Importer le certificat client signé dans le keystore
keytool -keystore kafka.client.keystore.jks \
    -alias client \
    -import \
    -file client.crt \
    -storepass azerty \
    -noprompt \
    -storetype PKCS12

# 7. Créer le truststore client
keytool -keystore kafka.client.truststore.jks \
    -alias CARoot \
    -import \
    -file ca.crt \
    -storepass azerty \
    -noprompt \
    -storetype PKCS12
