 package com.example.kafkamock.controller;

import com.example.kafkamock.model.Message;
import com.example.kafkamock.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour accéder aux métriques du mock Kafka
 */
@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Récupère les métriques globales du mock
     * 
     * @return Map contenant les métriques
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.info("Récupération des métriques globales");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSent", metricsService.getTotalSentMessages());
        metrics.put("totalReceived", metricsService.getTotalReceivedMessages());
        metrics.put("successRate", metricsService.getSuccessRate());
        metrics.put("averageResponseTime", metricsService.getAverageResponseTime());
        metrics.put("last24Hours", metricsService.getLast24HoursStats());
        metrics.put("errorCount", metricsService.getErrorMessages().size());
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Récupère les messages en erreur
     * 
     * @return Map des messages en erreur
     */
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Message>> getErrorMessages() {
        log.info("Récupération des messages en erreur");
        return ResponseEntity.ok(metricsService.getErrorMessages());
    }
    
    /**
     * Récupère un résumé des statistiques pour les dernières 24 heures
     * 
     * @return Map contenant les statistiques
     */
    @GetMapping("/last24hours")
    public ResponseEntity<Map<String, Object>> getLast24HoursStats() {
        log.info("Récupération des statistiques des dernières 24 heures");
        return ResponseEntity.ok(metricsService.getLast24HoursStats());
    }
}
