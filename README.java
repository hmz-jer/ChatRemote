#!/bin/bash

# Script de création de l'arborescence pour IBCPROXY
# ----------------------------------------------

# Vérification des arguments
if [ $# -lt 1 ]; then
    echo "Usage: $0 <chemin_installation>"
    echo "Exemple: $0 /opt/ibcproxy"
    exit 1
fi

# Chemin d'installation
INSTALL_PATH="$1"
echo "Création de l'arborescence IBCPROXY dans: $INSTALL_PATH"

# Création de l'arborescence principale
mkdir -p "$INSTALL_PATH"/{bin,conf,etc,logs,script}

echo "Structure principale créée."

# Création du fichier jbcproxy.cfg
cat > "$INSTALL_PATH/conf/jbcproxy.cfg" << 'EOF'
# Configuration pour l'application IBCPROXY
# -----------------------------------

# Nom de l'application
APP_NAME="ibcproxy"

# Chemin vers le JDK
JAVA_HOME="/usr/lib/jvm/java-17"

# Paramètres de mémoire JVM
JAVA_OPTS="-Xms256m -Xmx512m"

# Chemin pour stocker le PID de l'application
PID_FILE="ibcproxy.pid"

# Chemins des répertoires et fichiers
LOG_PATH="logs"

# Paramètres spécifiques à l'application
DEBUG_MODE="false"

# Timeout (en secondes) pour l'arrêt gracieux de l'application
STOP_TIMEOUT=30
EOF

echo "Fichier jbcproxy.cfg créé."

# Création du fichier logback.xml
cat > "$INSTALL_PATH/conf/logback.xml" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Utilise la variable LOG_PATH définie lors du démarrage de l'application -->
    <property name="LOG_FILE" value="${LOG_PATH}/ibcproxy.log"/>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- Rotation quotidienne des logs -->
            <fileNamePattern>${LOG_PATH}/archived/ibcproxy.%d{yyyy-MM-dd}.log</fileNamePattern>
            <!-- Conservation des logs pendant 30 jours -->
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
    </appender>
    
    <!-- Définition des niveaux de log -->
    <logger name="org.springframework" level="INFO"/>
    <logger name="com.votre.package" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
EOF

echo "Fichier logback.xml créé."

# Création du fichier application.yml
cat > "$INSTALL_PATH/etc/application.yml" << 'EOF'
spring:
  application:
    name: ibcproxy
  
  # Configuration Kafka
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: ibcproxy-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

# Configuration du serveur
server:
  port: 8080
  servlet:
    context-path: /ibcproxy

# Configuration de logging (complémentaire à logback.xml)
logging:
  level:
    root: INFO
    com.votre.package: DEBUG
    org.springframework: INFO

# Configuration de l'API Gateway
apigateway:
  url: http://localhost:8090
  connect-timeout: 5000
  read-timeout: 10000
  max-retries: 3
EOF

echo "Fichier application.yml créé."

# Création du fichier manage.sh
cat > "$INSTALL_PATH/script/manage.sh" << 'EOF'
#!/bin/bash

# Chemin du répertoire de l'application
APP_DIR="$(dirname $(dirname $(realpath $0)))"

# Chargement de la configuration
CONFIG_FILE="$APP_DIR/conf/jbcproxy.cfg"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
else
    echo "ERREUR: Fichier de configuration non trouvé: $CONFIG_FILE"
    exit 1
fi

# Définition des chemins basés sur la configuration
JAR_PATH="$APP_DIR/bin/${APP_NAME}.jar"
CONFIG_PATH="$APP_DIR/etc/application.yml"
LOGBACK_PATH="$APP_DIR/conf/logback.xml"
LOG_PATH="$APP_DIR/${LOG_PATH}"
PID_FILE="$APP_DIR/${PID_FILE}"

# Utilisation du JAVA_HOME depuis la configuration
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD="java"
fi

# Vérification de l'existence des dossiers et fichiers nécessaires
check_prerequisites() {
    if [ ! -f "$JAR_PATH" ]; then
        echo "ERREUR: Le fichier JAR n'existe pas: $JAR_PATH"
        exit 1
    fi
    
    if [ ! -f "$CONFIG_PATH" ]; then
        echo "ERREUR: Le fichier de configuration n'existe pas: $CONFIG_PATH"
        exit 1
    fi
    
    if [ ! -f "$LOGBACK_PATH" ]; then
        echo "ERREUR: Le fichier logback n'existe pas: $LOGBACK_PATH"
        exit 1
    fi
    
    # Création du dossier logs s'il n'existe pas
    if [ ! -d "$LOG_PATH" ]; then
        mkdir -p "$LOG_PATH"
    fi
    
    # Création du dossier archives s'il n'existe pas
    if [ ! -d "$LOG_PATH/archived" ]; then
        mkdir -p "$LOG_PATH/archived"
    fi
}

# Fonction pour démarrer l'application
start_app() {
    check_prerequisites
    
    if [ -f "$PID_FILE" ] && ps -p $(cat "$PID_FILE") > /dev/null; then
        echo "L'application est déjà en cours d'exécution avec PID $(cat "$PID_FILE")"
        return 1
    fi
    
    echo "Démarrage de l'application..."
    
    nohup $JAVA_CMD $JAVA_OPTS -jar \
        -Dspring.config.location=file:$CONFIG_PATH \
        -Dlogging.config=file:$LOGBACK_PATH \
        -DLOG_PATH=$LOG_PATH \
        $JAR_PATH > $LOG_PATH/console.log 2>&1 &
    
    echo $! > "$PID_FILE"
    echo "Application démarrée avec PID $(cat "$PID_FILE")"
}

# Fonction pour arrêter l'application
stop_app() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null; then
            echo "Arrêt de l'application avec PID $PID..."
            kill $PID
            
            # Attendre que le processus se termine (utilise STOP_TIMEOUT depuis la config)
            for i in $(seq 1 $STOP_TIMEOUT); do
                if ! ps -p $PID > /dev/null; then
                    break
                fi
                sleep 1
            done
            
            # Si le processus est toujours en cours d'exécution, force kill
            if ps -p $PID > /dev/null; then
                echo "L'application ne répond pas, force kill..."
                kill -9 $PID
            fi
            
            rm "$PID_FILE"
            echo "Application arrêtée."
        else
            echo "L'application n'est pas en cours d'exécution (PID $PID non trouvé)."
            rm "$PID_FILE"
        fi
    else
        echo "L'application n'est pas en cours d'exécution (fichier PID non trouvé)."
    fi
}

# Fonction pour vérifier le statut de l'application
status_app() {
    if [ -f "$PID_FILE" ] && ps -p $(cat "$PID_FILE") > /dev/null; then
        echo "L'application est en cours d'exécution avec PID $(cat "$PID_FILE")"
    else
        echo "L'application n'est pas en cours d'exécution."
        if [ -f "$PID_FILE" ]; then
            rm "$PID_FILE"
        fi
    fi
}

# Fonction d'affichage de l'aide
show_help() {
    echo "Utilisation: $0 {start|stop|restart|status}"
    echo "  start   : Démarrer l'application"
    echo "  stop    : Arrêter l'application"
    echo "  restart : Redémarrer l'application"
    echo "  status  : Vérifier le statut de l'application"
}

# Traitement des commandes
case "$1" in
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        stop_app
        sleep 2
        start_app
        ;;
    status)
        status_app
        ;;
    *)
        show_help
        exit 1
        ;;
esac

exit 0
EOF

# Rendre le script manage.sh exécutable
chmod +x "$INSTALL_PATH/script/manage.sh"

echo "Fichier manage.sh créé et rendu exécutable."

# Créer un fichier placeholder pour le jar
touch "$INSTALL_PATH/bin/ibcproxy.jar"
echo "Placeholder pour le fichier JAR créé. Remplacez-le par le vrai JAR."

echo ""
echo "========================================================"
echo "Structure IBCPROXY créée avec succès dans $INSTALL_PATH"
echo "N'oubliez pas de remplacer le fichier JAR placeholder dans bin/"
echo "avec votre JAR réel avant de démarrer l'application."
echo "========================================================"
