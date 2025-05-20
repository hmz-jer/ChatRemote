 # Refactorisation du code CertificateUtils

Voici le code refactorisé pour la classe CertificateUtils, avec une meilleure gestion des erreurs, une organisation plus claire, et des méthodes plus ciblées:

```java
package com.example.mockclientvop.util;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitaire pour la manipulation et validation des certificats X.509,
 * spécialement pour les certificats QWAC (Qualified Website Authentication Certificate) utilisés dans PSD2.
 */
@Component
public class CertificateUtils {

    private static final Logger logger = LoggerFactory.getLogger(CertificateUtils.class);
    private static final String PSD2_PATTERN = "PSDFR-ACPR-(\\d+)";

    /**
     * Extrait l'identifiant d'organisation (Organization Identifier) du certificat.
     * Cherche d'abord dans le sujet du certificat, puis dans les extensions si nécessaire.
     *
     * @param certificate Le certificat X.509 à analyser
     * @return Un Optional contenant l'identifiant d'organisation si trouvé, sinon Optional.empty()
     */
    public Optional<String> extractOrganizationIdFromCertificate(X509Certificate certificate) {
        try {
            // 1. Tenter d'extraire du sujet du certificat
            Optional<String> orgId = extractOrganizationIdFromSubject(certificate);
            if (orgId.isPresent()) {
                return orgId;
            }

            // 2. Si non trouvé dans le sujet, chercher dans les extensions
            return extractOrganizationIdFromExtensions(certificate);
        } catch (CertificateEncodingException e) {
            logger.error("Erreur lors de l'extraction de l'identifiant d'organisation", e);
            return Optional.empty();
        }
    }

    /**
     * Extrait l'identifiant PSP à partir de l'identifiant d'organisation en utilisant une expression régulière.
     *
     * @param organizationId L'identifiant d'organisation (format PSDFR-ACPR-XXXXX)
     * @param pattern L'expression régulière pour extraire l'ID PSP (doit contenir un groupe de capture)
     * @return Un Optional contenant l'identifiant PSP si trouvé, sinon Optional.empty()
     */
    public Optional<String> extractPSPIdFromOrganizationId(String organizationId, String pattern) {
        try {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(organizationId);
            
            if (matcher.find() && matcher.groupCount() >= 1) {
                return Optional.of(matcher.group(1));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Erreur lors de l'extraction de l'identifiant PSP", e);
            return Optional.empty();
        }
    }

    /**
     * Valide un certificat QWAC en vérifiant sa période de validité et la présence
     * d'un identifiant d'organisation au format PSD2 valide.
     *
     * @param certificate Le certificat QWAC à valider
     * @return true si le certificat est valide, false sinon
     */
    public boolean validateQWACCertificate(X509Certificate certificate) {
        try {
            // 1. Vérifier la période de validité du certificat
            certificate.checkValidity();
            
            // 2. Vérifier la présence de l'identifiant d'organisation
            Optional<String> orgId = extractOrganizationIdFromCertificate(certificate);
            if (!orgId.isPresent()) {
                logger.warn("Identifiant d'organisation absent dans le certificat");
                return false;
            }
            
            // 3. Vérifier le format de l'identifiant d'organisation (PSDFR-ACPR-XXXXX)
            Optional<String> pspId = extractPSPIdFromOrganizationId(orgId.get(), PSD2_PATTERN);
            return pspId.isPresent();
        } catch (CertificateException e) {
            logger.error("Certificat invalide ou expiré", e);
            return false;
        }
    }

    /**
     * Extrait l'identifiant d'organisation à partir du sujet du certificat.
     *
     * @param certificate Le certificat X.509
     * @return Un Optional contenant l'identifiant d'organisation si trouvé dans le sujet
     * @throws CertificateEncodingException Si le certificat ne peut pas être décodé
     */
    private Optional<String> extractOrganizationIdFromSubject(X509Certificate certificate) throws CertificateEncodingException {
        X500Name x500name = new JcaX509CertificateHolder(certificate).getSubject();
        RDN[] rdns = x500name.getRDNs(BCStyle.ORGANIZATION_IDENTIFIER);
        
        if (rdns.length > 0) {
            return Optional.of(rdns[0].getFirst().getValue().toString());
        }
        
        return Optional.empty();
    }

    /**
     * Extrait l'identifiant d'organisation à partir des extensions du certificat.
     *
     * @param certificate Le certificat X.509
     * @return Un Optional contenant l'identifiant d'organisation si trouvé dans les extensions
     */
    private Optional<String> extractOrganizationIdFromExtensions(X509Certificate certificate) {
        byte[] extensionValue = certificate.getExtensionValue(BCStyle.ORGANIZATION_IDENTIFIER.toString());
        if (extensionValue == null) {
            return Optional.empty();
        }

        try (ASN1InputStream asn1Stream = new ASN1InputStream(extensionValue)) {
            ASN1Primitive derObject = asn1Stream.readObject();
            if (!(derObject instanceof DEROctetString)) {
                return Optional.empty();
            }
            
            byte[] octets = ((DEROctetString) derObject).getOctets();
            try (ASN1InputStream asn1Stream2 = new ASN1InputStream(octets)) {
                ASN1Primitive asn1Value = asn1Stream2.readObject();
                return Optional.of(decodeASN1Value(asn1Value));
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture des extensions du certificat", e);
            return Optional.empty();
        }
    }

    /**
     * Décode une valeur ASN.1 en chaîne de caractères selon son type.
     *
     * @param asn1Value La valeur ASN.1 à décoder
     * @return La valeur décodée en chaîne de caractères
     */
    private String decodeASN1Value(ASN1Primitive asn1Value) {
        if (asn1Value instanceof DEROctetString) {
            return new String(((DEROctetString) asn1Value).getOctets(), StandardCharsets.UTF_8);
        } else if (asn1Value instanceof DERPrintableString) {
            return ((DERPrintableString) asn1Value).getString();
        } else if (asn1Value instanceof DERUTF8String) {
            return ((DERUTF8String) asn1Value).getString();
        } else if (asn1Value instanceof DERIA5String) {
            return ((DERIA5String) asn1Value).getString();
        } else {
            // Fallback pour d'autres types d'objets ASN.1
            return asn1Value.toString();
        }
    }
}
```

# Tests unitaires pour CertificateUtils

Voici les tests unitaires pour valider le fonctionnement de la classe CertificateUtils:

```java
package com.example.mockclientvop.util;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CertificateUtilsTest {

    private CertificateUtils certificateUtils;
    private static X509Certificate validCertificate;
    private static X509Certificate expiredCertificate;
    private static X509Certificate noPSD2Certificate;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        // Initialiser le fournisseur BouncyCastle
        Security.addProvider(new BouncyCastleProvider());

        // Générer une paire de clés pour les tests
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        // Créer un certificat valide avec un identifiant d'organisation PSD2
        validCertificate = generateCertificate(privateKey, "CN=Test,O=Test Organization,OU=Test Department,ORGANIZATIONIDENTIFIER=PSDFR-ACPR-12345", 
                new Date(System.currentTimeMillis() - 86400000), // Hier
                new Date(System.currentTimeMillis() + 86400000)); // Demain

        // Créer un certificat expiré
        expiredCertificate = generateCertificate(privateKey, "CN=Test,O=Test Organization,OU=Test Department,ORGANIZATIONIDENTIFIER=PSDFR-ACPR-12345", 
                new Date(System.currentTimeMillis() - 172800000), // Avant-hier
                new Date(System.currentTimeMillis() - 86400000)); // Hier

        // Créer un certificat sans identifiant d'organisation PSD2
        noPSD2Certificate = generateCertificate(privateKey, "CN=Test,O=Test Organization,OU=Test Department",
                new Date(System.currentTimeMillis() - 86400000), // Hier
                new Date(System.currentTimeMillis() + 86400000)); // Demain
    }

    @BeforeEach
    void setUp() {
        certificateUtils = new CertificateUtils();
    }

    @Test
    void extractOrganizationIdFromCertificate_shouldReturnOrganizationId_whenPresent() {
        Optional<String> result = certificateUtils.extractOrganizationIdFromCertificate(validCertificate);
        assertTrue(result.isPresent());
        assertEquals("PSDFR-ACPR-12345", result.get());
    }

    @Test
    void extractOrganizationIdFromCertificate_shouldReturnEmpty_whenNotPresent() {
        Optional<String> result = certificateUtils.extractOrganizationIdFromCertificate(noPSD2Certificate);
        assertFalse(result.isPresent());
    }

    @Test
    void extractPSPIdFromOrganizationId_shouldReturnPSPId_whenValidFormat() {
        Optional<String> result = certificateUtils.extractPSPIdFromOrganizationId("PSDFR-ACPR-12345", "PSDFR-ACPR-(\\d+)");
        assertTrue(result.isPresent());
        assertEquals("12345", result.get());
    }

    @Test
    void extractPSPIdFromOrganizationId_shouldReturnEmpty_whenInvalidFormat() {
        Optional<String> result = certificateUtils.extractPSPIdFromOrganizationId("INVALID-FORMAT", "PSDFR-ACPR-(\\d+)");
        assertFalse(result.isPresent());
    }

    @Test
    void validateQWACCertificate_shouldReturnTrue_whenCertificateIsValid() {
        boolean result = certificateUtils.validateQWACCertificate(validCertificate);
        assertTrue(result);
    }

    @Test
    void validateQWACCertificate_shouldReturnFalse_whenCertificateIsExpired() {
        boolean result = certificateUtils.validateQWACCertificate(expiredCertificate);
        assertFalse(result);
    }

    @Test
    void validateQWACCertificate_shouldReturnFalse_whenNoPSD2Identifier() {
        boolean result = certificateUtils.validateQWACCertificate(noPSD2Certificate);
        assertFalse(result);
    }

    @Test
    void validateQWACCertificate_shouldHandleExceptions() {
        // Créer un mock de certificat qui lance une exception lors de la vérification de validité
        X509Certificate mockCertificate = Mockito.mock(X509Certificate.class);
        try {
            when(mockCertificate.checkValidity()).thenThrow(new RuntimeException("Test exception"));
            
            boolean result = certificateUtils.validateQWACCertificate(mockCertificate);
            assertFalse(result);
        } catch (Exception e) {
            fail("Exception should have been caught: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour générer des certificats de test
    private static X509Certificate generateCertificate(PrivateKey privateKey, String subjectDN, Date notBefore, Date notAfter) throws Exception {
        X500Name subject = new X500Name(subjectDN);
        X500Name issuer = new X500Name("CN=Test CA,O=Test Organization,C=FR");
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
        
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, notBefore, notAfter, subject, 
                KeyPairGenerator.getInstance("RSA").generateKeyPair().getPublic());
        
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
        X509CertificateHolder certificateHolder = certificateBuilder.build(signer);
        
        return new JcaX509CertificateConverter().getCertificate(certificateHolder);
    }
}
```

## Améliorations apportées au code

1. **Refactorisation de la structure**:
   - Séparation claire des méthodes publiques et privées
   - Extraction des constantes et paramètres répétitifs
   - Documentation améliorée avec JavaDoc

2. **Gestion des erreurs**:
   - Utilisation systématique d'Optional pour les valeurs qui peuvent être absentes
   - Gestion des exceptions avec journalisation appropriée
   - Validation plus robuste des données d'entrée

3. **Performance et lisibilité**:
   - Réduction des redondances de code
   - Simplification de la logique de traitement des types ASN.1
   - Noms de méthodes et variables plus descriptifs

4. **Tests unitaires**:
   - Tests pour tous les scénarios: certificats valides, expirés, sans identifiant PSD2
   - Utilisation de certificats de test générés avec BouncyCastle
   - Tests de gestion des exceptions avec Mockito
   - Tests des méthodes d'extraction d'identifiants

Cette refactorisation rend le code plus robuste, plus facile à maintenir et à tester, tout en conservant la fonctionnalité essentielle de validation des certificats QWAC pour PSD2.
