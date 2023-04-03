#!/bin/bash

# Vérifier si les paramètres nécessaires sont fournis
if [ "$#" -ne 6 ]; then
    echo "Usage: $0 <url> <port> <xml_file> <client_crt> <client_key> <ca_crt>"
    exit 1
fi

# Assigner les paramètres à des variables
URL="$1"
PORT="$2"
XML_FILE="$3"
CLIENT_CRT="$4"
CLIENT_KEY="$5"
CA_CRT="$6"

# Vérifier si les fichiers spécifiés existent
for file in "$XML_FILE" "$CLIENT_CRT" "$CLIENT_KEY" "$CA_CRT"; do
    if [ ! -f "$file" ]; then
        echo "Le fichier spécifié n'existe pas: $file"
        exit 1
    fi
done

# Envoi de la requête POST avec le header X-SP-Request-Type, le contenu du fichier XML et les certificats SSL
curl -X POST -H "Content-Type: application/xml" -H "X-SP-Request-Type: custom" --data-binary "@$XML_FILE" \
     --cert "$CLIENT_CRT" --key "$CLIENT_KEY" --cacert "$CA_CRT" "https://$URL:$PORT"

