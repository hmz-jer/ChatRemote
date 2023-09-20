**Rapport d'Analyse de l'Application TH-PROXY Utilisant Netty**

**1. Gestion du FailOver dans Netty:**

Netty est une framework asynchrone basée sur des événements qui ne fournit pas une solution native de failover. Cependant, elle offre la flexibilité nécessaire pour mettre en œuvre diverses stratégies de gestion des erreurs.

- **Détection d'Erreurs :**
  - La méthode `exceptionCaught()` est souvent utilisée pour détecter les exceptions au niveau d'une connexion.
  - Dans le cas d'une défaillance détectée, vous pouvez décider de fermer la connexion, ou d'essayer de la récupérer selon le type d'exception.

  ```java
  Channel ch = ctx.channel();
  if(ch.isActive()) {
      ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
  }
  ```

- **Stratégie de Reconnexion :**
  - Une stratégie de reconnexion peut être mise en œuvre en utilisant un nouvel objet `Bootstrap`. 
  - Lorsqu'une tentative de connexion échoue ou qu'une connexion active est perdue, une nouvelle tentative de connexion peut être initiée après un délai.

**2. Load Balancing Fonctionnel dans TH-PROXY:**

Plutôt que d'agir comme un load balancer traditionnel qui répartit la charge en fonction de la capacité des serveurs, TH-PROXY semble fonctionner sur une logique de routage conditionnel.

- **Logique de Routage :**
  - Il y a deux modes principaux : `tghMode` et un mode non-`tghMode`.
    - En mode `tghMode`, TH-PROXY route la requête basée sur une IP spécifique.
    - En mode non-`tghMode`, la logique se base sur la version min, la version max et le nom du service.
  - Une liste des `sasHandlers` actifs est continuellement mise à jour pour déterminer la destination appropriée pour chaque requête.

**3. Pools de Connexions et Limitations dans Netty:**

- **NioEventLoopGroup :**
  - Netty utilise `NioEventLoopGroup` pour gérer les threads. Chaque `NioEventLoopGroup` contient plusieurs `EventLoops`.
  - Typiquement, deux instances de `NioEventLoopGroup` sont utilisées: 
    - `serverBossGroup`: traite les connexions entrantes.
    - `serverWorkerGroup`: traite les événements de lecture et d'écriture une fois que la connexion est établie.

- **Détermination du Nombre de Threads :**
  - La valeur optimale pour le nombre de threads dépend de nombreux facteurs, notamment la nature de l'application, la capacité du système et la charge attendue.
  - Une règle générale souvent utilisée pour les `EventLoop` workers est `Nombre de CPU * 2`. Cela permet d'exploiter pleinement la capacité multicœur des machines modernes.

- **Recommandations :**
  - Si TH-PROXY est principalement I/O-bound (lié aux opérations d'entrée/sortie), augmenter le nombre de threads peut aider. 
  - Une machine avec un grand nombre de cœurs (par exemple, 8, 16, ou 32) sera bénéfique pour traiter des milliers de requêtes simultanément.
  - Des tests de performance sont essentiels pour déterminer la configuration optimale.

---
