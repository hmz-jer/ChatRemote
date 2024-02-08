import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.KeyManagerFactory;
import java.security.KeyStore;
import java.net.URL;

public class HttpsClientExample {

    public static void main(String[] args) {
        try {
            // Charger le keystore et le truststore
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream("chemin/vers/keystore.jks"), "motDePasseKeystore".toCharArray());

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(new FileInputStream("chemin/vers/truststore.jks"), "motDePasseTruststore".toCharArray());

            // Initialiser le KeyManagerFactory avec le keystore
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "motDePasseKeystore".toCharArray());

            // Initialiser le TrustManagerFactory avec le truststore
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Initialiser le SSLContext avec les key managers et trust managers
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

            // Ouvrir la connexion
            URL url = new URL("https://example.com");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());

            // Effectuer la requête et lire la réponse
            InputStream inputStream = conn.getInputStream();
            // Lire inputStream pour obtenir la réponse...

            System.out.println("Réponse obtenue.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
