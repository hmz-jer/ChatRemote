if (value instanceof List) {
    List<?> listValue = (List<?>) value;
    listValue.forEach(item -> {
        if (item instanceof Map) {
            convertJsonSchemaExamples((Map<String, Object>) item); // Appel récursif pour les éléments des listes
        }
    });

    // Exemple pour distinguer entre ArrayList et LinkedList
    if (listValue instanceof ArrayList) {
        // Traitement spécifique à ArrayList
    } else if (listValue instanceof LinkedList) {
        // Traitement spécifique à LinkedList
    }
}
