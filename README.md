import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetRegex {
    public static void main(String[] args) {
        // Chaîne à analyser
        String input = "urn:iso:20022:tech:xsd:pacs.008.001.02";

        // Expression régulière pour récupérer la partie voulue
        String regex = ":xsd:(\\w+\\.\\d+\\.\\d+\\.\\d+\\.\\d+)";

        // Compilation de l'expression régulière
        Pattern pattern = Pattern.compile(regex);

        // Recherche de la partie voulue dans la chaîne
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Affichage de la partie récupérée
            System.out.println("Partie récupérée: " + matcher.group(1));
        } else {
            // Aucune partie récupérée
            System.out.println("Aucune partie récupérée");
        }
    }
}
