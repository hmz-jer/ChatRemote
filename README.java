 import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class CertificateProcessor {
    
    public static void main(String[] args) {
        try {
            CertificateProcessor processor = new CertificateProcessor();
            
            // Lire le fichier certificat depuis les ressources
            InputStream certStream = CertificateProcessor.class.getResourceAsStream("/client_certificate.crt");
            if (certStream == null) {
                System.err.println("Fichier certificat non trouvé dans les ressources!");
                System.err.println("Assurez-vous que le fichier 'client_certificate.crt' est dans le dossier resources");
                return;
            }
            
            // Convertir l'InputStream en byte[]
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = certStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            byte[] certificateBytes = baos.toByteArray();
            certStream.close();
            
            // Traiter le certificat
            Map<String, Object> result = processor.processCertificate(certificateBytes);
            
            // Afficher le résultat
            System.out.println("=== Résultat du traitement du certificat ===");
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public Map<String, Object> processCertificate(byte[] certificateBytes) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        // Convertir en String pour vérifier le format
        String certString = new String(certificateBytes, StandardCharsets.UTF_8);
        
        // Variables pour stocker les résultats
        String urTspCustomerAuthCert = null;
        byte[] caCertificateContent = null;
        String caFileName = null;
        
        try {
            // Valider et convertir le certificat
            byte[] validatedCert = validateCertificateAndConvertIntoDerFormat(certificateBytes);
            result.put("validatedCertificate", validatedCert != null);
            
            if (validatedCert != null) {
                // Convertir en PEM si nécessaire
                String pemCert = convertDERToPEM(validatedCert, false);
                urTspCustomerAuthCert = pemCert;
                result.put("urTspCustomerAuthCert", urTspCustomerAuthCert);
                
                // Calculer le hash SHA-256 pour le nom de fichier
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] certificatKey = pemCert.getBytes();
                String key = new String(Base64.getEncoder().encode(digest.digest(certificatKey)));
                key = key.replace('+', '-').replace('/', '-').replaceAll("=", "");
                
                caFileName = key;
                result.put("caFileName", caFileName);
                
                // Stocker le contenu du certificat
                caCertificateContent = validatedCert;
                result.put("caCertificateContentLength", caCertificateContent.length);
            }
            
            // Ajouter d'autres informations utiles
            result.put("certificateFormat", certString.contains("BEGIN CERTIFICATE") ? "PEM" : "DER");
            result.put("processedAt", new Date());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    public String convertDERToPEM(byte[] derString, boolean derStringIsInBase64) throws UnsupportedEncodingException {
        byte[] base64Der = derStringIsInBase64 ? derString : Base64.getEncoder().encode(derString);
        String base64String = new String(base64Der, "utf-8");
        StringBuilder tmp = new StringBuilder();
        int strLength = base64String.length();
        if (strLength > 64) {
            for (int index = 0; index < strLength; index += 64) {
                if (tmp.length() > 0) tmp.append("\r\n");
                tmp.append(base64String.substring(index, index + (strLength - index > 64 ? 64 : strLength - index)));
            }
        } else {
            tmp.append(base64String);
        }
        return "-----BEGIN CERTIFICATE-----" + "\r\n" + tmp.toString() + "\r\n" + "-----END CERTIFICATE-----";
    }
    
    public byte[] validateCertificateAndConvertIntoDerFormat(byte[] certificateContent) throws Exception {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(certificateContent);
            
            // Essayer de charger le certificat
            Collection<?> certificates = certificateFactory.generateCertificates(inputStream);
            
            if (certificates.isEmpty()) {
                throw new Exception("Pas de certificat trouvé dans le fichier");
            }
            
            // Prendre le premier certificat
            X509Certificate x509Certificate = (X509Certificate) certificates.iterator().next();
            
            // Vérifier la validité
            if (checkValidity()) {
                x509Certificate.checkValidity();
            }
            
            // Retourner le certificat en format DER
            return x509Certificate.getEncoded();
            
        } catch (Exception e) {
            // Si ce n'est pas un certificat valide, essayer de le traiter comme PEM
            String certString = new String(certificateContent, StandardCharsets.UTF_8);
            if (certString.contains("BEGIN CERTIFICATE")) {
                // Extraire le contenu entre les marqueurs
                String beginMarker = "-----BEGIN CERTIFICATE-----";
                String endMarker = "-----END CERTIFICATE-----";
                int beginIndex = certString.indexOf(beginMarker);
                int endIndex = certString.indexOf(endMarker);
                
                if (beginIndex != -1 && endIndex != -1) {
                    String base64Content = certString.substring(
                        beginIndex + beginMarker.length(), 
                        endIndex
                    ).replaceAll("\\s", "");
                    
                    // Décoder le Base64
                    byte[] derContent = Base64.getDecoder().decode(base64Content);
                    
                    // Valider le certificat DER
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    InputStream inputStream = new ByteArrayInputStream(derContent);
                    X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                    
                    if (checkValidity()) {
                        x509Certificate.checkValidity();
                    }
                    
                    return x509Certificate.getEncoded();
                }
            }
            
            throw new Exception("Format de certificat non reconnu: " + e.getMessage());
        }
    }
    
    // Méthode pour activer/désactiver la vérification de validité
    private boolean checkValidity() {
        // Vous pouvez modifier cette valeur selon vos besoins
        return true;
    }
    
    // Classe interne pour simuler l'entité ClientCertificat
    static class ClientCertificat {
        private Integer id;
        private byte[] certificateContent;
        private String urIcrl;
        private String fileName;
        private String serialNumber;
        private String thumbprint;
        private Date startDate;
        private Date endDate;
        private String fileDetails;
        private byte[] icaCertificateContent;
        private String caFileName;
        private String caSerialNumber;
        private String caThumbprint;
        private Date caStartDate;
        private Date caEndDate;
        private String caFileDetails;
        
        // Getters et setters
        public byte[] getCertificateContent() { return certificateContent; }
        public void setCertificateContent(byte[] content) { this.certificateContent = content; }
        public String getCaFileName() { return caFileName; }
        public void setCaFileName(String name) { this.caFileName = name; }
        public byte[] getIcaCertificateContent() { return icaCertificateContent; }
        public void setIcaCertificateContent(byte[] content) { this.icaCertificateContent = content; }
    }
}
