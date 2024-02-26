 import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchemaModifier {

    public void convertConstToEnum(Map<String, Object> schema) {
        // Créer une nouvelle liste pour éviter la modification concurrente
        List<String> keys = new ArrayList<>(schema.keySet());
        for (String key : keys) {
            Object value = schema.get(key);
            
            if (value instanceof Map) {
                // Cas récursif: la valeur est un objet JSON, qui pourrait être un schéma.
                @SuppressWarnings("unchecked")
                Map<String, Object> subSchema = (Map<String, Object>) value;
                convertConstToEnum(subSchema); // Appel récursif pour traiter les sous-schémas.
            }
            // Traitement spécifique pour la clé 'const'.
            if ("const".equals(key)) {
                Object constantValue = schema.get(key);
                schema.remove("const");
                schema.put("enum", Collections.singletonList(constantValue));
                log(String.format("Converted const: %s to enum", constantValue));
            }
        }
    }

    private void log(String message) {
        // Implémentez votre logique de journalisation ici
        System.out.println(message);
    }

    public static void main(String[] args) {
        // Exemple d'utilisation
        Map<String, Object> schema = new HashMap<>();
        schema.put("const", "value");
        
        new SchemaModifier().convertConstToEnum(schema);
        
        System.out.println(schema);
    }
}
