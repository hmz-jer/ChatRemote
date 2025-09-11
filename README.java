#!/bin/bash

# Script de validation de certificat(s) PEM
# Usage: ./validate_cert.sh <certificat.pem>
# Supporte les fichiers avec plusieurs certificats

set -e

CERT_FILE="$1"

# Vérification des arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <certificat.pem>"
    exit 1
fi

# Vérification que openssl est disponible
if ! command -v openssl >/dev/null 2>&1; then
    echo "Erreur: OpenSSL non trouvé"
    exit 1
fi

# Vérification de l'existence du fichier
if [ ! -f "$CERT_FILE" ]; then
    echo "Erreur: Fichier non trouvé: $CERT_FILE"
    exit 1
fi

echo "Validation du fichier: $CERT_FILE"

# Compter le nombre de certificats dans le fichier
cert_count=$(grep -c "BEGIN CERTIFICATE" "$CERT_FILE" 2>/dev/null || echo "0")

if [ "$cert_count" -eq 0 ]; then
    echo "Erreur: Aucun certificat trouvé dans le fichier"
    exit 2
fi

echo "Nombre de certificats trouvés: $cert_count"
echo ""

# Fonction pour valider un certificat individuel
validate_single_cert() {
    local temp_cert="$1"
    local cert_num="$2"
    
    echo "=== Certificat #$cert_num ==="
    
    # Vérifier le format PEM
    if ! openssl x509 -in "$temp_cert" -text -noout >/dev/null 2>&1; then
        echo "Erreur: Format PEM invalide pour le certificat #$cert_num"
        return 1
    fi
    echo "Format PEM: OK"
    
    # Vérifier les dates de validité
    if ! openssl x509 -in "$temp_cert" -noout -checkend 0 >/dev/null 2>&1; then
        echo "Erreur: Certificat #$cert_num expiré"
        return 1
    fi
    echo "Validité: OK"
    
    # Afficher les informations de base
    subject=$(openssl x509 -in "$temp_cert" -noout -subject | sed 's/subject= *//')
    issuer=$(openssl x509 -in "$temp_cert" -noout -issuer | sed 's/issuer= *//')
    not_after=$(openssl x509 -in "$temp_cert" -noout -enddate | sed 's/notAfter=//')
    
    echo "Subject: $subject"
    echo "Issuer: $issuer"
    echo "Expire le: $not_after"
    
    # Vérifier si expire bientôt (30 jours)
    if ! openssl x509 -in "$temp_cert" -noout -checkend 2592000 >/dev/null 2>&1; then
        echo "Attention: Certificat #$cert_num expire dans moins de 30 jours"
    fi
    
    echo ""
    return 0
}

# Séparer et valider chaque certificat
temp_dir=$(mktemp -d)
trap 'rm -rf "$temp_dir"' EXIT

# Séparer les certificats en fichiers temporaires
awk '
/-----BEGIN CERTIFICATE-----/ { cert++; print > "'$temp_dir'/cert" cert ".pem" }
cert > 0 { print > "'$temp_dir'/cert" cert ".pem" }
/-----END CERTIFICATE-----/ { cert_active=0 }
' "$CERT_FILE"

# Valider chaque certificat
cert_num=1
exit_code=0

for cert_file in "$temp_dir"/cert*.pem; do
    if [ -f "$cert_file" ]; then
        if ! validate_single_cert "$cert_file" "$cert_num"; then
            exit_code=1
        fi
        cert_num=$((cert_num + 1))
    fi
done

if [ $exit_code -eq 0 ]; then
    echo "Validation terminée: Tous les certificats sont valides"
else
    echo "Validation terminée: Des erreurs ont été détectées"
fi

exit $exit_code
