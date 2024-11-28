 Voici une estimation détaillée ainsi que les questions à poser pour clarifier les besoins et organiser le développement de cette évolution.


---

Étapes principales et estimation du temps :

1. Analyse du fichier CSV et définition du format standard (2 jours)

Définir la structure précise du fichier CSV : noms des colonnes pour la requête, réponse, champs absents, champs présents mais vides.

Écrire un guide pour le format et valider avec les stakeholders.



2. Mise à jour du back-end Spring Boot (5 jours)

Ajouter une méthode pour traiter l’upload de fichier CSV via une requête HTTP.

Lire et parser le fichier CSV en objets (par exemple, utiliser des librairies comme OpenCSV).

Adapter la logique métier pour générer et envoyer les requêtes en fonction des valeurs du CSV.

Vérifier la réponse et comparer avec les attentes du fichier CSV :

Gestion des champs absents (null).

Gestion des champs présents mais vides (non renseigné).

Vérification des scénarios sans réponse ou temporisation.




3. Gestion des scénarios spécifiques (2 jours)

Scénario sans réponse : implémenter un mécanisme pour détecter une absence de réponse (timeout).

Scénario avec temporisation : permettre de spécifier un délai maximal (par exemple, via une colonne timeout dans le fichier CSV).



4. Mise à jour du front-end React (3 jours)

Ajouter une interface pour uploader le fichier CSV.

Afficher les résultats des vérifications (succès/échec pour chaque requête).

Permettre le téléchargement ou l'affichage des logs des vérifications.



5. Tests unitaires et intégration (4 jours)

Écrire des tests unitaires pour chaque partie de la logique (parsing, envoi de requêtes, vérifications).

Tester l'application avec différents fichiers CSV pour s'assurer de la robustesse.



6. Documentation et déploiement (2 jours)

Rédiger une documentation technique et utilisateur pour la nouvelle fonctionnalité.

Déployer la nouvelle version et s’assurer de son bon fonctionnement.





---

Estimation totale : ~18 jours ouvrables


---

Questions à poser :

Sur le format et le contenu du fichier CSV :

1. Quelle est la structure exacte des colonnes du fichier CSV ? Exemple d’un fichier attendu ?


2. Comment gérer les colonnes optionnelles ou absentes ? (Par exemple, si une colonne de réponse n’est pas définie.)


3. Quelle convention utiliser pour les valeurs absentes (null, vide, ou autre ?) ?



Sur les scénarios spécifiques :

4. Quelle est la durée maximale pour une temporisation (timeout) ? Faut-il permettre de configurer cette valeur globalement ?


5. Que faire en cas de réponse partielle ou de format inattendu dans la réponse ?


6. Faut-il enregistrer les logs des requêtes/réponses pour les vérifier ultérieurement ?



Sur les contraintes techniques :

7. Quelle taille maximale pour le fichier CSV ? Y a-t-il des limites de performance à considérer ?


8. Faut-il stocker les fichiers CSV sur le serveur après traitement ou simplement traiter les données en mémoire ?


9. Y a-t-il une gestion particulière des erreurs (par exemple, que faire si une requête échoue) ?



Sur l’interface utilisateur :

10. L’upload du fichier CSV doit-il être sécurisé (authentification/autorisation) ?


11. Faut-il un système de notification ou d’alerte pour signaler les erreurs de validation ?




---

Avec ces clarifications, il sera possible de finaliser le périmètre du projet et d'ajuster l'estimation si nécessaire.

