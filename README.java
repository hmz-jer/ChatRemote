 package com.example.kafkamock.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka pour le mock
 * Cette classe configure les connexions vers Kafka, les serializers/deserializers,
 * et crée les topics nécessaires.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.outbound}")
    private String outboundTopic;

    @Value("${kafka.topic.inbound}")
    private String inboundTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.properties.security.protocol:SSL}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.ssl.truststore.location}")
    private String truststoreLocation;

    @Value("${spring.kafka.properties.ssl.truststore.password}")
    private String truststorePassword;

    @Value("${spring.kafka.properties.ssl.keystore.location}")
    private String keystoreLocation;

    @Value("${spring.kafka.properties.ssl.keystore.password}")
    private String keystorePassword;

    @Value("${spring.kafka.properties.ssl.key.password}")
    private String keyPassword;

    /**
     * Configuration de base pour l'administration Kafka
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        configs.put("ssl.truststore.location", truststoreLocation);
        configs.put("ssl.truststore.password", truststorePassword);
        configs.put("ssl.keystore.location", keystoreLocation);
        configs.put("ssl.keystore.password", keystorePassword);
        configs.put("ssl.key.password", keyPassword);
        return new KafkaAdmin(configs);
    }

    /**
     * Création du topic pour envoyer les messages
     */
    @Bean
    public NewTopic outboundTopic() {
        return new NewTopic(outboundTopic, 1, (short) 1);
    }

    /**
     * Création du topic pour recevoir les réponses
     */
    @Bean
    public NewTopic inboundTopic() {
        return new NewTopic(inboundTopic, 1, (short) 1);
    }

    /**
     * Configuration du producteur Kafka
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        configProps.put("ssl.truststore.location", truststoreLocation);
        configProps.put("ssl.truststore.password", truststorePassword);
        configProps.put("ssl.keystore.location", keystoreLocation);
        configProps.put("ssl.keystore.password", keystorePassword);
        configProps.put("ssl.key.password", keyPassword);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate pour envoyer des messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Configuration du consommateur Kafka
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.SECURITY_PROTOCOL_CONFIG, securityProtocol);
        configProps.put("ssl.truststore.location", truststoreLocation);
        configProps.put("ssl.truststore.password", truststorePassword);
        configProps.put("ssl.keystore.location", keystoreLocation);
        configProps.put("ssl.keystore.password", keystorePassword);
        configProps.put("ssl.key.password", keyPassword);
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Factory pour les listeners Kafka
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }
}
