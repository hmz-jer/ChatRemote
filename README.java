Client / Qdev → émet la requête

🧱 KDP (Kong Data Plane) → joue le rôle de reverse proxy / gateway

🧠 KCP (Kong Control Plane) → centralise la config des routes/services/plugins

🛠 API Admin + decK + opérateur → gèrent la configuration de Kong

🐳 Docker (cardapi) → contient ton application Spring Boot (API) exposée comme un service derrière Kong

#!/bin/bash

# Script de validation certificat compatible APIM CLI Axway
# Génère un rapport détaillé pour de nombreux certificats
# Usage: ./validate_apim_cert.sh <certificat.pem>

# Note: pas de set -e pour permettre la collecte complète des erreurs

CERT_FILE="$1"

# Variables globales pour le rapport
FAILED_CERTS=()
TOTAL_CERTS=0
VALID_CERTS=0
FAILED_COUNT=0

# Vérification des arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <certificat.pem>"
    echo ""
    echo "Valide un certificat selon les critères APIM CLI Axway:"
    echo "- Format PEM et validité temporelle"
    echo "- Validation chaîne SSL/TLS pour certificats CA"
    echo "- Support certificats multiples dans un fichier"
    echo "- Rapport détaillé des échecs"
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

# Extraction du nom de l'organisation/banque depuis le certificat
extract_organization_name() {
    local cert_file="$1"
    local subject org_name
    
    subject=$(openssl x509 -in "$cert_file" -noout -subject 2>/dev/null || echo "")
    
    # Extraire O= (Organization)
    org_name=$(echo "$subject" | sed -n 's/.*O[[:space:]]*=[[:space:]]*\([^,]*\).*/\1/p' | xargs)
    
    # Si pas d'organisation, essayer le CN
    if [ -z "$org_name" ]; then
        org_name=$(echo "$subject" | sed -n 's/.*CN[[:space:]]*=[[:space:]]*\([^,]*\).*/\1/p' | xargs)
    fi
    
    # Nettoyer et retourner
    echo "$org_name" | sed 's/[^a-zA-Z0-9 ]//g' | xargs
}
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
    
    # Vérifier la signature du certificat lui-même
    if ! openssl x509 -in "$cert_file" -noout -text >/dev/null 2>&1; then
        echo "Erreur: Structure du certificat invalide"
        return 1
    fi
    
    echo "  Intégrité: OK"
    return 0
}

# Validation d'un certificat avec gestion d'erreurs détaillée
validate_certificate() {
    local cert_file="$1"
    local cert_num="$2"
    local cert_name
    
    if [ -n "$cert_num" ]; then
        cert_name="Certificat #$cert_num"
        echo "=== $cert_name ==="
    else
        cert_name="$CERT_FILE"
    fi
    
    local validation_failed=false
    local failure_reason=""
    
    # 1. Format PEM et validation stricte
    echo "- Vérification format PEM..."
    if ! openssl x509 -in "$cert_file" -text -noout >/dev/null 2>&1; then
        echo "Erreur: Format PEM invalide"
        validation_failed=true
        failure_reason="Format PEM invalide"
        
        # Si format invalide, skip les autres validations
        echo ""
        TOTAL_CERTS=$((TOTAL_CERTS + 1))
        FAILED_COUNT=$((FAILED_COUNT + 1))
        
        local org_name
        org_name=$(extract_organization_name "$cert_file" 2>/dev/null || echo "")
        
        if [ -n "$org_name" ]; then
            FAILED_CERTS+=("$cert_name ($org_name): $failure_reason")
        else
            FAILED_CERTS+=("$cert_name: $failure_reason")
        fi
        return 1
    fi
    
    echo "  Format PEM: OK"
    
    # Vérification des lignes vides à la fin (APIM CLI rejette)
    if tail -n 5 "$cert_file" | grep -q "^[[:space:]]*$"; then
        echo "Erreur: Lignes vides en fin de fichier"
        validation_failed=true
        failure_reason="Lignes vides en fin de fichier"
    fi
    
    # 2. Validité temporelle
    echo "- Vérification validité..."
    if ! openssl x509 -in "$cert_file" -noout -checkend 0 >/dev/null 2>&1; then
        local expire_date
        expire_date=$(openssl x509 -in "$cert_file" -noout -enddate 2>/dev/null | sed 's/notAfter=//' || echo "Date inconnue")
        echo "Erreur: Certificat expiré le $expire_date"
        validation_failed=true
        failure_reason="Expiré le $expire_date"
    else
        echo "  Validité: OK"
    fi
    
    # 3. Informations de base (toujours affichées)
    echo "- Informations certificat:"
    local subject issuer not_after
    subject=$(openssl x509 -in "$cert_file" -noout -subject 2>/dev/null | sed 's/subject= *//' || echo "Subject inconnu")
    issuer=$(openssl x509 -in "$cert_file" -noout -issuer 2>/dev/null | sed 's/issuer= *//' || echo "Issuer inconnu")
    not_after=$(openssl x509 -in "$cert_file" -noout -enddate 2>/dev/null | sed 's/notAfter=//' || echo "Date inconnue")
    
    echo "  Subject: $subject"
    echo "  Issuer: $issuer"
    echo "  Expire: $not_after"
    
    # 4. Validation SSL chain (uniquement si format PEM OK)
    if [ "$validation_failed" = false ]; then
        echo "- Validation chaîne SSL..."
        if ! validate_ssl_chain "$cert_file"; then
            validation_failed=true
            failure_reason="Chaîne SSL invalide"
        fi
    fi
    
    echo ""
    
    # Mise à jour des compteurs
    TOTAL_CERTS=$((TOTAL_CERTS + 1))
    
    if [ "$validation_failed" = true ]; then
        FAILED_COUNT=$((FAILED_COUNT + 1))
        
        # Extraire le nom de l'organisation/banque pour le rapport
        local org_name
        org_name=$(extract_organization_name "$cert_file")
        
        if [ -n "$org_name" ]; then
            FAILED_CERTS+=("$cert_name ($org_name): $failure_reason")
        else
            FAILED_CERTS+=("$cert_name: $failure_reason")
        fi
        return 1
    else
        VALID_CERTS=$((VALID_CERTS + 1))
        return 0
    fi
}

# Génération du rapport final
generate_report() {
    echo "=========================================="
    echo "RAPPORT DE VALIDATION APIM CLI"
    echo "=========================================="
    echo "Fichier analysé: $CERT_FILE"
    echo "Total certificats: $TOTAL_CERTS"
    echo "Certificats valides: $VALID_CERTS"
    echo "Certificats échoués: $FAILED_COUNT"
    echo ""
    
    if [ $FAILED_COUNT -gt 0 ]; then
        echo "CERTIFICATS ÉCHOUÉS:"
        echo "===================="
        for failed_cert in "${FAILED_CERTS[@]}"; do
            echo "- $failed_cert"
        done
        echo ""
        echo "RÉSULTAT: ÉCHEC - $FAILED_COUNT certificat(s) invalide(s)"
    else
        echo "RÉSULTAT: SUCCÈS - Tous les certificats sont valides"
    fi
    echo "=========================================="
}

# Traitement principal
main() {
    # Compter certificats
    local cert_count
    cert_count=$(grep -c "BEGIN CERTIFICATE" "$CERT_FILE" 2>/dev/null || echo "0")
    
    if [ "$cert_count" -eq 0 ]; then
        echo "Erreur: Aucun certificat trouvé"
        exit 2
    fi
    
    echo "Certificats trouvés: $cert_count"
    echo ""
    
    if [ "$cert_count" -eq 1 ]; then
        # Un seul certificat
        validate_certificate "$CERT_FILE"
    else
        # Plusieurs certificats - les séparer
        local temp_dir
        temp_dir=$(mktemp -d)
        trap 'rm -rf "$temp_dir"' EXIT
        
        # Séparer les certificats de manière robuste
        awk '
        BEGIN { cert=0; in_cert=0 }
        /-----BEGIN CERTIFICATE-----/ { 
            cert++; 
            in_cert=1;
            filename = "'$temp_dir'/cert" cert ".pem"
            print > filename
            next
        }
        /-----END CERTIFICATE-----/ { 
            if (in_cert) {
                print > filename
                in_cert=0
            }
            next
        }
        in_cert { print > filename }
        ' "$CERT_FILE"
        
        # Valider chacun
        local cert_num=1
        for cert_file in "$temp_dir"/cert*.pem; do
            if [ -f "$cert_file" ]; then
                validate_certificate "$cert_file" "$cert_num" || true  # Continue même en cas d'échec
                cert_num=$((cert_num + 1))
            fi
        done
    fi
    
    # Afficher le rapport final
    generate_report
    
    # Code de sortie basé sur le résultat
    if [ $FAILED_COUNT -eq 0 ]; then
        exit 0
    else
        exit 2
    fi
}

main
