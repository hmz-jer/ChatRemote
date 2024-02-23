import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;

public class JsonSchemaConverter {

    private boolean deleteExampleWithId = true; // Suppose que cette variable est définie ailleurs

    public void convertJsonSchemaExamples(Map<String, Object> schema) {
        schema.forEach((key, value) -> {
            if (value instanceof Map) {
                // Traitement pour les objets Map
                Map<String, Object> subSchema = (Map<String, Object>) value;
                if ("examples".equals(key)) {
                    // Traitement spécifique pour les clés "examples"
                    List<Object> examples = (List<Object>) subSchema.get(key);
                    if (examples != null && !examples.isEmpty()) {
                        Object firstExample = examples.get(0);
                        if (deleteExampleWithId && firstExample instanceof Map && ((Map<?, ?>) firstExample).containsKey("id")) {
                            log("Deleted schema example with `id` property: " + firstExample);
                        } else {
                            subSchema.put("example", firstExample);
                            log("Replaces examples with example[0]. Old examples: " + examples);
                        }
                        subSchema.remove("examples");
                    }
                } else {
                    // Appel récursif pour les sous-objets
                    convertJsonSchemaExamples(subSchema);
                }
            } else if (value instanceof List) {
                // Traitement pour les listes
                List<?> listValue = (List<?>) value;
                listValue.forEach(item -> {
                    if (item instanceof Map) {
                        // Appel récursif pour les éléments de type Map dans les listes
                        convertJsonSchemaExamples((Map<String, Object>) item);
                    }
                });

                // Ici, vous pourriez vérifier le type spécifique de la liste si nécessaire
                if (listValue instanceof ArrayList) {
                    // Traitement spécifique à ArrayList, si nécessaire
                } else if (listValue instanceof LinkedList) {
                    // Traitement spécifique à LinkedList, si nécessaire
                }
            }
        });
    }

    private void log(String message) {
        // Méthode fictive pour logger des messages, remplacez par votre système de logging
        System.out.println(message);
    }
}
