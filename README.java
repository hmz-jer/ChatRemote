!/bin/bash

# Définir le chemin du dossier contenant les fichiers CSV et les fichiers de sortie
DOSSIER_CSV="/home/hmz/Documents/test"
FICHIER_SORTIE_CONSUMER="/home/hmz/Documents/sortie_consumer.csv"
FICHIER_SORTIE_PSP="/home/hmz/Documents/sortie_psp.csv"

# Concaténer les fichiers en fonction de leur type
function concatener {
    local premier=true
    local nb_colonnes=0
    local motif=$1
    local fichier_sortie=$2

    for fichier in $(ls -v $DOSSIER_CSV/$motif)
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
}

# Vérifier si le dossier existe et est lisible
if [ ! -d "$DOSSIER_CSV" ] || [ ! -r "$DOSSIER_CSV" ]; then
    echo "Le dossier $DOSSIER_CSV n'existe pas ou n'est pas lisible"
    exit 1
fi

# Appeler la fonction de concaténation pour chaque type de fichier
concatener "*client-info.csv" $FICHIER_SORTIE_CONSUMER
concatener "*psp-list.csv" $FICHIER_SORTIE_PSP
