 import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OpenAPIProcessor {

    public static void main(String[] args) {
        String filePath = "path/to/your/openapi/file.yaml"; // ou .json
        try {
            new OpenAPIProcessor().removeExamplesAttribute(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeExamplesAttribute(String filePath) throws IOException {
        ObjectMapper objectMapper = getObjectMapper(filePath);

        File file = new File(filePath);
        Map<String, Object> openAPISchema = objectMapper.readValue(file, Map.class);

        modifySchema(openAPISchema);

        objectMapper.writeValue(file, openAPISchema);
        System.out.println("Attribute 'examples' has been removed and the file saved.");
    }

    private void modifySchema(Map<String, Object> schema) {
        schema.forEach((key, value) -> {
            if (value instanceof Map) {
                if (key.equals("examples")) {
                    schema.remove(key);
                } else {
                    modifySchema((Map<String, Object>) value);
                }
            } else if (value instanceof List) {
                ((List<?>) value).forEach(item -> {
                    if (item instanceof Map) {
                        modifySchema((Map<String, Object>) item);
                    }
                });
            }
        });
        schema.entrySet().removeIf(entry -> entry.getKey().equals("examples"));
    }

    private ObjectMapper getObjectMapper(String filePath) {
        if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            return new ObjectMapper(new YAMLFactory());
        } else {
            return new ObjectMapper();
        }
    }
}
