 Map<String, String> headers = new HashMap<>();
if (responseConfig.containsKey("headers") && responseConfig.get("headers") instanceof Map) {
    @SuppressWarnings("unchecked")
    Map<String, Object> headersConfig = (Map<String, Object>) responseConfig.get("headers");
    headersConfig.forEach((key, value) -> {
        if (value != null) {
            // Injecter les variables dans les headers
            String headerValue = value.toString();
            for (Map.Entry<String, String> variable : variables.entrySet()) {
                headerValue = headerValue.replace("${" + variable.getKey() + "}", variable.getValue());
            }
            headers.put(key, headerValue);
        }
    });
}
