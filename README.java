import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentMapperTest {

    @Test
    void parseArguments_returnsEmptyMap_whenGivenNull() {
        // GIVEN
        String[] args = null;

        // WHEN
        Map<String, String> result = ArgumentMapper.parseArguments(args);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void parseArguments_returnsEmptyMap_whenGivenEmptyArray() {
        // GIVEN
        String[] args = new String[]{};

        // WHEN
        Map<String, String> result = ArgumentMapper.parseArguments(args);

        // THEN
        assertTrue(result.isEmpty());
    }

    @Test
    void parseArguments_returnsCorrectMap_whenGivenValidArguments() {
        // GIVEN
        String[] args = new String[]{"programName", "-arg1", "value1", "-arg2", "value2"};

        // WHEN
        Map<String, String> result = ArgumentMapper.parseArguments(args);

        // THEN
        assertEquals("value1", result.get("arg1"));
        assertEquals("value2", result.get("arg2"));
        assertNull(result.get("programName"));
        assertEquals(2, result.size());
    }

    @Test
    void parseArguments_returnsCorrectMap_whenGivenArgumentsWithRepeatedKeys() {
        // GIVEN
        String[] args = new String[]{"programName", "-arg1", "value1", "-arg2", "value2", "-arg1", "value3"};

        // WHEN
        Map<String, String> result = ArgumentMapper.parseArguments(args);

        // THEN
        assertEquals("value3", result.get("arg1"));
        assertEquals("value2", result.get("arg2"));
        assertNull(result.get("programName"));
        assertEquals(2, result.size());
    }

    @Test
    void parseArguments_returnsCorrectMap_whenGivenArgumentsWithoutValues() {
        // GIVEN
        String[] args = new String[]{"programName", "-arg1", "-arg2", "value2"};

        // WHEN
        Map<String, String> result = ArgumentMapper.parseArguments(args);

        // THEN
        assertEquals("", result.get("arg1"));
        assertEquals("value2", result.get("arg2"));
        assertNull(result.get("programName"));
        assertEquals(2, result.size());
    }
}
