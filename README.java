#!/bin/bash

# Créer un dossier pour le projet
mkdir monProjet
cd monProjet

# Initialiser un nouveau projet Node.js
npm init -y

# Installer les dépendances nécessaires
npm install openapi-to-postmanv2 postman-to-openapi

# Créer le script de conversion
cat <<EOF > convert.js
const fs = require('fs');
const openApiToPostman = require('openapi-to-postmanv2');
const postmanToOpenApi = require('postman-to-openapi');

const [,, openApiInputFile, openApiOutputFile] = process.argv;

const openApiData = fs.readFileSync(openApiInputFile, 'utf8');

openApiToPostman.convert({ type: 'string', data: openApiData }, (err, conversionResult) => {
  if (!conversionResult.result) {
    console.error('Conversion échouée:', conversionResult.reason);
    return;
  }

  const postmanCollection = conversionResult.output[0].data;
  postmanToOpenApi(postmanCollection, openApiOutputFile, { defaultTag: 'General' })
    .then(() => console.log('Conversion réussie en OpenAPI 3.0.3'))
    .catch(err => console.error('Erreur de conversion:', err));
});
EOF

# Donner les droits d'exécution au script
chmod +x convert.js

# Message de fin
echo "Le projet est prêt. Utilisez ./convert.js <input-file> <output-file> pour convertir."
