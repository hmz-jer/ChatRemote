 private void modifySchema(Map<String, Object> schema) {
    schema.forEach((key, value) -> {
        if ("properties".equals(key) && value instanceof Map) {
            // Nous sommes sous un objet "properties", traitons chaque propriété.
            Map<String, Object> properties = (Map<String, Object>) value;
            properties.forEach((propKey, propValue) -> {
                if (propValue instanceof Map) {
                    Map<String, Object> propertyMap = (Map<String, Object>) propValue;
                    if (propertyMap.containsKey("examples")) {
                        Object examples = propertyMap.get("examples");
                        if (examples instanceof List && !((List<?>) examples).isEmpty()) {
                            // Prendre le premier élément de la liste d'exemples
                            Object firstExample = ((List<?>) examples).get(0);
                            // Remplacer "examples" par "example" avec la première valeur trouvée.
                            propertyMap.put("example", firstExample);
                        }
                        propertyMap.remove("examples");
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
