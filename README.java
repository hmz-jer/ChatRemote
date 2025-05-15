  # Script unifié pour démarrer, arrêter et gérer le mock-client-VOP

Voici un script unique qui permet de démarrer, arrêter et gérer votre mock-client-VOP:

```bash
#!/bin/bash

# mock-vop.sh - Script de gestion pour le mock-client-VOP
# Usage: mock-vop.sh {start|stop|status|restart|cert-import|cert-list|cert-validate}

# Déterminer le répertoire d'installation
INSTALL_DIR="/opt/mock-client-vop"
JAVA_OPTS="-Xms256m -Xmx512m"
PID_FILE="$INSTALL_DIR/mock-client-vop.pid"
JAR_FILE=$(ls -t $INSTALL_DIR/lib/mock-client-vop-*.jar 2>/dev/null | head -1)
LOG_FILE="$INSTALL_DIR/logs/mock-client-vop.log"
CERTS_DIR="$INSTALL_DIR/certs"
TRUSTSTORE_DIR="$CERTS_DIR/truststore"
KEYSTORE_DIR="$CERTS_DIR/keystore"
TRUSTSTORE_FILE="$TRUSTSTORE_DIR/psd2-truststore.jks"
KEYSTORE_FILE="$KEYSTORE_DIR/mock-server.jks"
TRUSTSTORE_PASSWORD="changeit"

# Fonction d'aide
usage() {
  echo "Usage: $0 COMMAND [options]"
  echo ""
  echo "Commandes:"
  echo "  start                   Démarrer le mock-client-VOP"
  echo "  stop                    Arrêter le mock-client-VOP"
  echo "  status                  Afficher le statut du mock-client-VOP"
  echo "  restart                 Redémarrer le mock-client-VOP"
  echo "  logs [n]                Afficher les n dernières lignes des logs (défaut: 50)"
  echo "  cert-list               Lister les certificats dans le truststore"
  echo "  cert-import FILE ALIAS  Importer un certificat d'AC dans le truststore"
  echo "  cert-validate FILE      Valider un certificat QWAC avec le truststore"
  echo "  create-keystore FILE    Créer un nouveau keystore serveur à partir d'un PKCS12"
  exit 1
}

# Vérification des arguments
if [ $# -lt 1 ]; then
  usage
fi

# Vérification des répertoires requis
check_dirs() {
  mkdir -p "$INSTALL_DIR/logs"
  mkdir -p "$TRUSTSTORE_DIR" "$KEYSTORE_DIR"
}

# Fonction pour démarrer l'application
start_app() {
  # Vérifier si l'application est déjà en cours d'exécution
  if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null; then
      echo "L'application est déjà en cours d'exécution avec le PID $PID"
      return 0
    else
      echo "Ancien fichier PID trouvé, mais l'application n'est pas en cours d'exécution. Suppression du fichier PID."
      rm "$PID_FILE"
    fi
  fi

  # Vérifier que le JAR existe
  if [ -z "$JAR_FILE" ]; then
    echo "Fichier JAR de l'application introuvable dans $INSTALL_DIR/lib/"
    return 1
  fi

  # Charger les variables d'environnement personnalisées
  if [ -f "$INSTALL_DIR/config/env.sh" ]; then
    source "$INSTALL_DIR/config/env.sh"
  fi

  # Démarrer l'application
  echo "Démarrage de mock-client-VOP..."
  nohup java $JAVA_OPTS \
    -Dspring.config.additional-location=file:$INSTALL_DIR/config/ \
    -Dlogging.file.name=$LOG_FILE \
    -jar "$JAR_FILE" > "$INSTALL_DIR/logs/startup.log" 2>&1 &

  # Sauvegarder le PID
  echo $! > "$PID_FILE"
  echo "Application démarrée avec le PID $(cat $PID_FILE)"

  # Vérifier que l'application a bien démarré
  sleep 5
  if ps -p $(cat "$PID_FILE") > /dev/null; then
    echo "L'application a démarré avec succès"
    echo "Les logs sont disponibles dans $LOG_FILE"
    return 0
  else
    echo "L'application n'a pas démarré correctement. Consultez les logs pour plus d'informations."
    echo "Contenu du fichier startup.log:"
    cat "$INSTALL_DIR/logs/startup.log"
    return 1
  fi
}

# Fonction pour arrêter l'application
stop_app() {
  # Vérifier si le fichier PID existe
  if [ ! -f "$PID_FILE" ]; then
    echo "Fichier PID introuvable. L'application ne semble pas être en cours d'exécution."
    return 0
  fi

  # Lire le PID
  PID=$(cat "$PID_FILE")

  # Vérifier si le processus existe
  if ! ps -p $PID > /dev/null; then
    echo "Processus avec PID $PID introuvable. L'application ne semble pas être en cours d'exécution."
    rm "$PID_FILE"
    return 0
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
  return 0
}

# Fonction pour afficher le statut
show_status() {
  # Vérifier si le fichier PID existe
  if [ ! -f "$PID_FILE" ]; then
    echo "Statut: ARRÊTÉ (Fichier PID introuvable)"
    return 1
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
    
    return 0
  else
    echo "Statut: DÉFAILLANT (PID $PID n'existe pas, mais le fichier PID existe)"
    rm "$PID_FILE"
    return 1
  fi
}

# Fonction pour afficher les logs
show_logs() {
  LINES=${1:-50}
  if [ -f "$LOG_FILE" ]; then
    tail -n $LINES "$LOG_FILE"
  else
    echo "Fichier de log introuvable: $LOG_FILE"
    return 1
  fi
}

# Fonction pour lister les certificats
list_certs() {
  echo "Certificats dans le truststore:"
  if [ -f "$TRUSTSTORE_FILE" ]; then
    keytool -list -v -keystore "$TRUSTSTORE_FILE" -storepass "$TRUSTSTORE_PASSWORD"
  else
    echo "Le fichier truststore n'existe pas: $TRUSTSTORE_FILE"
    return 1
  fi
}

# Fonction pour importer un certificat d'AC
import_cert() {
  if [ $# -ne 2 ]; then
    echo "Erreur: Spécifiez le fichier de certificat et l'alias"
    return 1
  fi
  
  CERT_FILE="$1"
  ALIAS="$2"
  
  if [ ! -f "$CERT_FILE" ]; then
    echo "Erreur: Le fichier de certificat n'existe pas: $CERT_FILE"
    return 1
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

# Fonction pour valider un certificat QWAC
validate_cert() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez le fichier de certificat QWAC à valider"
    return 1
  fi
  
  QWAC_FILE="$1"
  TEMP_DIR="/tmp/cert-validate-$$"
  
  if [ ! -f "$QWAC_FILE" ]; then
    echo "Erreur: Le fichier de certificat n'existe pas: $QWAC_FILE"
    return 1
  fi
  
  if [ ! -f "$TRUSTSTORE_FILE" ]; then
    echo "Erreur: Le fichier truststore n'existe pas: $TRUSTSTORE_FILE"
    return 1
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

# Fonction pour créer un keystore
create_keystore() {
  if [ $# -ne 1 ]; then
    echo "Erreur: Spécifiez le fichier PKCS12 contenant la clé et le certificat"
    return 1
  fi
  
  PKCS12_FILE="$1"
  
  if [ ! -f "$PKCS12_FILE" ]; then
    echo "Erreur: Le fichier PKCS12 n'existe pas: $PKCS12_FILE"
    return 1
  fi
  
  # Demander les mots de passe
  read -sp "Entrez le mot de passe du fichier PKCS12: " PKCS12_PASSWORD
  echo ""
  read -sp "Entrez le mot de passe pour le nouveau keystore: " NEW_KEYSTORE_PASSWORD
  echo ""
  
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

# Vérifier les répertoires
check_dirs

# Traiter les commandes
case "$1" in
  "start")
    start_app
    ;;
  "stop")
    stop_app
    ;;
  "restart")
    stop_app
    sleep 2
    start_app
    ;;
  "status")
    show_status
    ;;
  "logs")
    show_logs "$2"
    ;;
  "cert-list")
    list_certs
    ;;
  "cert-import")
    import_cert "$2" "$3"
    ;;
  "cert-validate")
    validate_cert "$2"
    ;;
  "create-keystore")
    create_keystore "$2"
    ;;
  *)
    echo "Commande non reconnue: $1"
    usage
    ;;
esac

exit $?
```

## Instructions d'installation et d'utilisation

1. **Structure des répertoires**:
   ```bash
   sudo mkdir -p /opt/mock-client-vop/{bin,config,logs,certs/{truststore,keystore},lib}
   ```

2. **Copier le script**:
   ```bash
   sudo cp mock-vop.sh /opt/mock-client-vop/bin/
   sudo chmod +x /opt/mock-client-vop/bin/mock-vop.sh
   sudo ln -s /opt/mock-client-vop/bin/mock-vop.sh /usr/local/bin/mock-vop
   ```

3. **Copier le JAR de l'application**:
   ```bash
   sudo cp target/mock-client-vop-1.0.0.jar /opt/mock-client-vop/lib/
   ```

4. **Créer le fichier de configuration**:
   ```bash
   sudo cp src/main/resources/application.yml /opt/mock-client-vop/config/
   ```

5. **Configurer les certificats**:
   ```bash
   # Importer le certificat CA racine
   mock-vop cert-import natixis-ca.cert natixis-ca
   
   # Créer le keystore pour le serveur
   mock-vop create-keystore natixis-qwac.p12
   ```

6. **Démarrer le service**:
   ```bash
   mock-vop start
   ```

7. **Vérifier le statut**:
   ```bash
   mock-vop status
   ```

8. **Afficher les logs**:
   ```bash
   mock-vop logs 100
   ```

9. **Arrêter le service**:
   ```bash
   mock-vop stop
   ```

## Service systemd (optionnel)

Si vous préférez gérer le mock avec systemd plutôt qu'avec le script manuel, vous pouvez créer un fichier de service:

```bash
sudo tee /etc/systemd/system/mock-client-vop.service > /dev/null << 'EOF'
[Unit]
Description=Mock Client VOP for QWAC Certificate Validation
After=network.target

[Service]
Type=forking
User=root
WorkingDirectory=/opt/mock-client-vop
ExecStart=/opt/mock-client-vop/bin/mock-vop.sh start
ExecStop=/opt/mock-client-vop/bin/mock-vop.sh stop
PIDFile=/opt/mock-client-vop/mock-client-vop.pid
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable mock-client-vop
sudo systemctl start mock-client-vop
```

Avec cette configuration, vous pourrez utiliser les commandes systemd standard:

```bash
sudo systemctl start mock-client-vop
sudo systemctl stop mock-client-vop
sudo systemctl status mock-client-vop
sudo journalctl -u mock-client-vop
```

Ce script unifié vous permet de gérer tous les aspects de votre mock-client-VOP depuis un seul point d'entrée, tout en gardant le code modulaire et facile à maintenir.
