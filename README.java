import org.junit.Test;
import static org.junit.Assert.*;

public class SSLManagerTest {

    @Test
    public void testParseDERFromPEN() {

        SSLManager sslManager = new SSLManager();

        // Test with valid input
        String beginDelimiter = "-----BEGIN CERTIFICATE-----";
        String endDelimiter = "-----END CERTIFICATE-----";
        String pem = beginDelimiter + "\n" + "BASE64_ENCODED_CERTIFICATE_DATA" + "\n" + endDelimiter;
        byte[] expected = DatatypeConverter.parseBase64Binary("BASE64_ENCODED_CERTIFICATE_DATA");
        byte[] actual = sslManager.parseDERFromPEN(pem.getBytes(), beginDelimiter, endDelimiter);
        assertArrayEquals(expected, actual);

        // Test with invalid input
        pem = "INVALID_CERTIFICATE_DATA";
        expected = new byte[0];
        actual = sslManager.parseDERFromPEN(pem.getBytes(), beginDelimiter, endDelimiter);
        assertArrayEquals(expected, actual);
    }
}
