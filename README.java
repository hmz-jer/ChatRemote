import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCharsets;
import java.nio.file.attribute.PosixFilePermissions;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public static boolean sendAndCheckFor413UsingCurl(String urlString, String encryptedContent, String cacertPath, String certPath, String keyPath) {
    Path tempFile = null;
    try {
        // Créer un fichier temporaire pour les données JSON
        tempFile = Files.createTempFile("encryptedContent", ".json", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
        Files.write(tempFile, encryptedContent.getBytes(StandardCharsets.UTF_8));
        
        // Construire la commande curl en utilisant le fichier temporaire pour les données
        String command = String.format("curl -X POST -H \"Content-Type: application/json\" --cacert %s --cert %s --key %s -d @%s %s -w \"%%{http_code}\"",
                cacertPath, certPath, keyPath, tempFile.toAbsolutePath(), urlString);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);

        Process process = builder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code : " + exitCode);
        System.out.println("Response: " + output);

        // Vérifier si la sortie contient "413"
        return output.toString().contains("413");
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return false;
    } finally {
        // Nettoyer et supprimer le fichier temporaire
        if (tempFile != null) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
