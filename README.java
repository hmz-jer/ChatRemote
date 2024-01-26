#!/bin/bash

# Vérifie si le nombre de récurrences est passé en argument
if [ $# -eq 0 ]; then
    echo "Usage: $0 <nombre de récurrences>"
    exit 1
fi

# Attributs statiques
attribut1="valeur1"
attribut2="valeur2"
attribut3="valeur3"
attribut4="valeur4"

# Le nombre de récurrences de attributA
nombre_reccurences=$1

# Créer le cinquième attribut comme une liste
liste_attributA="["
for ((i = 1; i <= nombre_reccurences; i++)); do
    liste_attributA+="\"attributA\""
    if [ $i -lt $nombre_reccurences ]; then
        liste_attributA+=", "
    fi
done
liste_attributA+="]"

# Créer le fichier JSON avec les attributs
echo "{\"attribut1\":\"$attribut1\", \"attribut2\":\"$attribut2\", \"attribut3\":\"$attribut3\", \"attribut4\":\"$attribut4\", \"liste_attributA\":$liste_attributA}" > fichier.json

# Afficher le fichier JSON généré
cat fichier.json
