
@Override
public void run() {
    try {
        status = AvailableStatus.STARTED;
        RsLogEnum.KAFKA_CONSUMER_STARTED.generateLog();

        synchronized (this) {
            while (!stopped) {
                while (paused) {
                    wait(); // Attendre que paused soit mis à false
                }
                processTopic();
            }
        }
    } catch (InterruptedException e) {
        LOGGER.error("Consumer thread interrupted", e);
        RsLogEnum.KAFKA_POLLING_THREAD_ERROR.generateLog(e.getMessage());
        Thread.currentThread().interrupt();
    } catch (Exception e) {
        LOGGER.error("Global consuming error", e);
        RsLogEnum.KAFKA_POLLING_THREAD_ERROR.generateLog(e.getMessage());
    } finally {
        status = AvailableStatus.STOPPED;
        RsLogEnum.KAFKA_CONSUMER_STOPPED.generateLog();
        consumer.close();
    }
}
