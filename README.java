#!/bin/bash

# Configuration
KAFKA_BIN_PATH="/path/to/kafka/bin"
BROKER_LIST="localhost:9092"
TOPIC="your-topic"
MESSAGE="Votre message ici"
NUM_MESSAGES=100  # Nombre de fois à envoyer le message

# Envoyer le message plusieurs fois au sujet Kafka
for ((i=1; i<=NUM_MESSAGES; i++))
do
  echo "$MESSAGE" | "$KAFKA_BIN_PATH/kafka-console-producer.sh" --broker-list "$BROKER_LIST" --topic "$TOPIC"
done

echo "Le message a été envoyé $NUM_MESSAGES fois à Kafka."
