#!/bin/bash

# Vérifie si le nombre d'objets dans la liste est passé en argument
if [ $# -eq 0 ]; then
    echo "Usage: $0 <nombre d'objets dans la liste>"
    exit 1
fi

# Attributs statiques
attribut1="valeur1"
attribut2="valeur2"
attribut3="valeur3"
attribut4="valeur4"

# Le nombre d'objets dans la liste
nombre_objets=$1

# Créer le cinquième attribut comme une liste d'objets JSON
liste_attributA="["
for ((i = 1; i <= nombre_objets; i++)); do
    liste_attributA+="{\"attributA\":\"valeurA\", \"attributB\":\"valeurB\", \"attributC\":\"valeurC\"}"
    if [ $i -lt $nombre_objets ]; then
        liste_attributA+=", "
    fi
done
liste_attributA+="]"

# Créer le fichier JSON avec les attributs
echo "{\"attribut1\":\"$attribut1\", \"attribut2\":\"$attribut2\", \"attribut3\":\"$attribut3\", \"attribut4\":\"$attribut4\", \"liste_attributA\":$liste_attributA}" > fichier.json

# Afficher le fichier JSON généré
cat fichier.json
