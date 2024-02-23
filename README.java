 import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenAPIProcessor {

    public static void main(String[] args) {
        String filePath = "path/to/your/openapi/file.yaml"; // ou .json
        try {
            new OpenAPIProcessor().replaceExamplesWithExample(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replaceExamplesWithExample(String filePath) throws IOException {
        ObjectMapper objectMapper = getObjectMapper(filePath);

        File file = new File(filePath);
        Map<String, Object> openAPISchema = objectMapper.readValue(file, Map.class);

        modifySchema(openAPISchema);

        objectMapper.writeValue(file, openAPISchema);
        System.out.println("Attribute 'examples' has been replaced with 'example' where applicable.");
    }

    private void modifySchema(Map<String, Object> schema) {
        schema.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("examples".equals(key) && value instanceof Map) {
                // Trouve le premier exemple dans 'examples'
                Map<?, ?> examplesMap = (Map<?, ?>) value;
                if (!examplesMap.isEmpty()) {
                    Object firstExampleKey = examplesMap.keySet().iterator().next();
                    Object firstExampleValue = examplesMap.get(firstExampleKey);
                    // Remplace 'examples' par 'example' avec la première valeur trouvée
                    schema.put("example", firstExampleValue);
                }
                schema.remove(key);
            } else if (value instanceof Map) {
                // Appel récursif pour les sous-maps
                modifySchema((Map<String, Object>) value);
            } else if (value instanceof List) {
                // Appel récursif pour les listes contenant des maps
                ((List<?>) value).forEach(item -> {
                    if (item instanceof Map) {
                        modifySchema((Map<String, Object>) item);
                    }
                });
            }
        });
    }

    private ObjectMapper getObjectMapper(String filePath) {
        if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return new ObjectMapper(new YAMLFactory());
        } else {
            return new ObjectMapper();
        }
    }
}
