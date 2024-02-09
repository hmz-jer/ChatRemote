import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

public static boolean sendJsonAndCheckFor413(String urlString, SSLContext sslContext, String jsonContent, boolean secureConnection) throws Exception {
    URL url = new URL(urlString);
    HttpURLConnection conn;

    if (secureConnection && url.getProtocol().equals("https")) {
        conn = (HttpsURLConnection) url.openConnection();
        
        // Configurer le SSLContext, si fourni
        if (sslContext != null) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(sslContext.getSocketFactory());
        }

        // Désactiver la vérification du nom d'hôte
        ((HttpsURLConnection)conn).setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // Accepter tout nom d'hôte
            }
        });

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
    return responseCode == 413;
}
