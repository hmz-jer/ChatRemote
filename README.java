 Voici le compte rendu mis à jour avec l'information sur la PKI STET et les certificats QWAC :

# Point d'avancement sur le flux outbound

• Le développement du flux outbound progresse normalement sans blocage particulier

• Nous avons lancé en parallèle le développement d'une solution temporaire pour remplacer la partie back-end en attendant la finalisation des travaux de l'équipe de Cédric

• Florentino a demandé si nous pouvions développer un mock pour remplacer les banques (clients)

• Nous sommes dans l'impossibilité de procéder à ce développement car nous n'avons pas encore accès à la PKI STET

• La PKI STET est nécessaire pour générer des certificats QWAC (Qualified Website Authentication Certificate) conformes aux normes européennes eIDAS et PSD2 Ces certificats sont conçus pour sécuriser et authentifier les sites Web dans le contexte des services de paiement [SSLmarket](https://www.sslmarket.fr/ssl/quovadis-qualified-website-authentication-certificate-qwac)

• La génération de ces certificats nécessite une validation rigoureuse par une Autorité de Certification et contient des informations spécifiques sur l'organisation

• La personne responsable de nous fournir la PKI est actuellement en vacances

Citations:
- [Qualified Website Authentication Certificate (QWAC)](https://www.sslmarket.fr/ssl/quovadis-qualified-website-authentication-certificate-qwac)
