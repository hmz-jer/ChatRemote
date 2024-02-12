import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class HttpClientConfig {

    @Value("${ssl.keystore.location}")
    private String keystorePath;

    @Value("${ssl.keystore.password}")
    private String keystorePassword;

    @Value("${ssl.truststore.location}")
    private String truststorePath;

    @Value("${ssl.truststore.password}")
    private String truststorePassword;

    @Bean
    public CloseableHttpClient httpClient() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(Paths.get(keystorePath).toUri().toURL(), keystorePassword.toCharArray(), keystorePassword.toCharArray())
                .loadTrustMaterial(Paths.get(truststorePath).toUri().toURL(), truststorePassword.toCharArray())
                .build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        return HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
    }



import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

public class HttpClientPostExample {

    public static void main(String[] args) {
        // Créez une instance de CloseableHttpClient.
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            
            // Préparez l'URL et les données JSON à envoyer.
            String url = "https://exemple.com/api";
            String json = "{\"key\":\"value\"}";
            
            // Créez l'objet HttpPost et définissez l'en-tête Content-Type.
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json");
            
            // Associez les données JSON à la requête.
            StringEntity stringEntity = new StringEntity(json);
            httpPost.setEntity(stringEntity);
            
            // Exécutez la requête.
            HttpResponse response = httpClient.execute(httpPost);
            
            // Lisez le statut de la réponse et le contenu.
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            
            // Affichez le statut et la réponse.
            System.out.println("Status Code: " + statusCode);
            System.out.println("Response Body: " + responseBody);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
