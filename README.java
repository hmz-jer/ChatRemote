#!/bin/bash

# Chemin du fichier JSON
JSON_FILE_PATH=$1

# Chemin de création des dossiers
DIRECTORY_PATH=$2

# Vérifier si jq est installé
if ! [ -x "$(command -v jq)" ]; then
  echo 'Erreur: jq n est pas installé.' >&2
  exit 1
fi

# Lire le fichier JSON et obtenir les valeurs de l'attribut operationId
OPERATION_IDS=$(jq -r '.[] | .operationId' $JSON_FILE_PATH)

# Pour chaque operationId, créer un dossier correspondant dans le chemin spécifié
for id in $OPERATION_IDS
do
  mkdir -p "$DIRECTORY_PATH/$id"
done

# Créer les dossiers 'done' et 'erreur'
mkdir -p "$DIRECTORY_PATH/done"
mkdir -p "$DIRECTORY_PATH/erreur"
