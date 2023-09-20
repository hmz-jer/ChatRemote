Quant aux limites:

    ServerBossGroup: Comme les threads boss sont principalement responsables de l'acceptation des nouvelles connexions, leur charge est généralement plus légère que celle des threads worker. Dans la plupart des cas, même un seul thread boss est suffisant. Cependant, si vous servez un très grand nombre de connexions simultanées, vous pouvez augmenter ce nombre. Dans la pratique, un faible nombre de threads boss (1-4) est généralement suffisant pour la plupart des applications.

    ServerWorkerGroup: Le nombre idéal de threads worker dépend de la nature de votre application. Si votre application fait beaucoup de traitement CPU-intensif par connexion (par exemple, des opérations cryptographiques lourdes), vous pourriez vouloir avoir un nombre de threads proche du nombre de cœurs CPU. Si votre application est plus I/O-intensive (par exemple, attend beaucoup de données du réseau ou écrit souvent sur le réseau), vous pourriez vouloir avoir un nombre plus élevé de threads.

    Note: Le fait d'avoir trop de threads peut entraîner une contention excessive et une utilisation inefficace des ressources, en particulier dans les situations où les threads sont souvent bloqués ou en attente. Par ailleurs, trop peu de threads peut sous-exploiter vos ressources.
