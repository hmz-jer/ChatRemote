 package com.example.kafkamock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principale du mock Kafka pour tester le proxy Java
 * Cette application permet d'envoyer des messages sur un topic Kafka et de recevoir les réponses
 * sur un autre topic pour valider le fonctionnement du proxy Java.
 */
@SpringBootApplication
@EnableScheduling
public class KafkaMockApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KafkaMockApplication.class, args);
    }
}
