import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class GetXmlns {
    public static void main(String[] args) throws Exception {
        // Lecture du fichier XML
        File xmlFile = new File("file.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        
        // Récupération de l'élément racine
        Element root = doc.getDocumentElement();
        
        // Récupération de la valeur de l'attribut xmlns
        String xmlns = root.getAttribute("xmlns");
        
        // Affichage de la valeur de l'attribut
        System.out.println("xmlns: " + xmlns);
    }
}
