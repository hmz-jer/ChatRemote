#!/bin/bash

# Lire le fichier de configuration
source $(dirname "$0")/config.cfg

# Vérifier si les variables nécessaires sont définies
if [ -z "$DOSSIER_DONE" ] || [ -z "$DOSSIER_ERREUR" ] || [ -z "$KEY_PEM" ] || [ -z "$CER_PEM" ]; then
    echo "Erreur : Les variables nécessaires ne sont pas définies dans le fichier de configuration."
    exit 1
fi

echo "Début du traitement des fichiers CSV..."

# Fonction pour concaténer, signer et chiffrer les fichiers CSV
function traiter {
    local dossier=$1
    local fichier_sortie="$DOSSIER_DONE/${dossier##*/}_$(date +%Y%m%d%H%M%S).csv"
    local premier=true
    local nb_colonnes=0

    echo "Traitement des fichiers CSV dans le dossier $dossier..."

    for fichier in $(ls -v $dossier/*.csv)
    do
        if [ ! -f "$fichier" ] || [ ! -r "$fichier" ]; then
            echo "Erreur : Le fichier $fichier n'existe pas ou n'est pas lisible."
            mv "$fichier" "$DOSSIER_ERREUR"
            echo "Le fichier $fichier a été déplacé vers le dossier ERROR."
            continue
        fi

        echo "Concaténation du fichier $fichier..."

        if $premier; then
            nb_colonnes=$(head -1 $fichier | awk -F';' '{print NF}')
            head -1 $fichier > $fichier_sortie
            tail -n +2 $fichier | grep . >> $fichier_sortie
            premier=false
        else
            if [ $nb_colonnes -ne $(head -1 $fichier | awk -F';' '{print NF}') ]; then
                echo "Erreur : Les fichiers n'ont pas le même nombre de colonnes."
                mv "$fichier" "$DOSSIER_ERREUR"
                echo "Le fichier $fichier a été déplacé vers le dossier ERROR."
                continue
            fi
            tail -n +2 $fichier | grep . >> $fichier_sortie
        fi

        echo "Le fichier $fichier a été traité et supprimé."
        rm $fichier
    done

    # Vérifier si le fichier de sortie est vide
    if [ ! -s $fichier_sortie ]; then
        echo "Le fichier $fichier_sortie est vide. Il ne sera pas signé ni chiffré."
        rm $fichier_sortie
        return
    fi

    echo "Signature et chiffrement du fichier $fichier_sortie..."

    openssl smime -sign -signer $CER_PEM -inkey $KEY_PEM -in $fichier_sortie -out $fichier_sortie.signed -outform PEM -nodetach
    openssl smime -encrypt -binary -aes-256-cbc -in $fichier_sortie.signed -out $fichier_sortie.enc -outform DER $CER_PEM
    echo "Le fichier $fichier_sortie a été signé et chiffré."
    rm $fichier_sortie $fichier_sortie.signed
}

# Parcourir tous les dossiers qui commencent par "referenciel" et traiter leurs fichiers CSV
for dossier in $(dirname "$0")/referenciel*; do
    if [ -d "$dossier" ]; then
        traiter $dossier
    fi
done

echo "Fin du traitement des fichiers CSV."
