 Voici  des volumes Docker..."
  docker volume prune -f

  echo "Nettoyage terminé."
  ```

- **`restart-services.sh`** : Ce script redémarre les services définis dans `docker-compose.yml`.

  ```bash
  #!/bin/bash
  echo "Redémarrage des services Docker..."
  docker-compose down
  docker-compose up -d
  echo "Services redémarrés."
  ```

- **`start-services.sh`** : Ce script démarre les services Docker définis dans `docker-compose.yml` sans les arrêter au préalable.

  ```bash
  #!/bin/bash
  echo "Démarrage des services Docker..."
  docker-compose up -d
  echo "Services démarrés."
  ```

## Gestion de l'Application

### 1. Script `manage.sh`

Le script `manage.sh` permet de démarrer, arrêter et vérifier le statut de l'application Spring Boot. Voici un aperçu des fonctionnalités :

- **Démarrer l'application** : Utilisez l'option `1` pour démarrer l'application avec un profil spécifique.
- **Arrêter l'application** : Utilisez l'option `2` pour arrêter l'application via l'API d'arrêt d'Actuator.
- **Vérifier le statut** : Utilisez l'option `3` pour vérifier si l'application est en cours d'exécution.

```bash
#!/bin/bash

APP_NAME="OMC"
CONFIG_FILE="./config/app.conf"
PROFILE="run"

function start_application() {
    echo "Démarrage de l'application $APP_NAME..."
    java -jar -Dspring.config.location=$CONFIG_FILE -Dspring.profiles.active=$PROFILE omc-backend.jar &
    echo "Lancement en cours..."
    sleep 10
    check_status
}

function stop_application() {
    echo "Arrêt de l'application $APP_NAME..."
    curl -X POST http://localhost:8080/actuator/shutdown
}

function check_status() {
    STATUS=$(curl -s http://localhost:8080/actuator/health | grep '"status":"UP"')
    if [ -n "$STATUS" ]; then
        echo "$APP_NAME est en cours d'exécution. (OK)"
    else
        echo "$APP_NAME n'est pas en cours d'exécution. (KO)"
    fi
}

echo "Gestion de l'application $APP_NAME"
echo "1. Démarrer l'application"
echo "2. Arrêter l'application"
echo "3. Vérifier le statut"

read -p "Choisissez une option : " choice

case $choice in
    1) start_application ;;
    2) stop_application ;;
    3) check_status ;;
    *) echo "Option invalide" ;;
esac
```

### 2. Démarrer l'Application avec Spring Boot

Pour démarrer l'application Spring Boot, exécutez la commande suivante dans le terminal :

```bash
./scripts/manage.sh
```

Suivez les instructions pour démarrer l'application avec le profil de votre choix.

### 3. Lancer les Tests

Pour exécuter les tests, utilisez Maven :

```bash
mvn test
```

Cette commande exécute tous les tests unitaires définis dans votre projet.

## Conclusion

Ce fichier `README.md` fournit toutes les informations nécessaires pour configurer, gérer et tester l'application OMC. Les scripts inclus facilitent la gestion des conteneurs Docker ainsi que le démarrage et l'arrêt de l'application.

---

Ce fichier `README.md` donne une vue d'ensemble complète de la gestion de votre projet, y compris la configuration des conteneurs Docker, les scripts de gestion, et les instructions pour démarrer l'application et exécuter les tests.
