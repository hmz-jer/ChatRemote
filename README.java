import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RecursiveFileParser {

    public void parseFile(String filePath) throws IOException {
        ObjectMapper objectMapper;
        if (filePath.endsWith(".yaml") || filePath.endsWith(".yml")) {
            objectMapper = new ObjectMapper(new YAMLFactory());
        } else {
            objectMapper = new ObjectMapper();
        }

        File file = new File(filePath);
        Object content = objectMapper.readValue(file, Object.class);
        processElement(content);
    }

    private void processElement(Object element) {
        if (element instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) element;
            map.forEach((key, value) -> {
                System.out.println("Key: " + key);
                processElement(value);
            });
        } else if (element instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) element;
            list.forEach(this::processElement);
        } else {
            System.out.println("Value: " + element);
        }
    }

    public static void main(String[] args) {
        RecursiveFileParser parser = new RecursiveFileParser();
        try {
            parser.parseFile("path/to/your/file.json"); // ou .yaml
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
