version: '3.8'
services:
  kafka-ui:
    container_name: kafka-ui
    image: provectuslabs/kafka-ui:latest
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: mon-cluster-ssl
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-broker:9093
      KAFKA_CLUSTERS_0_PROPERTIES_SECURITY_PROTOCOL: SSL
      KAFKA_CLUSTERS_0_PROPERTIES_SSL_TRUSTSTORE_LOCATION: /etc/kafka/secrets/kafka.client.truststore.jks
      KAFKA_CLUSTERS_0_PROPERTIES_SSL_TRUSTSTORE_PASSWORD: changeit
      # Pour SSL mutuel
      KAFKA_CLUSTERS_0_PROPERTIES_SSL_KEYSTORE_LOCATION: /etc/kafka/secrets/kafka.client.keystore.jks
      KAFKA_CLUSTERS_0_PROPERTIES_SSL_KEYSTORE_PASSWORD: changeit
      KAFKA_CLUSTERS_0_PROPERTIES_SSL_KEY_PASSWORD: changeit
    volumes:
      - ./secrets:/etc/kafka/secrets:ro
