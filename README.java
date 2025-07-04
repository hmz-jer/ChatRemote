#!/bin/bash

###############################################
# Script de génération de certificats QWAC pour Mock VOP
# Usage: ./generate_certificates.sh
###############################################

# Définir le répertoire de base fixe
BASE_DIR="/opt/mock-client-vop"

# Configuration globale
CA_NAME="Generic-VOP-CA"
CA_DAYS=3650
CERT_DAYS=365
COUNTRY="FR"
STATE="Ile-de-France"
LOCALITY="Paris"
CA_ORG="VOP Mock CA"
SERVER_KEYSTORE_PASSWORD="serverpass"
TRUSTSTORE_PASSWORD="trustpass"

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Fonction d'affichage
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Vérifier que OpenSSL et keytool sont installés
check_prerequisites() {
    log_info "Vérification des prérequis..."
    
    if ! command -v openssl &> /dev/null; then
        log_error "OpenSSL n'est pas installé"
        exit 1
    fi
    
    if ! command -v keytool &> /dev/null; then
        log_error "Keytool (JDK) n'est pas installé"
        exit 1
    fi
    
    log_info "Prérequis OK"
}

# Créer la structure des répertoires
create_directories() {
    log_info "Création de la structure des répertoires..."
    
    mkdir -p "$BASE_DIR/certs/ca"
    mkdir -p "$BASE_DIR/certs/server"
    mkdir -p "$BASE_DIR/certs/providers"
    
    log_info "Structure créée"
}

# Générer la CA racine
generate_root_ca() {
    log_info "Génération de la CA racine..."
    
    cd "$BASE_DIR/certs/ca"
    
    # Clé privée de la CA
    openssl genrsa -out generic-ca.key 4096
    
    # Certificat auto-signé de la CA
    openssl req -x509 -new -nodes -key generic-ca.key -sha256 -days $CA_DAYS \
        -out generic-ca.crt \
        -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$CA_ORG/CN=Generic VOP Root CA"
    
    # Convertir en PEM (c'est déjà le cas, mais pour être sûr)
    cp generic-ca.crt generic-ca.pem
    
    log_info "CA racine générée"
    cd "$BASE_DIR"
}

# Générer la CA intermédiaire PSD2
generate_intermediate_ca() {
    log_info "Génération de la CA intermédiaire PSD2..."
    
    cd "$BASE_DIR/certs/ca"
    
    # Clé privée de la CA intermédiaire
    openssl genrsa -out psd2-intermediate-ca.key 4096
    
    # CSR pour la CA intermédiaire
    openssl req -new -key psd2-intermediate-ca.key \
        -out psd2-intermediate-ca.csr \
        -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$CA_ORG/CN=PSD2 Intermediate CA"
    
    # Signer avec la CA racine
    openssl x509 -req -in psd2-intermediate-ca.csr \
        -CA generic-ca.crt -CAkey generic-ca.key \
        -CAcreateserial -out psd2-intermediate-ca.crt \
        -days $CA_DAYS -sha256 \
        -extfile <(echo "basicConstraints=CA:TRUE")
    
    # Créer la chaîne de certificats
    cat psd2-intermediate-ca.crt generic-ca.crt > ca-chain.pem
    
    log_info "CA intermédiaire PSD2 générée"
    cd "$BASE_DIR"
}

# Générer le certificat du serveur Mock VOP
generate_server_certificate() {
    log_info "Génération du certificat serveur..."
    
    cd "$BASE_DIR/certs/server"
    
    # Clé privée du serveur
    openssl genrsa -out server.key 2048
    
    # Configuration pour le certificat serveur
    cat > server.ext <<EOF
basicConstraints = CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = localhost
DNS.2 = mock-vop.local
DNS.3 = *.mock-vop.local
IP.1 = 127.0.0.1
IP.2 = ::1
EOF
    
    # CSR du serveur
    openssl req -new -key server.key \
        -out server.csr \
        -subj "/C=$COUNTRY/ST=$STATE/L=$LOCALITY/O=$CA_ORG/CN=Mock VOP/CN=Mock VOP Server"
    
    # Signer le certificat serveur
    openssl x509 -req -in server.csr \
        -CA "$BASE_DIR/certs/ca/psd2-intermediate-ca.crt" \
        -CAkey "$BASE_DIR/certs/ca/psd2-intermediate-ca.key" \
        -CAcreateserial \
        -out server.crt \
        -days $CERT_DAYS \
        -extfile server.ext
    
    # Créer le keystore pour le serveur
    openssl pkcs12 -export \
        -out server.p12 \
        -inkey server.key \
        -in server.crt \
        -certfile "$BASE_DIR/certs/ca/ca-chain.pem" \
        -password pass:$SERVER_KEYSTORE_PASSWORD \
        -name "server"
    
    keytool -importkeystore \
        -srckeystore server.p12 \
        -srcstoretype PKCS12 \
        -srckeypass $SERVER_KEYSTORE_PASSWORD \
        -destkeystore server-keystore.jks \
        -deststoretype JKS \
        -deststorepass $SERVER_KEYSTORE_PASSWORD \
        -noprompt
    
    log_info "Certificat serveur généré"
    cd "$BASE_DIR"
}

# Créer le truststore pour le serveur
create_server_truststore() {
    log_info "Création du truststore serveur..."
    
    cd "$BASE_DIR/certs/server"
    
    # Importer la CA racine
    keytool -import -file "$BASE_DIR/certs/ca/generic-ca.crt" \
        -alias generic-ca \
        -keystore server-truststore.jks \
        -storepass $TRUSTSTORE_PASSWORD \
        -noprompt
    
    # Importer la CA intermédiaire
    keytool -import -file "$BASE_DIR/certs/ca/psd2-intermediate-ca.crt" \
        -alias psd2-ca \
        -keystore server-truststore.jks \
        -storepass $TRUSTSTORE_PASSWORD \
        -noprompt
    
    log_info "Truststore serveur créé"
    cd "$BASE_DIR"
}

# Fonction pour générer un certificat QWAC pour une banque
generate_bank_qwac() {
    local BANK_CODE=$1
    local BANK_NAME=$2
    local ORG_IDENTIFIER=$3
    local ORGANIZATION=$4
    local SERVER_IP=$5
    
    log_info "Génération du certificat QWAC pour $BANK_NAME..."
    
    # Créer le répertoire de la banque
    mkdir -p "$BASE_DIR/certs/providers/$BANK_CODE"
    cd "$BASE_DIR/certs/providers/$BANK_CODE"
    
    # Clé privée
    openssl genrsa -out ${BANK_CODE}-private.key 2048
    
    # Fichier de configuration pour les extensions QWAC
    cat > ${BANK_CODE}-qwac.ext <<EOF
basicConstraints = critical,CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

# Extensions QWAC PSD2
1.3.6.1.4.1.311.60.2.1.3 = ASN1:PRINTABLESTRING:FR
2.5.4.15 = ASN1:PRINTABLESTRING:Private Organization

[alt_names]
DNS.1 = ${BANK_CODE}.com
DNS.2 = www.${BANK_CODE}.com
DNS.3 = api.${BANK_CODE}.com
DNS.4 = localhost
IP.1 = 127.0.0.1
IP.2 = $SERVER_IP
EOF
    
    # CSR
    openssl req -new -key ${BANK_CODE}-private.key \
        -out ${BANK_CODE}.csr \
        -subj "/C=FR/ST=Ile-de-France/L=Paris/O=$ORGANIZATION/OU=Payment Services/CN=$BANK_NAME API/serialNumber=$ORG_IDENTIFIER/2.5.4.97=$ORG_IDENTIFIER"
    
    # Signer le certificat
    openssl x509 -req -in ${BANK_CODE}.csr \
        -CA "$BASE_DIR/certs/ca/psd2-intermediate-ca.crt" \
        -CAkey "$BASE_DIR/certs/ca/psd2-intermediate-ca.key" \
        -CAcreateserial \
        -out ${BANK_CODE}-qwac.crt \
        -days $CERT_DAYS \
        -extfile ${BANK_CODE}-qwac.ext
    
    # Créer la chaîne complète
    cat ${BANK_CODE}-qwac.crt "$BASE_DIR/certs/ca/generic-ca.crt" > ${BANK_CODE}-chain.crt
    
    # Exporter en P12 pour le client
    openssl pkcs12 -export \
        -out ${BANK_CODE}-client.p12 \
        -inkey ${BANK_CODE}-private.key \
        -in ${BANK_CODE}-qwac.crt \
        -certfile "$BASE_DIR/certs/ca/ca-chain.pem" \
        -password pass:password \
        -name "${BANK_CODE}"
    
    # Copier la CA au format PEM pour le client
    cp "$BASE_DIR/certs/ca/generic-ca.pem" ${BANK_CODE}-ca.pem
    
    # Créer un fichier README pour cette banque
    cat > README.md <<EOF
# Certificats pour $BANK_NAME

## Fichiers générés :
- \`${BANK_CODE}-client.p12\` : Certificat client (mot de passe: password)
- \`${BANK_CODE}-ca.pem\` : CA pour valider le serveur
- \`${BANK_CODE}-qwac.crt\` : Certificat QWAC au format PEM
- \`${BANK_CODE}-private.key\` : Clé privée (à protéger !)

## Configuration Postman :
1. Certificat: \`${BANK_CODE}-client.p12\`
2. Password: \`password\`
3. CA Certificate: \`${BANK_CODE}-ca.pem\`

## Test avec cURL :
\`\`\`bash
curl -v --cacert ${BANK_CODE}-ca.pem \\
    --cert ${BANK_CODE}-client.p12:password \\
    --cert-type P12 \\
    https://localhost:8443/api/provider/${ORG_IDENTIFIER##*-}/accounts
\`\`\`
EOF
    
    log_info "Certificat QWAC généré pour $BANK_NAME"
    cd "$BASE_DIR"
}

# Menu principal
main_menu() {
    while true; do
        echo
        echo "=== Générateur de certificats Mock VOP ==="
        echo "1. Initialisation complète (CA + Serveur)"
        echo "2. Ajouter une nouvelle banque"
        echo "3. Regénérer le certificat serveur"
        echo "4. Lister les banques configurées"
        echo "5. Quitter"
        echo
        read -p "Votre choix: " choice
        
        case $choice in
            1)
                initialize_all
                ;;
            2)
                add_new_bank
                ;;
            3)
                regenerate_server
                ;;
            4)
                list_banks
                ;;
            5)
                exit 0
                ;;
        esac
    done
}

# Initialisation complète
initialize_all() {
    log_info "Initialisation complète..."
    
    check_prerequisites
    create_directories
    generate_root_ca
    generate_intermediate_ca
    generate_server_certificate
    create_server_truststore
    
    # Générer quelques banques par défaut
    log_info "Génération des certificats pour les banques par défaut..."
    
    read -p "Adresse IP du serveur (défaut: 127.0.0.1): " SERVER_IP
    SERVER_IP=${SERVER_IP:-127.0.0.1}
    
    generate_bank_qwac "bnp" "BNP Paribas" "PSOPR-ACPR-14328" "BNP PARIBAS" "$SERVER_IP"
    
    log_info "Initialisation terminée !"
}

# Ajouter une nouvelle banque
add_new_bank() {
    echo
    read -p "Code de la banque (ex: ca): " BANK_CODE
    read -p "Nom de la banque (ex: Crédit Agricole): " BANK_NAME
    read -p "Identifiant PSP (ex: PSOPR-ACPR-17469): " ORG_IDENTIFIER
    read -p "Nom de l'organisation (ex: CREDIT AGRICOLE SA): " ORGANIZATION
    read -p "IP du serveur (défaut: 127.0.0.1): " SERVER_IP
    SERVER_IP=${SERVER_IP:-127.0.0.1}
    
    generate_bank_qwac "$BANK_CODE" "$BANK_NAME" "$ORG_IDENTIFIER" "$ORGANIZATION" "$SERVER_IP"
}

# Régénérer le certificat serveur
regenerate_server() {
    log_warning "Régénération du certificat serveur..."
    
    read -p "Êtes-vous sûr ? (y/N): " confirm
    if [[ $confirm == "y" || $confirm == "Y" ]]; then
        generate_server_certificate
        log_info "Certificat serveur régénéré. Redémarrage du serveur nécessaire."
    fi
}

# Lister les banques configurées
list_banks() {
    echo
    echo "=== Banques configurées ==="
    
    if [ -d "$BASE_DIR/certs/providers" ]; then
        for bank_dir in "$BASE_DIR/certs/providers"/*; do
            if [ -d "$bank_dir" ]; then
                bank_code=$(basename "$bank_dir")
                echo "- $bank_code"
                
                # Afficher les détails du certificat si disponible
                if [ -f "$bank_dir/${bank_code}-qwac.crt" ]; then
                    subject=$(openssl x509 -in "$bank_dir/${bank_code}-qwac.crt" -noout -subject)
                    echo "  Subject"
                fi
            fi
        done
    else
        echo "Aucune banque configurée"
    fi
}

# Créer un fichier de résumé
cat > "$BASE_DIR/README.md" <<EOF
# Certificats Mock VOP

Ce répertoire contient tous les certificats générés pour Mock VOP.

## Structure:
- \`ca/\` : Autorités de certification
- \`server/\` : Certificats du serveur Mock VOP
- \`providers/\` : Certificats des banques (un dossier par banque)

## Utilisation:
1. Exécuter \`./generate_certificates.sh\` pour générer les certificats
2. Configurer le serveur avec les certificats générés
3. Utiliser les certificats clients pour tester les APIs

## Mots de passe:
- Serveur keystore: \`serverpass\`
- Truststore: \`trustpass\`
- Certificats clients: \`password\`
EOF

# Lancer le menu principal si le script est exécuté directement
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main_menu
fi
