 Pour afficher le message reçu dans le contrôleur, voici comment modifier le code :Modification du MockController.javapackage com.example.mockclientvop.controller;

import com.example.mockclientvop.service.BankResponseService;
import com.example.mockclientvop.service.CertificateService;
import com.example.mockclientvop.service.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MockController {

    private static final Logger logger = LoggerFactory.getLogger(MockController.class);
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private RoutingService routingService;
    
    @Autowired
    private BankResponseService bankResponseService;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(HttpServletRequest request) {
        // Log des informations de la requête
        logRequestInfo(request, null);
        
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        String pspIdValue = pspId.orElse(null);
        
        // Générer une réponse personnalisée
        return bankResponseService.generateResponse(pspIdValue, request);
    }
    
    @PostMapping("/status")
    public ResponseEntity<?> postStatus(HttpServletRequest request, @RequestBody(required = false) String requestBody) {
        // Log des informations de la requête avec le body
        logRequestInfo(request, requestBody);
        
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        String pspIdValue = pspId.orElse(null);
        
        // Log du message reçu
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            logger.info("Message reçu dans POST /api/status: {}", requestBody);
        }
        
        // Générer une réponse personnalisée
        return bankResponseService.generateResponse(pspIdValue, request);
    }
    
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<?> handleRequest(HttpServletRequest request, @RequestBody(required = false) String requestBody) {
        // Log des informations de la requête
        logRequestInfo(request, requestBody);
        
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        String pspIdValue = pspId.orElse(null);
        
        // Construction de la réponse pour simuler un forward
        String targetUrl = routingService.determineTargetUrl(clientCert);
        
        logger.info("Simulating forwarding {} request to {}", request.getMethod(), targetUrl);
        
        // Générer une réponse personnalisée
        return bankResponseService.generateResponse(pspIdValue, request);
    }

    /**
     * Log les informations détaillées de la requête
     */
    private void logRequestInfo(HttpServletRequest request, String requestBody) {
        logger.info("=== DÉBUT DE LA REQUÊTE ===");
        logger.info("Méthode: {}", request.getMethod());
        logger.info("URI: {}", request.getRequestURI());
        logger.info("URL complète: {}", request.getRequestURL());
        logger.info("Query String: {}", request.getQueryString());
        logger.info("Remote Address: {}", request.getRemoteAddr());
        logger.info("User Agent: {}", request.getHeader("User-Agent"));
        
        // Log des headers
        logger.info("=== HEADERS ===");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            // Masquer les headers sensibles
            if (headerName.toLowerCase().contains("authorization") || 
                headerName.toLowerCase().contains("cookie")) {
                headerValue = "***MASKED***";
            }
            logger.info("{}: {}", headerName, headerValue);
        });
        
        // Log des paramètres
        if (!request.getParameterMap().isEmpty()) {
            logger.info("=== PARAMÈTRES ===");
            request.getParameterMap().forEach((key, values) -> {
                logger.info("{}: {}", key, String.join(", ", values));
            });
        }
        
        // Log du body pour les requêtes POST/PUT
        if (requestBody != null && !requestBody.trim().isEmpty()) {
            logger.info("=== BODY DE LA REQUÊTE ===");
            // Limiter la taille du log pour éviter les logs trop volumineux
            if (requestBody.length() > 1000) {
                logger.info("Body (tronqué): {}", requestBody.substring(0, 1000) + "... [TRONQUÉ]");
                logger.info("Taille totale du body: {} caractères", requestBody.length());
            } else {
                logger.info("Body: {}", requestBody);
            }
        }
        
        logger.info("=== FIN DES INFORMATIONS DE LA REQUÊTE ===");
    }
}Alternative avec un intercepteur (plus propre)Si vous voulez logger toutes les requêtes automatiquement, créez un intercepteur :RequestLoggingInterceptor.javapackage com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        logRequestDetails(request);
        return true;
    }

    private void logRequestDetails(HttpServletRequest request) {
        logger.info("=== REQUÊTE REÇUE ===");
        logger.info("Méthode: {} | URI: {} | IP: {}", 
                   request.getMethod(), 
                   request.getRequestURI(), 
                   request.getRemoteAddr());
        
        // Log du x-request-id s'il existe
        String requestId = request.getHeader("x-request-id");
        if (requestId != null) {
            logger.info("X-Request-ID: {}", requestId);
        }
        
        // Log du User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            logger.info("User-Agent: {}", userAgent);
        }
        
        // Log du Content-Type
        String contentType = request.getContentType();
        if (contentType != null) {
            logger.info("Content-Type: {}", contentType);
        }
        
        logger.info("===================");
    }
}WebConfig.javapackage com.example.mockclientvop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**"); // Appliquer seulement aux endpoints API
    }
}Configuration des logs dans application.ymllogging:
  level:
    com.example.mockclientvop.controller.MockController: DEBUG
    com.example.mockclientvop.config.RequestLoggingInterceptor: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{50} - %msg%n"Test avec curl# Test avec message JSON
curl -v \
  -H "Content-Type: application/json" \
  -H "x-request-id: test-12345" \
  -d '{"message": "Hello from client", "amount": 100.50, "currency": "EUR"}' \
  --cert-type P12 \
  --cert /provider/Natixis/natixis-qwac.p12:password \
  --cacert /provider/psd2-ac-root.cert.pem \
  https://10.55.8.12:8443/api/statusVous verrez dans les logs:2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - === DÉBUT DE LA REQUÊTE ===
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - Méthode: POST
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - URI: /api/status
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - x-request-id: test-12345
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - Content-Type: application/json
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - === BODY DE LA REQUÊTE ===
2024-01-15 14:30:25 [http-nio-8443-exec-1] INFO  MockController - Body: {"message": "Hello from client", "amount": 100.50, "currency": "EUR"}Cette approche vous permet de voir tous les détails des requêtes reçues par votre mock, y compris les messages JSON envoyés par les clients.
