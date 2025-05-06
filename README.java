 package com.example.kafkamock.controller;

import com.example.kafkamock.model.Message;
import com.example.kafkamock.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur REST pour interagir avec le mock Kafka
 * Expose des endpoints pour envoyer des messages et consulter les messages
 */
@RestController
@RequestMapping("/api/mock")
@RequiredArgsConstructor
@Slf4j
public class MockController {

    private final KafkaService kafkaService;

    /**
     * Envoie un message sur le topic outbound (topicA)
     * 
     * @param payload Map contenant le contenu du message à envoyer
     * @return Le message créé et envoyé
     */
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Map<String, String> payload) {
        String content = payload.getOrDefault("content", "");
        if (content.isEmpty()) {
            log.warn("Tentative d'envoi d'un message vide");
            return ResponseEntity.badRequest().build();
        }
        
        Message message = kafkaService.sendMessage(content);
        log.info("Message envoyé via l'API: {}", message.getId());
        return ResponseEntity.ok(message);
    }

    /**
     * Récupère tous les messages envoyés
     * 
     * @return Map des messages envoyés
     */
    @GetMapping("/sent")
    public ResponseEntity<Map<String, Message>> getSentMessages() {
        log.info("Récupération de tous les messages envoyés");
        return ResponseEntity.ok(kafkaService.getSentMessages());
    }

    /**
     * Récupère tous les messages reçus
     * 
     * @return Map des messages reçus
     */
    @GetMapping("/received")
    public ResponseEntity<Map<String, Message>> getReceivedMessages() {
        log.info("Récupération de tous les messages reçus");
        return ResponseEntity.ok(kafkaService.getReceivedMessages());
    }
    
    /**
     * Récupère un message envoyé par son ID
     * 
     * @param id L'ID du message
     * @return Le message ou 404 s'il n'existe pas
     */
    @GetMapping("/sent/{id}")
    public ResponseEntity<Message> getSentMessageById(@PathVariable String id) {
        Message message = kafkaService.getSentMessageById(id);
        if (message == null) {
            log.warn("Message envoyé non trouvé: {}", id);
            return ResponseEntity.notFound().build();
        }
        log.info("Récupération du message envoyé: {}", id);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Récupère un message reçu par son ID
     * 
     * @param id L'ID du message
     * @return Le message ou 404 s'il n'existe pas
     */
    @GetMapping("/received/{id}")
    public ResponseEntity<Message> getReceivedMessageById(@PathVariable String id) {
        Message message = kafkaService.getReceivedMessageById(id);
        if (message == null) {
            log.warn("Message reçu non trouvé: {}", id);
            return ResponseEntity.notFound().build();
        }
        log.info("Récupération du message reçu: {}", id);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Efface tous les messages stockés
     * 
     * @return 204 No Content
     */
    @DeleteMapping("/messages")
    public ResponseEntity<Void> clearMessages() {
        log.info("Suppression de tous les messages");
        kafkaService.clearMessages();
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Endpoint de santé simple
     * 
     * @return Message de statut
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.debug("Vérification de l'état de santé");
        return ResponseEntity.ok(Map.of("status", "UP", "service", "kafka-mock"));
    }
}
