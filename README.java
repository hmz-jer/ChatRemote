
Pour répondre à votre demande, nous allons structurer le code en plusieurs parties :
1. **ConsumerRunner** : Une classe qui implémente `Runnable` et contient la logique du consumer Kafka.
2. **StoppableConsumerThread** : Une classe qui encapsule un thread et un consumer Kafka, avec des méthodes pour démarrer, mettre en pause et réactiver le consumer.
3. **Main** : Une classe principale pour démarrer et contrôler les threads.

### Étape 1 : ConsumerRunner

Voici la classe `ConsumerRunner` qui implémente la logique du consumer Kafka :

```java
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class ConsumerRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);
    private final Consumer<String, String> consumer;
    private volatile boolean running = true;
    private volatile boolean paused = false;

    public ConsumerRunner(String brokers, String groupId, String topic) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        this.consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    @Override
    public void run() {
        try {
            while (running) {
                synchronized (this) {
                    while (paused) {
                        wait();
                    }
                }
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Consumed message: offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
                }
                consumer.commitSync();
            }
        } catch (WakeupException | InterruptedException e) {
            if (!running) {
                logger.info("Consumer wakeup triggered, exiting poll loop.");
            } else {
                logger.error("Consumer error: ", e);
            }
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        running = false;
        consumer.wakeup();
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notify();
    }
}
```

### Étape 2 : StoppableConsumerThread

Cette classe encapsule un thread et un consumer Kafka, avec des méthodes pour démarrer, mettre en pause et réactiver le consumer.

```java
public class StoppableConsumerThread {
    private final Thread thread;
    private final ConsumerRunner consumerRunner;

    public StoppableConsumerThread(String brokers, String groupId, String topic) {
        this.consumerRunner = new ConsumerRunner(brokers, groupId, topic);
        this.thread = new Thread(consumerRunner);
    }

    public void start() {
        thread.start();
    }

    public void shutdown() {
        consumerRunner.shutdown();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void pause() {
        consumerRunner.pause();
    }

    public void resume() {
        consumerRunner.resume();
    }
}
```

### Étape 3 : Main

Voici la classe principale pour démarrer et contrôler les threads.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class KafkaConsumerApp {
    public static void main(String[] args) {
        if (args.length < 5) {
            System.out.println("Usage: KafkaConsumerApp <brokers> <groupId> <topic> <numConsumers>");
            return;
        }

        String brokers = args[0];
        String groupId = args[1];
        String topic = args[2];
        int numConsumers = Integer.parseInt(args[3]);

        List<StoppableConsumerThread> consumers = new ArrayList<>();
        for (int i = 0; i < numConsumers; i++) {
            StoppableConsumerThread consumerThread = new StoppableConsumerThread(brokers, groupId, topic);
            consumers.add(consumerThread);
            consumerThread.start();
        }

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter command (pause <id> / resume <id> / stop <id> / stopall):");
            String command = scanner.nextLine();
            String[] parts = command.split(" ");
            if (parts.length > 1) {
                int id = Integer.parseInt(parts[1]);
                if (id >= 0 && id < consumers.size()) {
                    StoppableConsumerThread consumer = consumers.get(id);
                    if ("pause".equalsIgnoreCase(parts[0])) {
                        consumer.pause();
                        System.out.println("Paused consumer " + id);
                    } else if ("resume".equalsIgnoreCase(parts[0])) {
                        consumer.resume();
                        System.out.println("Resumed consumer " + id);
                    } else if ("stop".equalsIgnoreCase(parts[0])) {
                        consumer.shutdown();
                        System.out.println("Stopped consumer " + id);
                    }
                }
            } else if ("stopall".equalsIgnoreCase(parts[0])) {
                for (StoppableConsumerThread consumer : consumers) {
                    consumer.shutdown();
                }
                System.out.println("Stopped all consumers.");
                break;
            }
        }
        scanner.close();
    }
}
```

### Explications

1. **ConsumerRunner** :
   - Implémente `Runnable` et contient la logique du consumer Kafka.
   - Méthodes `pause()` et `resume()` pour contrôler la pause et la reprise de la consommation.
   - Méthode `shutdown()` pour arrêter proprement le consumer.

2. **StoppableConsumerThread** :
   - Encapsule un `Thread` et une instance de `ConsumerRunner`.
   - Méthodes pour démarrer (`start()`), arrêter (`shutdown()`), mettre en pause (`pause()`) et réactiver (`resume()`) le consumer.

3. **Main** :
   - Démarre le nombre spécifié de consumers.
   - Fournit une interface de commande pour mettre en pause, réactiver ou arrêter individuellement les consumers, ainsi que pour arrêter tous les consumers.

En suivant cette structure, vous pouvez démarrer, mettre en pause, et réactiver les consumers Kafka facilement en utilisant des commandes de ligne de commande interactives.
    
