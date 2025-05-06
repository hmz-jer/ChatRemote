 // src/main/resources/application.yaml
server:
  port: 8080

spring:
  application:
    name: kafka-mock
  kafka:
    bootstrap-servers: localhost:9093
    properties:
      security.protocol: SSL
      ssl.truststore.location: ${KAFKA_TRUSTSTORE_LOCATION:/etc/kafka-mock/ssl/client.truststore.jks}
      ssl.truststore.password: ${KAFKA_TRUSTSTORE_PASSWORD:truststorepassword}
      ssl.keystore.location: ${KAFKA_KEYSTORE_LOCATION:/etc/kafka-mock/ssl/client.keystore.jks}
      ssl.keystore.password: ${KAFKA_KEYSTORE_PASSWORD:keystorepassword}
      ssl.key.password: ${KAFKA_KEY_PASSWORD:keypassword}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: all
      retries: 3
    consumer:
      group-id: kafka-mock-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      enable-auto-commit: false

kafka:
  topic:
    outbound: topicA
    inbound: topicB

scheduler:
  enabled: false
  interval: 60000

logging:
  level:
    root: INFO
    com.example.kafkamock: DEBUG
    org.apache.kafka: WARN
    org.springframework.kafka: WARN
  file:
    name: ${LOG_DIR:/var/log/kafka-mock}/kafka-mock.log
    
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

---
# Configuration de développement
spring:
  config:
    activate:
      on-profile: dev
    
server:
  port: 8081

scheduler:
  enabled: true
  interval: 30000

---
# Configuration de test
spring:
  config:
    activate:
      on-profile: test
      
kafka:
  topic:
    outbound: test-topicA
    inbound: test-topicB
