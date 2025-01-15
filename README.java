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
    </dependencies>
</project>

// Application.java
package com.example.kafkarequestrepply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
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

@Data
public class RequestDTO {
    private String requestId;
    private String data;
}

// model/ResponseDTO.java
package com.example.kafkarequestrepply.model;

import lombok.Data;

@Data
public class ResponseDTO {
    private String requestId;
    private String result;
    private String status;
}

// controller/RequestController.java
package com.example.kafkarequestrepply.controller;

import com.example.kafkarequestrepply.model.RequestDTO;
import com.example.kafkarequestrepply.model.ResponseDTO;
import com.example.kafkarequestrepply.service.KafkaRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RequestController {

    private final KafkaRequestService kafkaRequestService;

    @PostMapping("/request")
    public ResponseDTO processRequest(@RequestBody RequestDTO request) 
            throws ExecutionException, InterruptedException {
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
