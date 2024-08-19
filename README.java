[Objet :] Configuration du NUC - Instructions à suivre

Bonjour à tous,

Merci de suivre les étapes ci-dessous pour configurer correctement le NUC :

    Accès à la VM DEV Pool :
        Connectez-vous d'abord à la VM DEV Pool.
        Les informations de connexion SSH sont disponibles dans le ticket SID2613.

    Configuration du Tunnel SSH avec MobaXterm :
        Avant de configurer le proxy sur Firefox, vous devez d'abord établir le tunnel SSH en suivant les instructions fournies ici.
        Assurez-vous que cette étape est bien réalisée avant de passer à la suivante.

    Configuration du Proxy sur Firefox :
        Une fois le tunnel SSH configuré, vous pouvez configurer le proxy sur Firefox en suivant ce lien.

    Activation du Compte GitHub :
        Le lien d'activation initial envoyé par Younes est expiré. Si besoin, demandez-lui un nouveau lien.
        Une fois le compte activé, configurez l'accès SSH à GitHub et clonez le projet via le lien fourni.

    Outils déjà installés :
        Git et Maven sont déjà installés.
        Si vous avez besoin de Java 17, vous pouvez utiliser celle disponible sous /usr/lib/jvm.

    Dépendances manquantes sur Nexus :
        Certaines dépendances sont encore manquantes sous Nexus. Nous les ajouterons progressivement.

En cas de difficulté ou de questions, n’hésitez pas à me contacter.

Cordialement,
