
Requête de l'API Gateway vers Kafka :

    Génération d'un identifiant unique pour chaque requête.
    Envoi de la requête à Kafka avec l'identifiant unique.
    Sauvegarde de l'identifiant dans MongoDB pour correspondance future.
    Attente de l'accusé de réception via un autre topic Kafka.
    Mise à jour de l'état de la requête dans MongoDB en fonction de l'accusé de réception.
    Envoi d'un accusé de réception à l'API Gateway après traitement.

Notification de Kafka vers l'API Gateway :

    Réception des notifications via un topic dédié.
    Génération d'un identifiant pour chaque notification.
    Envoi de la notification à l'API Gateway avec l'identifiant.
    Sauvegarde de l'identifiant dans MongoDB pour correspondance future.
    Attente de la réponse de l'API Gateway.
    Envoi de la réponse de l'API Gateway à un autre topic Kafka.
