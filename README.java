Pour réaliser cette tâche en Java 8, vous pouvez suivre les étapes suivantes :

1. **Extraire le nom du fichier du chemin d'entrée :**
   Utilisez la classe `Paths` pour créer un objet `Path` à partir de votre chemin de fichier, puis utilisez la méthode `getFileName()` pour obtenir le nom du fichier.

2. **Obtenir la date actuelle :**
   Utilisez la classe `LocalDate` de Java 8 pour obtenir la date du jour.

3. **Construire le nouveau chemin :**
   Concaténez le nom du fichier (sans l'extension `.csv`), la date du jour, et ajoutez de nouveau l'extension `.csv`.

Voici un exemple de code qui implémente ces étapes :

```java
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        String inputPath = "chemin/vers/votre/fichier.csv"; // Remplacez par votre chemin
        Path path = Paths.get(inputPath);
        
        // Extraire le nom du fichier sans l'extension
        String fileName = path.getFileName().toString();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        // Obtenir la date actuelle
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Construire le nouveau chemin
        String newPath = fileName + "_" + currentDate + ".csv";

        System.out.println("Nouveau chemin : " + newPath);
    }
}
```

Ce code générera un nouveau chemin avec le nom du fichier original, la date du jour formatée en `yyyyMMdd`, et l'extension `.csv`. Assurez-vous de remplacer `"chemin/vers/votre/fichier.csv"` par le chemin réel de votre fichier CSV.
