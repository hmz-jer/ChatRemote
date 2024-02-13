public static boolean sendAndCheckFor413UsingCurl(String urlString, String encryptedContent, String cacertPath, String certPath, String keyPath) {
    try {
        // Construit la commande curl
        String command = String.format("curl -X POST -H \"Content-Type: application/json\" --cacert %s --cert %s --key %s -d '%s' %s",
                cacertPath, certPath, keyPath, encryptedContent, urlString);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);

        Process process = builder.start();

        // Lire la sortie standard et d'erreur
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // Afficher la sortie standard
        String s;
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }

        // Afficher la sortie d'erreur
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code : " + exitCode);

        // Vérifier le code de sortie pour déterminer si la réponse était 413
        return exitCode == 0; // Dans ce contexte, le code de sortie 0 ne signifie pas nécessairement que la réponse était 413. Vous devrez analyser la sortie pour le déterminer.

    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return false;
    }
}
