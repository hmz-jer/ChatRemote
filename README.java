import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public static void main(String[] args) {
    if (args.length < 1) {
        System.out.println("Veuillez fournir un argument pour l'opération ou le chemin vers un fichier d'entrée.");
        return;
    }

    String firstArg = args[0];
    String operationType;
    String jsonContent = "";

    // Vérifier si l'argument est un chemin de fichier
    Path filePath = Paths.get(firstArg);
    if (Files.exists(filePath)) {
        try {
            List<String> lines = Files.readAllLines(filePath);
            if (!lines.isEmpty()) {
                operationType = lines.get(0); // La première ligne indique le type d'opération
                jsonContent = String.join("\n", lines.subList(1, lines.size())); // Le reste du fichier contient les données JSON
            } else {
                System.out.println("Le fichier est vide.");
                return;
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier : " + e.getMessage());
            return;
        }
    } else {
        operationType = firstArg; // Utiliser l'argument comme type d'opération si ce n'est pas un chemin de fichier
    }

    String operation;
    switch (operationType) {
        case "T":
        case "R": // 'R' est traité comme 'T'
            operation = "tokenisation";
            break;
        case "S":
            operation = "delete-token";
            break;
        case "D":
            operation = "costo/detokenisation";
            break;
        default:
            System.out.println("Argument non reconnu ou fichier non trouvé.");
            return;
    }

    // Configuration optionnelle du SSLContext pour les connexions sécurisées
    SSLContext sslContext = null;
    if (secureConnection) {
        sslContext = sslConfiguration.createSSLContext(keystorePath, keystorePassword, truststorePath, truststorePassword);
    }

    if (!jsonContent.isEmpty()) {
        // Si jsonContent n'est pas vide, cela signifie que nous avons lu un fichier. Traiter le contenu JSON directement.
        performJsonRequestWithContent(operation, sslContext, secureConnection, jsonContent);
    } else {
        // Sinon, générer des données basées sur l'opération spécifiée.
        performOptimizedJsonRequest(operation, sslContext, secureConnection);
    }
}
