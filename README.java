Objet : Explication du fonctionnement de l'application SOPA

Bonjour,

Je voulais vous donner une meilleure compréhension de l'application SOPA. D'abord, elle utilise Spring Actuator pour envoyer des rapports de statut de l'application, par exemple, le type 'UP' indique que l'application fonctionne correctement.

Ce qui est important à noter, c'est que le statut de SOPA dépend d'ICON. SOPA fait un appel REST à ICON, en particulier à l'URL "/iris/status", pour déterminer son propre statut.

SOPA sait à quelle instance de ICON se connecter en consultant un fichier de propriétés contenant une liste d'adresses IP. L'adresse spécifique à utiliser est spécifiée dans la propriété 'iris.server[0].port'.

En fonction des résultats de l'appel à ICON, SOPA détermine son propre rapport de statut. Si elle n'obtient aucune erreur ('KO') et aucune exception, elle renvoie 'OK'. Si tous les résultats sont en erreur, elle renvoie 'KO'.

Dans les autres cas, SOPA fait la somme des appels et calcule le pourcentage d'échecs. Ce pourcentage donne une indication sur l'état de santé global de l'application.

Si vous avez des questions ou besoin de plus de détails, n'hésitez pas à me le faire savoir.

