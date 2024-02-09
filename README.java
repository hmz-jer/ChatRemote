import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLPeerUnverifiedException;

public static void printServerSAN(String serverUrl) {
    try {
        URL url = new URL(serverUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.connect();
        
        Certificate[] certs = conn.getServerCertificates();
        System.out.println("Certificats du serveur :");
        for (Certificate cert : certs) {
            if (cert instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate) cert;
                Collection<List<?>> sanList = x509Cert.getSubjectAlternativeNames();
                if (sanList != null) {
                    System.out.println("Subject Alternative Names :");
                    for (List<?> sanItem : sanList) {
                        // Le type est dans sanItem.get(0), la valeur dans sanItem.get(1)
                        System.out.println(sanItem.get(1).toString());
                    }
                }
            }
        }
    } catch (SSLPeerUnverifiedException e) {
        System.out.println("Le pair SSL n'a pas pu être vérifié.");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
