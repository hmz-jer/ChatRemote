 # Guide des Bonnes Pratiques Java Moderne

## Table des matières
1. [Optional](#1-optional)
2. [Stream API](#2-stream-api)
3. [Records](#3-records)
4. [Try-with-resources](#4-try-with-resources)
5. [Pattern Matching](#5-pattern-matching)
6. [Immutabilité](#6-immutabilité)

## 1. Optional

### Introduction
Optional est une classe conteneur introduite dans Java 8 pour gérer explicitement la présence ou l'absence de valeurs. Elle aide à éviter les NullPointerException et rend le code plus expressif.

### Bonnes pratiques

#### 1.1 Utilisation basique
```java
// ✅ Bonne pratique
public Optional<User> findUserById(String id) {
    User user = database.query(id);
    return Optional.ofNullable(user);
}

// Utilisation
userService.findUserById("123")
    .ifPresent(user -> sendEmail(user));
```

#### 1.2 Chaînage des opérations
```java
// Exemple avec chaînage
public String getUserEmailById(String userId) {
    return userRepository.findById(userId)
        .filter(User::isActive)         // Vérifie si l'utilisateur est actif
        .map(User::getEmail)            // Extrait l'email
        .orElse("email non trouvé");    // Valeur par défaut
}
```

### À éviter
- Ne pas utiliser Optional comme paramètre de méthode
- Ne pas créer un Optional vide avec `Optional.of(null)`
- Ne pas utiliser Optional dans les classes d'entités ou DTOs

## 2. Stream API

### Introduction
L'API Stream fournit une approche fonctionnelle pour manipuler les collections. Elle permet d'écrire un code plus expressif et souvent plus concis.

### Exemples d'utilisation

#### 2.1 Filtrage et Transformation
```java
List<User> users = // ...
List<UserDTO> activeAdultUsers = users.stream()
    .filter(User::isActive)                // Filtre les utilisateurs actifs
    .filter(user -> user.getAge() >= 18)   // Filtre les adultes
    .map(UserDTO::fromUser)                // Transforme en DTO
    .collect(Collectors.toList());         // Collecte les résultats
```

#### 2.2 Groupement et Statistiques
```java
Map<String, Double> averageAgeByCountry = users.stream()
    .collect(Collectors.groupingBy(
        User::getCountry,
        Collectors.averagingInt(User::getAge)
    ));
```

### Bonnes pratiques
- Préférer les méthodes terminales appropriées (toList(), findFirst(), etc.)
- Utiliser parallel() avec précaution
- Éviter les effets de bord dans les opérations de stream

## 3. Records

### Introduction
Les records, introduits avec Java 16, sont des classes immuables conçues pour porter des données. Ils réduisent considérablement le code boilerplate.

### Caractéristiques
- Immutables par défaut
- equals(), hashCode(), et toString() générés automatiquement
- Construction concise
- Getters générés automatiquement

### Exemple complet
```java
public record UserDTO(
    @NotNull String id,
    @NotBlank String name,
    @Email String email,
    @Past LocalDate birthDate
) {
    // Constructeur compact avec validation
    public UserDTO {
        Objects.requireNonNull(id, "ID cannot be null");
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    // Méthode utilitaire
    public boolean isAdult() {
        return birthDate != null && 
               birthDate.until(LocalDate.now()).getYears() >= 18;
    }

    // Méthode factory
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getBirthDate()
        );
    }
}
```

## 4. Try-with-resources

### Introduction
Try-with-resources est une fonctionnalité qui assure la fermeture automatique des ressources. Elle est particulièrement utile pour la gestion des ressources comme les fichiers, les connexions réseau, etc.

### Exemple détaillé
```java
// Exemple de gestion de fichiers
public class FileProcessor {
    public String readFile(Path path) {
        // Les ressources sont automatiquement fermées
        try (BufferedReader reader = Files.newBufferedReader(path);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            
            return reader.lines()
                .collect(Collectors.joining("\n"));
                
        } catch (IOException e) {
            throw new FileProcessingException("Erreur lors de la lecture", e);
        }
    }
}
```

### Avantages
1. Fermeture automatique des ressources
2. Code plus propre et plus sûr
3. Gestion appropriée des exceptions
4. Meilleure gestion des ressources

## 5. Pattern Matching

### Introduction
Le pattern matching, introduit progressivement depuis Java 14, permet une correspondance de motifs plus expressive et plus puissante.

### Exemples

#### 5.1 Pattern Matching avec instanceof
```java
// Ancien style
if (obj instanceof String) {
    String str = (String) obj;
    // utiliser str
}

// Nouveau style avec pattern matching
if (obj instanceof String str) {
    // utiliser str directement
}
```

#### 5.2 Switch Expression avec Pattern Matching
```java
public String describeShape(Shape shape) {
    return switch (shape) {
        case Circle c -> "Cercle de rayon " + c.radius();
        case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
        case Triangle t -> "Triangle de base " + t.base();
        case null -> "Forme nulle";
    };
}
```

## 6. Immutabilité

### Introduction
L'immutabilité est un concept clé en programmation. Un objet immuable est un objet dont l'état ne peut pas être modifié après sa création.

### Avantages
1. Thread-safety naturel
2. Raisonnement plus simple sur le code
3. Pas d'effets de bord
4. Meilleure performance en cache

### Exemple d'implémentation
```java
public final class ImmutablePerson {
    private final String name;
    private final LocalDate birthDate;
    private final List<String> hobbies;

    public ImmutablePerson(String name, LocalDate birthDate, List<String> hobbies) {
        this.name = Objects.requireNonNull(name);
        this.birthDate = Objects.requireNonNull(birthDate);
        // Copie défensive
        this.hobbies = List.copyOf(hobbies);
    }

    // Getters (pas de setters)
    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public List<String> getHobbies() {
        return hobbies; // Déjà immuable grâce à List.copyOf
    }
}
```

### Règles pour créer une classe immuable
1. Marquer la classe comme `final`
2. Rendre tous les champs `private` et `final`
3. Pas de setters
4. Faire des copies défensives des objets mutables
5. Fournir uniquement des méthodes qui ne modifient pas l'état

## Conclusion

Ces bonnes pratiques modernes de Java permettent d'écrire un code :
- Plus sûr
- Plus maintenable
- Plus expressif
- Plus performant

L'utilisation combinée de ces fonctionnalités permet de créer des applications robustes et modernes, tout en réduisant la quantité de code boilerplate et les erreurs potentielles.
