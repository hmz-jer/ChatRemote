Aprivate void processElement(Object element) {
    if (element instanceof Map) {
        Map<?, ?> originalMap = (Map<?, ?>) element;
        // Convertit en ConcurrentHashMap si nécessaire pour le traitement spécifique
        ConcurrentHashMap<String, Object> concurrentMap = new ConcurrentHashMap<>(originalMap);
        concurrentMap.forEach((key, value) -> {
            System.out.println("Key: " + key);
            processElement(value);
        });
    } else if (element instanceof List) {
        List<?> list = (List<?>) element;
        list.forEach(this::processElement);
    } else {
        System.out.println("Value: " + element);
    }
}
