    // La classe pour référence
class SystemPropertiesManager {
    private static final String PROXY_HOST_PROPERTY = "http.proxyHost";
    private static final String PROXY_PORT_PROPERTY = "http.proxyPort";
    private static final String SYSTEM_PROXY_PROPERTY = "java.net.useSystemProxies";

    public static void setProxyProperties(String hostname, String port) {
        if (isAllowedPropertyKey(PROXY_HOST_PROPERTY) && isAllowedPropertyKey(PROXY_PORT_PROPERTY)) {
            System.setProperty(PROXY_HOST_PROPERTY, hostname);
            System.setProperty(PROXY_PORT_PROPERTY, port);
        } else {
            throw new SecurityException("Propriété système non autorisée");
        }
    }

    public static void setSystemProxyEnabled(boolean enabled) {
        if (isAllowedPropertyKey(SYSTEM_PROXY_PROPERTY)) {
            System.setProperty(SYSTEM_PROXY_PROPERTY, Boolean.toString(enabled));
        } else {
            throw new SecurityException("Propriété système non autorisée");
        }
    }

    private static boolean isAllowedPropertyKey(String key) {
        return PROXY_HOST_PROPERTY.equals(key) ||
               PROXY_PORT_PROPERTY.equals(key) ||
               SYSTEM_PROXY_PROPERTY.equals(key);
    }
}

// Tests unitaires
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SystemPropertiesManagerTest {
    
    private Properties originalProperties;
    
    @Before
    public void setUp() {
        // Sauvegarde des propriétés système originales
        originalProperties = (Properties) System.getProperties().clone();
    }
    
    @After
    public void tearDown() {
        // Restauration des propriétés système originales
        System.setProperties(originalProperties);
    }
    
    @Test
    public void testSetProxyPropertiesWithValidKeys() {
        // Arrange
        String hostname = "proxy.example.com";
        String port = "8080";
        
        // Act
        SystemPropertiesManager.setProxyProperties(hostname, port);
        
        // Assert
        assertEquals(hostname, System.getProperty("http.proxyHost"));
        assertEquals(port, System.getProperty("http.proxyPort"));
    }
    
    @Test
    public void testSetSystemProxyEnabledWithValidKey() {
        // Act
        SystemPropertiesManager.setSystemProxyEnabled(true);
        
        // Assert
        assertEquals("true", System.getProperty("java.net.useSystemProxies"));
    }
    
    @Test
    public void testSetSystemProxyDisabledWithValidKey() {
        // Act
        SystemPropertiesManager.setSystemProxyEnabled(false);
        
        // Assert
        assertEquals("false", System.getProperty("java.net.useSystemProxies"));
    }
    
    @Test(expected = SecurityException.class)
    public void testSetNonAllowedSystemProperty() {
        // Arrange
        System.setProperty("java.non.allowed.property", "test");
        
        // Act & Assert - devrait lever une SecurityException
        new SystemPropertiesManager().getClass()
            .getDeclaredMethod("isAllowedPropertyKey", String.class)
            .invoke(null, "java.non.allowed.property");
    }
    
    @Test
    public void testAllowedKeysPositiveTest() throws Exception {
        // Arrange
        java.lang.reflect.Method isAllowedPropertyKey = 
            SystemPropertiesManager.class.getDeclaredMethod("isAllowedPropertyKey", String.class);
        isAllowedPropertyKey.setAccessible(true);
        
        // Act & Assert
        assertTrue((Boolean) isAllowedPropertyKey.invoke(null, "http.proxyHost"));
        assertTrue((Boolean) isAllowedPropertyKey.invoke(null, "http.proxyPort"));
        assertTrue((Boolean) isAllowedPropertyKey.invoke(null, "java.net.useSystemProxies"));
    }
    
    @Test
    public void testAllowedKeysNegativeTest() throws Exception {
        // Arrange
        java.lang.reflect.Method isAllowedPropertyKey = 
            SystemPropertiesManager.class.getDeclaredMethod("isAllowedPropertyKey", String.class);
        isAllowedPropertyKey.setAccessible(true);
        
        // Act & Assert
        assertFalse((Boolean) isAllowedPropertyKey.invoke(null, "java.home"));
        assertFalse((Boolean) isAllowedPropertyKey.invoke(null, "user.dir"));
        assertFalse((Boolean) isAllowedPropertyKey.invoke(null, "os.name"));
    }
    
    @Test
    public void testSetProxyPropertiesWithNullValues() {
        // Arrange
        String hostname = null;
        String port = null;
        
        // Act
        SystemPropertiesManager.setProxyProperties(hostname, port);
        
        // Assert
        assertNull(System.getProperty("http.proxyHost"));
        assertNull(System.getProperty("http.proxyPort"));
    }
    
    @Test
    public void testOverwriteExistingProxyProperties() {
        // Arrange
        String initialHostname = "initial.proxy.com";
        String initialPort = "8081";
        SystemPropertiesManager.setProxyProperties(initialHostname, initialPort);
        
        String newHostname = "new.proxy.com";
        String newPort = "8082";
        
        // Act
        SystemPropertiesManager.setProxyProperties(newHostname, newPort);
        
        // Assert
        assertEquals(newHostname, System.getProperty("http.proxyHost"));
        assertEquals(newPort, System.getProperty("http.proxyPort"));
    }
}
