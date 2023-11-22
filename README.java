const fs = require('fs');
const Converter = require('openapi-to-postmanv2');
const postmanToOpenApi = require('postman-to-openapi');

const [,, openApiInputFile, openApiOutputFile] = process.argv;

fs.readFile(openApiInputFile, 'utf8', (err, openApiData) => {
  if (err) {
    console.error('Erreur de lecture du fichier OpenAPI:', err);
    return;
  }

  Converter.convert({ type: 'string', data: openApiData }, {}, (convertErr, conversionResult) => {
    if (convertErr) {
      console.error('Erreur lors de la conversion en collection Postman:', convertErr);
      return;
    }
    
    if (!conversionResult.result) {
      console.error('Conversion échouée:', conversionResult.reason);
      return;
    }

    const postmanCollection = conversionResult.output[0].data;
    postmanToOpenApi(postmanCollection, openApiOutputFile, { defaultTag: 'General' })
      .then(() => console.log('Conversion réussie en OpenAPI 3.0.3'))
      .catch(postmanErr => console.error('Erreur lors de la conversion en OpenAPI:', postmanErr));
  });
});
