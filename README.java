# Infrastructure CA générique pour Mock VOP

## Architecture des certificats

### Pour le serveur Mock VOP :
- **Keystore (JKS)** : Contient le certificat et la clé privée du serveur
- **Truststore (JKS)** : Contient les CA racines pour valider les certificats clients

### Pour chaque client bancaire :
- **Fichier P12** : Contient le certificat QWAC avec sa clé privée et publique
- **Fichier CA (PEM)** : CA racine au format PEM pour la validation côté client

## Script de génération générique

### 1. Configuration des variables
```bash
#!/bin/bash

# Variables globales
CA_NAME="Generic-VOP-CA"
CA_DAYS=3650
CERT_DAYS=365
COUNTRY="FR"
STATE="Ile-de-France"
LOCALITY="Paris"
CA_ORG="VOP Mock CA"

# Fonction pour générer les certificats d'une banque
generate_bank_certificates() {
    BANK_NAME=$1
    BANK_ORG_ID=$2  # Format: PSDFR-ACPR-XXXXX
    BANK_ORG_NAME=$3
    
    echo "Génération des certificats pour $BANK_NAME"
    
    # Création du répertoire
    mkdir -p providers/$BANK_NAME
    cd providers/$BANK_NAME
    
    # ... (suite du script ci-dessous)
}
```

### 2. Création de la CA racine générique (une seule fois)
```bash
# Générer la clé privée de la CA
openssl genrsa -out generic-ca.key 4096

# Créer le certificat auto-signé de la CA
openssl req -x509 -new -nodes -key generic-ca.key -sha256 -days $CA_DAYS \
    -out generic-ca.crt \
    -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$CA_ORG/CN=Generic VOP Root CA"

# Créer la CA intermédiaire PSD2 (optionnel mais recommandé)
openssl genrsa -out psd2-intermediate-ca.key 4096

openssl req -new -key psd2-intermediate-ca.key \
    -out psd2-intermediate-ca.csr \
    -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$CA_ORG/CN=PSD2 Intermediate CA"

openssl x509 -req -in psd2-intermediate-ca.csr \
    -CA generic-ca.crt -CAkey generic-ca.key \
    -CAcreateserial -out psd2-intermediate-ca.crt \
    -days $CA_DAYS -sha256
```

### 3. Script de génération pour chaque banque
```bash
#!/bin/bash

generate_qwac_certificate() {
    BANK_CODE=$1
    BANK_NAME=$2
    ORG_IDENTIFIER=$3  # Ex: PSDFR-ACPR-12345
    ORGANIZATION=$4
    SERVER_IP=$5       # IP du serveur pour le SAN
    
    # Créer le répertoire de la banque
    mkdir -p providers/$BANK_CODE
    cd providers/$BANK_CODE
    
    # Générer la clé privée
    openssl genrsa -out ${BANK_CODE}-private.key 2048
    
    # Créer le fichier de configuration pour les extensions QWAC
    cat > ${BANK_CODE}-qwac.ext <<EOF
basicConstraints = critical,CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

# Extensions QWAC spécifiques PSD2
1.3.6.1.4.1.311.60.2.1.3 = ASN1:PRINTABLESTRING:FR
2.5.4.97 = ASN1:PRINTABLESTRING:$ORG_IDENTIFIER
2.5.4.15 = ASN1:PRINTABLESTRING:Private Organization

[alt_names]
DNS.1 = ${BANK_CODE}.com
DNS.2 = www.${BANK_CODE}.com
DNS.3 = api.${BANK_CODE}.com
DNS.4 = localhost
IP.1 = 127.0.0.1
IP.2 = $SERVER_IP
EOF
    
    # Créer la CSR
    openssl req -new -key ${BANK_CODE}-private.key \
        -out ${BANK_CODE}.csr \
        -subj "/C=FR/ST=Ile-de-France/L=Paris/O=$ORGANIZATION/OU=Payment Services/CN=$BANK_NAME API/serialNumber=$ORG_IDENTIFIER"
    
    # Signer le certificat avec la CA
    openssl x509 -req -in ${BANK_CODE}.csr \
        -CA ../../psd2-intermediate-ca.crt \
        -CAkey ../../psd2-intermediate-ca.key \
        -CAcreateserial \
        -out ${BANK_CODE}-qwac.crt \
        -days $CERT_DAYS \
        -extfile ${BANK_CODE}-qwac.ext
    
    # Créer la chaîne complète
    cat ${BANK_CODE}-qwac.crt ../../psd2-intermediate-ca.crt ../../generic-ca.crt > ${BANK_CODE}-chain.crt
    
    # Exporter en P12 pour le client
    openssl pkcs12 -export \
        -out ${BANK_CODE}-client.p12 \
        -inkey ${BANK_CODE}-private.key \
        -in ${BANK_CODE}-qwac.crt \
        -certfile ../../psd2-intermediate-ca.crt \
        -password pass:password
    
    # Créer le keystore pour le serveur (JKS)
    keytool -importkeystore \
        -srckeystore ${BANK_CODE}-client.p12 \
        -srcstoretype PKCS12 \
        -srcstorepass password \
        -destkeystore ${BANK_CODE}-keystore.jks \
        -deststoretype JKS \
        -deststorepass password \
        -noprompt
    
    # Exporter la CA au format PEM pour le client
    cp ../../generic-ca.crt ${BANK_CODE}-ca.pem
    
    echo "Certificats générés pour $BANK_NAME dans providers/$BANK_CODE/"
    cd ../..
}
```

### 4. Création du truststore serveur (une seule fois)
```bash
# Créer le truststore qui contiendra toutes les CA
keytool -import -file generic-ca.crt \
    -alias generic-ca \
    -keystore server-truststore.jks \
    -storepass password \
    -noprompt

keytool -import -file psd2-intermediate-ca.crt \
    -alias psd2-ca \
    -keystore server-truststore.jks \
    -storepass password \
    -noprompt
```

### 5. Script d'utilisation
```bash
# Exemple d'utilisation pour plusieurs banques avec IP serveur
SERVER_IP="192.168.1.100"  # Remplacer par votre IP réelle

./generate_qwac_certificate.sh "natixis" "Natixis" "PSDFR-ACPR-15930" "NATIXIS PAYMENT SOLUTIONS" "$SERVER_IP"
./generate_qwac_certificate.sh "bnp" "BNP Paribas" "PSDFR-ACPR-14328" "BNP PARIBAS" "$SERVER_IP"
./generate_qwac_certificate.sh "sg" "Société Générale" "PSDFR-ACPR-13807" "SOCIETE GENERALE" "$SERVER_IP"
./generate_qwac_certificate.sh "ca" "Crédit Agricole" "PSDFR-ACPR-17449" "CREDIT AGRICOLE" "$SERVER_IP"
```

## Structure finale des fichiers

```
/mock-vop-certificates/
├── generic-ca.crt                    # CA racine générique
├── generic-ca.key                    # Clé privée CA (à protéger!)
├── psd2-intermediate-ca.crt         # CA intermédiaire PSD2
├── psd2-intermediate-ca.key         # Clé privée intermédiaire
├── server-truststore.jks            # Truststore pour le serveur Mock VOP
└── providers/
    ├── natixis/
    │   ├── natixis-private.key      # Clé privée
    │   ├── natixis-qwac.crt         # Certificat QWAC
    │   ├── natixis-chain.crt        # Chaîne complète
    │   ├── natixis-client.p12       # Pour le client (Postman, etc.)
    │   ├── natixis-keystore.jks     # Si besoin côté serveur
    │   └── natixis-ca.pem           # CA au format PEM pour le client
    ├── bnp/
    │   └── ...
    └── sg/
        └── ...
```

## Configuration application.yml mise à jour

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:server-keystore.jks  # Keystore du serveur Mock VOP
    key-store-password: password
    key-store-type: JKS
    trust-store: classpath:server-truststore.jks  # Contient les CA
    trust-store-password: password
    trust-store-type: JKS
    client-auth: need  # Exige un certificat client

mock-vop:
  providers:
    base-path: /providers
    url-prefix: /api/provider
  qwac:
    validation:
      organization-identifier-oid: "2.5.4.97"
      certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
  bank-responses:
    config-file: classpath:bank-responses.yml
```

## Configuration client (Postman/Insomnia)

Pour chaque banque, configurer :
1. **Certificate** : `providers/{bank}/{bank}-client.p12`
2. **Password** : `password`
3. **CA Certificate** : `providers/{bank}/{bank}-ca.pem` (pour valider le serveur)

## Avantages de cette approche

1. **CA unique** : Une seule CA racine pour toutes les banques
2. **Génération automatisée** : Script paramétrable pour chaque banque
3. **Structure standardisée** : Organisation cohérente des fichiers
4. **Sécurité** : Séparation des clés privées par banque
5. **Flexibilité** : Facile d'ajouter de nouvelles banques
6. **Conformité PSD2** : Extensions QWAC correctement configurées

## Test avec cURL

```bash
# Test pour une banque spécifique
curl -v --cacert providers/natixis/natixis-ca.pem \
     --cert providers/natixis/natixis-client.p12:password \
     --cert-type P12 \
     https://localhost:8443/api/provider/15930/accounts
```
