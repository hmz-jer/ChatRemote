package com.example.kafkamock.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration pour l'ObjectMapper JSON
 * Permet de gérer correctement les dates et formats de sérialisation
 */
@Configuration
public class JsonConfig {

    /**
     * Configure l'ObjectMapper pour gérer les dates et types Java correctement
     * 
     * @return ObjectMapper configuré
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
