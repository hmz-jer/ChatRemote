import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public static boolean sendJsonAndCheckFor413(String urlString, SSLContext sslContext, String jsonContent, boolean secureConnection) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn;

    if (secureConnection && url.getProtocol().equals("https")) {
        conn = (HttpsURLConnection) url.openConnection();
        if (sslContext != null) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(sslContext.getSocketFactory());
        }
    } else {
        conn = (HttpURLConnection) url.openConnection();
    }

    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.writeBytes(jsonContent);
        wr.flush();
    }

    int responseCode = conn.getResponseCode();
    InputStream inputStream;

    // Vérifier si le code de réponse indique une erreur
    if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
        inputStream = conn.getErrorStream(); // Obtenir le flux d'erreur pour les codes d'erreur
    } else {
        inputStream = conn.getInputStream(); // Obtenir le flux d'entrée normal pour les autres cas
    }

    // Lire le contenu du flux (soit le flux d'erreur, soit le flux d'entrée normal)
    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    String inputLine;
    StringBuilder response = new StringBuilder();

    while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
    }
    in.close();

    // Afficher la réponse complète ou le message d'erreur
    System.out.println("Réponse du serveur : " + response.toString());

    return responseCode == 413;
}
