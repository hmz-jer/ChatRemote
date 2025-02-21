 // src/main/java/com/example/DynamicApiApplication.java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DynamicApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(DynamicApiApplication.class, args);
    }
}

// src/main/java/com/example/config/ApiEndpointConfig.java
package com.example.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "apis")
@Getter
@Setter
public class ApiEndpointConfig {
    private List<EndpointDefinition> endpoints = new ArrayList<>();

    @Data
    public static class EndpointDefinition {
        private String path;
        private String method;
        private String operationId;
        private String description;
        private RequestDefinition request;
        private ResponseDefinition response;
        private MockData mock;
        private Map<String, String> headers;
        private List<ParameterDefinition> parameters;
    }

    @Data
    public static class RequestDefinition {
        private String contentType;
        private String schema;
        private List<String> required;
    }

    @Data
    public static class ResponseDefinition {
        private String success;
        private String error;
        private String contentType;
        private String schema;
    }

    @Data
    public static class MockData {
        private String data;
        private Map<String, String> headers;
        private Integer statusCode;
    }

    @Data
    public static class ParameterDefinition {
        private String name;
        private String in; // path, query, header
        private String description;
        private boolean required;
        private String type;
        private String format;
    }
}

// src/main/java/com/example/controller/DynamicApiController.java
package com.example.controller;

import com.example.config.ApiEndpointConfig;
import com.example.service.RequestProcessorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class DynamicApiController implements ApplicationContextAware {

    private final ApiEndpointConfig apiConfig;
    private final ObjectMapper objectMapper;
    private final RequestProcessorService requestProcessor;
    private ApplicationContext applicationContext;

    public DynamicApiController(ApiEndpointConfig apiConfig, 
                              ObjectMapper objectMapper,
                              RequestProcessorService requestProcessor) {
        this.apiConfig = apiConfig;
        this.objectMapper = objectMapper;
        this.requestProcessor = requestProcessor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void initializeEndpoints() {
        RequestMappingHandlerMapping handlerMapping = 
            applicationContext.getBean(RequestMappingHandlerMapping.class);

        apiConfig.getEndpoints().forEach(endpoint -> {
            try {
                Method handlerMethod = getClass().getDeclaredMethod(
                    "handleRequest",
                    HttpServletRequest.class,
                    ApiEndpointConfig.EndpointDefinition.class
                );

                RequestMappingInfo mappingInfo = RequestMappingInfo
                    .paths(endpoint.getPath())
                    .methods(HttpMethod.valueOf(endpoint.getMethod().toUpperCase()))
                    .produces(MediaType.APPLICATION_JSON_VALUE)
                    .build();

                handlerMapping.registerMapping(mappingInfo, this, handlerMethod);
                log.info("Registered endpoint: {} {}", endpoint.getMethod(), endpoint.getPath());
            } catch (Exception e) {
                log.error("Failed to register endpoint: {} {}", 
                    endpoint.getMethod(), endpoint.getPath(), e);
            }
        });
    }

    public ResponseEntity<Object> handleRequest(
            HttpServletRequest request,
            ApiEndpointConfig.EndpointDefinition endpoint) {
        try {
            return requestProcessor.processRequest(request, endpoint);
        } catch (Exception e) {
            log.error("Error processing request for endpoint: {}", endpoint.getPath(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", endpoint.getResponse().getError()));
        }
    }
}

// src/main/java/com/example/service/RequestProcessorService.java
package com.example.service;

import com.example.config.ApiEndpointConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestProcessorService {

    private final ObjectMapper objectMapper;

    public RequestProcessorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Object> processRequest(
            HttpServletRequest request,
            ApiEndpointConfig.EndpointDefinition endpoint) throws Exception {
        
        // Validation des paramètres requis
        if (!validateRequest(request, endpoint)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Missing required parameters"));
        }

        // Lecture du corps de la requête si nécessaire
        String requestBody = null;
        if (shouldReadBody(endpoint.getMethod())) {
            requestBody = request.getReader().lines().collect(Collectors.joining());
        }

        // Traitement selon l'operationId
        return processOperation(endpoint, requestBody);
    }

    private boolean validateRequest(
            HttpServletRequest request,
            ApiEndpointConfig.EndpointDefinition endpoint) {
        if (endpoint.getParameters() == null) {
            return true;
        }

        return endpoint.getParameters().stream()
            .filter(param -> param.isRequired())
            .allMatch(param -> {
                switch (param.getIn().toLowerCase()) {
                    case "path":
                        return request.getAttribute(param.getName()) != null;
                    case "query":
                        return request.getParameter(param.getName()) != null;
                    case "header":
                        return request.getHeader(param.getName()) != null;
                    default:
                        return false;
                }
            });
    }

    private boolean shouldReadBody(String method) {
        return "POST".equalsIgnoreCase(method) || 
               "PUT".equalsIgnoreCase(method) || 
               "PATCH".equalsIgnoreCase(method);
    }

    private ResponseEntity<Object> processOperation(
            ApiEndpointConfig.EndpointDefinition endpoint,
            String requestBody) throws Exception {
        
        // Utilisation des données mock pour la démonstration
        Object responseData = objectMapper.readValue(
            endpoint.getMock().getData(), 
            Object.class
        );

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseData);
    }
}

// src/main/java/com/example/config/OpenApiConfig.java
package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(ApiEndpointConfig apiConfig) {
        OpenAPI openAPI = new OpenAPI()
            .info(new Info()
                .title("API Dynamique")
                .version("1.0.0")
                .description("API générée dynamiquement à partir de la configuration YAML"));

        apiConfig.getEndpoints().forEach(endpoint -> {
            Operation operation = new Operation()
                .description(endpoint.getDescription())
                .responses(createApiResponses(endpoint));

            // Ajout des paramètres
            if (endpoint.getParameters() != null) {
                endpoint.getParameters().forEach(param -> {
                    Parameter swaggerParam = new Parameter()
                        .name(param.getName())
                        .in(param.getIn())
                        .description(param.getDescription())
                        .required(param.isRequired());
                    operation.addParametersItem(swaggerParam);
                });
            }

            PathItem pathItem = new PathItem();
            switch (endpoint.getMethod().toUpperCase()) {
                case "GET" -> pathItem.get(operation);
                case "POST" -> pathItem.post(operation);
                case "PUT" -> pathItem.put(operation);
                case "DELETE" -> pathItem.delete(operation);
                case "PATCH" -> pathItem.patch(operation);
            }

            openAPI.path(endpoint.getPath(), pathItem);
        });

        return openAPI;
    }

    private ApiResponses createApiResponses(ApiEndpointConfig.EndpointDefinition endpoint) {
        return new ApiResponses()
            .addApiResponse("200", new ApiResponse()
                .description(endpoint.getResponse().getSuccess()))
            .addApiResponse("400", new ApiResponse()
                .description("Requête invalide"))
            .addApiResponse("500", new ApiResponse()
                .description(endpoint.getResponse().getError()));
    }
}

// src/main/resources/application.yml
server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method

// src/main/resources/api-config.yml
apis:
  endpoints:
    - path: /users
      method: GET
      operationId: getUsers
      description: "Récupérer la liste des utilisateurs"
      parameters:
        - name: page
          in: query
          description: "Numéro de page"
          required: false
          type: integer
          format: int32
        - name: size
          in: query
          description: "Taille de la page"
          required: false
          type: integer
          format: int32
      response:
        success: "Liste des utilisateurs récupérée"
        error: "Erreur lors de la récupération des utilisateurs"
        contentType: "application/json"
      mock:
        data: '{"users": ["user1", "user2", "user3"], "total": 3, "page": 0, "size": 10}'
    
    - path: /products
      method: POST
      operationId: createProduct
      description: "Créer un nouveau produit"
      request:
        contentType: "application/json"
        schema: '{"type": "object", "properties": {"name": {"type": "string"}, "price": {"type": "number"}}}'
        required: ["name", "price"]
      response:
        success: "Produit créé avec succès"
        error: "Erreur lors de la création du produit"
        contentType: "application/json"
        schema: '{"type": "object", "properties": {"id": {"type": "string"}, "status": {"type": "string"}}}'
      mock:
        data: '{"productId": "123", "status": "created"}'
        statusCode: 201

// src/test/java/com/example/DynamicApiApplicationTests.java
package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DynamicApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
