1. Création du fichier config.properties

    Identifier les paramètres de configuration nécessaires pour le nouveau module.
    Créer et configurer le fichier config.properties.

2. Création du fichier de dictionnaire FIX

    Recueillir les attributs et les ID FIX spécifiques nécessaires.
    Créer le fichier de dictionnaire pour le protocole FIX.

3. Création du Module Java

    Initialisation de la structure de base du module.
    Développement des classes principales pour le traitement des messages.
    Implémentation des fonctionnalités de conversion et de communication.

4. Mise à Jour de la Classe Logger Constants

    Identifier les nouvelles constantes de log nécessaires pour le nouveau module.
    Mettre à jour la classe Logger Constants avec les nouvelles constantes.

5. Ajout des Endpoints Handlers

    Développement des NotificationHandlers et UrlHandlers en tant que classes Java.
    Un UrlHandler par API dans les services definitions.

6. Mise à Jour des Services Definitions

    Ajouter des définitions de services dans les UrlHandlers.
    Créer les nouveaux endpoints nécessaires.

7. Mise à Jour du Main de l'Application

    Intégrer le nouveau module dans le point d'entrée principal de l'application.
    Assurer la bonne initialisation et configuration du module.

8. Création d'un Custom Logging File pour le Nouveau Module

    Configurer un fichier de logging spécifique pour le nouveau module.
    Assurer que les logs respectent les standards de l'entreprise.

9. Intégration avec l'API Gateway

    Copier une instance existante de XEH (TEH, CEH ou GEH) et la configurer pour le nouveau module.
    Intégrer la nouvelle instance avec le nouveau module.

10. Tests Unitaires et d'Intégration

    Écrire des tests unitaires pour chaque composant du nouveau module.
    Effectuer des tests d’intégration pour valider l'interaction entre le nouveau module, l'API Gateway, et le backend.

11. Configuration des Logs

    Configurer le système de logging pour le nouveau module.
    S'assurer que les logs sont en conformité avec les standards de l'entreprise.

12. Mise à Jour de la Documentation

    Documenter la configuration et l'utilisation du nouveau module.
    Ajouter des exemples d'utilisation et des guides de dépannage.

13. Intégration de l'API Gateway avec le Reverse Proxy

    Configurer le reverse proxy pour rediriger le trafic vers l'API Gateway.
    Tester et valider l'intégration du reverse proxy avec l'API Gateway.
