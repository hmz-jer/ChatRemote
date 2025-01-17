version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9093:9093"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_LISTENERS: SSL://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: SSL://localhost:9093
      KAFKA_SSL_KEYSTORE_FILENAME: kafka.server.keystore.jks
      KAFKA_SSL_KEYSTORE_CREDENTIALS: keystore_creds
      KAFKA_SSL_KEY_CREDENTIALS: key_creds
      KAFKA_SSL_TRUSTSTORE_FILENAME: kafka.server.truststore.jks
      KAFKA_SSL_TRUSTSTORE_CREDENTIALS: truststore_creds
      KAFKA_SSL_CLIENT_AUTH: required
      KAFKA_SECURITY_PROTOCOL: SSL
      KAFKA_INTER_BROKER_LISTENER_NAME: SSL
      KAFKA_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM: ""
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    volumes:
      - ./certs:/etc/kafka/secrets
