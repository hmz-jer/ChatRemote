   Map<MetricName, ? extends Metric> metrics = consumer.metrics();
            for (MetricName metricName : metrics.keySet()) {
                String name = metricName.name();
                if (name.equals("connection-count") || name.equals("incoming-byte-rate") || name.equals("outgoing-byte-rate") || name.equals("request-rate") || name.equals("response-rate")) {
                    Metric metric = metrics.get(metricName);
                    double value = (Double) metric.metricValue();
                    if (value > 0) {
                        return true; // Kafka est accessible
                    }
                }
            }
