 # Guide de déploiement du mock-client-VOP sur un serveur

Je vais vous fournir une configuration complète pour déployer votre mock-client-VOP sur un serveur, avec les scripts shell nécessaires pour le démarrer, l'arrêter et gérer les certificats.

## 1. Structure des répertoires sur le serveur

```bash
mkdir -p /opt/mock-client-vop/{bin,config,logs,certs,lib}
```

Structure recommandée:
```
/opt/mock-client-vop/
├── bin/            # Scripts de démarrage/arrêt
├── config/         # Fichiers de configuration
├── logs/           # Logs de l'application
├── certs/          # Certificats et keystores
│   ├── truststore/ # Certificats d'AC de confiance
│   └── keystore/   # Certificats et clés du serveur
└── lib/            # JAR de l'application
```

## 2. Configuration pour lire les certificats depuis un répertoire externe

### Modification du fichier application.yml

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:/opt/mock-client-vop/certs/keystore/mock-server.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-store-type: JKS
    key-alias: server
    trust-store: file:/opt/mock-client-vop/certs/truststore/psd2-truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD:changeit}
    trust-store-type: JKS
    client-auth: need

spring:
  application:
    name: mock-client-vop
  config:
    import: optional:file:/opt/mock-client-vop/config/application-override.yml

logging:
  file:
    name: /opt/mock-client-vop/logs/mock-client-vop.log
  level:
    root: INFO
    com.example.mockclientvop: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG

# Configuration personnalisée pour le mock-client-VOP
mock-vop:
  qwac:
    validation:
      enabled: true
      certificate-chain-validation: true
      validity-period-validation: true
      psd2-extensions-validation: true
      organization-identifier-oid: "2.5.4.97"
      certs-directory: file:/opt/mock-client-vop/certs/truststore
  routing:
    enabled: true
    certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
    psp-mappings:
      "15930": "https://backend-15930.example.com"
      "default": "https://default-backend.example.com"
```

### Création d'une classe pour charger les certificats depuis un répertoire

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class CertificateLoaderConfig {

    private static final Logger logger = LoggerFactory.getLogger(CertificateLoaderConfig.class);

    @Value("${mock-vop.qwac.validation.certs-directory:file:/opt/mock-client-vop/certs/truststore}")
    private String certsDirectory;

    @Bean
    public List<X509Certificate> trustedCertificates() {
        List<X509Certificate> certificates = new ArrayList<>();
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(certsDirectory + "/*.{cer,crt,pem}");
            
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            
            for (Resource resource : resources) {
                try (FileInputStream fis = new FileInputStream(resource.getFile())) {
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
                    certificates.add(cert);
                    logger.info("Loaded trusted certificate: {}", cert.getSubjectX500Principal().getName());
                } catch (Exception e) {
                    logger.error("Failed to load certificate from {}: {}", resource.getFilename(), e.getMessage());
                }
            }
            
            logger.info("Loaded {} trusted certificates", certificates.size());
        } catch (IOException | CertificateException e) {
            logger.error("Error loading trusted certificates", e);
        }
        
        return certificates;
    }
    
    @Bean
    public KeyStore trustStore() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null); // Initialize an empty KeyStore
            
            List<X509Certificate> certs = trustedCertificates();
            for (int i = 0; i < certs.size(); i++) {
                X509Certificate cert = certs.get(i);
                String alias = "trusted-cert-" + i;
                trustStore.setCertificateEntry(alias, cert);
            }
            
            return trustStore;
        } catch (Exception e) {
            logger.error("Error creating trustStore", e);
            throw new RuntimeException("Failed to create trustStore", e);
        }
    }
}
```

## 3. Scripts shell pour démarrer/arrêter l'application

### Script de démarrage (start.sh)

```bash
#!/bin/bash

# start.sh - Script pour démarrer le mock-client-VOP

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
JAVA_OPTS="-Xms256m -Xmx512m"

# Vérifier que le répertoire existe
if [ ! -d "$INSTALL_DIR" ]; then
  echo "Répertoire d'installation $INSTALL_DIR introuvable"
  exit 1
fi

# Charger les variables d'environnement personnalisées
if [ -f "$INSTALL_DIR/config/env.sh" ]; then
  source "$INSTALL_DIR/config/env.sh"
fi

# Vérifier que le JAR existe
JAR_FILE=$(ls -t $INSTALL_DIR/lib/mock-client-vop-*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
  echo "Fichier JAR de l'application introuvable dans $INSTALL_DIR/lib/"
  exit 1
fi

# Vérifier si l'application est déjà en cours d'exécution
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"
if [ -f "$PID_FILE" ]; then
  PID=$(cat "$PID_FILE")
  if ps -p $PID > /dev/null; then
    echo "L'application est déjà en cours d'exécution avec le PID $PID"
    exit 0
  else
    echo "Ancien fichier PID trouvé, mais l'application n'est pas en cours d'exécution. Suppression du fichier PID."
    rm "$PID_FILE"
  fi
fi

# Créer le répertoire de logs s'il n'existe pas
mkdir -p "$INSTALL_DIR/logs"

# Démarrer l'application
echo "Démarrage de mock-client-VOP..."
nohup java $JAVA_OPTS \
  -Dspring.config.additional-location=file:$INSTALL_DIR/config/ \
  -Dlogging.file.name=$INSTALL_DIR/logs/mock-client-vop.log \
  -jar "$JAR_FILE" > "$INSTALL_DIR/logs/startup.log" 2>&1 &

# Sauvegarder le PID
echo $! > "$PID_FILE"
echo "Application démarrée avec le PID $(cat $PID_FILE)"

# Vérifier que l'application a bien démarré
sleep 5
if ps -p $(cat "$PID_FILE") > /dev/null; then
  echo "L'application a démarré avec succès"
  echo "Les logs sont disponibles dans $INSTALL_DIR/logs/mock-client-vop.log"
  exit 0
else
  echo "L'application n'a pas démarré correctement. Consultez les logs pour plus d'informations."
  echo "Contenu du fichier startup.log:"
  cat "$INSTALL_DIR/logs/startup.log"
  exit 1
fi
```

### Script d'arrêt (stop.sh)

```bash
#!/bin/bash

# stop.sh - Script pour arrêter le mock-client-VOP

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"

# Vérifier si le fichier PID existe
if [ ! -f "$PID_FILE" ]; then
  echo "Fichier PID introuvable. L'application ne semble pas être en cours d'exécution."
  exit 0
fi

# Lire le PID
PID=$(cat "$PID_FILE")

# Vérifier si le processus existe
if ! ps -p $PID > /dev/null; then
  echo "Processus avec PID $PID introuvable. L'application ne semble pas être en cours d'exécution."
  rm "$PID_FILE"
  exit 0
fi

# Arrêt gracieux
echo "Arrêt gracieux de l'application avec le PID $PID..."
kill $PID

# Attendre que le processus se termine (max 30 secondes)
TIMEOUT=30
for i in $(seq 1 $TIMEOUT); do
  if ! ps -p $PID > /dev/null; then
    break
  fi
  echo "En attente de l'arrêt de l'application... ($i/$TIMEOUT)"
  sleep 1
done

# Vérifier si le processus s'est arrêté
if ps -p $PID > /dev/null; then
  echo "L'application ne s'est pas arrêtée après $TIMEOUT secondes. Force l'arrêt."
  kill -9 $PID
  sleep 1
fi

# Supprimer le fichier PID
rm "$PID_FILE"
echo "L'application a été arrêtée."
```

### Script de statut (status.sh)

```bash
#!/bin/bash

# status.sh - Script pour vérifier le statut du mock-client-VOP

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"

# Vérifier si le fichier PID existe
if [ ! -f "$PID_FILE" ]; then
  echo "Statut: ARRÊTÉ (Fichier PID introuvable)"
  exit 1
fi

# Lire le PID
PID=$(cat "$PID_FILE")

# Vérifier si le processus existe
if ps -p $PID > /dev/null; then
  echo "Statut: EN COURS D'EXÉCUTION (PID: $PID)"
  
  # Afficher quelques informations supplémentaires
  UPTIME=$(ps -o etime= -p $PID)
  MEM=$(ps -o rss= -p $PID)
  MEM_MB=$(echo "scale=2; $MEM/1024" | bc)
  
  echo "Temps d'exécution: $UPTIME"
  echo "Mémoire utilisée: $MEM_MB MB"
  
  # Vérifier si le serveur répond
  if command -v curl &> /dev/null; then
    echo "Test de connexion au serveur..."
    curl -k -s -o /dev/null -w "Code de statut HTTP: %{http_code}\n" https://localhost:8443/api/status || echo "Impossible de se connecter au serveur"
  fi
  
  exit 0
else
  echo "Statut: DÉFAILLANT (PID $PID n'existe pas, mais le fichier PID existe)"
  rm "$PID_FILE"
  exit 1
fi
```

### Script de gestion des certificats (cert-manager.sh)

```bash
#!/bin/bash

# cert-manager.sh - Script pour gérer les certificats du mock-client-VOP

INSTALL_DIR="/opt/mock-client-vop"
CERTS_DIR="$INSTALL_DIR/certs"
TRUSTSTORE_DIR="$CERTS_DIR/truststore"
KEYSTORE_DIR="$CERTS_DIR/keystore"
TRUSTSTORE_FILE="$TRUSTSTORE_DIR/psd2-truststore.jks"
TRUSTSTORE_PASSWORD="changeit"
TEMP_DIR="/tmp/cert-manager-$$"

# Fonction d'aide
usage() {
  echo "Usage: $0 [OPTION]"
  echo "Gestion des certificats pour mock-client-VOP"
  echo ""
  echo "Options:"
  echo "  list                    Liste tous les certificats dans le truststore"
  echo "  import-ca FILE ALIAS    Importe un certificat d'AC dans le truststore"
  echo "  delete-ca ALIAS         Supprime un certificat d'AC du truststore"
  echo "  import-server-cert FILE Importe un certificat serveur dans le keystore"
  echo "  validate-cert FILE      Valide un certificat QWAC avec le truststore"
  echo "  create-keystore FILE    Crée un nouveau keystore serveur à partir d'un PKCS12"
  echo ""
  exit 1
}

# Vérification des répertoires
mkdir -p "$TRUSTSTORE_DIR" "$KEYSTORE_DIR"

# Traiter les arguments
if [ $# -lt 1 ]; then
  usage
fi

# Fonction pour lister les certificats
list_certs() {
  echo "Certificats dans le truststore:"
  if [ -f "$TRUSTSTORE_FILE" ]; then
    keytool -list -v -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD"
  else
    echo "Le fichier truststore n'existe pas: $TRUSTSTORE_FILE"
    exit 1
  fi
}

# Fonction pour importer un certificat d'AC
import_ca() {
  if [ $# -ne 2 ]; then
    echo "Erreur: Spécifiez le fichier de certificat et l'alias"
    usage
  fi
  
  CERT_FILE="$1"
  ALIAS="$2"
  
  if [ ! -f "$CERT_FILE" ]; then
    echo "Erreur: Le fichier de certificat n'existe pas: $CERT_FILE"
    exit 1
  fi
  
  # Créer le truststore s'il n'existe pas
  if [ ! -f "$TRUSTSTORE_FILE" ]; then
    echo "Création d'un nouveau truststore..."
    keytool -genkeypair -alias dummy -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD" \
            -keypass "$TRUSTSTORE_PASSWORD" -dname "CN=dummy" -keyalg RSA
    keytool -delete -alias dummy -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD"
  fi
  
  # Importer le certificat
  echo "Importation du certificat $CERT_FILE avec l'alias $ALIAS..."
  keytool -importcert -file "$CERT_FILE" -alias "$ALIAS" -keystore "$TRUSTSTORE_FILE" \
          -storepass "$TRUSTSTORE_PASSWORD" -noprompt
  
  # Copier également le fichier dans le répertoire des certificats
  cp "$CERT_FILE" "$TRUSTSTORE_DIR/"
  
  echo "Certificat importé avec succès"
}

# Fonction pour supprimer un certificat d'AC
delete_ca() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez l'alias du certificat à supprimer"
    usage
  fi
  
  ALIAS="$1"
  
  if [ ! -f "$TRUSTSTORE_FILE" ]; then
    echo "Erreur: Le fichier truststore n'existe pas: $TRUSTSTORE_FILE"
    exit 1
  fi
  
  # Supprimer le certificat
  echo "Suppression du certificat avec l'alias $ALIAS..."
  keytool -delete -alias "$ALIAS" -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD"
  
  echo "Certificat supprimé avec succès"
}

# Fonction pour importer un certificat serveur
import_server_cert() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez le fichier PKCS12 du certificat serveur"
    usage
  fi
  
  PKCS12_FILE="$1"
  
  if [ ! -f "$PKCS12_FILE" ]; then
    echo "Erreur: Le fichier PKCS12 n'existe pas: $PKCS12_FILE"
    exit 1
  fi
  
  # Demander le mot de passe du PKCS12
  read -sp "Entrez le mot de passe du fichier PKCS12: " PKCS12_PASSWORD
  echo ""
  
  # Demander le mot de passe du keystore
  read -sp "Entrez le mot de passe pour le nouveau keystore serveur: " KEYSTORE_PASSWORD
  echo ""
  
  # Nom du fichier keystore
  KEYSTORE_FILE="$KEYSTORE_DIR/mock-server.jks"
  
  # Convertir le PKCS12 en JKS
  echo "Conversion du PKCS12 en JKS..."
  keytool -importkeystore -srckeystore "$PKCS12_FILE" -srcstoretype PKCS12 -srcstorepass "$PKCS12_PASSWORD" \
          -destkeystore "$KEYSTORE_FILE" -deststoretype JKS -deststorepass "$KEYSTORE_PASSWORD"
  
  echo "Certificat serveur importé avec succès dans $KEYSTORE_FILE"
  
  # Mettre à jour le fichier de configuration avec le nouveau mot de passe
  ENV_FILE="$INSTALL_DIR/config/env.sh"
  touch "$ENV_FILE"
  
  # Remplacer ou ajouter la ligne SSL_KEYSTORE_PASSWORD
  if grep -q "SSL_KEYSTORE_PASSWORD" "$ENV_FILE"; then
    sed -i "s/export SSL_KEYSTORE_PASSWORD=.*/export SSL_KEYSTORE_PASSWORD=\"$KEYSTORE_PASSWORD\"/" "$ENV_FILE"
  else
    echo "export SSL_KEYSTORE_PASSWORD=\"$KEYSTORE_PASSWORD\"" >> "$ENV_FILE"
  fi
  
  echo "Mot de passe du keystore mis à jour dans $ENV_FILE"
}

# Fonction pour valider un certificat QWAC
validate_cert() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez le fichier de certificat QWAC à valider"
    usage
  fi
  
  QWAC_FILE="$1"
  
  if [ ! -f "$QWAC_FILE" ]; then
    echo "Erreur: Le fichier de certificat n'existe pas: $QWAC_FILE"
    exit 1
  fi
  
  if [ ! -f "$TRUSTSTORE_FILE" ]; then
    echo "Erreur: Le fichier truststore n'existe pas: $TRUSTSTORE_FILE"
    exit 1
  fi
  
  # Extraire les certificats individuels du truststore
  mkdir -p "$TEMP_DIR"
  
  # Lister les alias dans le truststore
  aliases=$(keytool -list -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD" | grep "trustedCertEntry" | awk '{print $1}')
  
  # Exporter chaque certificat
  for alias in $aliases; do
    keytool -exportcert -alias "$alias" -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD" -rfc -file "$TEMP_DIR/$alias.pem"
  done
  
  # Créer un fichier CAfile contenant tous les certificats d'AC
  cat "$TEMP_DIR"/*.pem > "$TEMP_DIR/all_ca.pem"
  
  # Valider le certificat QWAC
  echo "Validation du certificat QWAC..."
  openssl verify -CAfile "$TEMP_DIR/all_ca.pem" "$QWAC_FILE"
  
  # Afficher les informations du certificat
  echo ""
  echo "Informations sur le certificat QWAC:"
  openssl x509 -in "$QWAC_FILE" -text -noout | grep -E "Subject:|Issuer:|Validity|PSD2|2.5.4.97"
  
  # Nettoyer
  rm -rf "$TEMP_DIR"
}

# Fonction pour créer un nouveau keystore
create_keystore() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez le fichier PKCS12 contenant la clé et le certificat"
    usage
  fi
  
  PKCS12_FILE="$1"
  
  if [ ! -f "$PKCS12_FILE" ]; then
    echo "Erreur: Le fichier PKCS12 n'existe pas: $PKCS12_FILE"
    exit 1
  fi
  
  # Demander les mots de passe
  read -sp "Entrez le mot de passe du fichier PKCS12: " PKCS12_PASSWORD
  echo ""
  read -sp "Entrez le mot de passe pour le nouveau keystore: " NEW_KEYSTORE_PASSWORD
  echo ""
  
  # Nom du nouveau keystore
  KEYSTORE_FILE="$KEYSTORE_DIR/mock-server.jks"
  
  # Créer le keystore
  echo "Création d'un nouveau keystore..."
  keytool -importkeystore -srckeystore "$PKCS12_FILE" -srcstoretype PKCS12 -srcstorepass "$PKCS12_PASSWORD" \
          -destkeystore "$KEYSTORE_FILE" -deststoretype JKS -deststorepass "$NEW_KEYSTORE_PASSWORD"
  
  # Mettre à jour le fichier de configuration
  ENV_FILE="$INSTALL_DIR/config/env.sh"
  touch "$ENV_FILE"
  
  # Remplacer ou ajouter la ligne SSL_KEYSTORE_PASSWORD
  if grep -q "SSL_KEYSTORE_PASSWORD" "$ENV_FILE"; then
    sed -i "s/export SSL_KEYSTORE_PASSWORD=.*/export SSL_KEYSTORE_PASSWORD=\"$NEW_KEYSTORE_PASSWORD\"/" "$ENV_FILE"
  else
    echo "export SSL_KEYSTORE_PASSWORD=\"$NEW_KEYSTORE_PASSWORD\"" >> "$ENV_FILE"
  fi
  
  echo "Nouveau keystore créé: $KEYSTORE_FILE"
  echo "Mot de passe du keystore mis à jour dans $ENV_FILE"
}

# Exécuter la commande demandée
case "$1" in
  "list")
    list_certs
    ;;
  "import-ca")
    import_ca "$2" "$3"
    ;;
  "delete-ca")
    delete_ca "$2"
    ;;
  "import-server-cert")
    import_server_cert "$2"
    ;;
  "validate-cert")
    validate_cert "$2"
    ;;
  "create-keystore")
    create_keystore "$2"
    ;;
  *)
    echo "Option non reconnue: $1"
    usage
    ;;
esac

exit 0
```

## 4. Script d'installation complet

```bash
#!/bin/bash

# install.sh - Script d'installation du mock-client-VOP

set -e

# Répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
CURRENT_DIR=$(pwd)
JAR_FILE="mock-client-vop.jar"

# Vérifier les dépendances
for cmd in java keytool openssl; do
  if ! command -v $cmd &> /dev/null; then
    echo "Erreur: $cmd n'est pas installé"
    exit 1
  fi
done

# Vérifier que le JAR existe
if [ ! -f "$JAR_FILE" ]; then
  echo "Erreur: $JAR_FILE n'a pas été trouvé dans le répertoire courant"
  exit 1
fi

# Créer les répertoires
echo "Création des répertoires d'installation..."
mkdir -p "$INSTALL_DIR"/{bin,config,logs,certs/{truststore,keystore},lib}

# Copier le JAR
echo "Copie du fichier JAR de l'application..."
cp "$JAR_FILE" "$INSTALL_DIR/lib/"

# Copier les scripts
echo "Création des scripts de gestion..."
cat > "$INSTALL_DIR/bin/start.sh" << 'EOF'
#!/bin/bash
# start.sh - Script pour démarrer le mock-client-VOP
# [Contenu du script start.sh]
EOF

cat > "$INSTALL_DIR/bin/stop.sh" << 'EOF'
#!/bin/bash
# stop.sh - Script pour arrêter le mock-client-VOP
# [Contenu du script stop.sh]
EOF

cat > "$INSTALL_DIR/bin/status.sh" << 'EOF'
#!/bin/bash
# status.sh - Script pour vérifier le statut du mock-client-VOP
# [Contenu du script status.sh]
EOF

cat > "$INSTALL_DIR/bin/cert-manager.sh" << 'EOF'
#!/bin/bash
