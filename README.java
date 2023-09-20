Options du Pool de Connexions

    SO_BACKLOG:
        Définit la taille de la file d'attente des connexions entrantes en attente d'acceptation. Si cette limite est atteinte, les connexions supplémentaires peuvent être rejetées.
        Valeur par défaut: dépend du système d'exploitation et du noyau. Souvent, la valeur est de 128.

    java

bootstrap.option(ChannelOption.SO_BACKLOG, 200); // Exemple pour définir à 200

SO_REUSEADDR:

    Si cette option est activée, cela signifie que le socket peut se lier à une adresse qui est encore en TIME_WAIT.
    Valeur par défaut: false

java

bootstrap.option(ChannelOption.SO_REUSEADDR, true); // Exemple pour activer

SO_KEEPALIVE:

    Permet de vérifier les connexions inactives et de s'assurer qu'elles sont toujours vivantes.
    Valeur par défaut: false

java

bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); // Exemple pour activer

TCP_NODELAY:

    Cette option est utilisée pour activer/désactiver Nagle's algorithm. Si elle est activée, cela signifie que les données seront envoyées dès que possible, et le système n'essaiera pas de regrouper plusieurs petits paquets en un paquet plus grand.
    Valeur par défaut: false
