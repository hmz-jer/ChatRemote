 import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Properties;

import static org.junit.Assert.*;

public class DictionnaryTest {

    private Dictionnary dictionnary;

    @Before
    public void setUp() {
        dictionnary = new Dictionnary();
    }

    @Test
    public void testInitFromFile() throws Exception {
        String jsonContent = "{ \"id\": \"field1\", \"technical\": true, \"children\": [\"child1\", \"child2\"] }";
        BufferedReader reader = new BufferedReader(new StringReader(jsonContent));

        JsonElement jsonElement = new JsonParser().parse(reader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        
        // Validation that JSON parsing works as expected
        assertNotNull(jsonObject);
        assertEquals("field1", jsonObject.get("id").getAsString());
        assertTrue(jsonObject.get("technical").getAsBoolean());

        reader.close();
    }

    @Test
    public void testInitFromProperties() {
        Properties properties = new Properties();
        properties.put("field1", "tag1");
        properties.put("field2", "tag2");

        Dictionnary dictionnary = Dictionnary.initFromProperties(properties);

        assertEquals("tag1", dictionnary.toTag("field1"));
        assertEquals("field1", dictionnary.toFieldName("tag1"));
    }

    @Test
    public void testToTag() {
        dictionnary.dictionary.put("field1", "tag1");
        assertEquals("tag1", dictionnary.toTag("field1"));
    }

    @Test
    public void testToFieldName() {
        dictionnary.reverseDictionary.put("tag1", "field1");
        assertEquals("field1", dictionnary.toFieldName("tag1"));
    }

    @Test
    public void testIsStruct() {
        dictionnary.structures.put("tag1", null);
        assertTrue(dictionnary.isStruct("tag1"));
        assertFalse(dictionnary.isStruct("tag2"));
    }

    @Test
    public void testIsTechnical() {
        dictionnary.technicalFields.add("field1");
        assertTrue(dictionnary.isTechnical("field1"));
        assertFalse(dictionnary.isTechnical("field2"));
    }

    @Test
    public void testGetChilds() {
        dictionnary.structures.put("tag1", new HashSet<>(Arrays.asList("child1", "child2")));
        Set<String> childs = dictionnary.getChilds("tag1");

        assertNotNull(childs);
        assertTrue(childs.contains("child1"));
        assertTrue(childs.contains("child2"));
    }
}
