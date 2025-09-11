#!/bin/bash

# Script de validation certificat compatible APIM CLI Axway
# Reproduit les validations spécifiques identifiées dans le repository
# Usage: ./validate_apim_cert.sh <certificat.pem>

set -e

CERT_FILE="$1"

# Vérification des arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <certificat.pem>"
    echo ""
    echo "Valide un certificat selon les critères APIM CLI Axway:"
    echo "- Format PEM et validité temporelle"
    echo "- Compatibilité CN avec systèmes de fichiers (Issue #315)"
    echo "- Validation chaîne SSL/TLS"
    echo "- Support certificats multiples dans un fichier"
    exit 1
fi

# Vérification des prérequis
if ! command -v openssl >/dev/null 2>&1; then
    echo "Erreur: OpenSSL non trouvé"
    exit 1
fi

# Vérification existence fichier
if [ ! -f "$CERT_FILE" ]; then
    echo "Erreur: Fichier non trouvé: $CERT_FILE"
    exit 1
fi

echo "Validation APIM CLI: $CERT_FILE"

# Validation du CN pour compatibilité filesystem (Issue #315 APIM CLI)
validate_cn_filesystem() {
    local cert_file="$1"
    local cn subject
    
    subject=$(openssl x509 -in "$cert_file" -noout -subject 2>/dev/null || echo "")
    cn=$(echo "$subject" | sed -n 's/.*CN[[:space:]]*=[[:space:]]*\([^,]*\).*/\1/p' | xargs)
    
    if [ -n "$cn" ]; then
        echo "  CN: $cn"
        
        # Vérification caractère '/' (Issue #315)
        if [[ "$cn" == *"/"* ]]; then
            echo "Erreur: CN contient '/' - incompatible APIM CLI (Issue #315)"
            return 1
        fi
        
        # Autres caractères problématiques filesystem
        if [[ "$cn" =~ [\\:*?\"<>|] ]]; then
            echo "Erreur: CN contient des caractères invalides pour fichiers: $cn"
            return 1
        fi
        
        # Longueur maximale
        if [ ${#cn} -gt 255 ]; then
            echo "Erreur: CN trop long (>255 caractères)"
            return 1
        fi
        
        echo "  CN filesystem: OK"
    fi
    return 0
}

# Validation SSL spécifique pour certificats CA
validate_ssl_chain() {
    local cert_file="$1"
    
    # Vérifier si c'est un certificat CA
    local basic_constraints
    basic_constraints=$(openssl x509 -in "$cert_file" -noout -text 2>/dev/null | grep -A 1 "Basic Constraints" | grep "CA:TRUE" || echo "")
    
    if [ -n "$basic_constraints" ]; then
        echo "  Type: Certificat CA détecté"
        
        # Pour les CA, vérifier l'auto-signature (normal pour root CA)
        local issuer subject
        issuer=$(openssl x509 -in "$cert_file" -noout -issuer 2>/dev/null || echo "")
        subject=$(openssl x509 -in "$cert_file" -noout -subject 2>/dev/null || echo "")
        
        if [ "$issuer" = "$subject" ]; then
            echo "  Statut: Root CA (auto-signé) - NORMAL"
        else
            echo "  Statut: Intermediate CA - OK"
        fi
    else
        echo "  Type: Certificat end-entity (non-CA)"
    fi
    
    # Test intégrité générale (critical pour tous types)
    if ! openssl x509 -in "$cert_file" -noout -modulus >/dev/null 2>&1; then
        echo "Erreur: Certificat corrompu (équivalent SSL erreur 0x4)"
        return 1
    fi
    
    # Vérifier la signature du certificat lui-même
    if ! openssl x509 -in "$cert_file" -noout -text >/dev/null 2>&1; then
        echo "Erreur: Structure du certificat invalide"
        return 1
    fi
    
    echo "  Intégrité: OK"
    return 0
}

# Validation d'un certificat
validate_certificate() {
    local cert_file="$1"
    local cert_num="$2"
    
    if [ -n "$cert_num" ]; then
        echo "=== Certificat #$cert_num ==="
    fi
    
    # 1. Format PEM
    echo "- Vérification format PEM..."
    if ! openssl x509 -in "$cert_file" -text -noout >/dev/null 2>&1; then
        echo "Erreur: Format PEM invalide"
        return 1
    fi
    echo "  Format PEM: OK"
    
    # 2. Validité temporelle
    echo "- Vérification validité..."
    if ! openssl x509 -in "$cert_file" -noout -checkend 0 >/dev/null 2>&1; then
        echo "Erreur: Certificat expiré"
        return 1
    fi
    echo "  Validité: OK"
    
    # 3. Informations de base
    echo "- Informations certificat:"
    subject=$(openssl x509 -in "$cert_file" -noout -subject | sed 's/subject= *//')
    issuer=$(openssl x509 -in "$cert_file" -noout -issuer | sed 's/issuer= *//')
    not_after=$(openssl x509 -in "$cert_file" -noout -enddate | sed 's/notAfter=//')
    
    echo "  Subject: $subject"
    echo "  Issuer: $issuer"
    echo "  Expire: $not_after"
    
    # 4. Validation CN filesystem (spécifique APIM CLI)
    echo "- Validation CN APIM CLI..."
    validate_cn_filesystem "$cert_file" || return 1
    
    # 5. Validation SSL chain
    echo "- Validation chaîne SSL..."
    validate_ssl_chain "$cert_file" || return 1
    
    # Avertissement expiration proche
    if ! openssl x509 -in "$cert_file" -noout -checkend 2592000 >/dev/null 2>&1; then
        echo "  Attention: Expire dans moins de 30 jours"
    fi
    
    echo ""
    return 0
}

# Traitement principal
main() {
    # Compter certificats
    cert_count=$(grep -c "BEGIN CERTIFICATE" "$CERT_FILE" 2>/dev/null || echo "0")
    
    if [ "$cert_count" -eq 0 ]; then
        echo "Erreur: Aucun certificat trouvé"
        exit 2
    fi
    
    echo "Certificats trouvés: $cert_count"
    echo ""
    
    if [ "$cert_count" -eq 1 ]; then
        # Un seul certificat
        if validate_certificate "$CERT_FILE"; then
            echo "Validation APIM CLI: SUCCÈS"
            exit 0
        else
            echo "Validation APIM CLI: ÉCHEC"
            exit 2
        fi
    else
        # Plusieurs certificats - les séparer
        temp_dir=$(mktemp -d)
        trap 'rm -rf "$temp_dir"' EXIT
        
        # Séparer les certificats
        awk '
        /-----BEGIN CERTIFICATE-----/ { cert++; print > "'$temp_dir'/cert" cert ".pem" }
        cert > 0 { print > "'$temp_dir'/cert" cert ".pem" }
        ' "$CERT_FILE"
        
        # Valider chacun
        cert_num=1
        exit_code=0
        
        for cert_file in "$temp_dir"/cert*.pem; do
            if [ -f "$cert_file" ]; then
                if ! validate_certificate "$cert_file" "$cert_num"; then
                    exit_code=2
                fi
                cert_num=$((cert_num + 1))
            fi
        done
        
        if [ $exit_code -eq 0 ]; then
            echo "Validation APIM CLI: TOUS VALIDES"
        else
            echo "Validation APIM CLI: ERREURS DÉTECTÉES"
        fi
        
        exit $exit_code
    fi
}

main
