import org.junit.Test;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

public class TestGeneratePrivateKeyFromDER {

    @Test
    public void testGeneratePrivateKeyFromDER_GivenValidKeyBytes_WhenCalled_ThenReturnPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        // GIVEN: des octets de clé valides
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        byte[] keyBytes = keyGen.generateKeyPair().getPrivate().getEncoded();

        // WHEN: on appelle la méthode generatePrivateKeyFromDER avec les octets de clé
        RSAPrivateKey privateKey = generatePrivateKeyFromDER(keyBytes);

        // THEN: la méthode doit retourner une clé privée RSA valide
        assertNotNull(privateKey);
        assertEquals("RSA", privateKey.getAlgorithm());
        assertEquals(2048, privateKey.getModulus().bitLength());
    }

    @Test
    public void testGeneratePrivateKeyFromDER_GivenEmptyKeyBytes_WhenCalled_ThenGenerateNewPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        // GIVEN: des octets de clé vides
        byte[] keyBytes = new byte[0];

        // WHEN: on appelle la méthode generatePrivateKeyFromDER avec les octets de clé vides
        RSAPrivateKey privateKey = generatePrivateKeyFromDER(keyBytes);

        // THEN: la méthode doit générer une nouvelle clé privée RSA valide
        assertNotNull(privateKey);
        assertEquals("RSA", privateKey.getAlgorithm());
        assertEquals(2048, privateKey.getModulus().bitLength());
    }

    @Test(expected = InvalidKeySpecException.class)
    public void testGeneratePrivateKeyFromDER_GivenInvalidKeyBytes_WhenCalled_ThenThrowInvalidKeySpecException() throws InvalidKeySpecException, NoSuchAlgorithmException {
        // GIVEN: des octets de clé invalides
        byte[] keyBytes = new byte[]{1, 2, 3, 4};

        // WHEN: on appelle la méthode generatePrivateKeyFromDER avec les octets de clé invalides
        generatePrivateKeyFromDER(keyBytes);

        // THEN: la méthode doit lancer une exception InvalidKeySpecException
    }
}
