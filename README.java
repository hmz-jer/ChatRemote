 Pour créer un PKI spécifique à la société STET, en accord avec le workflow décrit dans vos documents précédents, nous allons adapter notre approche. Selon les informations fournies, STET génère la bi-clé (clé privée et CSR) et ne fournit que la clé publique aux participants.

Voici comment nous pouvons mettre en place un PKI simulant ce modèle:

1. **Créer l'infrastructure de base**

```bash
# Créer un répertoire pour notre PKI STET
mkdir -p pki_stet/{ca,intermediates,participants,certs}
```

2. **Créer une autorité de certification racine**

```bash
# Générer la clé privée de la CA racine
openssl genrsa -out pki_stet/ca/root-ca.key 4096

# Générer le certificat auto-signé de la CA racine
openssl req -x509 -new -nodes -key pki_stet/ca/root-ca.key -sha256 -days 3650 -out pki_stet/ca/root-ca.crt -subj "/C=FR/O=STET CA/OU=PKI/CN=STET Root CA"
```

3. **Créer une autorité de certification intermédiaire pour les QWAC**

```bash
# Générer la clé privée pour la CA intermédiaire
openssl genrsa -out pki_stet/intermediates/qwac-ca.key 4096

# Créer un fichier de configuration pour la CA intermédiaire
cat > pki_stet/intermediates/qwac-ca.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_ca
prompt = no

[req_distinguished_name]
C = FR
O = STET
OU = QWAC Authority
CN = STET QWAC CA

[v3_ca]
basicConstraints = critical, CA:true, pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
subjectKeyIdentifier = hash
EOF

# Générer la demande de certificat pour la CA intermédiaire
openssl req -new -key pki_stet/intermediates/qwac-ca.key -out pki_stet/intermediates/qwac-ca.csr -config pki_stet/intermediates/qwac-ca.conf

# Signer la CA intermédiaire avec la CA racine
openssl x509 -req -in pki_stet/intermediates/qwac-ca.csr -CA pki_stet/ca/root-ca.crt -CAkey pki_stet/ca/root-ca.key -CAcreateserial -out pki_stet/intermediates/qwac-ca.crt -days 1825 -sha256 -extensions v3_ca -extfile pki_stet/intermediates/qwac-ca.conf
```

4. **Créer une bi-clé pour un participant (simulant le processus STET)**

```bash
# Générer la clé privée pour le participant
openssl genrsa -out pki_stet/participants/participant1.key 2048

# Créer un fichier de configuration pour le CSR du participant
cat > pki_stet/participants/participant1.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = FR
O = Participant Bancaire
OU = 0002 123456789
CN = api.participant.fr
2.5.4.97 = PSDFR-ACPR-30003
L = PARIS

[v3_req]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = api.participant.fr
EOF

# Générer le CSR (STET génère la bi-clé selon les spécifications)
openssl req -new -key pki_stet/participants/participant1.key -out pki_stet/participants/participant1.csr -config pki_stet/participants/participant1.conf
```

5. **Signer le certificat du participant (par l'autorité QWAC intermédiaire)**

```bash
# Signer le CSR pour créer le certificat
openssl x509 -req -in pki_stet/participants/participant1.csr -CA pki_stet/intermediates/qwac-ca.crt -CAkey pki_stet/intermediates/qwac-ca.key -CAcreateserial -out pki_stet/participants/participant1.crt -days 730 -sha256 -extensions v3_req -extfile pki_stet/participants/participant1.conf
```

6. **Extraire la clé publique (ce que STET fournirait au participant)**

```bash
# Extraire la clé publique du certificat
openssl x509 -in pki_stet/participants/participant1.crt -pubkey -noout > pki_stet/participants/participant1.pub
```

7. **Créer un PKCS#12 pour le participant (que STET conserverait)**

```bash
# Créer un PKCS#12 pour utilisation
openssl pkcs12 -export -out pki_stet/participants/participant1.p12 -inkey pki_stet/participants/participant1.key -in pki_stet/participants/participant1.crt -certfile pki_stet/intermediates/qwac-ca.crt -CAfile pki_stet/ca/root-ca.crt -name "Participant 1" -passout pass:password
```

8. **Créer un certificat pour le serveur PSP Externe VOP**

```bash
# Générer la clé privée pour le serveur VOP
openssl genrsa -out pki_stet/certs/vop_server.key 2048

# Créer un fichier de configuration pour le serveur VOP
cat > pki_stet/certs/vop_server.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = FR
O = PSP Externe VOP
OU = Systèmes
CN = vop.psp.fr

[v3_req]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = vop.psp.fr
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

# Générer la demande de certificat
openssl req -new -key pki_stet/certs/vop_server.key -out pki_stet/certs/vop_server.csr -config pki_stet/certs/vop_server.conf

# Signer avec notre CA intermédiaire
openssl x509 -req -in pki_stet/certs/vop_server.csr -CA pki_stet/intermediates/qwac-ca.crt -CAkey pki_stet/intermediates/qwac-ca.key -CAcreateserial -out pki_stet/certs/vop_server.crt -days 730 -sha256 -extensions v3_req -extfile pki_stet/certs/vop_server.conf
```

9. **Créer un certificat pour l'API Gateway**

```bash
# Générer la clé privée pour l'API Gateway
openssl genrsa -out pki_stet/certs/api_gateway.key 2048

# Créer un fichier de configuration pour l'API Gateway
cat > pki_stet/certs/api_gateway.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = FR
O = STET
OU = API Gateway
CN = api-gateway.stet.fr

[v3_req]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = api-gateway.stet.fr
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

# Générer la demande de certificat
openssl req -new -key pki_stet/certs/api_gateway.key -out pki_stet/certs/api_gateway.csr -config pki_stet/certs/api_gateway.conf

# Signer avec notre CA intermédiaire
openssl x509 -req -in pki_stet/certs/api_gateway.csr -CA pki_stet/intermediates/qwac-ca.crt -CAkey pki_stet/intermediates/qwac-ca.key -CAcreateserial -out pki_stet/certs/api_gateway.crt -days 730 -sha256 -extensions v3_req -extfile pki_stet/certs/api_gateway.conf
```

10. **Créer les keystores et truststores pour le test**

```bash
# Créer un PKCS#12 pour le serveur VOP
openssl pkcs12 -export -out pki_stet/certs/vop_server.p12 -inkey pki_stet/certs/vop_server.key -in pki_stet/certs/vop_server.crt -certfile pki_stet/intermediates/qwac-ca.crt -CAfile pki_stet/ca/root-ca.crt -name "VOP Server" -passout pass:password

# Créer un PKCS#12 pour l'API Gateway
openssl pkcs12 -export -out pki_stet/certs/api_gateway.p12 -inkey pki_stet/certs/api_gateway.key -in pki_stet/certs/api_gateway.crt -certfile pki_stet/intermediates/qwac-ca.crt -CAfile pki_stet/ca/root-ca.crt -name "API Gateway" -passout pass:password

# Créer un truststore JKS qui contient la CA racine
keytool -import -trustcacerts -alias root-ca -file pki_stet/ca/root-ca.crt -keystore pki_stet/certs/truststore.jks -storepass password -noprompt

# Créer un truststore JKS qui contient la CA intermédiaire QWAC
keytool -import -trustcacerts -alias qwac-ca -file pki_stet/intermediates/qwac-ca.crt -keystore pki_stet/certs/truststore.jks -storepass password -noprompt
```

11. **Créer un fichier de chaîne de confiance pour les tests**

```bash
# Concaténer les certificats pour avoir la chaîne complète
cat pki_stet/intermediates/qwac-ca.crt pki_stet/ca/root-ca.crt > pki_stet/certs/chain.pem
```

Cette configuration simule le modèle décrit dans vos documents, où STET génère la bi-clé et les participants utilisent les certificats pour l'authentification MTLS avec les PSP Externes VOP. Le certificateEmitterId (PSDFR-ACPR-30003) est inclus dans le certificat comme indiqué dans l'image du certificat QWAC que vous avez partagée.

Vous pouvez maintenant utiliser ces certificats pour mettre en place votre environnement de test avec les composants du système (mock VOP, API Gateway, etc.) comme décrit précédemment.
