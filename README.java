import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class DisableHostnameVerification {

    public static void main(String[] args) {
        try {
            // Créer un contexte SSL qui n'effectue pas de vérification du nom d'hôte
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, new java.security.SecureRandom());

            // Installer le gestionnaire d'hôtes qui ne vérifie pas les certificats
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true; // Accepter tout nom d'hôte
                }
            };

            // Appliquer les paramètres au niveau global (affecte toutes les connexions HttpsURLConnection)
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            // À partir d'ici, toute connexion HttpsURLConnection ignorera la vérification du nom d'hôte
            // Exemple d'utilisation :
            HttpsURLConnection conn = (HttpsURLConnection) new URL("https://exemple.com").openConnection();
            // Lire la réponse, etc.

            System.out.println("Connexion réussie");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
