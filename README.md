Java XPath Read Attribut


hamza.jeridi@outlook.com
Donne moi un code java qui lit un fichier xml et récupère un attribut a partir d'un xpath. 

Voici un exemple de code Java qui lit un fichier XML, crée un objet XPath et utilise cet objet pour récupérer la valeur de l'attribut "attributName" à partir de l'élément correspondant à l'expression XPath "xpathExpression":

Copy code
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import java.io.File;

public class XPathAttribute {
    public static void main(String[] args) throws Exception {
        // Lecture du fichier XML
        File xmlFile = new File("file.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(xmlFile);

        // Création de l'objet XPath
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Récupération de la valeur de l'attribut
        String expression = "xpathExpression";
        String attributeName = "attributName";
        String attributeValue = xpath.compile(expression).evaluate(doc, XPathConstants.STRING).toString();

        // Affichage de la valeur de l'attribut
        System.out.println(attributeValue);
    }
}
