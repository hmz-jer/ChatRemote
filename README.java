    D'accord, je vais ajuster le script en conséquence. Si l'utilisateur entre `1` ou `start`, le script redémarrera l'application, et s'il entre `0` ou `stop`, il arrêtera l'application.

Voici le script modifié:

```bash
#!/bin/bash

# Nom du script : nomfichier.sh

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 setadminstatus {0|stop|1|start}"
    exit 1
fi

command=$1
value=$2

if [ "$command" != "setadminstatus" ]; then
    echo "La commande n'est pas reconnue."
    exit 2
fi

case $value in
    0 | stop)
        echo "Arrêt de l'application..."
        # Commande pour arrêter l'application ici
        ;;
    1 | start)
        echo "Démarrage de l'application..."
        # Commande pour démarrer l'application ici
        ;;
    *)
        echo "Valeur non reconnue. Utilisez 0 ou stop pour arrêter et 1 ou start pour démarrer."
        exit 3
        ;;
esac

exit 0
```

Comme mentionné précédemment, n'oubliez pas de remplacer les commentaires par les commandes réelles pour démarrer et arrêter votre application. Donnez également les permissions d'exécution avec `chmod +x nomfichier.sh`.
