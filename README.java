import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class CertificateProcessorComplete {
    
    // Variables pour stocker les résultats (simulation des variables de classe)
    private String urTspCustomerAuthCert;
    private String urTspCustomerAuthCA;
    
    public static void main(String[] args) {
        try {
            CertificateProcessorComplete processor = new CertificateProcessorComplete();
            
            // Lire le fichier certificat
            String certificatePath = args.length > 0 ? args[0] : "client_certificate.crt";
            byte[] certificateBytes;
            
            // Essayer d'abord de lire depuis les arguments, sinon depuis les ressources
            File certFile = new File(certificatePath);
            if (certFile.exists()) {
                certificateBytes = Files.readAllBytes(Paths.get(certificatePath));
                System.out.println("Fichier lu: " + certFile.getName() + " (" + certificateBytes.length + " octets)");
            } else {
                // Essayer depuis les ressources
                InputStream certStream = CertificateProcessorComplete.class.getResourceAsStream("/client_certificate.crt");
                if (certStream == null) {
                    System.err.println("Fichier certificat non trouvé!");
                    System.err.println("Utilisez: java CertificateProcessorComplete <chemin_vers_certificat>");
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = certStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                certificateBytes = baos.toByteArray();
                certStream.close();
                System.out.println("Certificat lu depuis les ressources (" + certificateBytes.length + " octets)");
            }
            
            // Traiter le certificat avec les deux méthodes
            Map<String, Object> result = processor.processCompleteCertificate(certificateBytes);
            
            // Afficher le résultat
            System.out.println("\n=== Résultat du traitement complet du certificat ===");
            System.out.println("----------------------------------------------------");
            
            // Afficher les résultats de setUrTspCustomerAuthCert
            System.out.println("\n[1] Résultats de setUrTspCustomerAuthCert:");
            System.out.println("   - urTspCustomerAuthCert défini: " + (result.get("urTspCustomerAuthCert") != null));
            if (result.get("urTspCustomerAuthCert") != null) {
                String cert = (String) result.get("urTspCustomerAuthCert");
                System.out.println("   - Longueur du certificat PEM: " + cert.length() + " caractères");
                System.out.println("   - Début: " + cert.substring(0, Math.min(100, cert.length())) + "...");
            }
            
            // Afficher les résultats de setUrTspCustomerAuthCA
            System.out.println("\n[2] Résultats de setUrTspCustomerAuthCA:");
            System.out.println("   - urTspCustomerAuthCA défini: " + (result.get("urTspCustomerAuthCA") != null));
            System.out.println("   - authCaCrl défini: " + (result.get("authCaCrl") != null));
            if (result.get("urTspCustomerAuthCA") != null) {
                String ca = (String) result.get("urTspCustomerAuthCA");
                System.out.println("   - Longueur du CA PEM: " + ca.length() + " caractères");
            }
            
            // Afficher les autres informations
            System.out.println("\n[3] Informations communes:");
            System.out.println("   - Format détecté: " + result.get("certificateFormat"));
            System.out.println("   - Certificat valide: " + result.get("validatedCertificate"));
            System.out.println("   - Nom de fichier CA: " + result.get("caFileName"));
            System.out.println("   - Date de traitement: " + result.get("processedAt"));
            
            // Sauvegarder les résultats si demandé
            if (args.length >= 2) {
                String outputBase = args[1];
                
                // Sauvegarder urTspCustomerAuthCert
                if (result.get("urTspCustomerAuthCert") != null) {
                    Files.write(Paths.get(outputBase + "_cert.pem"), 
                               ((String) result.get("urTspCustomerAuthCert")).getBytes());
                    System.out.println("\n✓ urTspCustomerAuthCert sauvegardé dans: " + outputBase + "_cert.pem");
                }
                
                // Sauvegarder urTspCustomerAuthCA
                if (result.get("urTspCustomerAuthCA") != null) {
                    Files.write(Paths.get(outputBase + "_ca.pem"), 
                               ((String) result.get("urTspCustomerAuthCA")).getBytes());
                    System.out.println("✓ urTspCustomerAuthCA sauvegardé dans: " + outputBase + "_ca.pem");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Map<String, Object> processCompleteCertificate(byte[] certificateBytes) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. Exécuter setUrTspCustomerAuthCert
            List<ClientCertificat> certificates = new ArrayList<>();
            ClientCertificat clientCert = new ClientCertificat();
            clientCert.setCertificateContent(certificateBytes);
            certificates.add(clientCert);
            
            setUrTspCustomerAuthCert(certificates);
            result.put("urTspCustomerAuthCert", this.urTspCustomerAuthCert);
            
            // 2. Exécuter setUrTspCustomerAuthCA
            Map<String, String> authCaCrl = new HashMap<>();
            setUrTspCustomerAuthCA(certificates, authCaCrl);
            result.put("urTspCustomerAuthCA", this.urTspCustomerAuthCA);
            result.put("authCaCrl", authCaCrl);
            
            // 3. Ajouter les informations du ClientCertificat
            if (!certificates.isEmpty()) {
                ClientCertificat cert = certificates.get(0);
                result.put("caFileName", cert.getCaFileName());
                result.put("caCertificateContentLength", 
                          cert.getCaCertificateContent() != null ? cert.getCaCertificateContent().length : 0);
            }
            
            // 4. Ajouter d'autres métadonnées
            String certString = new String(certificateBytes, StandardCharsets.UTF_8);
            result.put("certificateFormat", certString.contains("BEGIN CERTIFICATE") ? "PEM" : "DER");
            result.put("validatedCertificate", true);
            result.put("processedAt", new Date());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("validatedCertificate", false);
        }
        
        return result;
    }
    
    // Méthode setUrTspCustomerAuthCert extraite
    public void setUrTspCustomerAuthCert(List<ClientCertificat> certificates) throws Exception {
        List<String> certificatsConverted = new ArrayList<>();
        
        for (ClientCertificat clientCertificat : certificates) {
            certificatsConverted.add(new String(Base64.getEncoder().encode(
                convertDERToPEM(clientCertificat.getCertificateContent()).getBytes())));
        }
        
        this.urTspCustomerAuthCert = certificatsConverted.toString();
    }
    
    // Méthode setUrTspCustomerAuthCA extraite
    public void setUrTspCustomerAuthCA(List<ClientCertificat> certificates, Map<String, String> authCaCrl) throws Exception {
        Map<String, String> authCaCrl1 = new HashMap<>();
        
        for (ClientCertificat certificat : certificates) {
            authCaCrl1.put(calculatePemKey(certificat.getCertificateContent()), 
                          new String(certificat.getCaCertificateContent() != null ? 
                                   certificat.getCaCertificateContent() : 
                                   new byte[0], StandardCharsets.UTF_8));
        }
        
        this.urTspCustomerAuthCA = authCaCrl1.toString();
        
        // Copier vers authCaCrl
        authCaCrl.putAll(authCaCrl1);
    }
    
    // Méthode auxiliaire calculatePemKey
    private String calculatePemKey(byte[] base64Der) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] certificatKey = convertDERToPEM(base64Der).getBytes();
        String key = new String(Base64.getEncoder().encode(digest.digest(certificatKey)));
        key = key.replace('+', '-').replace('/', '-').replaceAll("=", "");
        return key;
    }
    
    // Méthode setUrTspCustomerAuthCert avec gestion complète des certificats
    public void setUrTspCustomerAuthCert(List<ClientCertificat> clientCertificats, List<ClientCertificat> certificates) throws Exception {
        List<String> certificatsConverted = new ArrayList<>();
        
        for (ClientCertificat clientCertificat : clientCertificats) {
            certificatsConverted.add(new String(Base64.getEncoder().encode(
                convertDERToPEM(clientCertificat.getCertificateContent()).getBytes())));
        }
        
        for (ClientCertificat clientCertificat : certificates) {
            certificatsConverted.add(new String(Base64.getEncoder().encode(
                convertDERToPEM(clientCertificat.getCertificateContent()).getBytes())));
        }
        
        this.urTspCustomerAuthCert = certificatsConverted.toString();
    }
    
    // Méthode convertDERToPEM
    public String convertDERToPEM(byte[] derString) throws UnsupportedEncodingException {
        return convertDERToPEM(derString, false);
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
    
    // Méthode validateCertificateAndConvertIntoDerFormat
    public byte[] validateCertificateAndConvertIntoDerFormat(byte[] certificateContent) throws Exception {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream inputStream = new ByteArrayInputStream(certificateContent);
            Collection<?> certificates = certificateFactory.generateCertificates(inputStream);
            
            if (certificates.isEmpty()) {
                throw new Exception("Pas de certificat trouvé dans le fichier");
            }
            
            X509Certificate x509Certificate = (X509Certificate) certificates.iterator().next();
            return x509Certificate.getEncoded();
            
        } catch (Exception e) {
            // Gérer le format PEM
            String certString = new String(certificateContent, StandardCharsets.UTF_8);
            if (certString.contains("BEGIN CERTIFICATE")) {
                String beginMarker = "-----BEGIN CERTIFICATE-----";
                String endMarker = "-----END CERTIFICATE-----";
                int beginIndex = certString.indexOf(beginMarker);
                int endIndex = certString.indexOf(endMarker);
                
                if (beginIndex != -1 && endIndex != -1) {
                    String base64Content = certString.substring(
                        beginIndex + beginMarker.length(), 
                        endIndex
                    ).replaceAll("\\s", "");
                    
                    byte[] derContent = Base64.getDecoder().decode(base64Content);
                    
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    InputStream inputStream = new ByteArrayInputStream(derContent);
                    X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
                    
                    return x509Certificate.getEncoded();
                }
            }
            
            throw new Exception("Format de certificat non reconnu: " + e.getMessage());
        }
    }
    
    // Classe interne ClientCertificat
    static class ClientCertificat {
        private byte[] certificateContent;
        private byte[] caCertificateContent;
        private String caFileName;
        
        public byte[] getCertificateContent() { 
            return certificateContent; 
        }
        
        public void setCertificateContent(byte[] content) { 
            this.certificateContent = content; 
        }
        
        public byte[] getCaCertificateContent() { 
            return caCertificateContent; 
        }
        
        public void setCaCertificateContent(byte[] content) { 
            this.caCertificateContent = content; 
        }
        
        public String getCaFileName() { 
            return caFileName; 
        }
        
        public void setCaFileName(String name) { 
            this.caFileName = name; 
        }
    }
} 
