security.protocol=SSL
ssl.truststore.location=./certs/kafka.client.truststore.jks
ssl.truststore.password=azerty
ssl.keystore.location=./certs/kafka.client.keystore.jks
ssl.keystore.password=azerty
ssl.key.password=azerty

  kafka-topics.sh --create --bootstrap-server localhost:9093 --command-config client-ssl.properties --replication-factor 1 --partitions 1 --topic test-topic
