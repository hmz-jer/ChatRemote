#!/bin/bash

# Définir le chemin du dossier contenant les fichiers CSV et le fichier de sortie
DOSSIER_CSV="/chemin/vers/le/dossier/csv"
FICHIER_SORTIE="/chemin/vers/le/fichier/sortie.csv"

# Vérifier si le dossier existe et est lisible
if [ ! -d "$DOSSIER_CSV" ] || [ ! -r "$DOSSIER_CSV" ]; then
    echo "Le dossier $DOSSIER_CSV n'existe pas ou n'est pas lisible"
    exit 1
fi

# Trouver tous les fichiers .csv dans le dossier donné, sauter la première ligne (l'en-tête)
# pour tous les fichiers après le premier et concaténer dans le fichier de sortie
premier=true
nb_colonnes=0
for fichier in $DOSSIER_CSV/*.csv
do
  # Vérifier si le fichier existe et est lisible
  if [ ! -f "$fichier" ] || [ ! -r "$fichier" ]; then
      echo "Le fichier $fichier n'existe pas ou n'est pas lisible"
      exit 1
  fi

  # Vérifier si tous les fichiers ont le même nombre de colonnes
  if $premier; then
      nb_colonnes=$(head -1 $fichier | awk -F';' '{print NF}')
  else
      if [ $nb_colonnes -ne $(head -1 $fichier | awk -F';' '{print NF}') ]; then
          echo "Les fichiers n'ont pas le même nombre de colonnes."
          exit 1
      fi
  fi

  if $premier; then
     # Prendre l'en-tête du premier fichier:
     head -1 $fichier > $FICHIER_SORTIE
     tail -n +2 $fichier | grep . >> $FICHIER_SORTIE
     premier=false
  else
     tail -n +2 $fichier | grep . >> $FICHIER_SORTIE
  fi
done
