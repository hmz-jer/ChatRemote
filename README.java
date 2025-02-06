**Normes de développement Java pour l'entreprise**

## 1. Langage Java J2E

### 1.1 Organisation des fichiers
#### 1.1.1 Les fichiers sources Java
- Les fichiers sources Java doivent être bien organisés dans une hiérarchie de packages respectant la convention : **com.entreprise.projet.module**.
- Chaque fichier doit correspondre à une seule classe publique, et le nom du fichier doit refléter le nom de la classe.
- Exemple de hiérarchie de fichiers :
```
com
└── entreprise
    └── projet
        ├── service
        │   └── ServiceExample.java
        ├── model
        │   └── ExampleModel.java
        └── util
            └── ExampleUtils.java
```

### 1.2 Exemple de code
```java
package com.entreprise.projet.module;

/**
 * Exemple de classe démontrant les bonnes pratiques de développement.
 */
public class Exemple {
    /**
     * Méthode principale du programme.
     * @param args Arguments de la ligne de commande
     */
    public static void main(String[] args) {
        System.out.println("Bonjour, entreprise !");
    }
}
```

### 1.3 Organisation d’un fichier
#### 1.3.1 Les Directives de nommage
- **Classes** : UpperCamelCase
- **Interfaces** : commencent par un **I** (ex: `IService`)
- **Variables et méthodes** : lowerCamelCase
- **Constantes** : UPPER_CASE

### 1.4 Les commentaires
#### 1.4.1 Rédaction de commentaires sur les documents
- Tous les fichiers doivent contenir un en-tête avec des informations sur l’auteur, la date, et une description :
```java
/**
 * Classe démonstrative pour l'entreprise.
 * 
 * Auteur : John Doe
 * Date : 05/02/2025
 */
```
#### 1.4.2 Descriptions
- Chaque classe et méthode publique doit inclure des descriptions via JavaDoc pour expliquer leur rôle.

#### 1.4.3 Conventions et ordre des balises
- Respecter cet ordre dans les JavaDocs :
  - Description générale
  - `@param` pour les paramètres
  - `@return` pour la valeur retournée
  - `@throws` pour les exceptions

#### 1.4.4 Exemple de code avec des commentaires
```java
/**
 * Multiplie deux nombres entiers.
 * 
 * @param a Premier entier
 * @param b Deuxième entier
 * @return Le produit des deux entiers
 */
public int multiplier(int a, int b) {
    return a * b;
}
```

### 1.5 Indentation
#### 1.5.1 Longueur d’une ligne
- Limiter la longueur d’une ligne de code à 120 caractères pour une meilleure lisibilité.

#### 1.5.2 Expressions nécessitant plusieurs lignes
- Utiliser une indentation appropriée pour les expressions complexes :
```java
String result = condition ? "Valeur 1" :
                  "Valeur 2";
```

### 1.6 Lignes blanches
#### 1.6.1 Les espaces
- Ajouter une ligne blanche entre les différentes sections logiques du code pour une meilleure clarté.

### 1.7 Codage sécurisé
#### 1.7.1 Guide du codage sécurisé
##### 1.7.1.1 Gestion des ressources
- **DOS-1** : Soyez vigilant avec les activités consommant des ressources disproportionnées.
- **DOS-2** : Libérez systématiquement les ressources dans tous les cas.
- **DOS-3** : Assurez-vous que les vérifications des limites des ressources ne provoquent pas de dépassements.

##### 1.7.1.2 Information confidentielle
- **CONFIDENTIAL-1** : Purgez les informations sensibles des exceptions.
- **CONFIDENTIAL-2** : Ne journalisez jamais d'informations hautement sensibles.
- **CONFIDENTIAL-3** : Envisagez de purger la mémoire contenant des données sensibles après utilisation.

##### 1.7.1.3 Injection et inclusion
- **INJECT-1** : Générez des formats valides pour les entrées utilisateurs.
- **INJECT-2** : Évitez les requêtes SQL dynamiques.
- **INJECT-3** : Prenez soin lors de la génération d'XML et HTML.
- **INJECT-4** : Ne faites jamais confiance aux données passées via la ligne de commande.
- **INJECT-5** : Restreignez l'inclusion XML.

##### 1.7.1.4 Entrées utilisateur (INPUT)
- **INPUT-1** : Validez toutes les entrées utilisateur avant traitement.
- **INPUT-2** : Évitez les injections de commande en limitant les caractères spéciaux.
- **INPUT-3** : Utilisez des librairies standard pour parser les données utilisateur (ex : Jackson pour JSON).

##### 1.7.1.5 Objets Mutables (MUTABLE)
- **MUTABLE-1** : Réduisez l'accès aux champs d’instance mutables en les rendant privés.
- **MUTABLE-2** : Utilisez des objets immuables autant que possible, par exemple avec les `record` en Java 17.
- **MUTABLE-3** : Clonez les objets mutables avant de les exposer.

##### 1.7.1.6 Objets (OBJECT)
- **OBJECT-1** : Redéfinissez toujours `equals` et `hashCode` ensemble pour assurer la cohérence.
- **OBJECT-2** : Évitez de référencer des objets supprimés pour prévenir les fuites mémoire.
- **OBJECT-3** : Utilisez `try-with-resources` pour gérer les objets implémentant `AutoCloseable`.
- **OBJECT-4** : Limitez l'exposition des instances de `ClassLoader`. Ne chargez que les classes strictement nécessaires pour éviter des vulnérabilités de type ClassLoader hijacking.
- **OBJECT-5** : Limitez l'extensibilité des classes et des méthodes pour empêcher des comportements inattendus dans les sous-classes.

##### 1.7.1.7 Sérialisation (SERIAL)
- **SERIAL-1** : Évitez la sérialisation native Java, utilisez des bibliothèques modernes comme Jackson ou Gson.
- **SERIAL-2** : Implémentez une validation personnalisée lors de la désérialisation.
- **SERIAL-3** : N’exposez jamais directement des données sensibles via des objets sérialisés.
- **SERIAL-4** : Assurez-vous que les classes sérialisées implémentent l'interface `Serializable` de manière cohérente.
- **SERIAL-5** : Redéfinissez la méthode `readObject` pour protéger contre des données manipulées.

##### 1.7.1.8 Accessibilité et extensibilité (ACCESS)
- **ACCESS-1** : Limitez l'accessibilité des classes, interfaces, méthodes et champs au minimum nécessaire.
- **ACCESS-2** : Évitez l'accès public ou protégé à des champs mutables.
- **ACCESS-3** : Utilisez les annotations comme `@Deprecated` pour indiquer les API obsolètes.
- **ACCESS-4** : Restreignez l'accès aux classes internes pour éviter des abus potentiels.

#### 1.7.2 Directives de codage sécurisé pour Java SE
##### 1.7.2.1 Expressions modernes
- **Utilisez le switch amélioré** (Java 17) pour améliorer la lisibilité :
```java
String result = switch (day) {
    case MONDAY, FRIDAY, SUNDAY -> "Weekend";
    case TUESDAY -> "Busy day";
    default -> "Regular day";
};
```

##### 1.7.2.2 Gestion de la mémoire
- Utilisez des outils modernes comme `try-with-resources` pour éviter les fuites de ressources.
```java
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    e.printStackTrace();
}
```

##### 1.7.2.3 Cryptographie
- Utilisez des algorithmes standards sécurisés comme AES pour le chiffrement des données sensibles.
```java
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
```

### 1.8 Norme de codage Oracle SEI CERT pour Java
#### 1.8.1 Règles
- Respectez les règles strictes pour éviter les vulnérabilités liées aux données et à la sérialisation.

#### 1.8.2 Recommandations
- Préférez l'utilisation de bibliothèques modernes pour la sécurité (Spring Security, etc.).

## 2. Gestion des exceptions
- Toujours capturer les exceptions spécifiques plutôt que des exceptions générales.
- Exemple :
```java
try {
    operationCritique();
} catch (IOException e) {
    LOGGER.error("Erreur lors de l'opération critique", e);
}
```
- Toujours inclure un message explicite lors de la levée d’une exception :
```java
throw new IllegalArgumentException("Paramètre invalide : " + param);
```

## 3. Tests unitaires
- Utilisation de **JUnit** et **Mockito** pour les tests unitaires.
- Exemple de test unitaire simple :
```java
@Test
public void testMultiplier() {
    Exemple exemple = new Exemple();
    assertEquals(6, exemple.multiplier(2, 3));
}
```

## 4. Revue de code et validation
- Soumettre le code via une **pull request** sur le dépôt Git.
- Utilisation de **SonarQube** pour analyser la qualité du code.

## 5. Sécurité et propriété intellectuelle
- Inclure dans chaque fichier la mention suivante :
```java
// Ce code est la propriété de [Nom de l’entreprise] et ne peut pas être utilisé en dehors.
```
- Toute dérogation à ces normes doit être approuvée par un architecte technique.

## Conclusion
Ce document constitue une référence essentielle pour maintenir la qualité, la sécurité et la maintenabilité des projets Java au sein de l’entreprise. En respectant ces normes, chaque développeur contribue à la création de solutions robustes et performantes, tout en garantissant la conformité aux meilleures pratiques de l'industrie. Toute amélioration ou mise à jour doit être discutée et validée par les équipes concernées afin d'assurer une évolution continue des standards de développement.

