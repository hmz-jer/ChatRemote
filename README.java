    D'accord, voici un exemple de script shell que vous pouvez utiliser :

```bash
#!/bin/bash

# Nom du script : nomfichier.sh

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 setadminstatus {0|1}"
    exit 1
fi

command=$1
value=$2

if [ "$command" != "setadminstatus" ]; then
    echo "La commande n'est pas reconnue."
    exit 2
fi

case $value in
    0)
        echo "Arrêt de l'application..."
        # Commande pour arrêter l'application ici
        ;;
    1)
        echo "Redémarrage de l'application..."
        # Commande pour redémarrer l'application ici
        ;;
    *)
        echo "Valeur non reconnue. Utilisez 0 pour arrêter et 1 pour redémarrer."
        exit 3
        ;;
esac

exit 0
```

Remarques:

1. Vous devez remplacer les commentaires `# Commande pour arrêter l'application ici` et `# Commande pour redémarrer l'application ici` par les commandes réelles pour arrêter et redémarrer votre application.
2. Assurez-vous de donner les permissions d'exécution à ce script après l'avoir créé. Vous pouvez le faire avec la commande : `chmod +x nomfichier.sh`.
3. Pour exécuter le script : `./nomfichier.sh setadminstatus 0` pour arrêter et `./nomfichier.sh setadminstatus 1` pour redémarrer.
