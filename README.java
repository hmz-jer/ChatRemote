# Validateur de certificats pour APIM CLI Axway

Script de validation préventive des certificats CA avant import dans Axway API Management CLI. Détecte les erreurs de certificats qui causeraient des échecs lors de l'exécution d'`apim api import`.

## Problème résolu

APIM CLI d'Axway peut échouer lors de l'import de certificats CA avec des erreurs comme :
- `SSL verify_cb, 0x13, 0` (certificats auto-signés traités comme erreurs)
- `unknown CA [fatal]` (chaînes de certificats invalides)

Ce script valide les certificats **en amont** pour éviter ces erreurs à l'exécution.

## Utilisation

### Validation d'une chaîne de certificats
```bash
./validate_apim_cert.sh ca-chain.pem
```

### Exemple de sortie
```
Validation APIM CLI: ca-root.pem
Certificats trouvés: 1

- Vérification format PEM...
  Format PEM: OK
- Vérification validité...
  Validité: OK
- Informations certificat:
  Subject: CN=Root CA,O=MonOrg,C=FR
  Issuer: CN=Root CA,O=MonOrg,C=FR
  Expire: Dec 31 23:59:59 2025 GMT
- Validation CN APIM CLI...
  CN: Root CA
  CN filesystem: OK
- Validation chaîne SSL...
  Type: Certificat CA détecté
  Statut: Root CA (auto-signé) - NORMAL
  Intégrité: OK

Validation APIM CLI: SUCCÈS
```

## Validations effectuées

### 1. Format PEM standard
- **Commande OpenSSL** : `openssl x509 -in certificat.pem -text -noout`
- **Objectif** : Vérifier la structure X.509 et les marqueurs BEGIN/END CERTIFICATE
- **Erreur APIM CLI évitée** : Format de certificat invalide

### 2. Validité temporelle
- **Commandes OpenSSL** :
  - `openssl x509 -in certificat.pem -noout -checkend 0` (expiration)
- **Objectif** : Détecter les certificats expirés
- **Erreur APIM CLI évitée** : Certificats expirés causant des échecs SSL

### 3. Extraction des métadonnées
- **Commandes OpenSSL** :
  - `openssl x509 -in certificat.pem -noout -subject` (Subject DN)
  - `openssl x509 -in certificat.pem -noout -issuer` (Issuer DN)
  - `openssl x509 -in certificat.pem -noout -enddate` (Date expiration)
- **Objectif** : Afficher les informations du certificat pour vérification

### 4. Validation CN filesystem (Spécifique APIM CLI)
- **Commande OpenSSL** : `openssl x509 -in certificat.pem -noout -subject`
- **Traitement** : Extraction du CN avec `sed -n 's/.*CN[[:space:]]*=[[:space:]]*\([^,]*\).*/\1/p'`
- **Validations** :
  - Pas de caractère "/" (Issue #315 APIM CLI)
  - Pas de caractères interdits : `\ : * ? " < > |`
  - Longueur max 255 caractères
- **Erreur APIM CLI évitée** : `Can't write certificate to disc`

### 5. Détection et validation des certificats CA
- **Commande OpenSSL** : `openssl x509 -in certificat.pem -noout -text | grep -A 1 "Basic Constraints"`
- **Objectif** : Identifier les certificats CA
- **Erreur APIM CLI évitée** : Rejet incorrect des Root CA

### 6. Validation de l'intégrité SSL
- **Commandes OpenSSL** :
  - Comparaison issuer/subject pour auto-signature
- **Objectif** : Détecter la corruption ou les problèmes de chaîne
- **Erreur APIM CLI évitée** : Erreurs SSL 0x4, 0x13, 0x230


## Commandes OpenSSL de référence

Le script utilise ces commandes OpenSSL principales :

```bash
# Validation format et structure
openssl x509 -in cert.pem -text -noout

# Vérification expiration
openssl x509 -in cert.pem -noout -checkend 0

# Extraction métadonnées
openssl x509 -in cert.pem -noout -subject
openssl x509 -in cert.pem -noout -issuer
openssl x509 -in cert.pem -noout -enddate

# Détection Basic Constraints (CA)
openssl x509 -in cert.pem -noout -text | grep -A 1 "Basic Constraints"
```

## Références

- [APIM CLI Issue #315](https://github.com/Axway-API-Management-Plus/apim-cli/issues/315) - Caractères "/" dans CN
- [APIM CLI Issue #221](https://github.com/Axway-API-Management-Plus/apim-cli/issues/221) - SSL mutual authentication
- [RFC 5280](https://datatracker.ietf.org/doc/html/rfc5280) - X.509 PKI Certificate Profile

