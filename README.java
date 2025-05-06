 package com.example.kafkamock.scheduler;

import com.example.kafkamock.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Planificateur pour envoyer des messages périodiquement
 * Permet de tester automatiquement le flux sans intervention manuelle
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MessageScheduler {

    private final KafkaService kafkaService;
    
    @Value("${scheduler.enabled:false}")
    private boolean schedulerEnabled;

    /**
     * Envoie un message à intervalles réguliers si activé
     * L'intervalle est configurable dans application.properties
     */
    @Scheduled(fixedRateString = "${scheduler.interval:60000}")
    public void sendPeriodicMessage() {
        if (schedulerEnabled) {
            String content = String.format("Message automatique %s envoyé à %s", 
                UUID.randomUUID(), 
                LocalDateTime.now());
                
            log.info("Envoi d'un message planifié: {}", content);
            kafkaService.sendMessage(content);
        }
    }
}
