 # Génération de certificats QWAC au format P12 (PKCS#12) pour les clients

Voici les étapes détaillées pour générer des certificats QWAC au format P12 (PKCS#12), qui contiendront à la fois le certificat public et la clé privée, protégés par un mot de passe:

## Étape 1: Créer une clé privée pour l'AC générique

```bash
openssl genrsa -out psd2-ac-root.key 4096
```

**Explication**: Génère une clé RSA de 4096 bits pour l'Autorité de Certification générique.

## Étape 2: Créer un certificat auto-signé pour l'AC générique

```bash
openssl req -x509 -new -nodes -key psd2-ac-root.key -sha256 -days 3650 -out psd2-ac-root.cert \
-subj "/C=FR/ST=Paris/L=Paris/O=PSD2 Generic Root CA/CN=PSD2 Root Certification Authority"
```

**Explication**: Crée un certificat auto-signé pour l'AC racine générique, valide pour 10 ans.

## Étape 3: Générer une clé privée pour le client

```bash
openssl genrsa -out client.key 2048
```

**Explication**: Génère une clé RSA de 2048 bits pour le client (banque).

## Étape 4: Créer une demande de certificat (CSR) pour le client

```bash
openssl req -new -key client.key -out client.csr \
-subj "/C=FR/ST=Paris/L=Paris/O=BANK NAME/OU=Payment Services/CN=api.bank.com/organizationIdentifier=PSDFR-ACPR-12345"
```

**Explication**: Crée une demande de signature de certificat (CSR) pour le client avec l'identifiant d'organisation au format PSD2. Vous devez remplacer `BANK NAME` et `PSDFR-ACPR-12345` par les informations spécifiques du client.

## Étape 5: Créer un fichier d'extensions QWAC

Créez un fichier `qwac.ext` avec les extensions requises pour PSD2:

```
basicConstraints = critical, CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, serverAuth
subjectAltName = @alt_names
certificatePolicies = @policy_section
qcStatements = critical, @qc_statements

[ alt_names ]
DNS.1 = api.bank.com
DNS.2 = www.api.bank.com
DNS.3 = localhost
IP.1 = 127.0.0.1

[ policy_section ]
policyIdentifier = 0.4.0.19495.3.1
userNotice.1 = @policy_notice

[ policy_notice ]
explicitText = "PSD2 Qualified Website Authentication Certificate"

[ qc_statements ]
id-etsi-psd2-qcStatement = DER:30:64:31:10:30:0E:06:03:2B:06:01:05:05:07:0C:01:01:01:01:01:FF:04:04:50:53:44:32:31:25:30:23:06:0A:2A:06:01:04:01:97:55:01:03:01:01:30:15:0C:09:50:53:44:46:52:2D:41:43:50:52:0C:08:31:32:33:34:35:31:29:30:27:06:08:2B:06:01:05:05:07:0C:02:30:1B:1E:19:68:74:74:70:73:3A:2F:2F:70:73:64:32:2E:62:61:6E:6B:2E:63:6F:6D
```

**Explication**: Ce fichier définit les extensions X.509 nécessaires pour un certificat QWAC conforme à PSD2, incluant les qcStatements requis.

## Étape 6: Signer le CSR avec la clé de l'AC générique et inclure les extensions QWAC

```bash
openssl x509 -req -in client.csr -CA psd2-ac-root.cert -CAkey psd2-ac-root.key -CAcreateserial \
-out client.crt -days 730 -sha256 -extfile qwac.ext
```

**Explication**: Signe le CSR du client avec la clé privée de l'AC générique, créant ainsi un certificat QWAC valide.

## Étape 7: Créer un fichier PKCS#12 (P12) contenant le certificat et la clé privée

```bash
openssl pkcs12 -export -in client.crt -inkey client.key -certfile psd2-ac-root.cert \
-name "client-qwac" -out client-qwac.p12 -passout pass:SecurePassword123
```

**Explication**: Cette commande crée un fichier PKCS#12 (extension .p12) qui contient:
- Le certificat client signé (`client.crt`)
- La clé privée associée (`client.key`)
- Le certificat de l'AC qui l'a signé (`psd2-ac-root.cert`)
- Le tout protégé par le mot de passe `SecurePassword123`

Le paramètre `-name` définit un alias qui pourra être utilisé pour référencer ce certificat dans le fichier P12.

## Étape 8: Vérifier le contenu du fichier P12

```bash
openssl pkcs12 -info -in client-qwac.p12 -noout -passin pass:SecurePassword123
```

**Explication**: Affiche les informations contenues dans le fichier P12 sans extraire les clés, pour vérifier qu'il contient bien le certificat et la clé privée.

## Étape 9: Création d'un fichier P12 pour le truststore (optionnel)

Si vos clients ont également besoin d'un truststore au format P12 pour valider d'autres certificats:

```bash
openssl pkcs12 -export -in psd2-ac-root.cert -name "psd2-root-ca" -nokeys \
-out psd2-truststore.p12 -passout pass:TrustPassword123
```

**Explication**: Crée un fichier PKCS#12 contenant uniquement le certificat de l'AC racine, pour être utilisé comme truststore.

## Étape 10: Distribution des certificats aux clients

Pour chaque client, vous devez leur fournir:

1. **Le fichier P12** (`client-qwac.p12`) contenant le certificat et la clé privée
2. **Le mot de passe** associé au fichier P12 (`SecurePassword123` dans cet exemple)
3. **Le certificat de l'AC racine** (`psd2-ac-root.cert`) pour qu'ils puissent valider d'autres certificats émis par cette AC

## Guide d'utilisation pour les clients

Voici un guide que vous pouvez fournir aux clients pour utiliser leur certificat P12:

### Avec curl

```bash
curl --cert-type P12 --cert client-qwac.p12:SecurePassword123 \
     --cacert psd2-ac-root.cert https://api.exemple.com/endpoint
```

### Avec Java

```java
// Charger le keystore P12
KeyStore keyStore = KeyStore.getInstance("PKCS12");
try (FileInputStream fis = new FileInputStream("client-qwac.p12")) {
    keyStore.load(fis, "SecurePassword123".toCharArray());
}

// Configurer le SSLContext
KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
keyManagerFactory.init(keyStore, "SecurePassword123".toCharArray());

// Charger le truststore
KeyStore trustStore = KeyStore.getInstance("PKCS12");
try (FileInputStream fis = new FileInputStream("psd2-truststore.p12")) {
    trustStore.load(fis, "TrustPassword123".toCharArray());
}

TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
trustManagerFactory.init(trustStore);

SSLContext sslContext = SSLContext.getInstance("TLS");
sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

// Utiliser ce SSLContext avec votre HttpClient
// [code pour configurer le HttpClient avec sslContext]
```

### Avec Spring Boot (application.yml)

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:client-qwac.p12
    key-store-password: SecurePassword123
    key-store-type: PKCS12
    key-alias: client-qwac
    trust-store: classpath:psd2-truststore.p12
    trust-store-password: TrustPassword123
    trust-store-type: PKCS12
    client-auth: need
```

## Points importants à communiquer aux clients

1. **Sécurité du fichier P12**: Le fichier P12 contient la clé privée et doit être conservé en sécurité
2. **Protection du mot de passe**: Le mot de passe protégeant le fichier P12 doit être gardé confidentiel
3. **Validité du certificat**: Informez le client de la durée de validité du certificat (730 jours dans cet exemple)
4. **Procédure de renouvellement**: Expliquez la procédure à suivre lorsque le certificat approche de sa date d'expiration
5. **Contact en cas de compromission**: Fournissez un point de contact en cas de compromission du certificat ou de la clé privée

Cette approche simplifie l'utilisation des certificats pour vos clients, car ils n'ont besoin de gérer qu'un seul fichier P12 protégé par mot de passe, plutôt que des fichiers de certificat et de clé privée séparés.
