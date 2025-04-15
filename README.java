# IBCPROXY

## Vue d'ensemble
IBCPROXY est un service proxy développé en Java 17 avec Spring Boot qui sert d'intermédiaire entre Kafka et une API Gateway. Cette application facilite la communication entre les différents composants de l'infrastructure en assurant le routage des messages et la transformation des données.

## Fonctionnalités principales
- Consommation de messages depuis les topics Kafka
- Transformation et routage des messages
- Acheminement des requêtes vers l'API Gateway
- Gestion des réponses et des erreurs

## Structure

```
integration/
├── bin/                # Contient le fichier JAR exécutable
├── conf/               # Fichiers de configuration
├── etc/                # Configuration Spring Boot
├── logs/               # Logs d'application
└── script/             # Scripts de gestion
```

## Démarrage rapide

### Installation
1. Copiez le dossier `integration` sur votre serveur CentOS
2. Rendez le script executable: `chmod +x integration/script/manage.sh`
3. Ajustez la configuration dans `conf/jbcproxy.cfg` et `etc/application.yml`

### Utilisation
```bash
# Démarrer le service
./integration/script/manage.sh start

# Vérifier l'état
./integration/script/manage.sh status

# Arrêter le service
./integration/script/manage.sh stop

# Redémarrer
./integration/script/manage.sh restart
```

## Configuration

### Kafka
Configuration dans `application.yml` :
- Serveurs bootstrap
- Topics consommés
- Groupes de consommateurs
- Paramètres de sérialisation/désérialisation

### API Gateway
- URL de l'API Gateway
- Paramètres d'authentification
- Timeouts et retry policies

## Logs
Les logs de l'application sont disponibles dans le dossier `logs/`

## Support
Pour toute assistance, veuillez contacter l'équipe technique.
