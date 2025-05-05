 package com.example.kafkamock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Modèle de message utilisé pour communiquer avec Kafka
 * Cette classe définit la structure des messages échangés entre le mock et le proxy Java
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    /**
     * Identifiant unique du message
     */
    private String id;
    
    /**
     * Contenu du message
     */
    private String content;
    
    /**
     * Timestamp de création du message
     */
    private LocalDateTime timestamp;
    
    /**
     * Statut actuel du message
     */
    private MessageStatus status;
    
    /**
     * Métadonnées additionnelles (optionnelles)
     */
    private String metadata;
    
    /**
     * Types de statuts possibles pour un message
     */
    public enum MessageStatus {
        SENT,       // Message envoyé
        RECEIVED,   // Message reçu
        PROCESSED,  // Message traité
        ERROR       // Erreur lors du traitement
    }
}
