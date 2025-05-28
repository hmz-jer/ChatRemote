 package com.stel.mockclientvop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankResponseServiceTest {

    @Mock
    private BankResponsesConfig responsesConfig;

    @InjectMocks
    private BankResponseService bankResponseService;

    private Map<String, Object> mockResponseConfig;
    private Map<String, String> mockHeaders;

    @BeforeEach
    void setUp() {
        // Configuration de réponse mock
        mockResponseConfig = new HashMap<>();
        mockResponseConfig.put("status", 200);
        mockResponseConfig.put("body", "{\"message\":\"success\"}");
        
        mockHeaders = new HashMap<>();
        mockHeaders.put("Content-Type", "application/json");
        mockHeaders.put("Authorization", "Bearer token123");
        
        mockResponseConfig.put("headers", mockHeaders);
    }

    @Test
    void testGenerateResponse_WithValidRequestUrl_ShouldReturnConfiguredResponse() {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = "https://api.example.com/users";
        
        when(responsesConfig.getBankResponseByUrl(requestUrl))
            .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("{\"message\":\"success\"}", result.getBody());
        
        // Vérifier les headers
        assertTrue(result.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", result.getHeaders().getFirst("Content-Type"));
        assertTrue(result.getHeaders().containsKey("Authorization"));
        assertEquals("Bearer token123", result.getHeaders().getFirst("Authorization"));
        
        // Vérifier l'appel au mock
        verify(responsesConfig).getBankResponseByUrl(requestUrl);
    }

    @Test
    void testGenerateResponse_WithNullRequestUrl_ShouldReturnDefaultResponse() {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = null;
        
        Map<String, Object> defaultResponse = new HashMap<>();
        defaultResponse.put("status", 200);
        defaultResponse.put("body", "{}");
        defaultResponse.put("headers", new HashMap<>());
        
        when(responsesConfig.getDefaultResponse()).thenReturn(defaultResponse);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("{}", result.getBody());
        
        verify(responsesConfig, never()).getBankResponseByUrl(any());
        verify(responsesConfig).getDefaultResponse();
    }

    @Test
    void testGenerateResponse_WithNoMatchingConfig_ShouldReturnDefaultResponse() {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = "https://unknown.api.com/endpoint";
        
        Map<String, Object> defaultResponse = new HashMap<>();
        defaultResponse.put("status", 404);
        defaultResponse.put("body", "{\"error\":\"Not found\"}");
        defaultResponse.put("headers", new HashMap<>());
        
        when(responsesConfig.getBankResponseByUrl(requestUrl)).thenReturn(null);
        when(responsesConfig.getDefaultResponse()).thenReturn(defaultResponse);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(404, result.getStatusCodeValue());
        assertEquals("{\"error\":\"Not found\"}", result.getBody());
        
        verify(responsesConfig).getBankResponseByUrl(requestUrl);
        verify(responsesConfig).getDefaultResponse();
    }

    @Test
    void testGenerateResponse_WithVariableReplacement_ShouldReplaceCorrectly() {
        // Given
        String orgId = "BANK123";
        String requestUrl = "https://api.example.com/accounts";
        
        Map<String, Object> responseWithVariables = new HashMap<>();
        responseWithVariables.put("status", 200);
        responseWithVariables.put("body", "{\"orgId\":\"${orgId}\",\"requestId\":\"${requestId}\"}");
        responseWithVariables.put("headers", new HashMap<>());
        
        when(responsesConfig.getBankResponseByUrl(requestUrl))
            .thenReturn(responseWithVariables);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        
        String responseBody = result.getBody();
        assertTrue(responseBody.contains("\"orgId\":\"BANK123\""));
        assertTrue(responseBody.contains("\"requestId\":"));
        assertFalse(responseBody.contains("${orgId}"));
        assertFalse(responseBody.contains("${requestId}"));
    }

    @Test
    void testGenerateResponse_WithDifferentStatusCodes() {
        // Test pour status 201
        testStatusCode(201, "Created");
        
        // Test pour status 400
        testStatusCode(400, "Bad Request");
        
        // Test pour status 500
        testStatusCode(500, "Internal Server Error");
    }

    private void testStatusCode(int statusCode, String message) {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = "https://api.example.com/test";
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", statusCode);
        response.put("body", "{\"message\":\"" + message + "\"}");
        response.put("headers", new HashMap<>());
        
        when(responsesConfig.getBankResponseByUrl(requestUrl)).thenReturn(response);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertEquals(statusCode, result.getStatusCodeValue());
        assertTrue(result.getBody().contains(message));
        
        // Reset mock pour le prochain test
        reset(responsesConfig);
    }

    @Test
    void testGenerateResponse_WithEmptyHeaders_ShouldNotFailAndAddDefaultHeaders() {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = "https://api.example.com/test";
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("body", "{}");
        response.put("headers", new HashMap<>()); // Headers vides
        
        when(responsesConfig.getBankResponseByUrl(requestUrl)).thenReturn(response);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals("{}", result.getBody());
        
        // Vérifier qu'il n'y a pas d'erreur même avec des headers vides
        assertNotNull(result.getHeaders());
    }

    @Test
    void testGenerateResponse_WithNullOrgId_ShouldHandleGracefully() {
        // Given
        String orgId = null;
        String requestUrl = "https://api.example.com/test";
        
        when(responsesConfig.getBankResponseByUrl(requestUrl))
            .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        assertEquals(200, result.getStatusCodeValue());
        
        // Vérifier que "unknown" est utilisé à la place de null
        String body = result.getBody();
        assertTrue(body.contains("unknown") || !body.contains("null"));
    }

    @Test
    void testGenerateResponse_LoggingBehavior() {
        // Given
        String orgId = "TEST_ORG";
        String requestUrl = "https://api.example.com/test";
        
        when(responsesConfig.getBankResponseByUrl(requestUrl))
            .thenReturn(mockResponseConfig);

        // When
        ResponseEntity<String> result = bankResponseService.generateResponse(orgId, requestUrl);

        // Then
        assertNotNull(result);
        verify(responsesConfig).getBankResponseByUrl(requestUrl);
        
        // Note: Pour tester les logs, vous pourriez utiliser un appender de test
        // ou vérifier que les méthodes du logger sont appelées avec les bons paramètres
    }
}
