import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {
    public static void main(String[] args) {
        String input = "urn:iso:20022:tech:xsd:pacs.008.001.02";
        String pattern = "pacs\\.(\\d+\\.\\d+\\.\\d+)";
        
        // Compilation de l'expression régulière
        Pattern p = Pattern.compile(pattern);
        
        // Recherche de correspondance
        Matcher m = p.matcher(input);
        
        if (m.find()) {
            String result = m.group(1);
            System.out.println(result);
        } else {
            System.out.println("Aucune correspondance trouvée");
        }
    }
}
