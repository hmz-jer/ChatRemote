 Ah d'accord ! Vous voulez créer un vrai objet HttpServletRequest pour les tests. Voici comment faire :Création d'un HttpServletRequest pour les testspackage com.example.mockclientvop.service;

import com.example.mockclientvop.config.BankResponsesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankResponseServiceTest {

    @Mock
    private BankResponsesConfig bankResponsesConfig;

    @InjectMocks
    private BankResponseService bankResponseService;

    private Map<String, Object> mockResponseConfig;
    private Map<String, String> mockHeaders;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // Configuration de réponse mock
        mockResponseConfig = new HashMap<>();
        mockResponseConfig.put("status", 200);
        mockResponseConfig.put("body", "{\"message\":\"success\"}");

        mockHeaders = new HashMap<>();
        mockHeaders.put("Content-Type", "application/json");
        mockResponseConfig.put("headers", mockHeaders);

        // Création d'un HttpServletRequest réel pour les tests
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/status");
        request.setMethod("GET");
        request.setServerName("localhost");
        request.setServerPort(8443);
        request.setScheme("https");
    }

    @Test
    void testGenerateResponse_WithValidRequestUrl_ShouldReturnConfiguredResponse() {
        // Given
        String orgId = "TEST_ORG";
        request.setRequestURI("/api/provider/12345/status");

        when(bankResponsesConfig.getBankResponseByUrl("/api/provider/12345/status"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("{\"message\":\"success\"}", result.getBody());

        // Vérifier les headers
        assertTrue(result.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", result.getHeaders().getFirst("Content-Type"));

        // Vérifier l'appel au mock
        verify(bankResponsesConfig).getBankResponseByUrl("/api/provider/12345/status");
    }

    @Test
    void testGenerateResponse_WithXRequestIdHeader_ShouldUseProvidedRequestId() {
        // Given
        String orgId = "TEST_ORG";
        String providedRequestId = "custom-request-id-123";
        
        // Ajouter le header x-request-id
        request.addHeader("x-request-id", providedRequestId);
        request.setRequestURI("/api/status");

        // Configuration du body avec variable requestId
        mockResponseConfig.put("body", "{\"message\":\"success\",\"requestId\":\"${requestId}\"}");

        when(bankResponsesConfig.getBankResponseByUrl("/api/status"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getBody().contains(providedRequestId));
        assertTrue(result.getBody().contains("\"requestId\":\"" + providedRequestId + "\""));
    }

    @Test
    void testGenerateResponse_WithoutXRequestIdHeader_ShouldGenerateRequestId() {
        // Given
        String orgId = "TEST_ORG";
        request.setRequestURI("/api/status");
        // Pas de header x-request-id ajouté

        // Configuration du body avec variable requestId
        mockResponseConfig.put("body", "{\"message\":\"success\",\"requestId\":\"${requestId}\"}");

        when(bankResponsesConfig.getBankResponseByUrl("/api/status"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        // Vérifier qu'un requestId a été généré (format UUID)
        assertTrue(result.getBody().contains("\"requestId\":\""));
        // Le requestId généré doit avoir un format UUID
        assertTrue(result.getBody().matches(".*\"requestId\":\"[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\".*"));
    }

    @Test
    void testGenerateResponse_WithVariablesInHeaders_ShouldReplaceVariables() {
        // Given
        String orgId = "TEST_ORG";
        String providedRequestId = "header-request-id-456";
        
        // Configuration de la request
        request.addHeader("x-request-id", providedRequestId);
        request.setRequestURI("/api/test");

        // Configuration des headers avec variables
        mockHeaders.put("X-Request-ID", "${requestId}");
        mockHeaders.put("X-Timestamp", "${timestamp}");
        mockHeaders.put("X-PSP-ID", "${pspId}");

        when(bankResponsesConfig.getBankResponseByUrl("/api/test"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertEquals(providedRequestId, result.getHeaders().getFirst("X-Request-ID"));
        assertEquals(orgId, result.getHeaders().getFirst("X-PSP-ID"));
        assertNotNull(result.getHeaders().getFirst("X-Timestamp"));
        // Vérifier que le timestamp a le bon format ISO
        assertTrue(result.getHeaders().getFirst("X-Timestamp").matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
    }

    @Test
    void testGenerateResponse_WithTimestampVariable_ShouldUseCorrectFormat() {
        // Given
        String orgId = "TEST_ORG";
        request.setRequestURI("/api/timestamp-test");

        // Configuration du body avec variable timestamp
        mockResponseConfig.put("body", "{\"timestamp\":\"${timestamp}\",\"server\":\"test\"}");

        when(bankResponsesConfig.getBankResponseByUrl("/api/timestamp-test"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        // Vérifier le format timestamp ISO 8601 avec Z (UTC)
        assertTrue(result.getBody().matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z\".*"));
    }

    @Test
    void testGenerateResponse_WithPspIdVariable_ShouldReplacePspId() {
        // Given
        String orgId = "PSDFR-ACPR-12345";
        request.setRequestURI("/api/psp-test");

        // Configuration du body avec variable pspId
        mockResponseConfig.put("body", "{\"pspId\":\"${pspId}\",\"status\":\"active\"}");

        when(bankResponsesConfig.getBankResponseByUrl("/api/psp-test"))
                .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertTrue(result.getBody().contains("\"pspId\":\"" + orgId + "\""));
    }

    @Test
    void testGenerateResponse_WithComplexRequest_ShouldHandleAllVariables() {
        // Given
        String orgId = "COMPLEX_TEST_PSP";
        String requestId = "complex-request-123";
        
        // Configuration d'une request complexe
        request.setRequestURI("/api/provider/54321/accounts");
        request.setMethod("POST");
        request.addHeader("x-request-id", requestId);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("User-Agent", "TestClient/1.0");

        // Configuration d'une réponse complexe avec toutes les variables
        Map<String, Object> complexResponse = new HashMap<>();
        complexResponse.put("status", 201);
        
        Map<String, String> complexHeaders = new HashMap<>();
        complexHeaders.put("Content-Type", "application/json");
        complexHeaders.put("X-Request-ID", "${requestId}");
        complexHeaders.put("X-Timestamp", "${timestamp}");
        complexHeaders.put("X-PSP-ID", "${pspId}");
        complexResponse.put("headers", complexHeaders);
        
        String complexBody = "{\n" +
                "  \"status\": \"created\",\n" +
                "  \"requestId\": \"${requestId}\",\n" +
                "  \"timestamp\": \"${timestamp}\",\n" +
                "  \"pspId\": \"${pspId}\",\n" +
                "  \"data\": {\n" +
                "    \"accountId\": \"ACC-${requestId}\"\n" +
                "  }\n" +
                "}";
        complexResponse.put("body", complexBody);

        when(bankResponsesConfig.getBankResponseByUrl("/api/provider/54321/accounts"))
                .thenReturn(complexResponse);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertEquals(201, result.getStatusCodeValue());
        
        // Vérifier les headers
        assertEquals(requestId, result.getHeaders().getFirst("X-Request-ID"));
        assertEquals(orgId, result.getHeaders().getFirst("X-PSP-ID"));
        assertNotNull(result.getHeaders().getFirst("X-Timestamp"));
        
        // Vérifier le body
        String body = result.getBody();
        assertTrue(body.contains("\"requestId\": \"" + requestId + "\""));
        assertTrue(body.contains("\"pspId\": \"" + orgId + "\""));
        assertTrue(body.contains("\"accountId\": \"ACC-" + requestId + "\""));
        assertTrue(body.matches(".*\"timestamp\": \"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z\".*"));
    }

    @Test
    void testGenerateResponse_WithDefaultResponse_WhenNoConfigFound() {
        // Given
        String orgId = "DEFAULT_TEST";
        request.setRequestURI("/api/unknown-endpoint");

        Map<String, Object> defaultResponse = new HashMap<>();
        defaultResponse.put("status", 200);
        defaultResponse.put("body", "{\"message\":\"Default response\",\"requestId\":\"${requestId}\"}");
        defaultResponse.put("headers", new HashMap<>());

        when(bankResponsesConfig.getBankResponseByUrl("/api/unknown-endpoint")).thenReturn(null);
        when(bankResponsesConfig.getDefaultResponse()).thenReturn(defaultResponse);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, request);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("Default response"));
        // Vérifier qu'un requestId a été généré même pour la réponse par défaut
        assertTrue(result.getBody().matches(".*\"requestId\":\"[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\".*"));
    }
}Dépendance à ajouter dans build.gradledependencies {
    // ... autres dépendances
    testImplementation 'org.springframework:spring-test'
}Avantages de cette approcheHttpServletRequest réel : Utilisation de MockHttpServletRequest qui implémente vraiment l'interfaceConfiguration flexible : Possibilité de configurer tous les aspects de la request (URI, headers, méthode, etc.)Tests plus réalistes : Les tests reflètent mieux le comportement réel de l'applicationFacilité de maintenance : Plus facile à comprendre et maintenir qu'avec des mocks complexesAvec MockHttpServletRequest, vous pouvez :Définir l'URI : request.setRequestURI("/api/test")Ajouter des headers : request.addHeader("x-request-id", "123")Configurer la méthode HTTP : request.setMethod("POST")Ajouter des paramètres : request.addParameter("param", "value") 
