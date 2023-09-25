D'accord. Pour vérifier le statut d'un broker Kafka en utilisant l'API Admin de Kafka, voici un exemple simple en Java :Ajoutez les dépendances nécessaires à votre fichier pom.xml (si vous utilisez Maven):<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.8.0</version> <!-- Utilisez la version appropriée -->
</dependency>Exemple de code pour vérifier le statut de Kafka avec l'API Admin:import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;

import java.util.Properties;

public class KafkaStatusChecker {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092"; // Remplacez par l'adresse de votre broker

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BOOTSTRAP_SERVERS);

        try (AdminClient adminClient = AdminClient.create(properties)) {
            DescribeClusterResult clusterDescription = adminClient.describeCluster();
            KafkaFuture<Integer> controllerFuture = clusterDescription.controller();

            // Si on peut récupérer le broker controller, alors Kafka est up
            if (controllerFuture.get() != null) {
                System.out.println("Kafka est up");
            } else {
                System.out.println("Kafka est down");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la vérification du statut de Kafka");
        }
    }
}Remarque : Ce code vérifie la disponibilité du broker "controller" de Kafka. Si le "controller" est disponible, on considère que Kafka est opérationnel. Si vous avez un cluster avec plusieurs brokers, assurez-vous de lister tous les "bootstrap.servers".Ce code est un exemple de base pour vérifier le statut de Kafka. Dans une application réelle, vous voudrez peut-être ajouter plus de gestion d'erreurs et de vérifications.
