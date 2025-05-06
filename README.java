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
