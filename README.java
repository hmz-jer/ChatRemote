 # Configuration pour utiliser application.yml dans le dossier conf

Pour configurer votre mock-client-VOP afin qu'il utilise un fichier `application.yml` situé dans le dossier `/opt/mock-client-vop/conf/`, voici les modifications nécessaires:

## 1. Modification du script de démarrage

Modifiez le script `mock-vop.sh` pour qu'il utilise le fichier de configuration externe:

```bash
#!/bin/bash

# mock-vop.sh - Script de gestion pour le mock-client-VOP
# Usage: mock-vop.sh {start|stop|status|restart|cert-import|cert-list|cert-validate}

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
CONF_DIR="$INSTALL_DIR/conf"
JAVA_OPTS="-Xms256m -Xmx512m"
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"
JAR_FILE=$(ls -t $INSTALL_DIR/lib/mock-client-vop-*.jar 2>/dev/null | head -1)
LOG_FILE="$INSTALL_DIR/logs/mock-client-vop.log"
CERTS_DIR="$INSTALL_DIR/certs"
TRUSTSTORE_DIR="$CERTS_DIR/truststore"
KEYSTORE_DIR="$CERTS_DIR/keystore"
TRUSTSTORE_FILE="$TRUSTSTORE_DIR/truststore.jks"
KEYSTORE_FILE="$KEYSTORE_DIR/server.jks"
TRUSTSTORE_PASSWORD="changeit"

# Vérifier que le fichier de configuration existe
CONFIG_FILE="$CONF_DIR/application.yml"
if [ ! -f "$CONFIG_FILE" ]; then
  echo "Erreur: Fichier de configuration introuvable: $CONFIG_FILE"
  echo "Veuillez créer ce fichier avant de démarrer l'application."
  exit 1
fi

# Fonction pour démarrer l'application
start_app() {
  # [code existant...]

  # Charger les variables d'environnement personnalisées
  if [ -f "$CONF_DIR/env.sh" ]; then
    source "$CONF_DIR/env.sh"
  fi

  # Démarrer l'application
  echo "Démarrage de mock-client-VOP avec la configuration: $CONFIG_FILE"
  nohup java $JAVA_OPTS \
    -Dspring.config.location=file:$CONFIG_FILE \
    -Dlogging.file.name=$LOG_FILE \
    -Dmock-vop.certs-dir=$CERTS_DIR \
    -jar "$JAR_FILE" > "$INSTALL_DIR/logs/startup.log" 2>&1 &

  # [reste du code existant...]
}

# [reste du script...]
```

## 2. Création de la structure de répertoires

```bash
# Créer le répertoire de configuration
sudo mkdir -p /opt/mock-client-vop/conf
```

## 3. Création du fichier application.yml dans le dossier conf

```bash
sudo tee /opt/mock-client-vop/conf/application.yml > /dev/null << 'EOF'
server:
  port: 8443
  address: 10.55.8.12  # L'adresse IP sur laquelle votre serveur écoute
  ssl:
    enabled: true
    # Utiliser le chemin absolu vers les fichiers de certificat
    key-store: file:/opt/mock-client-vop/certs/keystore/server.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-store-type: JKS
    key-alias: server
    trust-store: file:/opt/mock-client-vop/certs/truststore/truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD:changeit}
    trust-store-type: JKS
    client-auth: need  # Rend l'authentification mutuelle obligatoire

spring:
  application:
    name: mock-client-vop
    version: 1.0.0

# Configuration personnalisée pour le mock-client-VOP
mock-vop:
  qwac:
    validation:
      enabled: true
      certificate-chain-validation: true
      validity-period-validation: true
      psd2-extensions-validation: true
      organization-identifier-oid: "2.5.4.97"
      # Dossier contenant les certificats d'AC supplémentaires
      additional-ca-certs: file:/opt/mock-client-vop/certs/truststore/ca
  routing:
    enabled: true
    certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
    psp-mappings:
      "15930": "https://backend-15930.example.com"
      "default": "https://default-backend.example.com"

logging:
  file:
    name: /opt/mock-client-vop/logs/mock-client-vop.log
  level:
    root: INFO
    com.example.mockclientvop: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
EOF
```

## 4. Création du fichier env.sh dans le dossier conf

```bash
sudo tee /opt/mock-client-vop/conf/env.sh > /dev/null << 'EOF'
#!/bin/bash

# Configuration des mots de passe pour les keystores
export SSL_KEYSTORE_PASSWORD="changeit"
export SSL_TRUSTSTORE_PASSWORD="changeit"

# Configuration additionnelle pour l'environnement Java
export JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8"
EOF

sudo chmod 600 /opt/mock-client-vop/conf/env.sh
```

## 5. Modification de l'application Java pour utiliser des propriétés externes

Ajoutez une classe de configuration dans votre code source pour supporter la configuration externe:

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import javax.annotation.PostConstruct;

@Configuration
@PropertySources({
    @PropertySource(value = "file:${spring.config.location:classpath:application.yml}", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:application.yml", ignoreResourceNotFound = true)
})
public class ExternalConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ExternalConfigLoader.class);

    @Value("${spring.config.location:classpath:application.yml}")
    private String configLocation;

    @Value("${mock-vop.certs-dir:#{null}}")
    private String certsDir;

    @PostConstruct
    public void logConfigLocation() {
        logger.info("Application démarrée avec le fichier de configuration: {}", configLocation);
        if (certsDir != null) {
            logger.info("Répertoire des certificats: {}", certsDir);
        }
    }
}
```

## 6. Ajouter une fonction pour recharger la configuration au script

```bash
# Fonction pour recharger la configuration
reload_config() {
  if [ ! -f "$PID_FILE" ]; then
    echo "L'application n'est pas en cours d'exécution. Démarrez-la d'abord."
    return 1
  fi
  
  PID=$(cat "$PID_FILE")
  if ! ps -p $PID > /dev/null; then
    echo "L'application n'est pas en cours d'exécution. Le fichier PID existe mais le processus est introuvable."
    rm "$PID_FILE"
    return 1
  fi
  
  echo "Envoi du signal de rechargement au processus $PID..."
  kill -HUP $PID
  
  echo "Configuration rechargée. Vérifiez les logs pour confirmer."
  return 0
}

# Dans le case du script, ajoutez:
case "$1" in
  # [autres cas existants]
  "reload")
    reload_config
    ;;
  # [autres cas existants]
esac
```

## 7. Modification du ApplicationStartupLogger pour afficher le chemin de configuration

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class ApplicationStartupLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupLogger.class);

    @Autowired
    private Environment env;

    @Value("${spring.config.location:classpath:application.yml}")
    private String configLocation;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String port = env.getProperty("server.port", "8443");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String protocol = env.getProperty("server.ssl.enabled", "true").equals("true") ? "https" : "http";
        
        logger.info("╔═══════════════════════════════════════════════════════════════════════════╗");
        logger.info("║                                                                          ║");
        logger.info("║                    MOCK-CLIENT-VOP DÉMARRÉ AVEC SUCCÈS                   ║");
        logger.info("║                                                                          ║");
        logger.info("╠══════════════════════════════════════════════════════════════════════════╣");
        logger.info("║ Configuration: {}", configLocation);
        
        // [reste du code existant...]
    }
}
```

## 8. Script complet pour vérifier/copier la configuration

Créez un script utilitaire pour gérer la configuration:

```bash
#!/bin/bash

# config-manager.sh - Script pour gérer la configuration du mock-client-VOP
# Usage: config-manager.sh {check|backup|restore|edit}

INSTALL_DIR="/opt/mock-client-vop"
CONF_DIR="$INSTALL_DIR/conf"
CONFIG_FILE="$CONF_DIR/application.yml"
BACKUP_DIR="$CONF_DIR/backups"

# Fonction d'aide
usage() {
  echo "Usage: $0 COMMAND"
  echo ""
  echo "Commandes:"
  echo "  check              Vérifier la configuration actuelle"
  echo "  backup [suffix]    Sauvegarder la configuration (avec suffixe optionnel)"
  echo "  restore [file]     Restaurer une configuration (depuis fichier ou dernière sauvegarde)"
  echo "  edit               Ouvrir le fichier de configuration dans l'éditeur par défaut"
  exit 1
}

# Vérifier les arguments
if [ $# -lt 1 ]; then
  usage
fi

# Créer le répertoire de sauvegarde s'il n'existe pas
mkdir -p "$BACKUP_DIR"

# Fonction pour vérifier la configuration
check_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Erreur: Fichier de configuration introuvable: $CONFIG_FILE"
    return 1
  fi
  
  echo "Fichier de configuration: $CONFIG_FILE"
  echo ""
  echo "Contenu du fichier:"
  echo "-------------------"
  cat "$CONFIG_FILE" | grep -v "password:"
  echo ""
  echo "Validation avec YQ (si disponible):"
  if command -v yq &> /dev/null; then
    yq validate "$CONFIG_FILE" && echo "✓ Configuration valide" || echo "✗ Configuration invalide"
  else
    echo "YQ non installé, impossible de valider le YAML"
  fi
}

# Fonction pour sauvegarder la configuration
backup_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Erreur: Fichier de configuration introuvable: $CONFIG_FILE"
    return 1
  fi
  
  SUFFIX=${1:-$(date +"%Y%m%d_%H%M%S")}
  BACKUP_FILE="$BACKUP_DIR/application_$SUFFIX.yml"
  
  cp "$CONFIG_FILE" "$BACKUP_FILE"
  echo "Configuration sauvegardée dans: $BACKUP_FILE"
}

# Fonction pour restaurer la configuration
restore_config() {
  RESTORE_FILE="$1"
  
  # Si aucun fichier n'est spécifié, utiliser la dernière sauvegarde
  if [ -z "$RESTORE_FILE" ]; then
    RESTORE_FILE=$(ls -t "$BACKUP_DIR"/application_*.yml 2>/dev/null | head -1)
    if [ -z "$RESTORE_FILE" ]; then
      echo "Erreur: Aucune sauvegarde disponible"
      return 1
    fi
  fi
  
  if [ ! -f "$RESTORE_FILE" ]; then
    echo "Erreur: Fichier de sauvegarde introuvable: $RESTORE_FILE"
    return 1
  fi
  
  # Sauvegarder la configuration actuelle avant de la remplacer
  if [ -f "$CONFIG_FILE" ]; then
    backup_config "before_restore"
  fi
  
  cp "$RESTORE_FILE" "$CONFIG_FILE"
  echo "Configuration restaurée depuis: $RESTORE_FILE"
}

# Fonction pour éditer la configuration
edit_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Le fichier de configuration n'existe pas, création d'un fichier vide"
    mkdir -p "$CONF_DIR"
    touch "$CONFIG_FILE"
  fi
  
  # Déterminer l'éditeur à utiliser
  EDITOR=${EDITOR:-vi}
  if command -v nano &> /dev/null; then
    EDITOR="nano"
  fi
  
  $EDITOR "$CONFIG_FILE"
}

# Traiter les commandes
case "$1" in
  "check")
    check_config
    ;;
  "backup")
    backup_config "$2"
    ;;
  "restore")
    restore_config "$2"
    ;;
  "edit")
    edit_config
    ;;
  *)
    echo "Commande non reconnue: $1"
    usage
    ;;
esac

exit 0
```

## 9. Intégration dans le script principal mock-vop.sh

Modifiez votre script principal pour inclure les nouvelles fonctionnalités:

```bash
#!/bin/bash

# mock-vop.sh - Script de gestion pour le mock-client-VOP
# Usage: mock-vop.sh {start|stop|status|restart|logs|cert-list|cert-import|cert-validate|config}

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
CONF_DIR="$INSTALL_DIR/conf"
JAVA_OPTS="-Xms256m -Xmx512m"
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"
JAR_FILE=$(ls -t $INSTALL_DIR/lib/mock-client-vop-*.jar 2>/dev/null | head -1)
LOG_FILE="$INSTALL_DIR/logs/mock-client-vop.log"
CERTS_DIR="$INSTALL_DIR/certs"
TRUSTSTORE_DIR="$CERTS_DIR/truststore"
KEYSTORE_DIR="$CERTS_DIR/keystore"
TRUSTSTORE_FILE="$TRUSTSTORE_DIR/truststore.jks"
KEYSTORE_FILE="$KEYSTORE_DIR/server.jks"
TRUSTSTORE_PASSWORD="changeit"
CONFIG_FILE="$CONF_DIR/application.yml"

# Fonction d'aide
usage() {
  echo "Usage: $0 COMMAND [options]"
  echo ""
  echo "Gestion du mock-client-VOP:"
  echo "  start                   Démarrer le mock-client-VOP"
  echo "  stop                    Arrêter le mock-client-VOP"
  echo "  status                  Afficher le statut du mock-client-VOP"
  echo "  restart                 Redémarrer le mock-client-VOP"
  echo "  logs [n]                Afficher les n dernières lignes des logs (défaut: 50)"
  echo ""
  echo "Gestion des certificats:"
  echo "  certs                   Vérifier les certificats disponibles"
  echo "  cert-list               Lister les certificats dans le truststore"
  echo "  cert-import FILE ALIAS  Importer un certificat d'AC dans le truststore"
  echo "  cert-validate FILE      Valider un certificat QWAC avec le truststore"
  echo ""
  echo "Gestion de la configuration:"
  echo "  config check            Vérifier la configuration actuelle"
  echo "  config backup [suffix]  Sauvegarder la configuration"
  echo "  config restore [file]   Restaurer une configuration"
  echo "  config edit             Éditer le fichier de configuration"
  exit 1
}

# [autres fonctions existantes...]

# Fonction pour gérer la configuration
manage_config() {
  if [ $# -lt 1 ]; then
    echo "Erreur: Action de configuration non spécifiée"
    return 1
  fi
  
  case "$1" in
    "check")
      check_config
      ;;
    "backup")
      backup_config "$2"
      ;;
    "restore")
      restore_config "$2"
      ;;
    "edit")
      edit_config
      ;;
    *)
      echo "Action de configuration non reconnue: $1"
      return 1
      ;;
  esac
}

# Fonction pour vérifier la configuration
check_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Erreur: Fichier de configuration introuvable: $CONFIG_FILE"
    return 1
  fi
  
  echo "Fichier de configuration: $CONFIG_FILE"
  echo ""
  echo "Contenu du fichier (sans mots de passe):"
  echo "-------------------"
  cat "$CONFIG_FILE" | grep -v "password:"
  echo ""
  echo "Validation avec YQ (si disponible):"
  if command -v yq &> /dev/null; then
    yq validate "$CONFIG_FILE" && echo "✓ Configuration valide" || echo "✗ Configuration invalide"
  else
    echo "YQ non installé, impossible de valider le YAML"
  fi
}

# Fonction pour sauvegarder la configuration
backup_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Erreur: Fichier de configuration introuvable: $CONFIG_FILE"
    return 1
  fi
  
  BACKUP_DIR="$CONF_DIR/backups"
  mkdir -p "$BACKUP_DIR"
  
  SUFFIX=${1:-$(date +"%Y%m%d_%H%M%S")}
  BACKUP_FILE="$BACKUP_DIR/application_$SUFFIX.yml"
  
  cp "$CONFIG_FILE" "$BACKUP_FILE"
  echo "Configuration sauvegardée dans: $BACKUP_FILE"
}

# Fonction pour restaurer la configuration
restore_config() {
  BACKUP_DIR="$CONF_DIR/backups"
  RESTORE_FILE="$1"
  
  # Si aucun fichier n'est spécifié, utiliser la dernière sauvegarde
  if [ -z "$RESTORE_FILE" ]; then
    RESTORE_FILE=$(ls -t "$BACKUP_DIR"/application_*.yml 2>/dev/null | head -1)
    if [ -z "$RESTORE_FILE" ]; then
      echo "Erreur: Aucune sauvegarde disponible"
      return 1
    fi
  fi
  
  if [ ! -f "$RESTORE_FILE" ]; then
    echo "Erreur: Fichier de sauvegarde introuvable: $RESTORE_FILE"
    return 1
  fi
  
  # Sauvegarder la configuration actuelle avant de la remplacer
  if [ -f "$CONFIG_FILE" ]; then
    backup_config "before_restore"
  fi
  
  cp "$RESTORE_FILE" "$CONFIG_FILE"
  echo "Configuration restaurée depuis: $RESTORE_FILE"
}

# Fonction pour éditer la configuration
edit_config() {
  if [ ! -f "$CONFIG_FILE" ]; then
    echo "Le fichier de configuration n'existe pas, création d'un fichier vide"
    mkdir -p "$CONF_DIR"
    touch "$CONFIG_FILE"
  fi
  
  # Déterminer l'éditeur à utiliser
  EDITOR=${EDITOR:-vi}
  if command -v nano &> /dev/null; then
    EDITOR="nano"
  fi
  
  $EDITOR "$CONFIG_FILE"
}

# Dans le case du script, ajoutez:
case "$1" in
  # [autres cas existants]
  "config")
    shift
    manage_config "$@"
    ;;
  # [autres cas existants]
esac
```

## Structure de répertoire finale

```
/opt/mock-client-vop/
├── bin/
│   └── mock-vop.sh
├── conf/               # Dossier de configuration
│   ├── application.yml # Fichier de configuration principal
│   ├── env.sh          # Variables d'environnement
│   └── backups/        # Sauvegardes de configuration
├── logs/
│   └── mock-client-vop.log
├── certs/
│   ├── keystore/
│   │   ├── server.jks
│   │   ├── server.crt
│   │   └── server.key
│   └── truststore/
│       ├── truststore.jks
│       ├── ca.crt
│       └── ca/
│           ├── ac1.crt
│           ├── ac2.crt
│           └── ac3.crt
└── lib/
    └── mock-client-vop-1.0.0.jar
```

## Installation et utilisation

1. Créez la structure de répertoires:
   ```bash
   sudo mkdir -p /opt/mock-client-vop/{bin,conf/backups,logs,certs/{keystore,truststore/ca},lib}
   ```

2. Copiez le script et rendez-le exécutable:
   ```bash
   sudo cp mock-vop.sh /opt/mock-client-vop/bin/
   sudo chmod +x /opt/mock-client-vop/bin/mock-vop.sh
   sudo ln -s /opt/mock-client-vop/bin/mock-vop.sh /usr/local/bin/mock-vop
   ```

3. Créez une configuration initiale:
   ```bash
   mock-vop config edit
   ```

4. Vérifiez la configuration:
   ```bash
   mock-vop config check
   ```

5. Vérifiez les certificats:
   ```bash
   mock-vop certs
   ```

6. Démarrez le service:
   ```bash
   mock-vop start
   ```

Avec cette configuration, votre application lira le fichier `application.yml` depuis `/opt/mock-client-vop/conf/` au démarrage, et vous pourrez facilement le modifier sans avoir à reconstruire l'application.
