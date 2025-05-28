  # Mock-Client-VOP

Mock-Client-VOP est une application Spring Boot qui simule un PSP externe pour valider les certificats QWAC (Qualified Website Authentication Certificate) dans le cadre de PSD2.

## Fonctionnalités

- 🔐 Validation des certificats QWAC avec authentification mTLS
- 🏦 Gestion des certificats par banque (`/provider/NomDeLaBanque/`)
- 📝 Réponses personnalisées configurables en YAML
- ⚙️ Rechargement à chaud des configurations
- 🔍 API d'administration intégrée

## Installation rapide

```bash
# Structure des répertoires
mkdir -p /opt/mock-client-vop/{bin,conf,logs,certs,lib}
mkdir -p /provider/{Natixis,BNP,SocieteGenerale}

# Copier les fichiers
cp target/mock-client-vop-1.0.0.jar /opt/mock-client-vop/lib/
cp scripts/mock-vop.sh /opt/mock-client-vop/bin/
cp src/main/resources/application.yml /opt/mock-client-vop/conf/
```

## Configuration

### application.yml
```yaml
server:
  port: 8443
  ssl:
    enabled: true
    client-auth: need

mock-vop:
  providers:
    base-path: /provider
    url-prefix: provider
  qwac:
    validation:
      enabled: true
      psd2-extensions-validation: true
```

### bank-responses.yml
```yaml
responses:
  default:
    status: 200
    body: |
      {"status": "success", "message": "Défaut"}
  
  providers:
    "15930":  # Natixis PSP ID
      status: 200
      body: |
        {"status": "success", "bank": "Natixis", "pspId": "PSDFR-ACPR-15930"}
```

## Génération des certificats

### Script generate_certs.sh

Le script `generate_certs.sh` automatise la création de certificats QWAC conformes PSD2:

```bash
# Créer l'AC racine (une fois)
./scripts/generate_certs.sh --create-ca-only

# Générer certificat pour une banque
./scripts/generate_certs.sh \
  --bank "Natixis" \
  --psp-id "15930" \
  --domain "api.natixis.com" \
  --password "NatixisPass123"

# Copier vers le provider
cp certificates/Natixis/natixis-qwac.p12 /provider/Natixis/
cp certificates/psd2-ac-root.cert.pem /provider/
```

**Paramètres principaux:**
- `--bank`: Nom de la banque
- `--psp-id`: ID PSP (PSDFR-ACPR-XXXXX)
- `--domain`: Domaine API
- `--password`: Mot de passe P12

## Utilisation

### Démarrage
```bash
# Démarrer
./bin/mock-vop.sh start

# Vérifier le statut
./bin/mock-vop.sh status

# Voir les logs
./bin/mock-vop.sh logs 50
```

### API
- `GET /api/status` - Validation certificat + réponse personnalisée
- `POST /api/admin/providers/reload` - Recharger certificats
- `GET /api/admin/providers/list` - Liste des providers

## Tests avec certificats

### Exemple avec Natixis (PSP ID: 15930)

```bash
# Test validation certificat + réponse personnalisée
curl -v \
  --cert-type P12 \
  --cert /provider/Natixis/natixis-qwac.p12:NatixisPass123 \
  --cacert /provider/psd2-ac-root.cert.pem \
  https://10.55.8.12:8443/api/status

# Test via URL pattern
curl -v \
  --cert-type P12 \
  --cert /provider/Natixis/natixis-qwac.p12:NatixisPass123 \
  --cacert /provider/psd2-ac-root.cert.pem \
  https://10.55.8.12:8443/api/provider/15930/accounts

# Réponse attendue:
# {
#   "status": "success",
#   "bank": "Natixis",
#   "pspId": "PSDFR-ACPR-15930",
#   "message": "Connexion établie avec Natixis",
#   "timestamp": "2024-01-15T10:30:00"
# }
```

### Test sans certificat (doit échouer)
```bash
curl -k https://10.55.8.12:8443/api/status
# Erreur: No client certificate provided
```

### Test avec certificat invalide
```bash
curl -v \
  --cert invalid-cert.crt \
  --key invalid-key.key \
  --cacert /provider/psd2-ac-root.cert.pem \
  https://10.55.8.12:8443/api/status
# Erreur: Invalid QWAC certificate
```

## Structure des providers

```
/provider/
├── Natixis/
│   ├── natixis-qwac.p12      # Certificat client P12
│   └── natixis-ca.cert.pem   # Certificat AC (optionnel)
├── BNP/
│   ├── bnp-qwac.p12
│   └── password.txt          # Mot de passe P12
└── psd2-ac-root.cert.pem     # AC racine commune
```

## Technologies

- **Spring Boot 2.7+** - Framework principal
- **Spring Security** - Authentification mTLS
- **OpenSSL** - Génération certificats
- **BouncyCastle** - Manipulation certificats X.509

## Licence

MIT License
