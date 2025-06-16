 import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;

public class CertificateProcessorTwoFiles {
    
    private String urTspCustomerAuthCert;
    private String urTspCustomerAuthCA;
    
    public static void main(String[] args) {
        try {
            CertificateProcessorTwoFiles processor = new CertificateProcessorTwoFiles();
            
            // ===== LIGNES À MODIFIER : METTRE VOS CHEMINS ICI =====
            String caCertificatePath = "C:/chemin/vers/votre/ca_certificate.ca";
            String crtCertificatePath = "C:/chemin/vers/votre/certificate.crt";
            
            System.out.println("================================================");
            System.out.println("TRAITEMENT DE DEUX FICHIERS CERTIFICATS");
            System.out.println("================================================\n");
            
            // 1. TRAITER LE FICHIER CA
            System.out.println("=== [1] TRAITEMENT DU FICHIER CA ===");
            System.out.println("Fichier: " + caCertificatePath);
            System.out.println("------------------------------------");
            
            try {
                byte[] caCertificateBytes = Files.readAllBytes(Paths.get(caCertificatePath));
                System.out.println("✓ Fichier CA lu: " + caCertificateBytes.length + " octets");
                
                Map<String, Object> caResult = processor.processFullCertificate(caCertificateBytes, "CA");
                displayResults(caResult, "CA");
                
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du traitement du fichier CA: " + e.getMessage());
            }
            
            System.out.println("\n");
            
            // 2. TRAITER LE FICHIER CRT
            System.out.println("=== [2] TRAITEMENT DU FICHIER CRT ===");
            System.out.println("Fichier: " + crtCertificatePath);
            System.out.println("-------------------------------------");
            
            try {
                byte[] crtCertificateBytes = Files.readAllBytes(Paths.get(crtCertificatePath));
                System.out.println("✓ Fichier CRT lu: " + crtCertificateBytes.length + " octets");
                
                Map<String, Object> crtResult = processor.processFullCertificate(crtCertificateBytes, "CRT");
                displayResults(crtResult, "CRT");
                
            } catch (Exception e) {
                System.err.println("✗ Erreur lors du traitement du fichier CRT: " + e.getMessage());
            }
            
            System.out.println("\n================================================");
            System.out.println("TRAITEMENT TERMINÉ");
            System.out.println("================================================");
            
        } catch (Exception e) {
            System.err.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public Map<String, Object> processFullCertificate(byte[] certificateBytes, String fileType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("fileType", fileType);
        
        try {
            // Créer un ClientCertificat
            ClientCertificat clientCert = new ClientCertificat();
            clientCert.setCertificateContent(certificateBytes);
            
            // Valider et convertir
            byte[] derCertificate = validateCertificateAndConvertIntoDerFormat(certificateBytes);
            
            // Calculer le nom de fichier CA
            String caFileName = calculatePemKey(derCertificate);
            clientCert.setCaFileName(caFileName);
            clientCert.setCaCertificateContent(derCertificate);
            
            List<ClientCertificat> certificates = Arrays.asList(clientCert);
            
            // 1. setUrTspCustomerAuthCert
            setUrTspCustomerAuthCert(certificates);
            result.put("urTspCustomerAuthCert", this.urTspCustomerAuthCert);
            
            // 2. setUrTspCustomerAuthCA
            Map<String, String> authCaCrl = new HashMap<>();
            setUrTspCustomerAuthCA(certificates, authCaCrl);
            result.put("urTspCustomerAuthCA", this.urTspCustomerAuthCA);
            result.put("authCaCrl", authCaCrl);
            
            // Informations additionnelles
            result.put("caFileName", caFileName);
            result.put("certificateContentLength", derCertificate.length);
            result.put("validatedCertificate", true);
            
            // Détecter le format
            String certString = new String(certificateBytes, StandardCharsets.UTF_8);
            result.put("certificateFormat", certString.contains("BEGIN CERTIFICATE") ? "PEM" : "DER");
            
            // Extraire les infos X509
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate x509 = (X509Certificate) cf.generateCertificate(
                new ByteArrayInputStream(derCertificate));
            
            result.put("subject", x509.getSubjectDN().toString());
            result.put("issuer", x509.getIssuerDN().toString());
            result.put("serialNumber", x509.getSerialNumber().toString());
            result.put("notBefore", x509.getNotBefore());
            result.put("notAfter", x509.getNotAfter());
            result.put("signatureAlgorithm", x509.getSigAlgName());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            result.put("validatedCertificate", false);
        }
        
        return result;
    }
    
    private static void displayResults(Map<String, Object> result, String fileType) {
        Boolean isValid = (Boolean) result.getOrDefault("validatedCertificate", false);
        
        if (isValid) {
            System.out.println("\n✓ Certificat " + fileType + " traité avec succès");
            
            // Format
            System.out.println("  Format détecté: " + result.get("certificateFormat"));
            
            // Informations X509
            System.out.println("  Sujet: " + result.get("subject"));
            System.out.println("  Émetteur: " + result.get("issuer"));
            System.out.println("  Numéro de série: " + result.get("serialNumber"));
            System.out.println("  Valide du: " + result.get("notBefore"));
            System.out.println("  Valide jusqu'au: " + result.get("notAfter"));
            System.out.println("  Algorithme: " + result.get("signatureAlgorithm"));
            
            // Résultats des méthodes
            System.out.println("\n  Résultats des méthodes:");
            
            // urTspCustomerAuthCert
            String authCert = (String) result.get("urTspCustomerAuthCert");
            if (authCert != null) {
                System.out.println("  - urTspCustomerAuthCert: " + authCert.length() + " caractères");
                System.out.println("    Début: " + authCert.substring(0, Math.min(60, authCert.length())) + "...");
            }
            
            // urTspCustomerAuthCA
            String authCA = (String) result.get("urTspCustomerAuthCA");
            if (authCA != null) {
                System.out.println("  - urTspCustomerAuthCA: " + authCA.length() + " caractères");
                System.out.println("    Début: " + authCA.substring(0, Math.min(60, authCA.length())) + "...");
            }
            
            // CA FileName
            System.out.println("  - CA FileName (hash): " + result.get("caFileName"));
            System.out.println("  - Taille DER: " + result.get("certificateContentLength") + " octets");
            
        } else {
            System.out.println("\n✗ Erreur lors du traitement du certificat " + fileType);
            System.out.println("  Type d'erreur: " + result.get("errorType"));
            System.out.println("  Message: " + result.get("error"));
        }
    }
    
    // Méthode setUrTspCustomerAuthCert
    public void setUrTspCustomerAuthCert(List<ClientCertificat> certificates) throws Exception {
        List<String> certificatsConverted = new ArrayList<>();
        
        for (ClientCertificat clientCertificat : certificates) {
            byte[] derContent = validateCertificateAndConvertIntoDerFormat(
                clientCertificat.getCertificateContent());
            String pemCertificate = convertDERToPEM(derContent);
            String base64EncodedPem = Base64.getEncoder().encodeToString(pemCertificate.getBytes());
            certificatsConverted.add(base64EncodedPem);
        }
        
        this.urTspCustomerAuthCert = certificatsConverted.toString();
    }
    
    // Méthode setUrTspCustomerAuthCA
    public void setUrTspCustomerAuthCA(List<ClientCertificat> certificates, Map<String, String> authCaCrl) 
            throws Exception {
        Map<String, String> authCaCrl1 = new HashMap<>();
        
        for (ClientCertificat certificat : certificates) {
            byte[] derContent = validateCertificateAndConvertIntoDerFormat(
                certificat.getCertificateContent());
            String pemKey = calculatePemKey(derContent);
            
            byte[] caContent = certificat.getCaCertificateContent() != null ? 
                               certificat.getCaCertificateContent() : derContent;
            String caPem = convertDERToPEM(caContent);
            
            authCaCrl1.put(pemKey, caPem);
        }
        
        this.urTspCustomerAuthCA = authCaCrl1.toString();
        authCaCrl.putAll(authCaCrl1);
    }
    
    // Calculer la clé PEM
    private String calculatePemKey(byte[] derCertificate) throws Exception {
        String pemCertificate = convertDERToPEM(derCertificate);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(pemCertificate.getBytes());
        String key = Base64.getEncoder().encodeToString(hash);
        key = key.replace('+', '-').replace('/', '-').replaceAll("=", "");
        return key;
    }
    
    // Convertir DER vers PEM
    public String convertDERToPEM(byte[] derContent) throws UnsupportedEncodingException {
        return convertDERToPEM(derContent, false);
    }
    
    public String convertDERToPEM(byte[] derContent, boolean isAlreadyBase64) 
            throws UnsupportedEncodingException {
        byte[] base64Content = isAlreadyBase64 ? derContent : Base64.getEncoder().encode(derContent);
        String base64String = new String(base64Content, StandardCharsets.UTF_8);
        
        StringBuilder pemBuilder = new StringBuilder();
        pemBuilder.append("-----BEGIN CERTIFICATE-----\r\n");
        
        int length = base64String.length();
        for (int i = 0; i < length; i += 64) {
            int end = Math.min(i + 64, length);
            pemBuilder.append(base64String.substring(i, end)).append("\r\n");
        }
        
        pemBuilder.append("-----END CERTIFICATE-----");
        return pemBuilder.toString();
    }
    
    // Valider et convertir en DER
    public byte[] validateCertificateAndConvertIntoDerFormat(byte[] certificateContent) 
            throws Exception {
        String certString = new String(certificateContent, StandardCharsets.UTF_8);
        
        if (certString.contains("BEGIN CERTIFICATE")) {
            String beginMarker = "-----BEGIN CERTIFICATE-----";
            String endMarker = "-----END CERTIFICATE-----";
            
            int beginIndex = certString.indexOf(beginMarker);
            int endIndex = certString.indexOf(endMarker);
            
            if (beginIndex == -1 || endIndex == -1) {
                throw new Exception("Marqueurs PEM invalides");
            }
            
            String base64Content = certString
                .substring(beginIndex + beginMarker.length(), endIndex)
                .replaceAll("\\s", "");
            
            certificateContent = Base64.getDecoder().decode(base64Content);
        }
        
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
            new ByteArrayInputStream(certificateContent));
        
        return cert.getEncoded();
    }
    
    // Classe ClientCertificat
    static class ClientCertificat {
        private byte[] certificateContent;
        private byte[] caCertificateContent;
        private String caFileName;
        
        public byte[] getCertificateContent() { return certificateContent; }
        public void setCertificateContent(byte[] content) { this.certificateContent = content; }
        
        public byte[] getCaCertificateContent() { return caCertificateContent; }
        public void setCaCertificateContent(byte[] content) { this.caCertificateContent = content; }
        
        public String getCaFileName() { return caFileName; }
        public void setCaFileName(String name) { this.caFileName = name; }
    }
}
