 # Mail à envoyer à Ivan

Objet : Validation des étapes de création des certificats QWAC et tests du mock-client

Bonjour Ivan,

Suite à nos travaux sur la mise en place du flux outbound avec validation de certificats QWAC, je souhaitais vérifier avec toi si notre approche est conforme aux exigences.

Nous avons élaboré les étapes suivantes pour la création et la validation des certificats QWAC :

1. **Création d'une AC racine de test** utilisant OpenSSL pour simuler l'infrastructure PKI
2. **Génération des certificats QWAC** incluant les extensions spécifiques PSD2, notamment :
   - L'identifiant d'organisation au format PSDFR-ACPR-XXXXX dans le champ 2.5.4.97
   - Les extensions nécessaires (KeyUsage, ExtendedKeyUsage, QCStatements)
3. **Mise en place d'un mock-client-VOP** capable de :
   - Valider les connexions MTLS
   - Extraire et vérifier le certificateOwnerId
   - Router les requêtes en fonction de l'identifiant PSP

Nous avons effectué plusieurs tests avec notre mock pour valider le bon fonctionnement du processus :
- Validation de chaînes de certificats complètes
- Vérification de l'extraction correcte de l'identifiant PSP
- Tests de connexion avec différents certificats

Pourrais-tu nous confirmer si cette approche est en ligne avec tes attentes ? Y a-t-il des points spécifiques concernant la structure des certificats ou le processus de validation que nous devrions ajuster ?

Je reste disponible pour échanger sur le sujet ou pour te présenter les détails techniques de notre implémentation si nécessaire.

Merci d'avance pour ton retour,

Cordialement,
[Votre nom]
