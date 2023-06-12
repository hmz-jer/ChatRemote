Pour générer une signature avec un format de sortie PEM, vous pouvez utiliser l'option -outform PEM dans la commande openssl smime -sign. De même, pour le chiffrement, vous pouvez utiliser l'option -outform DER. Voici comment vous pouvez le faire :

Sur le système A (envoyeur):

    Créez une paire de clés et un certificat auto-signé :

    bash

openssl req -x509 -newkey rsa:4096 -keyout keyA.pem -out certA.pem -sha256 -days 365 -subj '/CN=SystemA' -nodes

Signez le fichier en utilisant smime avec l'option -outform PEM :

bash

openssl smime -sign -binary -in sortie.csv -signer certA.pem -inkey keyA.pem -outform PEM -out sortie.pem

Chiffrez le fichier signé avec la clé publique du système B (certB.pem, qui doit être fourni par le système B), en utilisant l'option -outform DER :

bash

    openssl smime -encrypt -binary -aes256 -outform DER certB.pem < sortie.pem > file.enc

    Envoyez file.enc et certA.pem au système B.

Sur le système B (récepteur):

    Créez une paire de clés et un certificat auto-signé :

    bash

openssl req -x509 -newkey rsa:4096 -keyout keyB.pem -out certB.pem -sha256 -days 365 -subj '/CN=SystemB' -nodes

Envoyez certB.pem au système A.

Lorsque vous recevez file.enc et certA.pem du système A, déchiffrez le fichier avec votre clé privée :

bash

openssl smime -decrypt -in file.enc -recip certB.pem -inkey keyB.pem -out sortie.pem

Vérifiez la signature avec la clé publique du système A (certA.pem) :

bash

    openssl smime -verify -in sortie.pem -signer certA.pem -CAfile certA.pem -out sortie.csv

Ces commandes vous permettront de signer le fichier avec le format de sortie PEM, puis de le chiffrer en utilisant le format de sortie DER. Le fichier chiffré sera file.enc.
