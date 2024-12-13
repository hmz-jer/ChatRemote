 import org.apache.commons.text.StringEscapeUtils;

public class StringSanitizer {
    private static final int MAX_LENGTH = 1000;

    /**
     * Sanitize une chaîne de caractères pour le logging
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "null";
        }

        // Suppression des caractères de contrôle
        String sanitized = input.replaceAll("[\\r\\n\\t]", " ");
        
        // Échappement des caractères spéciaux
        sanitized = StringEscapeUtils.escapeJava(sanitized);
        
        // Troncature si nécessaire
        return truncate(sanitized);
    }

    /**
     * Sanitize un tableau de chaînes
     */
    public static String[] sanitizeArray(String... inputs) {
        if (inputs == null) {
            return new String[0];
        }

        return java.util.Arrays.stream(inputs)
            .map(StringSanitizer::sanitize)
            .toArray(String[]::new);
    }

    /**
     * Tronque une chaîne si elle dépasse la longueur maximale
     */
    public static String truncate(String input) {
        if (input != null && input.length() > MAX_LENGTH) {
            return input.substring(0, MAX_LENGTH) + "...";
        }
        return input;
    }

    /**
     * Formate un tableau de chaînes en une représentation lisible
     */
    public static String formatArray(String[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        return "[" + String.join(", ", array) + "]";
    }
}
