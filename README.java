#!/bin/bash

# Lire le fichier de configuration
source /chemin/vers/config.cfg

# Vérifier si les variables nécessaires sont définies
if [ -z "$DOSSIER_CSV_CONSUMER" ] || [ -z "$DOSSIER_CSV_PSP" ] || [ -z "$DOSSIER_DONE" ] || [ -z "$DOSSIER_ERREUR" ] || [ -z "$KEY_PEM" ] || [ -z "$CER_PEM" ]; then
    echo "Les variables nécessaires ne sont pas définies dans le fichier de configuration."
    exit 1
fi

# Créer des fichiers de sortie temporaires
FICHIER_SORTIE_CONSUMER=$(mktemp)
FICHIER_SORTIE_PSP=$(mktemp)

# Concaténer les fichiers en fonction de leur type
function concatener {
    local premier=true
    local nb_colonnes=0
    local dossier_csv=$1
    local fichier_sortie=$2
    local dossier_done=$3
    local dossier_erreur=$4
    local fichier_sortie_chiffre=$5

    for fichier in $(ls -v $dossier_csv/*.csv)
    do
        if [ ! -f "$fichier" ] || [ ! -r "$fichier" ]; then
            echo "Erreur: Le fichier $fichier n'existe pas ou n'est pas lisible."
            mv $fichier $dossier_erreur
            continue
        fi

        if $premier; then
            nb_colonnes=$(head -1 $fichier | awk -F';' '{print NF}')
            head -1 $fichier > $fichier_sortie
            tail -n +2 $fichier | grep . >> $fichier_sortie
            premier=false
        else
            if [ $nb_colonnes -ne $(head -1 $fichier | awk -F';' '{print NF}') ]; then
                echo "Erreur: Le fichier $fichier n'a pas le même nombre de colonnes."
                mv $fichier $dossier_erreur
                continue
            fi
            tail -n +2 $fichier | grep . >> $fichier_sortie
        fi

        mv $fichier $dossier_done
    done

    # Chiffrer et signer le fichier de sortie
    openssl smime -sign -in $fichier_sortie -text -out $fichier_sortie.sig -signer $CER_PEM -inkey $KEY_PEM -outform PEM
    openssl smime -encrypt -binary -outform DER -in $fichier_sortie.sig -out $fichier_sortie_chiffre $CER_PEM

    # Supprimer le fichier de sortie original et la signature
    rm $fichier_sortie
    rm $fichier_sortie.sig
}

# Appel de la fonction pour chaque type de fichier
concatener $DOSSIER_CSV_CONSUMER $FICHIER_SORTIE_CONSUMER $DOSSIER_DONE $DOSSIER_ERREUR sortie_consumer.enc
concatener $DOSSIER_CSV_PSP $FICHIER_SORTIE_PSP $DOSSIER_DONE $DOSSIER_ERREUR sortie_psp.enc
