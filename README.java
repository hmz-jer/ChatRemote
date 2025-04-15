# IBCPROXY

## Vue d'ensemble
IBCPROXY est un service proxy développé en Java 17 avec Spring Boot qui sert d'intermédiaire entre Kafka et une API Gateway. Cette application facilite la communication entre les différents composants de l'infrastructure en assurant le routage des messages et la transformation des données.

## Fonctionnalités principales
- Consommation de messages depuis les topics Kafka
- Transformation et routage des messages
- Acheminement des requêtes vers l'API Gateway
- Gestion des réponses et des erreurs

## Structure détaillée

```
integration/
├── bin/                # Contient le fichier JAR exécutable de l'application
│   └── ibcproxy.jar    # JAR principal de l'application
├── conf/               # Fichiers de configuration de l'environnement
│   ├── jbcproxy.cfg    # Configuration des paramètres d'exécution (JVM, chemins)
│   └── logback.xml     # Configuration du système de journalisation
├── etc/                # Configuration Spring Boot
│   └── application.yml # Paramètres Spring, Kafka et API Gateway
├── logs/               # Dossier des fichiers journaux
│   ├── ibcproxy.log    # Journal principal de l'application
│   └── console.log     # Sortie console de l'application
└── script/             # Scripts de gestion
    └── manage.sh       # Script pour démarrer/arrêter/vérifier l'application
```

### Description des dossiers et fichiers

#### Dossier `bin/`
Ce dossier contient uniquement le fichier JAR exécutable de l'application IBCPROXY. Après la compilation de votre projet, placez le fichier JAR généré ici.

#### Dossier `conf/`
Contient les fichiers de configuration de l'environnement :
- **jbcproxy.cfg** : Configuration du démarrage de l'application
  - Chemin du JDK Java à utiliser
  - Options JVM (mémoire, etc.)
  - Autres paramètres d'exécution

- **logback.xml** : Configuration détaillée du système de logs
  - Format des messages de log
  - Politique de rotation des fichiers
  - Niveaux de log par package

#### Dossier `etc/`
Contient la configuration Spring Boot de l'application :
- **application.yml** : Configuration complète
  - Paramètres de connexion Kafka
  - Configuration de l'API Gateway
  - Autres paramètres applicatifs

#### Dossier `logs/`
Les fichiers de log seront automatiquement générés dans ce dossier :
- **ibcproxy.log** : Logs générés par le framework de logging
- **console.log** : Capture de la sortie console de l'application

#### Dossier `script/`
Contient les utilitaires de gestion :
- **manage.sh** : Script de contrôle qui permet de :
  - Démarrer l'application
  - Arrêter l'application
  - Vérifier son statut
  - Redémarrer l'application

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
