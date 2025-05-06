 package com.example.kafkamock.service;

import com.example.kafkamock.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service qui gère l'envoi et la réception des messages Kafka
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kafka.topic.outbound}")
    private String outboundTopic;
    
    // Stockage des messages envoyés et reçus pour le suivi
    private final ConcurrentMap<String, Message> sentMessages = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Message> receivedMessages = new ConcurrentHashMap<>();

    /**
     * Envoie un message sur le topic outbound (topicA)
     * 
     * @param content Le contenu du message à envoyer
     * @return L'objet Message créé et envoyé
     */
    public Message sendMessage(String content) {
        Message message = Message.builder()
                .id(UUID.randomUUID().toString())
                .content(content)
                .timestamp(LocalDateTime.now())
                .status(Message.MessageStatus.SENT)
                .build();
                
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            log.info("Envoi d'un message sur le topic {}: {}", outboundTopic, messageJson);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(outboundTopic, message.getId(), messageJson);
                
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message envoyé avec succès: {}", message.getId());
                    sentMessages.put(message.getId(), message);
                } else {
                    log.error("Échec de l'envoi du message: {}", ex.getMessage(), ex);
                    message.setStatus(Message.MessageStatus.ERROR);
                }
            });
            
            return message;
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la sérialisation du message", e);
            message.setStatus(Message.MessageStatus.ERROR);
            return message;
        }
    }

    /**
     * Écoute le topic inbound (topicB) pour recevoir les réponses du proxy Java
     * 
     * @param message Le message reçu sous forme de chaîne JSON
     * @param ack L'objet Acknowledgment pour confirmer la réception
     */
    @KafkaListener(topics = "${kafka.topic.inbound}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeMessage(String message, Acknowledgment ack) {
        log.info("Message reçu du topic: {}", message);
        
        try {
            Message receivedMessage = objectMapper.readValue(message, Message.class);
            receivedMessage.setStatus(Message.MessageStatus.RECEIVED);
            receivedMessages.put(receivedMessage.getId(), receivedMessage);
            
            // Vérifier si ce message est une réponse à un message envoyé
            if (sentMessages.containsKey(receivedMessage.getId())) {
                log.info("Réponse reçue pour le message: {}", receivedMessage.getId());
            }
            
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Erreur lors de la désérialisation du message", e);
        }
    }
    
    /**
     * Récupère tous les messages envoyés
     * 
     * @return Map des messages envoyés
     */
    public ConcurrentMap<String, Message> getSentMessages() {
        return sentMessages;
    }
    
    /**
     * Récupère tous les messages reçus
     * 
     * @return Map des messages reçus
     */
    public ConcurrentMap<String, Message> getReceivedMessages() {
        return receivedMessages;
    }
    
    /**
     * Récupère un message envoyé par son ID
     * 
     * @param id L'ID du message
     * @return Le message ou null s'il n'existe pas
     */
    public Message getSentMessageById(String id) {
        return sentMessages.get(id);
    }
    
    /**
     * Récupère un message reçu par son ID
     * 
     * @param id L'ID du message
     * @return Le message ou null s'il n'existe pas
     */
    public Message getReceivedMessageById(String id) {
        return receivedMessages.get(id);
    }
    
    /**
     * Efface les messages envoyés et reçus
     */
    public void clearMessages() {
        sentMessages.clear();
        receivedMessages.clear();
        log.info("Tous les messages ont été effacés");
    }
}
