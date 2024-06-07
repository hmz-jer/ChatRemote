public class Consumer extends BaseConsumer implements Stoppable {
    private volatile boolean stopped = false;
    private volatile boolean paused = false;

    public synchronized void pause() {
        this.paused = true;
        synchronized (this) {
            notifyAll(); // Réveille les threads en attente pour vérifier l'état
        }
        System.out.println("PAUSE => " + paused);
    }

    public synchronized void resume() {
        this.paused = false;
        synchronized (this) {
            notifyAll(); // Réveille les threads en attente pour reprendre le traitement
        }
        System.out.println("RESUME => " + paused);
    }

    @Override
    public synchronized void stop() {
        this.stopped = true;
        this.paused = false; // Vous pouvez mettre à jour paused si nécessaire
        synchronized (this) {
            notifyAll(); // Assurez-vous que le thread ne reste pas bloqué en pause
        }
        status = AvailableStatus.STOPPED;
        RsLogEnum.KAFKA_CONSUMER_STOP_REQUESTED.generateLog();
        System.out.println("STOP => " + stopped);
    }

    private void processTopic() {
        long processDuration = 0;
        if (messageToBeProcessedListener.canAddMessagesBlock(blockSize)) {
            bufferFullDebounce.set(false);

            // Consommer les messages Kafka ici
            ConsumerRecords<String, String> records;
            try {
                records = consumer.poll(Duration.ofMillis(100));
            } catch (Exception e) {
                LOGGER.error("Error during Kafka poll", e);
                return; // En cas d'erreur, retourner pour permettre la gestion par la méthode run
            }

            if (!records.isEmpty()) {
                LOGGER.debug("Before buffer size {} = messageToBeProcessedListener.getBufferSize()");
                for (ConsumerRecord<String, String> record : records) {
                    MapConfigurationItem<String, List<Pair<TextMsg, String>>> waitingMessage = new HashMap<>();
                    long startTime = System.currentTimeMillis();
                    LOGGER.info("Msgs received from {}: {}", records.count());

                    for (ConsumerRecord<String, String> rec : records) {
                        counterManager.addTotalMessage();
                        String read = record.value();
                        processMessageReceivedFromKafka(read, waitingMessage, record.offset());
                    }
                    fillUpMessagesToBeSentBuffer(waitingMessage);
                }

                try {
                    consumer.commitSync();
                } catch (Exception e) {
                    LOGGER.error("Cannot commit Kafka poll", e);
                    RsLogEnum.KAFKA_COMMIT_IMPOSSIBLE.generateLog(e.getMessage());
                    LOGGER.warn("Closing current consumer");
                    consumer.close();
                    this.consumer = null;
                    createConsumer(false);
                }
            }
        } else {
            processDuration = System.currentTimeMillis() - startTime;
            if (bufferFullDebounce.get() || processDuration >= 10) {
                bufferFullDebounce.set(true);
                LOGGER.debug("The buffer icon msg {} is null, the buffer size = {}, get name {}", blockSize, getName());
                try {
                    Thread.sleep(processDuration > 500 ? 100 : 500);
                } catch (InterruptedException e) {
                    LOGGER.error("Consumer thread interrupted during sleep", e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            status = AvailableStatus.STARTED;
            RsLogEnum.KAFKA_CONSUMER_STARTED.generateLog();

            while (!stopped) {
                synchronized (this) {
                    while (paused) {
                        wait(); // Attendre que paused soit mis à false
                    }
                }
                processTopic();
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
            if (consumer != null) {
                consumer.close();
            }
        }
    }
}
