import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class SSLClient {
    public static void main(String[] args) {
        try {
            // Créez une connexion SSL à l'hôte et au port spécifiés
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("host", 443);

            // Récupérez les entrées et les sorties du socket
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            // Boucle pour gérer les messages reçus et envoyés
            while (true) {
                // Lisez le message reçu du serveur
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String message = reader.readLine();

                // Vérifiez le message reçu et envoyez un message XML en conséquence
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();

                if (message.equals("Request 1")) {
                    Element rootElement = doc.createElement("Response");
                    doc.appendChild(rootElement);
                    rootElement.setAttribute("type", "1");
                    rootElement.appendChild(doc.createTextNode("Response 1"));
                } else if (message.equals("Request 2")) {
                    Element rootElement = doc.createElement("Response");
                    doc.appendChild(rootElement);
                    rootElement.setAttribute("type", "2");
                    rootElement.appendChild(doc.createTextNode("Response 2"));
                }

                // Écrivez le message XML sur la sortie du socket
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(output);
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
