import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexExample {
    public static void main(String[] args) {
        String input = "urn:iso:20022:tech:xsd:pacs.008.001.02";
        String regex = "(pacs|camt)\\.\\d{3}\\.\\d{2}\\.\\d{1}\\.\\d{2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
