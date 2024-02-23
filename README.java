 private void modifySchema(Map<String, Object> schema) {
    List<String> keysToRemove = new ArrayList<>();
    Map<String, Object> itemsToAdd = new HashMap<>();

    schema.forEach((key, value) -> {
        if ("examples".equals(key) && value instanceof Map) {
            Map<?, ?> examplesMap = (Map<?, ?>) value;
            if (!examplesMap.isEmpty()) {
                Object firstExampleValue = examplesMap.values().iterator().next();
                itemsToAdd.put("example", firstExampleValue);
            }
            keysToRemove.add(key);
        } else if (value instanceof Map) {
            modifySchema((Map<String, Object>) value);
        } else if (value instanceof List) {
            ((List<?>) value).forEach(item -> {
                if (item instanceof Map) {
                    modifySchema((Map<String, Object>) item);
                }
            });
        }
    });

    keysToRemove.forEach(schema::remove);
    schema.putAll(itemsToAdd);
}
