 import java.util.List;
import java.util.Map;

public class SchemaModifier {

    public void ensureHttpsInServersUrls(Map<String, Object> schema) {
        // Vérifie si le schéma contient un élément "servers"
        if (schema.containsKey("servers") && schema.get("servers") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> servers = (List<Map<String, Object>>) schema.get("servers");
            for (Map<String, Object> server : servers) {
                // Vérifie chaque serveur pour une clé "url"
                if (server.containsKey("url")) {
                    String url = (String) server.get("url");
                    // Si l'URL ne commence pas par "https", ajoute "https://" au début
                    if (!url.startsWith("https://")) {
                        url = "https://" + url;
                        server.put("url", url);
                        log("Updated server URL to use HTTPS: " + url);
                    }
                }
            }
        }
    }

    private void log(String message) {
        // Implémentez votre logique de journalisation ici
        System.out.println(message);
    }

    public static void main(String[] args) {
        // Exemple d'utilisation
        Map<String, Object> schema = Map.of(
            "servers", List.of(
                Map.of("url", "http://example.com"),
                Map.of("url", "https://secure.example.com")
            )
        );

        SchemaModifier modifier = new SchemaModifier();
        modifier.ensureHttpsInServersUrls(schema);

        System.out.println(schema);
    }
}
