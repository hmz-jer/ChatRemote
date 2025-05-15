 
# Guide de génération de certificats QWAC pour PSD2 avec explications détaillées

Voici les étapes complètes pour générer tous les certificats nécessaires à votre mock-client-VOP, basées sur les commandes OpenSSL que vous avez partagées mais avec des explications détaillées.

## 1. Création d'une clé privée et d'un certificat d'autorité de certification (CA)

```bash
# Créer une clé privée pour la CA
openssl genrsa -out natixis-ca.key 4096
```
**Explication**: Cette commande génère une clé privée RSA de 4096 bits qui sera utilisée par votre autorité de certification. La longueur de 4096 bits offre une sécurité supérieure à 2048 bits.

```bash
# Créer un certificat auto-signé pour la CA
openssl req -x509 -new -nodes -key natixis-ca.key -sha256 -days 1024 -out natixis-ca.cert \
-subj "/C=FR/ST=Paris/L=Paris/O=NATIXIS Local CA/CN=natixis-local-ca"
```
**Explication**: Cette commande crée un certificat racine auto-signé valide pour 1024 jours. Le sujet du certificat identifie votre CA locale. L'option `-x509` indique qu'il s'agit d'un certificat auto-signé et `-nodes` signifie "no DES" (la clé n'est pas chiffrée avec un mot de passe).

## 2. Création d'un fichier de configuration pour les extensions QWAC

Créez un fichier `qwac.ext` avec le contenu suivant:

```
basicConstraints = critical,CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[ alt_names ]
DNS.1 = natixis.com
DNS.2 = www.natixis.com
DNS.3 = localhost
IP.1 = 127.0.0.1
IP.2 = 10.56.8.69

# Champs spécifiques aux QWAC
1.3.6.1.4.1.311.60.2.1.3 = ASN1:PRINTABLESTRING:FR
2.5.4.97 = ASN1:PRINTABLESTRING:PSDFR-ACPR-15930
2.5.4.15 = ASN1:PRINTABLESTRING:Private Organization
```

**Explication**: 
- `basicConstraints`: Indique que ce n'est pas un certificat d'AC
- `keyUsage`: Précise les usages autorisés de la clé (signature et chiffrement)
- `extendedKeyUsage`: Précise que le certificat peut être utilisé pour l'authentification serveur et client
- `subjectAltName`: Liste des noms alternatifs valides (DNS et IPs)
- `1.3.6.1.4.1.311.60.2.1.3`: Code pays pour l'enregistrement juridique (FR)
- `2.5.4.97`: Identifiant d'organisation PSD2 au format PSDFR-ACPR-XXXXX
- `2.5.4.15`: Type d'organisation

## 3. Génération de la clé privée QWAC

```bash
# Générer une clé RSA pour le certificat QWAC
keytool -genkeypair -alias natixis-qwac -keyalg RSA -keysize 2048 -keystore natixis-keystore.jks \
-dname "CN=natixis.com, SERIALNUMBER=345155337, L=PARIS, O=NATIXIS PAYMENT SOLUTIONS, C=FR" \
-storepass password -keypass password -validity 365
```

**Explication**: Cette commande utilise keytool (outil Java) pour générer une paire de clés RSA de 2048 bits, stockée dans un keystore JKS. Le sujet inclut un CN (nom commun), un numéro de série, une localité, une organisation et un pays. Le certificat sera valide 365 jours.

## 4. Création d'une demande de certificat (CSR)

```bash
# Créer une demande de certificat (CSR)
keytool -certreq -alias natixis-qwac -keystore natixis-keystore.jks -file natixis.csr \
-storepass password
```

**Explication**: Cette commande crée une demande de signature de certificat (CSR) à partir de la clé générée précédemment. Le CSR contient la clé publique et des informations sur l'entité qui demande le certificat.

## 5. Signature du certificat QWAC par la CA

```bash
# Signer le CSR avec la CA en ajoutant les extensions QWAC
openssl x509 -req -in natixis.csr -CA natixis-ca.cert -CAkey natixis-ca.key -CAcreateserial \
-out natixis-qwac.crt -days 365 -extfile qwac.ext
```

**Explication**: Cette commande utilise la CA créée à l'étape 1 pour signer le CSR et générer un certificat QWAC. Les extensions définies dans qwac.ext sont incluses dans le certificat. Le certificat est valide pour 365 jours.

## 6. Importation du certificat signé dans le keystore

```bash
# Importation du certificat CA dans le keystore
keytool -importcert -file natixis-ca.cert -alias natixis-ca -keystore natixis-keystore.jks -storepass password -noprompt

# Importation du certificat QWAC signé dans le keystore
keytool -importcert -file natixis-qwac.crt -alias natixis-qwac -keystore natixis-keystore.jks -storepass password
```

**Explication**: Ces commandes importent d'abord le certificat CA puis le certificat QWAC signé dans le keystore JKS. Cela permet d'établir la chaîne de confiance complète.

## 7. Extraction de la clé privée pour les tests

```bash
# Export au format PKCS12 pour extraire la clé privée
keytool -importkeystore -srckeystore natixis-keystore.jks -srcstorepass password -srcalias natixis-qwac \
-destkeystore natixis-qwac.p12 -deststoretype PKCS12 -deststorepass password -destkeypass password

# Extraction de la clé privée du format PKCS12
openssl pkcs12 -in natixis-qwac.p12 -nokeys -out natixis-qwac.pem -password pass:password
openssl pkcs12 -in natixis-qwac.p12 -nocerts -out natixis-private.encrypted.key -password pass:password

# Suppression du mot de passe de la clé privée (optionnel, mais plus pratique pour les tests)
openssl rsa -in natixis-private.encrypted.key -out natixis-private.key -passin pass:password
```

**Explication**: 
- La première commande convertit le keystore JKS en format PKCS12
- La deuxième extrait le certificat en format PEM
- La troisième extrait la clé privée (encore protégée par mot de passe)
- La quatrième retire le mot de passe de la clé privée pour faciliter les tests

## 8. Création d'un truststore pour la validation des certificats client

```bash
# Créer un truststore contenant le certificat client
keytool -import -file natixis-qwac.crt -alias natixis-qwac -keystore truststore.jks -storepass password -noprompt
```

**Explication**: Cette commande crée un truststore qui contient le certificat QWAC. Le serveur utilisera ce truststore pour valider les certificats clients présentés lors de l'authentification mutuelle.

## 9. Vérification des certificats générés

```bash
# Vérifier le certificat QWAC
openssl x509 -in natixis-qwac.crt -text -noout
```

**Explication**: Cette commande affiche les détails du certificat QWAC pour vérifier qu'il contient toutes les extensions et informations requises.

```bash
# Vérifier que le certificat QWAC est bien signé par la CA
openssl verify -CAfile natixis-ca.cert natixis-qwac.crt
```

**Explication**: Cette commande vérifie que le certificat QWAC est bien signé par la CA et que la chaîne de confiance est valide.

## 10. Configuration du fichier application.yml pour Spring Boot

```yaml
server:
  port: 8443
  ssl:
    key-store: classpath:natixis-keystore.jks
    key-store-password: password
    key-store-type: JKS
    key-alias: natixis-qwac
    trust-store: classpath:truststore.jks
    trust-store-password: password
    client-auth: need # Rend l'authentification mutuelle obligatoire

# Configuration pour la validation des certificats QWAC
mock-vop:
  qwac:
    validation:
      enabled: true
      organization-identifier-oid: "2.5.4.97"
  
  # Configuration pour le routage basé sur le certificateOwnerId
  routing:
    enabled: true
    certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
    psp-mappings:
      "15930": "https://backend-15930.example.com"
      "default": "https://default-backend.example.com"
```

**Explication**: Cette configuration:
- Configure le serveur SSL sur le port 8443
- Utilise le keystore et truststore générés précédemment
- Active l'authentification client obligatoire (MTLS)
- Configure la validation des certificats QWAC et le routage basé sur l'identifiant d'organisation

## 11. Test avec curl

```bash
# Test de la connexion MTLS
curl -v --cacert natixis-ca.cert --cert natixis-qwac.crt --key natixis-private.key https://127.0.0.1:8443/api/status
```

**Explication**: Cette commande teste la connexion MTLS en utilisant:
- Le certificat de la CA pour valider le certificat serveur
- Le certificat QWAC et sa clé privée pour l'authentification client

## Points clés et bonnes pratiques:

1. **Sécurité des clés privées**: En production, protégez toujours vos clés privées avec un mot de passe fort.

2. **Validité**: Les certificats QWAC en production ont généralement une validité de 2 ans maximum.

3. **Extensions PSD2**: Assurez-vous que toutes les extensions PSD2 requises sont présentes:
   - L'identifiant PSP au format PSDFR-ACPR-XXXXX dans le champ 2.5.4.97
   - Les rôles PSD2 (si nécessaire)

4. **Environnement de production**: En production, utilisez une AC qualifiée PSD2 reconnue plutôt qu'une AC auto-signée.

5. **SubjectAlternativeName**: Incluez tous les noms DNS et adresses IP par lesquels votre serveur sera accessible.

6. **Validation de certificat**: Implémentez une validation complète, incluant la vérification de la chaîne, la validité et les extensions spécifiques à PSD2.

Ces étapes vous permettront de créer un environnement complet pour tester la validation des certificats QWAC dans le cadre de PSD2.
