    import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SecureFileReader {
    
    private static final String VALID_FILE_PATH_PATTERN = "^[a-zA-Z0-9/_.-]+$";
    private static final String BASE_DIRECTORY = "/chemin/vers/repertoire/autorise";
    
    public static List<String> readFileSecurely(String fileName) throws IOException {
        // Validation du nom de fichier
        if (!isValidFileName(fileName)) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        // Construction du chemin sécurisé
        Path normalizedPath = Paths.get(BASE_DIRECTORY, fileName).normalize();
        if (!normalizedPath.startsWith(BASE_DIRECTORY)) {
            throw new SecurityException("Tentative d'accès à un répertoire non autorisé");
        }

        File file = normalizedPath.toFile();
        List<String> lines = new ArrayList<>();
        
        // Utilisation de try-with-resources pour fermer automatiquement les ressources
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }
    
    private static boolean isValidFileName(String fileName) {
        return fileName != null 
            && !fileName.isEmpty() 
            && fileName.matches(VALID_FILE_PATH_PATTERN);
    }
    
    // Exemple d'utilisation
    public static void main(String[] args) {
        try {
            List<String> contenu = readFileSecurely("mon_fichier.txt");
            contenu.forEach(System.out::println);
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Erreur de sécurité: " + e.getMessage());
        }
    }
}
