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

# Générer la liste pour le cinquième attribut
liste_attributA=()
for ((i = 0; i < nombre_reccurences; i++)); do
    liste_attributA+=("attributA")
done

# Créer le fichier JSON avec les attributs
echo "{\"attribut1\":\"$attribut1\", \"attribut2\":\"$attribut2\", \"attribut3\":\"$attribut3\", \"attribut4\":\"$attribut4\", \"liste_attributA\":$(jq -n --argjson vals "${liste_attributA[@]}" '$vals')}" > fichier.json

# Afficher le fichier JSON généré
cat fichier.json
