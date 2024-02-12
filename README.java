import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
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

    // Envoyer le contenu JSON dans la requête POST
    try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.writeBytes(jsonContent);
        wr.flush();
    }

    int responseCode = conn.getResponseCode();
    
    // Lire la réponse du serveur
    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String inputLine;
    StringBuilder response = new StringBuilder();

    while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
    }
    in.close();

    // Afficher la réponse complète
    System.out.println("Réponse du serveur : " + response.toString());

    return responseCode == 413;
}
