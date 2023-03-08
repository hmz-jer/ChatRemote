import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.junit.Assert.*;

public class TestCopyToBytearray {

    @Test
    public void testCopyToBytearray() throws IOException {
        // Crée un tableau d'octets d'entrée à partir d'une chaîne de caractères
        String inputString = "Hello, world!";
        byte[] inputData = inputString.getBytes();

        // Crée un InputStream à partir du tableau d'octets d'entrée
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputData);

        // Appelle la méthode copyToBytearray pour copier les données de l'InputStream
        byte[] outputData = copyToBytearray(inputStream);

        // Vérifie que les données copiées sont égales à l'entrée d'origine
        assertArrayEquals(inputData, outputData);
    }
}
