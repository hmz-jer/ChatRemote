public static boolean sendAndCheckFor413UsingCurl(String urlString, String encryptedContent, String cacertPath, String certPath, String keyPath) {
    try {
        // Ajoute -w "%{http_code}" pour inclure le code de réponse HTTP dans la sortie
        String command = String.format("curl -X POST -H \"Content-Type: application/json\" --cacert %s --cert %s --key %s -d '%s' %s -w \"%%{http_code}\"",
                cacertPath, certPath, keyPath, encryptedContent, urlString);

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", command);

        Process process = builder.start();

        // Lire la sortie combinée (standard + code HTTP)
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code : " + exitCode);
        System.out.println("Response: " + output);

        // Vérifier si la sortie contient "413"
        return output.toString().endsWith("413");

    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
        return false;
    }
}
