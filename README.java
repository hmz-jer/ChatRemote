  package com.example.kafkamock.service;

import com.example.kafkamock.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Service pour exécuter des tests de charge sur le flux Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TestService {

    private final KafkaService kafkaService;
    
    @Value("${test.max-threads:10}")
    private int maxThreads;
    
    @Value("${test.timeout-seconds:60}")
    private int timeoutSeconds;
    
    /**
     * Exécute un test de charge en envoyant plusieurs messages simultanément
     * 
     * @param messageCount Nombre de messages à envoyer
     * @param messageTemplate Modèle de message à utiliser
     * @return Liste des messages envoyés
     */
    public List<Message> runLoadTest(int messageCount, String messageTemplate) {
        log.info("Démarrage d'un test de charge avec {} messages", messageCount);
        
        List<Message> sentMessages = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(messageCount, maxThreads)
        );
        
        List<CompletableFuture<Message>> futures = new ArrayList<>();
        
        for (int i = 0; i < messageCount; i++) {
            final int messageIndex = i;
            CompletableFuture<Message> future = CompletableFuture.supplyAsync(() -> {
                String content = String.format("%s - %d (%s)", 
                    messageTemplate, 
                    messageIndex,
                    UUID.randomUUID());
                    
                return kafkaService.sendMessage(content);
            }, executor);
            
            futures.add(future);
        }
        
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            allFutures.get(timeoutSeconds, TimeUnit.SECONDS);
            
            for (CompletableFuture<Message> future : futures) {
                sentMessages.add(future.get());
            }
            
            log.info("Test de charge terminé, {} messages envoyés", sentMessages.size());
        } catch (Exception e) {
            log.error("Erreur lors du test de charge", e);
        } finally {
            executor.shutdown();
        }
        
        return sentMessages;
    }
    
    /**
     * Attend que tous les messages du test reçoivent une réponse
     * 
     * @param sentMessages Liste des messages envoyés
     * @param timeoutSeconds Délai d'attente maximum en secondes
     * @return Pourcentage de messages ayant reçu une réponse
     */
    public double waitForResponses(List<Message> sentMessages, int timeoutSeconds) {
        log.info("Attente des réponses pour {} messages", sentMessages.size());
        
        LocalDateTime startTime = LocalDateTime.now();
        int totalMessages = sentMessages.size();
        int receivedCount = 0;
        
        while (Duration.between(startTime, LocalDateTime.now()).getSeconds() < timeoutSeconds) {
            receivedCount = 0;
            
            for (Message message : sentMessages) {
                if (kafkaService.getReceivedMessageById(message.getId()) != null) {
                    receivedCount++;
                }
            }
            
            if (receivedCount == totalMessages) {
                log.info("Toutes les réponses reçues");
                return 100.0;
            }
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interruption pendant l'attente des réponses");
                break;
            }
        }
        
        double successRate = totalMessages > 0 ? (double) receivedCount / totalMessages * 100.0 : 0.0;
        log.info("Délai d'attente écoulé, {} réponses reçues sur {} ({}%)", 
            receivedCount, totalMessages, String.format("%.2f", successRate));
            
        return successRate;
    }
}
