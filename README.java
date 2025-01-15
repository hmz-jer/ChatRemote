 
Je vais mettre à jour la configuration pour utiliser Swagger UI version 4.18.0 avec springdoc.

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>kafka-request-reply</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Swagger UI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>swagger-ui</artifactId>
            <version>4.18.0</version>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>

// Application.java
package com.example.kafkarequestrepply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Configuration OpenAPI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Kafka Request-Reply")
                        .version("1.0.0")
                        .description("API de traitement asynchrone des requêtes via Kafka")
                        .contact(new Contact()
                                .name("Votre Nom")
                                .email("contact@example.com")));
    }
}

// config/KafkaConfig.java
package com.example.kafkarequestrepply.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "request-reply-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentMessageListenerContainer<String, Object> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Object> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory) {
        ConcurrentMessageListenerContainer<String, Object> repliesContainer =
                containerFactory.createContainer("replies-topic");
        repliesContainer.getContainerProperties().setGroupId("replies-group");
        return repliesContainer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// model/RequestDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de requête")
public class RequestDTO {
    @Schema(description = "Identifiant unique de la requête", example = "req-123")
    @NotNull(message = "L'identifiant de la requête est obligatoire")
    private String requestId;

    @Schema(description = "Données à traiter", example = "données à traiter")
    @NotBlank(message = "Les données sont obligatoires")
    private String data;
}

// model/ResponseDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de réponse")
public class ResponseDTO {
    @Schema(description = "Identifiant de la requête traitée", example = "req-123")
    private String requestId;

    @Schema(description = "Résultat du traitement", example = "Traitement terminé avec succès")
    private String result;

    @Schema(description = "Statut du traitement", example = "SUCCESS")
    private String status;
}

// controller/RequestController.java
package com.example.kafkarequestrepply.controller;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import com.example.kafkarequestrepply.service.KafkaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Request Controller", description = "API de gestion des requêtes asynchrones")
public class RequestController {

    private final KafkaRequestService kafkaRequestService;

    @Operation(
        summary = "Traiter une requête de manière asynchrone",
        description = "Envoie une requête pour traitement asynchrone via Kafka et attend la réponse"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Requête traitée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors du traitement de la requête",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        )
    })
    @PostMapping("/request")
    public ResponseDTO processRequest(
        @RequestBody @Valid 
        @Parameter(description = "Requête à traiter", required = true) 
        RequestDTO request
    ) throws ExecutionException, InterruptedException {
        return kafkaRequestService.sendRequest(request);
    }
}

// service/KafkaRequestService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaRequestService {

    private final ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
    private static final String REQUEST_TOPIC = "requests-topic";

    public ResponseDTO sendRequest(RequestDTO request) throws ExecutionException, InterruptedException {
        ProducerRecord<String, Object> record = new ProducerRecord<>(REQUEST_TOPIC, request);
        RequestReplyFuture<String, Object, Object> future = kafkaTemplate.sendAndReceive(record);
        
        try {
            ConsumerRecord<String, Object> response = future.get(10, TimeUnit.SECONDS);
            return (ResponseDTO) response.value();
        } catch (TimeoutException e) {
            ResponseDTO errorResponse = new ResponseDTO();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("ERROR");
            errorResponse.setResult("Request timed out");
            return errorResponse;
        }
    }
}

// service/ProcessingService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    @KafkaListener(topics = "requests-topic", groupId = "processing-group")
    @SendTo("replies-topic")
    public ResponseDTO processRequest(RequestDTO request) {
        // Simuler un traitement
        ResponseDTO response = new ResponseDTO();
        response.setRequestId(request.getRequestId());
        response.setStatus("SUCCESS");
        response.setResult("Processed: " + request.getData());
        return response;
    }
}

// application.yml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: request-reply-group
      auto-offset-reset: earliest
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    defaultModelsExpandDepth: 1

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>kafka-request-reply</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Swagger UI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>swagger-ui</artifactId>
            <version>4.18.0</version>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>

// Application.java
package com.example.kafkarequestrepply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Configuration OpenAPI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Kafka Request-Reply")
                        .version("1.0.0")
                        .description("API de traitement asynchrone des requêtes via Kafka")
                        .contact(new Contact()
                                .name("Votre Nom")
                                .email("contact@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation additionnelle")
                        .url("https://example.com/docs"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}

// config/KafkaConfig.java
package com.example.kafkarequestrepply.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "request-reply-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentMessageListenerContainer<String, Object> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Object> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory) {
        ConcurrentMessageListenerContainer<String, Object> repliesContainer =
                containerFactory.createContainer("replies-topic");
        repliesContainer.getContainerProperties().setGroupId("replies-group");
        return repliesContainer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// model/RequestDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de requête")
public class RequestDTO {
    @Schema(description = "Identifiant unique de la requête", example = "req-123")
    @NotNull(message = "L'identifiant de la requête est obligatoire")
    private String requestId;

    @Schema(description = "Données à traiter", example = "données à traiter")
    @NotBlank(message = "Les données sont obligatoires")
    private String data;
}

// model/ResponseDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de réponse")
public class ResponseDTO {
    @Schema(description = "Identifiant de la requête traitée", example = "req-123")
    private String requestId;

    @Schema(description = "Résultat du traitement", example = "Traitement terminé avec succès")
    private String result;

    @Schema(description = "Statut du traitement", example = "SUCCESS")
    private String status;
}

// controller/RequestController.java
package com.example.kafkarequestrepply.controller;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import com.example.kafkarequestrepply.service.KafkaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Request Controller", description = "API de gestion des requêtes asynchrones")
public class RequestController {

    private final KafkaRequestService kafkaRequestService;

    @Operation(
        summary = "Traiter une requête de manière asynchrone",
        description = "Envoie une requête pour traitement asynchrone via Kafka et attend la réponse"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Requête traitée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors du traitement de la requête",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        )
    })
    @PostMapping("/request")
    public ResponseDTO processRequest(
        @RequestBody @Valid 
        @Parameter(description = "Requête à traiter", required = true) 
        RequestDTO request
    ) throws ExecutionException, InterruptedException {
        return kafkaRequestService.sendRequest(request);
    }
}

// service/KafkaRequestService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaRequestService {

    private final ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
    private static final String REQUEST_TOPIC = "requests-topic";

    public ResponseDTO sendRequest(RequestDTO request) throws ExecutionException, InterruptedException {
        ProducerRecord<String, Object> record = new ProducerRecord<>(REQUEST_TOPIC, request);
        RequestReplyFuture<String, Object, Object> future = kafkaTemplate.sendAndReceive(record);
        
        try {
            ConsumerRecord<String, Object> response = future.get(10, TimeUnit.SECONDS);
            return (ResponseDTO) response.value();
        } catch (TimeoutException e) {
            ResponseDTO errorResponse = new ResponseDTO();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("ERROR");
            errorResponse.setResult("Request timed out");
            return errorResponse;
        }
    }
}

// service/ProcessingService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    @KafkaListener(topics = "requests-topic", groupId = "processing-group")
    @SendTo("replies-topic")
    public ResponseDTO processRequest(RequestDTO request) {
        // Simuler un traitement
        ResponseDTO response = new ResponseDTO();
        response.setRequestId(request.getRequestId());
        response.setStatus("SUCCESS");
        response.setResult("Processed: " + request.getData());
        return response;
    }
}

// application.yml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: request-reply-group
      auto-offset-reset: earliest
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    defaultModelsExpandDepth: 1

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>kafka-request-reply</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Swagger UI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>swagger-ui</artifactId>
            <version>4.18.0</version>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>

// Application.java
package com.example.kafkarequestrepply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springdoc.core.models.GroupedOpenApi;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Configuration OpenAPI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Kafka Request-Reply")
                        .version("1.0.0")
                        .description("API de traitement asynchrone des requêtes via Kafka")
                        .contact(new Contact()
                                .name("Votre Nom")
                                .email("contact@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation additionnelle")
                        .url("https://example.com/docs"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}

// config/KafkaConfig.java
package com.example.kafkarequestrepply.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "request-reply-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentMessageListenerContainer<String, Object> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Object> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory) {
        ConcurrentMessageListenerContainer<String, Object> repliesContainer =
                containerFactory.createContainer("replies-topic");
        repliesContainer.getContainerProperties().setGroupId("replies-group");
        return repliesContainer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// model/RequestDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de requête")
public class RequestDTO {
    @Schema(description = "Identifiant unique de la requête", example = "req-123")
    @NotNull(message = "L'identifiant de la requête est obligatoire")
    private String requestId;

    @Schema(description = "Données à traiter", example = "données à traiter")
    @NotBlank(message = "Les données sont obligatoires")
    private String data;
}

// model/ResponseDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de réponse")
public class ResponseDTO {
    @Schema(description = "Identifiant de la requête traitée", example = "req-123")
    private String requestId;

    @Schema(description = "Résultat du traitement", example = "Traitement terminé avec succès")
    private String result;

    @Schema(description = "Statut du traitement", example = "SUCCESS")
    private String status;
}

// controller/RequestController.java
package com.example.kafkarequestrepply.controller;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import com.example.kafkarequestrepply.service.KafkaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Request Controller", description = "API de gestion des requêtes asynchrones")
public class RequestController {

    private final KafkaRequestService kafkaRequestService;

    @Operation(
        summary = "Traiter une requête de manière asynchrone",
        description = "Envoie une requête pour traitement asynchrone via Kafka et attend la réponse"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Requête traitée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors du traitement de la requête",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        )
    })
    @PostMapping("/request")
    public ResponseDTO processRequest(
        @RequestBody @Valid 
        @Parameter(description = "Requête à traiter", required = true) 
        RequestDTO request
    ) throws ExecutionException, InterruptedException {
        return kafkaRequestService.sendRequest(request);
    }
}

// service/KafkaRequestService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaRequestService {

    private final ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
    private static final String REQUEST_TOPIC = "requests-topic";

    public ResponseDTO sendRequest(RequestDTO request) throws ExecutionException, InterruptedException {
        ProducerRecord<String, Object> record = new ProducerRecord<>(REQUEST_TOPIC, request);
        RequestReplyFuture<String, Object, Object> future = kafkaTemplate.sendAndReceive(record);
        
        try {
            ConsumerRecord<String, Object> response = future.get(10, TimeUnit.SECONDS);
            return (ResponseDTO) response.value();
        } catch (TimeoutException e) {
            ResponseDTO errorResponse = new ResponseDTO();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("ERROR");
            errorResponse.setResult("Request timed out");
            return errorResponse;
        }
    }
}

// service/ProcessingService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    @KafkaListener(topics = "requests-topic", groupId = "processing-group")
    @SendTo("replies-topic")
    public ResponseDTO processRequest(RequestDTO request) {
        // Simuler un traitement
        ResponseDTO response = new ResponseDTO();
        response.setRequestId(request.getRequestId());
        response.setStatus("SUCCESS");
        response.setResult("Processed: " + request.getData());
        return response;
    }
}

// application.yml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: request-reply-group
      auto-offset-reset: earliest
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    defaultModelsExpandDepth: 1

// pom.xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>kafka-request-reply</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- Swagger UI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.webjars</groupId>
            <artifactId>swagger-ui</artifactId>
            <version>4.18.0</version>
        </dependency>
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
    </dependencies>
</project>

// Application.java
package com.example.kafkarequestrepply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springdoc.core.models.GroupedOpenApi;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Configuration OpenAPI
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Kafka Request-Reply")
                        .version("1.0.0")
                        .description("API de traitement asynchrone des requêtes via Kafka")
                        .contact(new Contact()
                                .name("Votre Nom")
                                .email("contact@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation additionnelle")
                        .url("https://example.com/docs"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}

// config/KafkaConfig.java
package com.example.kafkarequestrepply.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "request-reply-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ReplyingKafkaTemplate<String, Object, Object> replyingKafkaTemplate(
            ProducerFactory<String, Object> producerFactory,
            ConcurrentMessageListenerContainer<String, Object> repliesContainer) {
        return new ReplyingKafkaTemplate<>(producerFactory, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Object> repliesContainer(
            ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory) {
        ConcurrentMessageListenerContainer<String, Object> repliesContainer =
                containerFactory.createContainer("replies-topic");
        repliesContainer.getContainerProperties().setGroupId("replies-group");
        return repliesContainer;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());
        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

// model/RequestDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de requête")
public class RequestDTO {
    @Schema(description = "Identifiant unique de la requête", example = "req-123")
    @NotNull(message = "L'identifiant de la requête est obligatoire")
    private String requestId;

    @Schema(description = "Données à traiter", example = "données à traiter")
    @NotBlank(message = "Les données sont obligatoires")
    private String data;
}

// model/ResponseDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "Objet de réponse")
public class ResponseDTO {
    @Schema(description = "Identifiant de la requête traitée", example = "req-123")
    private String requestId;

    @Schema(description = "Résultat du traitement", example = "Traitement terminé avec succès")
    private String result;

    @Schema(description = "Statut du traitement", example = "SUCCESS")
    private String status;
}

// controller/RequestController.java
package com.example.kafkarequestrepply.controller;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import com.example.kafkarequestrepply.service.KafkaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Request Controller", description = "API de gestion des requêtes asynchrones")
public class RequestController {

    private final KafkaRequestService kafkaRequestService;

    @Operation(
        summary = "Traiter une requête de manière asynchrone",
        description = "Envoie une requête pour traitement asynchrone via Kafka et attend la réponse"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Requête traitée avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erreur lors du traitement de la requête",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))
        )
    })
    @PostMapping("/request")
    public ResponseDTO processRequest(
        @RequestBody @Valid 
        @Parameter(description = "Requête à traiter", required = true) 
        RequestDTO request
    ) throws ExecutionException, InterruptedException {
        return kafkaRequestService.sendRequest(request);
    }
}

// service/KafkaRequestService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class KafkaRequestService {

    private final ReplyingKafkaTemplate<String, Object, Object> kafkaTemplate;
    private static final String REQUEST_TOPIC = "requests-topic";

    public ResponseDTO sendRequest(RequestDTO request) throws ExecutionException, InterruptedException {
        ProducerRecord<String, Object> record = new ProducerRecord<>(REQUEST_TOPIC, request);
        RequestReplyFuture<String, Object, Object> future = kafkaTemplate.sendAndReceive(record);
        
        try {
            ConsumerRecord<String, Object> response = future.get(10, TimeUnit.SECONDS);
            return (ResponseDTO) response.value();
        } catch (TimeoutException e) {
            ResponseDTO errorResponse = new ResponseDTO();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("ERROR");
            errorResponse.setResult("Request timed out");
            return errorResponse;
        }
    }
}

// service/ProcessingService.java
package com.example.kafkarequestrepply.service;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    @KafkaListener(topics = "requests-topic", groupId = "processing-group")
    @SendTo("replies-topic")
    public ResponseDTO processRequest(RequestDTO request) {
        // Simuler un traitement
        ResponseDTO response = new ResponseDTO();
        response.setRequestId(request.getRequestId());
        response.setStatus("SUCCESS");
        response.setResult("Processed: " + request.getData());
        return response;
    }
}

// application.yml
server:
  port: 8080

spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: request-reply-group
      auto-offset-reset: earliest
    producer:
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    defaultModelsExpandDepth: 1
    docExpansion: none
    displayRequestDuration: true
    filter: true
    syntaxHighlight:
      activated: true
      theme: "monokai"
  cache:
    disabled: true
  show-actuator: true
  default-consumes-media-type: application/json
  default-produces-media-type: application/json
  writer-with-default-pretty-printer: true

J'ai mis à jour la configuration avec Swagger UI 4.18.0 et ajouté des améliorations :

1. Dépendances mises à jour :
   - springdoc-openapi-starter-webmvc-ui en version 2.1.0 (compatible avec Swagger UI 4.18.0)
   - Ajout explicite de swagger-ui en version 4.18.0
   - Ajout de spring-boot-starter-validation pour les validations

2. Configuration OpenAPI améliorée :
   - Ajout de documentation externe
   - Configuration des groupes d'API
   - Support des annotations de validation

3. Configuration Swagger UI enrichie dans application.yml :
   - Chemin de l'API docs mis à jour vers `/v3/api-docs`
   - Activation de la coloration syntaxique avec thème Monokai
   - Ajout du filtre de recherche
   - Affichage de la durée des requêtes
   - Configuration du cache désactivée pour le développement
   - Support des actuators
   - Configuration des types de médias par défaut

Pour accéder à Swagger UI :
1. Démarrez l'application
2. Ouvrez http://localhost:8080/swagger-ui.html



```bash
curl -X POST http://localhost:8080/api/request \
-H "Content-Type: application/json" \
-d '{
  "requestId": "req-123",
  "data": "test data"
}'
```

