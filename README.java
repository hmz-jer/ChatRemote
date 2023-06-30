#!/bin/bash

# Lire le fichier de configuration
source /chemin/vers/config.cfg

# Vérifier si les variables nécessaires sont définies
if [ -z "$DOSSIER_CSV_CONSUMER" ] || [ -z "$DOSSIER_CSV_PSP" ] || [ -z "$DOSSIER_DONE" ] || [ -z "$DOSSIER_ERREUR" ] || [ -z "$KEY_PEM" ] || [ -z "$CER_PEM" ]; then
    echo "Erreur : Les variables nécessaires ne sont pas définies dans le fichier de configuration."
    exit 1
fi

# Vérifier si les dossiers existent et sont lisibles
for DOSSIER in $DOSSIER_CSV_CONSUMER $DOSSIER_CSV_PSP $DOSSIER_DONE $DOSSIER_ERREUR
do
    if [ ! -d "$DOSSIER" ] || [ ! -r "$DOSSIER" ]; then
        echo "Erreur : Le dossier $DOSSIER n'existe pas ou n'est pas lisible."
        exit 1
    fi
done

echo "Début du traitement des fichiers CSV..."

# Fonction pour concaténer, compresser, signer et chiffrer les fichiers CSV
function traiter {
    local dossier=$1
    local motif=$2
    local fichier_sortie="$DOSSIER_DONE/$(date +%Y%m%d%H%M%S).$motif"
    local premier=true
    local nb_colonnes=0

    echo "Traitement des fichiers $motif dans le dossier $dossier..."

    for fichier in $(ls -v $dossier/$motif)
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

    echo "Compression, signature et chiffrement du fichier $fichier_sortie..."

    tar -czvf $fichier_sortie.tar.gz $fichier_sortie
    openssl smime -sign -signer $CER_PEM -inkey $KEY_PEM -in $fichier_sortie.tar.gz -out $fichier_sortie.tar.gz.signed -outform PEM -nodetach
    openssl smime -encrypt -binary -aes-256-cbc -in $fichier_sortie.tar.gz.signed -out $fichier_sortie.tar.gz.enc -outform DER $CER_PEM
    echo "Le fichier $fichier_sortie a été compressé, signé et chiffré."
    rm $fichier_sortie $fichier_sortie.tar.gz $fichier_sortie.tar.gz.signed
}

# Appeler la fonction de traitement pour chaque type de fichier
traiter $DOSSIER_CSV_CONSUMER "*consumer-info.csv"
traiter $DOSSIER_CSV_PSP "*psp-list.csv"

echo "Fin du traitement des fichiers CSV."
