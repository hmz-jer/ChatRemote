package com.hmz.configInjecteur.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.Node;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaHealthChecker {

    private String bootstrapServers; // Liste des serveurs Kafka séparés par des virgules

    public KafkaHealthChecker(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String checkClusterHealth() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            // Vérifier l'état du cluster Kafka
            DescribeClusterResult clusterResult = adminClient.describeCluster(new DescribeClusterOptions().timeoutMs(5000));

            // Vérifier les brokers
            Collection<Node> nodes = clusterResult.nodes().get();
            if (nodes.isEmpty()) {
                return "DOWN";
            }

            for (Node node : nodes) {
                if (!node.hasRack()) { // Utiliser une condition appropriée pour vérifier si le broker est opérationnel
                    return "DOWN";
                }
            }

            // Vérifier l'état des topics ici si nécessaire

            return "OK";
        } catch (InterruptedException | ExecutionException e) {
            // Gérer les exceptions liées aux opérations de l'AdminClient
            Thread.currentThread().interrupt();
            return "DOWN";
        } catch (Exception e) {
            // Gérer les autres exceptions
            return "DOWN";
        }
    }

    public static void main(String[] args) {
        KafkaHealthChecker checker = new KafkaHealthChecker("localhost:9092"); // Remplacer par vos serveurs Kafka
        String clusterStatus = checker.checkClusterHealth();
        System.out.println("Status: " + clusterStatus);
    }
}
