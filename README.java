
Cher(e) collègue,

Je suis heureux/se de vous annoncer que notre travail acharné a finalement porté ses fruits! Nous avons réussi à livrer vendredi, et je tiens à vous remercier pour votre engagement et votre dévouement envers ce projet.

Je suis ravi(e) de partager avec vous les résultats de nos travaux :

Tout d'abord, nous avons testé avec succès la connexion entre le serveur de développement et IRIS en utilisant un ancien manuel d'utilisation d'IRIS. Nous avons réussi à comprendre les paramètres que IRIS prend en compte, et nous avons pu voir une réponse JSON à travers l'interface de contrôle de message d'IRIS.

De plus, nous avons développé notre propre application de simulation qui remplace IRIS en environnement de développement, et qui nous renvoie également des réponses JSON. Cette application de simulation a été déployée sur le serveur de développement, et nous l'utilisons également pour générer le contrat d'interface, qui est techniquement le fichier swagger.

En ce qui concerne la passerelle API, nous avons récupéré la configuration de l'équipe Axway, et avons reconstruit la configuration de création de jetons afin de sécuriser nos API. Nous avons également créé nos propres API de backend au niveau de l'APIG, et développé nos politiques qui permettent de modifier les requêtes HTTP et d'ajouter les attributs et les modèles nécessaires pour la communication avec IRIS. Enfin, nous avons virtualisé nos API et les avons sécurisées à travers les jetons.

En parallèle de tout cela, nous avons travaillé sur des outils de test d'API qui facilitent le processus de création, de test et de documentation d'API. Nous avons utilisé Postman, un outil graphique qui permet d'envoyer des requêtes et de déboguer les réponses. Il permet également de gérer les autorisations d'accès, de créer des environnements de test et même de faire des tests de charge. Actuellement, nous travaillons sur Newman, un outil de ligne de commande qui permet d'exécuter des collections de tests API créées avec Postman. Newman nous aidera côté développement à automatiser les tests et les appels d'API, et peut également être utilisé côté build dans la chaîne CI/CD.

En résumé, Postman est utilisé pour créer, tester et documenter des API, tandis que Newman est utilisé pour automatiser les tests de ces API.

Le build du projet EPI est terminé, et nous attendons le feu vert dès que le RPM sera sur le serveur APIM d'AWS. Encore une fois, je tiens à vous féliciter pour votre excellent travail et je suis ravi(e) de travailler avec une équipe aussi compétente et déterminée.

Cordialement,


Je suis désolé(e) de ne pas avoir mentionné ce point crucial dans mon précédent discours. Nous avons également pris soin de documenter tous les travaux réalisés en environnement de développement, afin que nous puissions utiliser cette documentation pour préparer l'environnement AWS.

Cette documentation détaillera toutes les étapes nécessaires pour déployer l'application avec succès sur AWS. Nous pourrons nous y référer pour configurer correctement les serveurs et les services nécessaires à la mise en production de notre application.

Je tiens à souligner l'importance de cette documentation, car elle assurera une transition en douceur de l'environnement de développement à l'environnement de production. Nous pourrons ainsi garantir une stabilité et une fiabilité maximales de notre application.

Encore une fois, merci à toute l'équipe pour votre travail acharné et votre dévouement envers ce projet. Nous sommes sur la bonne voie pour accomplir de grandes choses ensemble.

Les "policies" de l'API Gateway d'Axway sont des ensembles de règles et d'actions prédéfini au niveau de l’APIG et ils vont étre excuté lorsque on recoit  des requêtes provenant du clientes.
Ces politiques peuvent être utilisées pour effectuer une variété de tâches, telles que :
    • L'authentification et l'autorisation des utilisateurs et des applications clientes
    • La validation et la transformation des messages entrants et sortants
