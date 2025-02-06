# HISTORIQUE DES VERSIONS DU DOCUMENT

Version 2.0 - Mise à jour pour Java 17 LTS
Dernière modification : 05/02/2025

# 1. LANGAGE JAVA 17

## 1.1. Organisation des fichiers

### 1.1.1. Les fichiers sources Java
- Extension `.java` obligatoire
- Un fichier par classe publique
- Nom du fichier identique au nom de la classe publique
- Structure de packages cohérente reflétant la hiérarchie du projet
- Organisation en modules avec `module-info.java` à la racine
- Respect des conventions Maven/Gradle pour la structure du projet :
  ```
  src/
    main/
      java/
        module-info.java
        com/
          company/
            project/
    test/
      java/
        com/
          company/
            project/
  ```

## 1.2. Exemple de code moderne
```java
// Exemple d'utilisation des fonctionnalités modernes de Java 17
public sealed interface Shape permits Circle, Rectangle {
    double area();
}

public record Circle(double radius) implements Shape {
    public Circle {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

public record Rectangle(double width, double height) implements Shape {
    public Rectangle {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
    }

    @Override
    public double area() {
        return width * height;
    }
}
```

## 1.3. Organisation d'un fichier

### 1.3.1. Les Directives de nommage
- Packages: minuscules, sans underscore (`com.entreprise.projet`)
- Classes: PascalCase (`UserService`)
- Interfaces: PascalCase (`Printable`)
- Méthodes: camelCase (`calculateTotal`)
- Variables: camelCase (`userName`)
- Constants: SNAKE_CASE majuscule (`MAX_CONNECTIONS`)
- Modules: minuscules, avec points (`com.entreprise.core`)

## 1.4. Les commentaires

### 1.4.1. Rédaction de commentaires sur les documents
```java
/**
 * Service responsable de la gestion des utilisateurs.
 * Implémente les opérations CRUD et la logique métier associée.
 *
 * @author Équipe Backend
 * @version 2.0
 * @since 17
 */
@ThreadSafe
public final class UserService {
    // Implémentation
}
```

### 1.4.2. Descriptions
Les commentaires doivent être :
- Clairs et concis
- En français ou en anglais (cohérence dans le projet)
- Pertinents et à jour
- Focalisés sur le "pourquoi" plutôt que le "comment"

### 1.4.3. Conventions et ordre des balises
1. Description générale
2. @param
3. @return
4. @throws
5. @since
6. @deprecated (si applicable)
7. @author (optionnel)
8. @see (optionnel)

### 1.4.4. Exemple de code avec des commentaires
```java
/**
 * Gère les opérations de paiement de manière thread-safe.
 * Utilise le pattern Strategy pour supporter différents processeurs de paiement.
 */
public sealed interface PaymentOperation 
    permits CreditCardPayment, BankTransfer {
    
    /**
     * Exécute l'opération de paiement.
     *
     * @param amount montant à traiter
     * @return résultat de l'opération contenant l'ID de transaction
     * @throws InsufficientFundsException si le solde est insuffisant
     * @throws PaymentProcessingException en cas d'erreur technique
     * @since 17
     */
    TransactionResult execute(BigDecimal amount);
}
```

## 1.5. Indentation

### 1.5.1. Longueur d'une ligne
- Maximum 120 caractères
- Préférer des lignes plus courtes pour la lisibilité
- Indentation de 4 espaces (pas de tabulations)
- Alignement cohérent des paramètres

```java
// Bon exemple de formatage
public record CustomerProfile(
    String id,
    String firstName,
    String lastName,
    String email,
    LocalDate birthDate
) {
    public CustomerProfile {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
    }
}
```

### 1.5.2. Expressions nécessitant plusieurs lignes
```java
// Exemple avec text blocks pour SQL
String query = """
    SELECT u.id, u.name,
           u.email, u.status,
           COUNT(o.id) as order_count
    FROM users u
    LEFT JOIN orders o ON u.id = o.user_id
    WHERE u.status = 'ACTIVE'
    GROUP BY u.id, u.name, u.email, u.status
    HAVING COUNT(o.id) > 0
    """;

// Chaînage de méthodes
List<String> activeUsers = users.stream()
    .filter(user -> user.isActive())
    .map(User::getName)
    .sorted()
    .collect(Collectors.toList());
```

## 1.6. Lignes blanches

### 1.6.1. Les espaces
- Une ligne vide entre les méthodes
- Une ligne vide après les blocs d'imports
- Une ligne vide entre les classes
- Pas de lignes vides excessives
- Regroupement logique des imports

```java
package com.company.project;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

import com.company.project.model.User;
import com.company.project.service.UserService;

public final class UserManager {
    private final UserService userService;
    
    public UserManager(UserService userService) {
        this.userService = Objects.requireNonNull(userService);
    }
    
    public Optional<User> findActiveUser(String id) {
        return userService.findById(id)
            .filter(User::isActive);
    }
}
```

## 1.7. Codage sécurisé

### 1.7.1. Guide du codage sécurisé
Principes fondamentaux :
- Validation des entrées
- Principe du moindre privilège
- Defense en profondeur
- Fail-safe defaults
- Économie de mécanisme

### 1.7.2. Directives de codage sécurisé pour Java SE

#### 1.7.2.1. Déni de Service (DOS)

★★★☆ **Guideline 1-1 / DOS-1**: Attention aux activités pouvant utiliser des ressources disproportionnées
```java
public class ResourceManager {
    private static final int MAX_BUFFER_SIZE = 8192;
    private static final int MAX_ARRAY_SIZE = 1000000;

    // Bon exemple
    public byte[] processData(byte[] input) {
        if (input.length > MAX_BUFFER_SIZE) {
            throw new IllegalArgumentException("Input too large");
        }
        return Arrays.copyOf(input, input.length);
    }
}
```

★★★★ **Guideline 1-2 / DOS-2**: Libérer les ressources dans tous les cas
```java
public class FileProcessor {
    public String readFile(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            return reader.lines()
                .collect(Collectors.joining("\n"));
        }
    }
}
```

#### 1.7.2.2. Information Confidentielle

★★★☆ **Guideline 2-1 / CONFIDENTIAL-1**: Purger les informations sensibles des exceptions
```java
public class SecurityManager {
    public void authenticate(String username, String password) {
        try {
            // Process authentication
        } catch (Exception e) {
            // Bon exemple : pas d'information sensible dans le message
            throw new SecurityException("Authentication failed");
        }
    }
}
```

★★★★ **Guideline 2-2 / CONFIDENTIAL-2**: Ne pas logger d'informations sensibles
```java
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public void processUserData(UserCredentials credentials) {
        // Bon exemple
        logger.info("Processing user: {}", credentials.username());
        // Mauvais exemple
        // logger.info("User {} with password {}", credentials.username(), credentials.password());
    }
}
```

#### 1.7.2.3. Injection et Inclusion

★★★★ **Guideline 3-1 / INJECT-1**: Générer un formatage valide
```java
public class DatabaseService {
    public List<User> findUsers(String searchTerm) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchTerm);
            return processResultSet(stmt.executeQuery());
        }
    }
}
```

★★★☆ **Guideline 3-2 / INJECT-2**: Éviter le SQL dynamique
```java
public class QueryBuilder {
    private static final Map<String, String> VALID_SORT_COLUMNS = Map.of(
        "name", "username",
        "date", "creation_date",
        "status", "user_status"
    );

    public String buildSortQuery(String userInput) {
        String column = VALID_SORT_COLUMNS.getOrDefault(userInput, "id");
        return "SELECT * FROM users ORDER BY " + column;
    }
}
```

## 1.8. Exemples de fonctionnalités Java 17

### 1.8.1. Pattern Matching pour switch
```java
public class TypeHandler {
    public String processValue(Object obj) {
        return switch (obj) {
            case String s -> handleString(s);
            case Integer i -> handleInteger(i);
            case List<?> l -> handleList(l);
            case null -> "null value";
            default -> handleUnknown(obj);
        };
    }
    
    private String handleString(String s) {
        return "String length: " + s.length();
    }
    
    private String handleInteger(Integer i) {
        return "Integer value: " + i;
    }
    
    private String handleList(List<?> l) {
        return "List size: " + l.size();
    }
    
    private String handleUnknown(Object obj) {
        return "Unknown type: " + obj.getClass().getSimpleName();
    }
}
```

### 1.8.2. Records pour les données immuables
```java
public record CustomerDTO(
    String id,
    String name,
    String email,
    LocalDate birthDate
) {
    // Validation dans le constructeur compact
    public CustomerDTO {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    // Méthodes dérivées
    public boolean isAdult() {
        return birthDate != null && 
               birthDate.until(LocalDate.now(), ChronoUnit.YEARS) >= 18;
    }

    public String getDisplayName() {
        return name.trim() + " (" + id + ")";
    }
}
```

### 1.8.3. Sealed Classes
```java
public sealed interface PaymentMethod 
    permits CreditCard, BankTransfer, DigitalWallet {
    
    PaymentResult process(BigDecimal amount);
    String getDisplayName();
}

public final record CreditCard(
    String cardNumber,
    String expiryDate,
    String cvv
) implements PaymentMethod {
    @Override
    public PaymentResult process(BigDecimal amount) {
        // Implémentation sécurisée
        return PaymentResult.success("CC-" + cardNumber.substring(12));
    }

    @Override
    public String getDisplayName() {
        return "Credit Card ****" + cardNumber.substring(12);
    }
}

public final record BankTransfer(
    String iban,
    String bic
) implements PaymentMethod {
    @Override
    public PaymentResult process(BigDecimal amount) {
        return PaymentResult.pending("BT-" + iban.substring(iban.length() - 4));
    }

    @Override
    public String getDisplayName() {
        return "Bank Transfer " + iban.substring(iban.length() - 4);
    }
}
```

### 1.8.4. Modules
```java
// module-info.java
module com.company.application {
    // APIs publiques
    exports com.company.application.api;
    exports com.company.application.model;
    
    // Exports qualifiés
    exports com.company.application.internal to 
        com.company.application.test;
    
    // Dépendances
    requires java.base;
    requires java.sql;
    requires static lombok;
    requires transitive com.fasterxml.jackson.databind;
    
    // Services
    uses com.company.application.spi.PaymentProcessor;
    provides com.company.application.spi.PaymentProcessor with
        com.company.application.internal.DefaultPaymentProcessor;
}
```

### 1.8.5. Text Blocks
```java
public class QueryRepository {
    private static final String COMPLEX_QUERY = """
        SELECT 
            u.id,
            u.username,
            u.email,
            COUNT(o.id) as order_count,
            SUM(o.total_amount) as total_spent
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.status = 'ACTIVE'
            AND u.created_at >= ?
        GROUP BY 
            u.id, 
            u.username,
            u.email
        HAVING COUNT(o.id) > 0
        ORDER BY total_spent DESC
        LIMIT 10
        """;
        
    private static final String JSON_CONFIG = """
        {
            "application": "MyApp",
            "version": "1.0.0",
            "database": {
                "url": "jdbc:postgresql://localhost:5432/mydb",
                "username": "admin",
                "maxPoolSize": 20
            }
        }
        """;
}
```

### 1.8.6. Enhanced Pseudo-Random Number Generators
```java
public class SecurityUtil {
    private final RandomGenerator secureRandom;
    
    public SecurityUtil() {
        this.secureRandom = RandomGenerator.of("L64X128MixRandom");
    }
    
    public String generateToken() {
        byte[] token = new byte[32];
  
