 # Questions à clarifier - Maquettes Application CardArt

## 🔍 PAGE ACCUEIL ET RECHERCHE

### 1. **Logique de recherche par Banque**
- L'interface sera-t-elle **dédiée à chaque banque** ou **centralisée pour toutes les banques** ?
- Si l'interface est dédiée par banque, pourquoi proposer une recherche par "Code Banque" ?
- Qui aura accès à quoi : un utilisateur "Global Bank" voit-il les CardArts d'autres banques ?

### 2. **Types de cartes Visa/MasterCard**
- Une même banque peut-elle émettre à la fois des cartes **Visa ET MasterCard** ?
- Si oui, un même CardArt peut-il être utilisé pour les deux réseaux ou faut-il un CardArt distinct par réseau ?
- Quelle est la différence métier entre "CA-CbVisa" et "CA-CbMaster" ?

### 3. **Colonne CardArt ID**
- Dans la colonne "CardArt ID (CardArtRefId)", que faut-il afficher exactement :
  - L'**identifiant unique** du CardArt (ex: 0001GB01) ?
  - Le **type de carte** (ex: CA-CbVisa) ?
  - Les **deux informations** ?
- Quelle est la différence entre le "CardArt ID" et le "Code Banque" ?
- Cet identifiant est-il généré automatiquement ou saisi manuellement ?

### 4. **Bouton "Afficher tout"**
- À quoi sert le bouton "Afficher tout" si on a déjà des critères de recherche ?
- Doit-il :
  - Ignorer tous les filtres et afficher tous les CardArts ?
  - Réinitialiser les filtres ?
  - Être supprimé ?

### 5. **Notifications "Pas de visuel par défaut"**
- Comment peut-on avoir une notification "pas de visuel par défaut" si le visuel est **obligatoire** lors de la création ?
- Existe-t-il des états intermédiaires où un CardArt peut exister sans visuel (brouillon, en cours de validation) ?
- Peut-on supprimer le visuel d'un CardArt après sa création ?

### 6. **Colonne "CardArt Visuel"**
- Que doit contenir exactement la colonne "CardArt Visuel (CardArtImg)" :
  - Le nom du fichier image ?
  - Une miniature de la carte ?
  - Un statut "Présent/Absent" ?
- Est-elle utile si on affiche déjà la miniature de la carte ailleurs ?

### 7. **Tri des colonnes**
- Toutes les colonnes doivent-elles être triables ?
- Sur quels critères les utilisateurs trient-ils habituellement leurs CardArts ?
- Le tri sur "CardArt Visuel" a-t-il un sens métier ?

### 8. **Affichage de l'heure**
- Pourquoi afficher la date et l'heure actuelles dans l'interface ?
- Y a-t-il un besoin métier spécifique (traçabilité, sessions temporisées, deadlines) ?
- Cette information est-elle réellement utile aux utilisateurs ?

### 9. **Navigation générale**
- Comment l'utilisateur navigue-t-il entre les différentes banques (si interface centralisée) ?
- Y a-t-il des niveaux de droits différents selon les profils utilisateurs ?
- Qui peut créer/modifier/supprimer des CardArts ?

---

## 🎨 ÉCRAN CRÉATION DE VISUEL

### 1. **Logique du CardArt ID**
- Comment se compose exactement le **CardArt ID** :
  - L'utilisateur choisit d'abord le **type de carte** (Visa/MasterCard) puis saisit un **nom libre** ?
  - Le format final est-il : `[Type]-[Nom saisi]` (ex: CA-CbVisa-MonNom) ?
  - Ou bien : `[Code Banque]-[Type]-[Nom]` (ex: GB-Visa-MonNom) ?
- Y a-t-il des **règles de nommage** à respecter (longueur, caractères autorisés) ?
- Le système vérifie-t-il l'**unicité** du nom saisi ?

### 2. **Sélection du type de carte**
- Les **radio buttons** CA-CbVisa / CA-CbMaster :
  - Définissent-ils le début de l'identifiant ?
  - Ont-ils un impact sur les règles graphiques du visuel ?
  - Conditionnent-ils d'autres champs du formulaire ?

### 3. **Gestion des visuels multiples**
- Si on peut avoir **plusieurs visuels par CardArt** :
  - Comment désigne-t-on le **visuel par défaut** ?
  - Y a-t-il une case à cocher "Définir comme défaut" ?
  - Le premier visuel créé devient-il automatiquement le défaut ?
  - Peut-on changer le visuel par défaut après création ?

### 4. **Organisation des visuels**
- Un CardArt peut-il avoir :
  - **Plusieurs versions** d'un même visuel (v1, v2, v3) ?
  - **Plusieurs formats** du même visuel (PNG, JPG, SVG) ?
  - **Plusieurs déclinaisons** (avec/sans nom, différentes couleurs) ?
- Comment l'utilisateur **distingue-t-il** ces différents visuels ?

### 5. **Affichage Banque (Code/Nom)**
- Pourquoi afficher le **code banque** et **nom banque** si l'application est dédiée à une seule banque ?
- Ces informations sont-elles :
  - **Pré-remplies automatiquement** selon l'utilisateur connecté ?
  - **Modifiables** par l'utilisateur ?
  - **Juste informatives** ?
- Y a-t-il des cas où un utilisateur d'une banque pourrait créer des CardArts pour une autre banque ?

### 6. **Processus de création**
- Quel est l'**ordre obligatoire** de saisie :
  1. Type de carte → Nom → Upload visuel ?
  2. Tous les champs peuvent être remplis dans n'importe quel ordre ?
- Peut-on **sauvegarder un brouillon** sans visuel ?
- Y a-t-il une **validation** avant création définitive ?

### 7. **Règles de gestion**
- Si plusieurs visuels par CardArt :
  - Comment les **numéroter/nommer** (Visuel 1, Visuel 2, ou noms spécifiques) ?
  - Peut-on **réordonner** les visuels ?
  - Y a-t-il une **limite** au nombre de visuels par CardArt ?

### 8. **Comportement de la zone de saisie**
- La "zone de saisie" pour le nom :
  - A-t-elle des **suggestions automatiques** ?
  - Vérifie-t-elle la **disponibilité** du nom en temps réel ?
  - Formate-t-elle automatiquement le texte (majuscules, caractères spéciaux) ?

### 9. **Prévisualisation**
- L'utilisateur peut-il **prévisualiser** le CardArt ID final avant validation ?
- Y a-t-il un **aperçu** de ce à quoi ressemblera l'identifiant complet ?

### 10. **Cas d'usage concret**
- **Scenario** : Si je veux créer un CardArt "Carte Gold Visa" :
  - Je sélectionne "CA-CbVisa"
  - Je saisis "Gold" dans la zone
  - Le résultat final sera "CA-CbVisa-Gold" ?
- Et pour créer un **deuxième visuel** de cette même carte, comment procède-t-on ?

---

## 📋 SYNTHÈSE DES POINTS CRITIQUES

### **🚨 Incohérences détectées**
1. **Interface par banque** vs **recherche multi-banques**
2. **Visuel obligatoire** vs **notifications "pas de visuel"**
3. **CardArt ID** : identifiant technique ou type de carte ?
4. **Colonne redondante** CardArt Visuel vs miniature
5. **Utilité de l'heure** dans l'interface

### **🎯 Clarifications urgentes**
1. **Modèle de données** : CardArt → Visuels (1 à n)
2. **Règles métier** : création, modification, suppression
3. **Niveaux d'accès** : qui fait quoi selon son profil
4. **Format des identifiants** : convention de nommage
5. **Gestion des défauts** : visuel principal par CardArt

---

*Ces questions permettront d'ajuster les maquettes selon les vraies règles métier et d'éviter les incohérences de conception.*
