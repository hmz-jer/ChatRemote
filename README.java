# Simulator API pour Stresser l'Environnement Digital SI

La Simulator API est conçue pour générer et envoyer des requêtes configurables afin de tester la robustesse et la performance de l'environnement Digital SI. 

Cet outil permet aux développeurs et aux testeurs de simuler différentes charges et scénarios d'utilisation pour évaluer la réactivité et la fiabilité du système.

## Fonctionnalités

- **Génération de Requêtes Configurables :** Permet la création dynamique de requêtes basées sur les spécifications de l'utilisateur, incluant différentes opérations telles que tokenisation, suppression, et détokenisation.

- **Support de Connexions Sécurisées :** Capable d'effectuer des requêtes via HTTP ou HTTPS, en fonction de la configuration de l'environnement cible.

- **Stress Test :** Conçu pour tester la capacité de l'environnement Digital SI à gérer des volumes élevés de requêtes et opérations.
- **JWE/JWS:** Notre API utilise les standards JWE (JSON Web Encryption) et JWS (JSON Web Signature) pour assurer la confidentialité et l'intégrité des données échangées :
  JWE - JSON Web Encryption
## Prérequis

- Java 8
- Spring Boot 2.3.3
- [Gradle] pour la gestion du projet

## Configuration

Assurez-vous que les propriétés suivantes sont correctement définies dans le fichier `application.properties` :

```properties
# Configuration SSL
keystore.path=chemin/vers/keystore
keystore.password=motdepasse
truststore.path=chemin/vers/truststore
truststore.password=motdepasse

# Configuration de l'API
application.scheme=http # ou https pour les connexions sécurisées
application.host=adresse.host.si
application.port=port
application.basePath=/chemin/base/api

# Paramètres de test
secure.connection=true # ou false pour désactiver SSL
min.attributes=nombre minimum d'attributs
max.attributes=nombre maximum d'attributs 
```

## Utilisation

Utilisez les arguments suivants au démarrage de l'application pour spécifier l'opération désirée :

    T ou R pour Tokenisation
    S pour Suppression
    D pour Détokenisation

Exemple :

```bash
java -jar target/simulatorapi-0.0.1-SNAPSHOT.jar T
```
