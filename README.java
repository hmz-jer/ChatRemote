Bonjour à tous,

Je tiens à vous informer que le problème de lenteur rencontré au niveau de la commande manage.sh status a été résolu.

Nous avons remplacé l'utilisation de Kafka Admin par Kafka Metrics. Cette modification nous permet d'obtenir une réponse plus rapide et nous évite de créer un Kafka Admin à chaque exécution de la commande.

Nous allons continuer à tester cette solution sur l'environnement de développement (DEV) afin de nous assurer de sa stabilité et de son efficacité avant de la livrer en production.

Merci de votre patience et de votre compréhension.

Cordialement,
