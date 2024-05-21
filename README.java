 

    import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Properties;

public class ConsumerRunner implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerRunner.class);
    private final String topic;
    private final Properties properties;
    private volatile boolean running = true;
    private Consumer<String, String> consumer;

    public ConsumerRunner(String brokers, String groupId, String topic) {
        this.topic = topic;
        properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    }

    @Override
    public void run() {
        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (running) {
                try {
                    ConsumerRecords<String, String> records = consumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Consumed message: offset = {}, key = {}, value = {}", record.offset(), record.key(), record.value());
                    }
                    consumer.commitSync();
                    logger.info("Offsets have been committed successfully.");
                } catch (WakeupException e) {
                    if (!running) {
                        logger.info("Consumer wakeup triggered, exiting poll loop.");
                        break;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Consumer error: ", e);
        } finally {
            try {
                consumer.commitSync();
                logger.info("Final commit of offsets before closing.");
            } catch (Exception e) {
                logger.error("Error committing offsets during shutdown: ", e);
            }
            consumer.close();
            logger.info("Consumer closed.");
        }
    }

    public void shutdown() {
        running = false;
        if (consumer != null) {
            consumer.wakeup();
        }
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: KafkaConsumerApp <start|stop> <brokers> <groupId> <topic> <numConsumers>");
            return;
        }

        String command = args[0];
        String brokers = args[1];
        String groupId = args[2];
        String topic = args[3];
        int numConsumers = Integer.parseInt(args[4]);

        ConsumerManager manager = new ConsumerManager(numConsumers, brokers, groupId, topic);

        if ("start".equalsIgnoreCase(command)) {
            manager.startConsumers();
            Runtime.getRuntime().addShutdownHook(new Thread(manager::stopConsumers));
            System.out.println("Consumers started.");
        } else if ("stop".equalsIgnoreCase(command)) {
            manager.stopConsumers();
            System.out.println("Consumers stopped.");
        } else {
            System.out.println("Unknown command: " + command);
        }
    }
}

class ConsumerManager {
    private final int numConsumers;
    private final List<ConsumerRunner> consumers = new ArrayList<>();
    private final ExecutorService executor;

    public ConsumerManager(int numConsumers, String brokers, String groupId, String topic) {
        this.numConsumers = numConsumers;
        this.executor = Executors.newFixedThreadPool(numConsumers);
        for (int i = 0; i < numConsumers; i++) {
            ConsumerRunner consumer = new ConsumerRunner(brokers, groupId, topic);
            consumers.add(consumer);
        }
    }

    public void startConsumers() {
        for (ConsumerRunner consumer : consumers) {
            executor.submit(consumer);
        }
    }

    public void stopConsumers() {
        for (ConsumerRunner consumer : consumers) {
            consumer.shutdown();
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
