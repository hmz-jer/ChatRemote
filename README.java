 package com.example.kafkamock.service;

import com.example.kafkamock.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service qui fournit des métriques sur les messages
 * Permet de suivre les performances et le taux de réussite du flux
 */
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final KafkaService kafkaService;
    
    /**
     * Récupère le nombre total de messages envoyés
     * 
     * @return Nombre de messages envoyés
     */
    public int getTotalSentMessages() {
        return kafkaService.getSentMessages().size();
    }
    
    /**
     * Récupère le nombre total de messages reçus
     * 
     * @return Nombre de messages reçus
     */
    public int getTotalReceivedMessages() {
        return kafkaService.getReceivedMessages().size();
    }
    
    /**
     * Calcule le taux de réussite (messages reçus / messages envoyés)
     * 
     * @return Taux de réussite en pourcentage
     */
    public double getSuccessRate() {
        int sent = getTotalSentMessages();
        if (sent == 0) {
            return 0.0;
        }
        
        int received = getTotalReceivedMessages();
        return (double) received / sent * 100.0;
    }
    
    /**
     * Calcule le temps de réponse moyen pour les messages
     * 
     * @return Temps de réponse moyen en millisecondes
     */
    public long getAverageResponseTime() {
        ConcurrentMap<String, Message> sentMessages = kafkaService.getSentMessages();
        ConcurrentMap<String, Message> receivedMessages = kafkaService.getReceivedMessages();
        
        AtomicInteger count = new AtomicInteger(0);
        long totalDuration = sentMessages.entrySet().stream()
            .filter(entry -> receivedMessages.containsKey(entry.getKey()))
            .mapToLong(entry -> {
                Message sent = entry.getValue();
                Message received = receivedMessages.get(entry.getKey());
                count.incrementAndGet();
                return Duration.between(sent.getTimestamp(), received.getTimestamp()).toMillis();
            })
            .sum();
        
        return count.get() > 0 ? totalDuration / count.get() : 0;
    }
    
    /**
     * Récupère les statistiques des messages des dernières 24 heures
     * 
     * @return Map contenant les statistiques
     */
    public Map<String, Object> getLast24HoursStats() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        long recentSent = kafkaService.getSentMessages().values().stream()
            .filter(message -> message.getTimestamp().isAfter(cutoff))
            .count();
            
        long recentReceived = kafkaService.getReceivedMessages().values().stream()
            .filter(message -> message.getTimestamp().isAfter(cutoff))
            .count();
            
        double recentSuccessRate = recentSent > 0 ? (double) recentReceived / recentSent * 100.0 : 0.0;
        
        return Map.of(
            "sent", recentSent,
            "received", recentReceived,
            "successRate", recentSuccessRate
        );
    }
    
    /**
     * Récupère les messages en erreur
     * 
     * @return Map des messages en erreur
     */
    public Map<String, Message> getErrorMessages() {
        return kafkaService.getSentMessages().values().stream()
            .filter(message -> message.getStatus() == Message.MessageStatus.ERROR)
            .collect(Collectors.toMap(Message::getId, message -> message));
    }
    
    /**
     * Calcule le temps de réponse médian pour les messages
     * 
     * @return Temps de réponse médian en millisecondes
     */
    public long getMedianResponseTime() {
        ConcurrentMap<String, Message> sentMessages = kafkaService.getSentMessages();
        ConcurrentMap<String, Message> receivedMessages = kafkaService.getReceivedMessages();
        
        long[] responseTimes = sentMessages.entrySet().stream()
            .filter(entry -> receivedMessages.containsKey(entry.getKey()))
            .mapToLong(entry -> {
                Message sent = entry.getValue();
                Message received = receivedMessages.get(entry.getKey());
                return Duration.between(sent.getTimestamp(), received.getTimestamp()).toMillis();
            })
            .sorted()
            .toArray();
            
        if (responseTimes.length == 0) {
            return 0;
        }
        
        if (responseTimes.length % 2 == 0) {
            return (responseTimes[responseTimes.length / 2 - 1] + responseTimes[responseTimes.length / 2]) / 2;
        } else {
            return responseTimes[responseTimes.length / 2];
        }
    }
    
    /**
     * Calcule le nombre de messages par statut
     * 
     * @return Map avec le compte de messages par statut
     */
    public Map<Message.MessageStatus, Long> getMessageCountByStatus() {
        // Compter les messages envoyés par statut
        Map<Message.MessageStatus, Long> sentByStatus = kafkaService.getSentMessages().values().stream()
            .collect(Collectors.groupingBy(Message::getStatus, Collectors.counting()));
            
        // Compter les messages reçus par statut
        Map<Message.MessageStatus, Long> receivedByStatus = kafkaService.getReceivedMessages().values().stream()
            .collect(Collectors.groupingBy(Message::getStatus, Collectors.counting()));
            
        // Fusionner les deux maps
        receivedByStatus.forEach((status, count) -> 
            sentByStatus.merge(status, count, Long::sum)
        );
        
        return sentByStatus;
    }
    
    /**
     * Calcule le taux d'erreur des messages
     * 
     * @return Taux d'erreur en pourcentage
     */
    public double getErrorRate() {
        int totalMessages = getTotalSentMessages();
        if (totalMessages == 0) {
            return 0.0;
        }
        
        long errorCount = getErrorMessages().size();
        return (double) errorCount / totalMessages * 100.0;
    }
    
    /**
     * Génère un rapport complet de métriques
     * 
     * @return Map contenant toutes les métriques
     */
    public Map<String, Object> getCompleteMetricsReport() {
        return Map.of(
            "totalSent", getTotalSentMessages(),
            "totalReceived", getTotalReceivedMessages(),
            "successRate", getSuccessRate(),
            "errorRate", getErrorRate(),
            "averageResponseTime", getAverageResponseTime(),
            "medianResponseTime", getMedianResponseTime(),
            "messagesByStatus", getMessageCountByStatus(),
            "last24Hours", getLast24HoursStats(),
            "errorCount", getErrorMessages().size(),
            "timestamp", LocalDateTime.now()
        );
    }
}
