 private void modifySchema(Map<String, Object> schema) {
    // Parcourir récursivement le schéma pour trouver des objets avec un attribut "properties".
    schema.forEach((key, value) -> {
        if ("properties".equals(key) && value instanceof Map) {
            // Nous sommes sous un objet "properties", traitons chaque propriété.
            Map<String, Object> properties = (Map<String, Object>) value;
            properties.forEach((propKey, propValue) -> {
                if (propValue instanceof Map) {
                    Map<String, Object> propertyMap = (Map<String, Object>) propValue;
                    if (propertyMap.containsKey("examples")) {
                        Object examples = propertyMap.get("examples");
                        if (examples instanceof Map) {
                            Map<?, ?> examplesMap = (Map<?, ?>) examples;
                            if (!examplesMap.isEmpty()) {
                                Object firstExampleValue = examplesMap.values().iterator().next();
                                // Remplacer "examples" par "example" avec la première valeur trouvée.
                                propertyMap.put("example", firstExampleValue);
                            }
                            propertyMap.remove("examples");
                        }
                    }
                }
            });
        } else if (value instanceof Map) {
            // Continuer la recherche récursive dans d'autres parties du schéma.
            modifySchema((Map<String, Object>) value);
        } else if (value instanceof List) {
            // Pour les listes, appliquer la modification récursive sur chaque élément de type Map.
            ((List<?>) value).forEach(item -> {
                if (item instanceof Map) {
                    modifySchema((Map<String, Object>) item);
                }
            });
        }
    });
}
