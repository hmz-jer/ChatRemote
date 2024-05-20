import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsumerManager {
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
    }
}
