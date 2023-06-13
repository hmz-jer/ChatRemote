#!/bin/bash

# Définir le chemin du dossier contenant les fichiers CSV et les fichiers de sortie
DOSSIER_CSV_CONSUMER="/chemin/vers/le/dossier/consumer/csv"
DOSSIER_CSV_PSP="/chemin/vers/le/dossier/psp/csv"
FICHIER_SORTIE_CONSUMER="/chemin/vers/le/fichier/sortie_consumer.csv"
FICHIER_SORTIE_PSP="/chemin/vers/le/fichier/sortie_psp.csv"

# Définir le chemin vers les clés et certificats
CLE_PUBLIQUE="/chemin/vers/la/clé/publique"
CLE_PRIVEE="/chemin/vers/la/clé/privée"
CERTIFICAT="/chemin/vers/le/certificat"

# Concaténer les fichiers en fonction de leur type
function concatener {
    local premier=true
    local nb_colonnes=0
    local dossier=$1
    local fichier_sortie=$2

    for fichier in $(ls -v $dossier/*.csv)
    do
      if [ ! -f "$fichier" ] || [ ! -r "$fichier" ]; then
          echo "Le fichier $fichier n'existe pas ou n'est pas lisible"
          exit 1
      fi

      if $premier; then
          nb_colonnes=$(head -1 $fichier | awk -F';' '{print NF}')
          head -1 $fichier > $fichier_sortie
          tail -n +2 $fichier | grep . >> $fichier_sortie
          premier=false
      else
          if [ $nb_colonnes -ne $(head -1 $fichier | awk -F';' '{print NF}') ]; then
              echo "Les fichiers n'ont pas le même nombre de colonnes."
              exit 1
          fi
          tail -n +2 $fichier | grep . >> $fichier_sortie
      fi
    done

    # Signer le fichier en utilisant smime avec l'option -outform PEM
    openssl smime -sign -binary -in $fichier_sortie -signer $CERTIFICAT -inkey $CLE_PRIVEE -outform PEM -out $fichier_sortie.pem

    # Chiffrer le fichier signé avec la clé publique du récepteur, en utilisant l'option -outform PEM
    openssl smime -encrypt -binary -aes256 -outform PEM $CLE_PUBLIQUE < $fichier_sortie.pem > $fichier_sortie.enc

    rm $fichier_sortie $fichier_sortie.pem
}

# Vérifier si le dossier existe et est lisible
if [ ! -d "$DOSSIER_CSV_CONSUMER" ] || [ ! -r "$DOSSIER_CSV_CONSUMER" ]; then
    echo "Le dossier $DOSSIER_CSV_CONSUMER n'existe pas ou n'est pas lisible"
    exit 1
fi

if [ ! -d "$DOSSIER_CSV_PSP" ] || [ ! -r "$DOSSIER_CSV_PSP" ]; then
    echo "Le dossier $DOSSIER_CSV_PSP n'existe pas ou n'est pas lisible"
    exit 1
fi

# Appeler la fonction de concaténation pour chaque type de fichier
concatener $DOSSIER_CSV_CONSUMER $FICHIER_SORTIE_CONSUMER
concatener $DOSSIER_CSV_PSP $FICHIER_SORTIE_PSP
