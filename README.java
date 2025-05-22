 # Modification pour lire les certificats depuis /provider/NomDeLaBanque

Voici les modifications nécessaires pour que votre application lise les certificats depuis des dossiers spécifiques par banque sous `/provider/NomDeLaBanque`:

## 1. Mise à jour du service CertificateService

```java
package com.example.mockclientvop.service;

import com.example.mockclientvop.util.CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Stream;

@Service
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    
    @Autowired
    private CertificateUtils certificateUtils;
    
    @Value("${mock-vop.provider-base-path:/provider}")
    private String providerBasePath;
    
    @Value("${mock-vop.qwac.validation.enabled:true}")
    private boolean validationEnabled;
    
    @Value("${mock-vop.qwac.validation.certificate-chain-validation:true}")
    private boolean certificateChainValidation;
    
    @Value("${mock-vop.qwac.validation.validity-period-validation:true}")
    private boolean validityPeriodValidation;
    
    @Value("${mock-vop.qwac.validation.psd2-extensions-validation:true}")
    private boolean psd2ExtensionsValidation;
    
    @Value("${mock-vop.routing.certificate-owner-id-pattern:PSDFR-ACPR-(\\\\d+)}")
    private String certificateOwnerIdPattern;
    
    // Cache des certificats par banque
    private Map<String, List<X509Certificate>> bankCertificatesCache = new HashMap<>();
    private Map<String, KeyStore> bankKeystoresCache = new HashMap<>();
    
    @PostConstruct
    public void init() {
        logger.info("Service de validation des certificats initialisé:");
        logger.info("- Validation activée: {}", validationEnabled);
        logger.info("- Chemin de base des providers: {}", providerBasePath);
        logger.info("- Validation de la chaîne de certificats: {}", certificateChainValidation);
        logger.info("- Validation de la période de validité: {}", validityPeriodValidation);
        logger.info("- Validation des extensions PSD2: {}", psd2ExtensionsValidation);
        
        // Charger les certificats de toutes les banques au démarrage
        loadAllBankCertificates();
    }

    /**
     * Charge les certificats de toutes les banques trouvées dans le répertoire provider
     */
    public void loadAllBankCertificates() {
        try {
            Path basePath = Paths.get(providerBasePath);
            if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
                logger.warn("Répertoire provider base introuvable: {}", providerBasePath);
                return;
            }
            
            try (Stream<Path> bankDirs = Files.list(basePath)) {
                bankDirs.filter(Files::isDirectory)
                        .forEach(this::loadBankCertificates);
            }
            
            logger.info("Certificats chargés pour {} banques", bankCertificatesCache.size());
        } catch (IOException e) {
            logger.error("Erreur lors du chargement des certificats des banques", e);
        }
    }

    /**
     * Charge les certificats d'une banque spécifique
     */
    private void loadBankCertificates(Path bankDir) {
        String bankName = bankDir.getFileName().toString();
        logger.debug("Chargement des certificats pour la banque: {}", bankName);
        
        List<X509Certificate> certificates = new ArrayList<>();
        
        try {
            // Chercher les fichiers de certificat (.crt, .cer, .pem)
            try (Stream<Path> certFiles = Files.walk(bankDir)) {
                certFiles.filter(Files::isRegularFile)
                        .filter(path -> {
                            String filename = path.getFileName().toString().toLowerCase();
                            return filename.endsWith(".crt") || 
                                   filename.endsWith(".cer") || 
                                   filename.endsWith(".pem");
                        })
                        .forEach(certPath -> {
                            try {
                                X509Certificate cert = loadCertificateFromPEM(certPath);
                                if (cert != null) {
                                    certificates.add(cert);
                                    logger.debug("Certificat chargé: {} pour banque: {}", 
                                        certPath.getFileName(), bankName);
                                }
                            } catch (Exception e) {
                                logger.error("Erreur lors du chargement du certificat: {}", 
                                    certPath, e);
                            }
                        });
            }
            
            // Chercher les keystores (.p12, .jks)
            KeyStore keystore = loadBankKeystore(bankDir);
            if (keystore != null) {
                bankKeystoresCache.put(bankName, keystore);
            }
            
            if (!certificates.isEmpty()) {
                bankCertificatesCache.put(bankName, certificates);
                logger.info("Chargé {} certificats pour la banque: {}", 
                    certificates.size(), bankName);
            } else {
                logger.warn("Aucun certificat trouvé pour la banque: {}", bankName);
            }
            
        } catch (IOException e) {
            logger.error("Erreur lors du chargement des certificats pour la banque: {}", 
                bankName, e);
        }
    }

    /**
     * Charge un certificat X.509 depuis un fichier PEM
     */
    private X509Certificate loadCertificateFromPEM(Path certPath) throws Exception {
        try (FileInputStream fis = new FileInputStream(certPath.toFile())) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(fis);
        }
    }

    /**
     * Charge un keystore depuis le répertoire d'une banque
     */
    private KeyStore loadBankKeystore(Path bankDir) {
        try (Stream<Path> keystoreFiles = Files.walk(bankDir)) {
            Optional<Path> p12File = keystoreFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".p12"))
                    .findFirst();
            
            if (p12File.isPresent()) {
                return loadPKCS12Keystore(p12File.get());
            }
            
            // Chercher les fichiers JKS si pas de P12
            try (Stream<Path> jksFiles = Files.walk(bankDir)) {
                Optional<Path> jksFile = jksFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".jks"))
                        .findFirst();
                
                if (jksFile.isPresent()) {
                    return loadJKSKeystore(jksFile.get());
                }
            }
            
        } catch (IOException e) {
            logger.error("Erreur lors de la recherche de keystore pour: {}", 
                bankDir.getFileName(), e);
        }
        
        return null;
    }

    /**
     * Charge un keystore PKCS12
     */
    private KeyStore loadPKCS12Keystore(Path p12Path) {
        try {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            
            // Essayer différents mots de passe par défaut
            String[] passwords = {"changeit", "password", "", "123456"};
            
            for (String password : passwords) {
                try (FileInputStream fis = new FileInputStream(p12Path.toFile())) {
                    keystore.load(fis, password.toCharArray());
                    logger.debug("Keystore PKCS12 chargé avec succès: {} (password: {})", 
                        p12Path.getFileName(), password.isEmpty() ? "vide" : "***");
                    return keystore;
                } catch (Exception e) {
                    logger.debug("Échec du chargement du keystore avec le mot de passe: {}", 
                        password.isEmpty() ? "vide" : "***");
                }
            }
            
            logger.error("Impossible de charger le keystore PKCS12: {} (aucun mot de passe valide)", 
                p12Path.getFileName());
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du keystore PKCS12: {}", p12Path, e);
        }
        
        return null;
    }

    /**
     * Charge un keystore JKS
     */
    private KeyStore loadJKSKeystore(Path jksPath) {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            
            // Essayer différents mots de passe par défaut
            String[] passwords = {"changeit", "password", "", "123456"};
            
            for (String password : passwords) {
                try (FileInputStream fis = new FileInputStream(jksPath.toFile())) {
                    keystore.load(fis, password.toCharArray());
                    logger.debug("Keystore JKS chargé avec succès: {} (password: {})", 
                        jksPath.getFileName(), password.isEmpty() ? "vide" : "***");
                    return keystore;
                } catch (Exception e) {
                    logger.debug("Échec du chargement du keystore avec le mot de passe: {}", 
                        password.isEmpty() ? "vide" : "***");
                }
            }
            
            logger.error("Impossible de charger le keystore JKS: {} (aucun mot de passe valide)", 
                jksPath.getFileName());
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du keystore JKS: {}", jksPath, e);
        }
        
        return null;
    }

    /**
     * Détermine le nom de la banque à partir du certificat client
     */
    public Optional<String> determineBankNameFromCertificate(X509Certificate certificate) {
        // Extraire l'ID PSP du certificat
        Optional<String> pspId = extractPSPIdFromCertificate(certificate);
        if (!pspId.isPresent()) {
            return Optional.empty();
        }
        
        // Chercher dans les certificats chargés celui qui correspond
        for (Map.Entry<String, List<X509Certificate>> entry : bankCertificatesCache.entrySet()) {
            String bankName = entry.getKey();
            List<X509Certificate> bankCerts = entry.getValue();
            
            for (X509Certificate bankCert : bankCerts) {
                Optional<String> bankPspId = extractPSPIdFromCertificate(bankCert);
                if (bankPspId.isPresent() && bankPspId.get().equals(pspId.get())) {
                    logger.debug("Banque déterminée: {} pour PSP ID: {}", bankName, pspId.get());
                    return Optional.of(bankName);
                }
            }
        }
        
        // Si pas trouvé par PSP ID, essayer par correspondance du sujet
        String clientSubject = certificate.getSubjectX500Principal().getName();
        for (Map.Entry<String, List<X509Certificate>> entry : bankCertificatesCache.entrySet()) {
            String bankName = entry.getKey();
            List<X509Certificate> bankCerts = entry.getValue();
            
            for (X509Certificate bankCert : bankCerts) {
                String bankSubject = bankCert.getSubjectX500Principal().getName();
                if (bankSubject.equals(clientSubject)) {
                    logger.debug("Banque déterminée par sujet: {} pour certificat: {}", 
                        bankName, clientSubject);
                    return Optional.of(bankName);
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Valide un certificat QWAC en utilisant les certificats de la banque correspondante
     */
    public boolean validateQWACCertificate(X509Certificate certificate) {
        if (!validationEnabled) {
            logger.debug("Validation des certificats désactivée, acceptation automatique");
            return true;
        }
        
        logger.debug("Validation du certificat QWAC: {}", certificate.getSubjectX500Principal());
        
        if (validityPeriodValidation) {
            try {
                certificate.checkValidity();
                logger.debug("Période de validité vérifiée avec succès");
            } catch (Exception e) {
                logger.error("Échec de la vérification de validité: {}", e.getMessage());
                return false;
            }
        }
        
        if (psd2ExtensionsValidation) {
            boolean isValid = certificateUtils.validateQWACCertificate(certificate);
            if (!isValid) {
                logger.error("Échec de la validation des extensions PSD2");
                return false;
            }
            logger.debug("Extensions PSD2 validées avec succès");
        }
        
        if (certificateChainValidation) {
            Optional<String> bankName = determineBankNameFromCertificate(certificate);
            if (bankName.isPresent()) {
                boolean chainValid = validateCertificateChainForBank(certificate, bankName.get());
                if (!chainValid) {
                    logger.error("Échec de la validation de la chaîne de certificats pour la banque: {}", 
                        bankName.get());
                    return false;
                }
                logger.debug("Chaîne de certificats validée avec succès pour la banque: {}", 
                    bankName.get());
            } else {
                logger.warn("Impossible de déterminer la banque pour le certificat, validation de chaîne ignorée");
            }
        }
        
        return true;
    }

    /**
     * Valide la chaîne de certificats pour une banque spécifique
     */
    private boolean validateCertificateChainForBank(X509Certificate certificate, String bankName) {
        List<X509Certificate> bankCerts = bankCertificatesCache.get(bankName);
        if (bankCerts == null || bankCerts.isEmpty()) {
            logger.warn("Aucun certificat de confiance trouvé pour la banque: {}", bankName);
            return false;
        }
        
        // Vérifier si le certificat est signé par l'un des certificats de confiance de la banque
        for (X509Certificate trustedCert : bankCerts) {
            try {
                certificate.verify(trustedCert.getPublicKey());
                logger.debug("Certificat validé avec le certificat de confiance de la banque: {}", 
                    bankName);
                return true;
            } catch (Exception e) {
                // Continuer avec le prochain certificat
                logger.debug("Certificat non validé avec ce certificat de confiance: {}", 
                    e.getMessage());
            }
        }
        
        return false;
    }
    
    public Optional<String> extractPSPIdFromCertificate(X509Certificate certificate) {
        Optional<String> organizationId = certificateUtils.extractOrganizationIdFromCertificate(certificate);
        if (organizationId.isPresent()) {
            logger.debug("Identifiant d'organisation extrait: {}", organizationId.get());
            return certificateUtils.extractPSPIdFromOrganizationId(organizationId.get(), certificateOwnerIdPattern);
        }
        return Optional.empty();
    }
    
    /**
     * Recharge les certificats de toutes les banques
     */
    public void reloadAllBankCertificates() {
        logger.info("Rechargement de tous les certificats des banques...");
        bankCertificatesCache.clear();
        bankKeystoresCache.clear();
        loadAllBankCertificates();
    }
    
    /**
     * Obtient la liste des banques disponibles
     */
    public Set<String> getAvailableBanks() {
        return bankCertificatesCache.keySet();
    }
    
    /**
     * Obtient les certificats d'une banque spécifique
     */
    public List<X509Certificate> getBankCertificates(String bankName) {
        return bankCertificatesCache.getOrDefault(bankName, Collections.emptyList());
    }
}
```

## 2. Mise à jour de la configuration application.yml

```yaml
server:
  port: 8443
  address: 10.55.8.12
  ssl:
    enabled: true
    # Configuration SSL basique - les certificats spécifiques sont maintenant dans /provider/
    key-store: file:/provider/default/server.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-store-type: JKS
    key-alias: server
    trust-store: file:/provider/default/truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD:changeit}
    trust-store-type: JKS
    client-auth: need

spring:
  application:
    name: mock-client-vop
    version: 1.0.0

# Configuration personnalisée pour le mock-client-VOP
mock-vop:
  # Chemin de base pour les répertoires des providers/banques
  provider-base-path: /provider
  
  qwac:
    validation:
      enabled: true
      certificate-chain-validation: true
      validity-period-validation: true
      psd2-extensions-validation: true
      organization-identifier-oid: "2.5.4.97"
  routing:
    enabled: true
    certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
    psp-mappings:
      "15930": "https://backend-natixis.example.com"
      "12345": "https://backend-bnp.example.com"
      "default": "https://default-backend.example.com"

logging:
  file:
    name: /opt/mock-client-vop/logs/mock-client-vop.log
  level:
    root: INFO
    com.example.mockclientvop: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
```

## 3. Nouveau contrôleur pour la gestion des providers

```java
package com.example.mockclientvop.controller;

import com.example.mockclientvop.service.CertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/providers")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);

    @Autowired
    private CertificateService certificateService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listProviders() {
        Map<String, Object> response = new HashMap<>();
        
        Set<String> banks = certificateService.getAvailableBanks();
        response.put("totalBanks", banks.size());
        response.put("banks", banks);
        
        Map<String, Integer> certCounts = new HashMap<>();
        for (String bank : banks) {
            List<X509Certificate> certs = certificateService.getBankCertificates(bank);
            certCounts.put(bank, certs.size());
        }
        response.put("certificateCounts", certCounts);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bankName}/certificates")
    public ResponseEntity<Map<String, Object>> getBankCertificates(@PathVariable String bankName) {
        Map<String, Object> response = new HashMap<>();
        
        List<X509Certificate> certificates = certificateService.getBankCertificates(bankName);
        
        if (certificates.isEmpty()) {
            response.put("error", "Aucun certificat trouvé pour la banque: " + bankName);
            return ResponseEntity.notFound().build();
        }
        
        response.put("bankName", bankName);
        response.put("certificateCount", certificates.size());
        
        // Détails des certificats
        certificates.forEach(cert -> {
            Map<String, Object> certInfo = new HashMap<>();
            certInfo.put("subject", cert.getSubjectX500Principal().getName());
            certInfo.put("issuer", cert.getIssuerX500Principal().getName());
            certInfo.put("notBefore", cert.getNotBefore());
            certInfo.put("notAfter", cert.getNotAfter());
            certInfo.put("serialNumber", cert.getSerialNumber().toString());
            
            // Extraire l'ID PSP si présent
            certificateService.extractPSPIdFromCertificate(cert)
                .ifPresent(pspId -> certInfo.put("pspId", pspId));
            
            response.put("certificate_" + cert.getSerialNumber(), certInfo);
        });
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reload")
    public ResponseEntity<Map<String, Object>> reloadAllProviders() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            certificateService.reloadAllBankCertificates();
            Set<String> banks = certificateService.getAvailableBanks();
            
            response.put("success", true);
            response.put("message", "Certificats des providers rechargés avec succès");
            response.put("totalBanks", banks.size());
            response.put("banks", banks);
        } catch (Exception e) {
            logger.error("Erreur lors du rechargement des providers", e);
            response.put("success", false);
            response.put("message", "Erreur lors du rechargement: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
```

## 4. Structure de répertoires recommandée

```
/provider/
├── Natixis/
│   ├── natixis-qwac.p12      # Certificat client Natixis
│   ├── natixis-ca.cert.pem   # Certificat AC pour Natixis
│   └── password.txt          # Mot de passe du P12 (optionnel)
├── BNP/
│   ├── bnp-qwac.p12
│   ├── bnp-ca.cert.pem
│   └── password.txt
├── SocieteGenerale/
│   ├── sg-qwac.p12
│   ├── sg-ca.cert.pem
│   └── password.txt
├── CreditMutuel/
│   ├── cm-qwac.jks
│   ├── cm-ca.cert.pem
│   └── password.txt
└── default/                  # Configuration par défaut du serveur
    ├── server.jks
    └── truststore.jks
```

## 5. Script pour la gestion des providers

```bash
#!/bin/bash

# provider-manager.sh - Script pour gérer les providers de banques

PROVIDER_BASE="/provider"

# Fonction pour créer un nouveau provider
create_provider() {
    BANK_NAME="$1"
    
    if [ -z "$BANK_NAME" ]; then
        echo "Usage: $0 create-provider <nom_banque>"
        return 1
    fi
    
    PROVIDER_DIR="$PROVIDER_BASE/$BANK_NAME"
    
    if [ -d "$PROVIDER_DIR" ]; then
        echo "Le provider $BANK_NAME existe déjà"
        return 1
    fi
    
    mkdir -p "$PROVIDER_DIR"
    echo "Provider créé: $PROVIDER_DIR"
    
    # Créer un fichier README explicatif
    cat > "$PROVIDER_DIR/README.md" << EOF
# Provider: $BANK_NAME

## Fichiers requis:
- \`*.p12\` : Fichier PKCS12 contenant le certificat et la clé privée du client
- \`*.cert.pem\` : Certificat de l'AC racine pour la validation
- \`password.txt\` : Mot de passe du fichier P12 (optionnel)

## Alternative:
- \`*.jks\` : Keystore Java contenant le certificat client
- \`*.crt\`, \`*.cer\`, \`*.pem\` : Certificats individuels

## Note:
L'application détectera automatiquement les certificats présents dans ce répertoire.
EOF
    
    echo "Fichier README créé dans $PROVIDER_DIR/README.md"
}

# Fonction pour lister les providers
list_providers() {
    echo "Providers disponibles dans $PROVIDER_BASE:"
    if [ -d "$PROVIDER_BASE" ]; then
        for dir in "$PROVIDER_BASE"/*; do
            if [ -d "$dir" ]; then
                BANK_NAME=$(basename "$dir")
                CERT_COUNT=$(find "$dir" -name "*.p12" -o -name "*.jks" -o -name "*.crt" -o -name "*.cer" -o -name "*.pem" | wc -l)
                echo "  - $BANK_NAME ($CERT_COUNT fichiers de certificat)"
            fi
        done
    else
        echo "Répertoire provider non trouvé: $PROVIDER_BASE"
    fi
}

# Fonction pour valider un provider
validate_provider() {
    BANK_NAME="$1"
    
    if [ -z "$BANK_NAME" ]; then
        echo "Usage: $0 validate-provider <nom_banque>"
        return 1
    fi
    
    PROVIDER_DIR="$PROVIDER_BASE/$BANK_NAME"
    
    if [ ! -d "$PROVIDER_DIR" ]; then
        echo "Provider non trouvé: $BANK_NAME"
        return 1
    fi
    
    echo "Validation du provider: $BANK_NAME"
    echo "Répertoire: $PROVIDER_DIR"
    
    # Chercher les fichiers P12
    P12_FILES=$(find "$PROVIDER_DIR" -name "*.p12" 2>/dev/null)
    if [ -n "$P12_FILES" ]; then
        echo "Fichiers P12 trouvés:"
        echo "$P12_FILES" | while read -r file; do
            echo "  - $(basename "$file")"
            # Tenter de lister le contenu du P12
            openssl pkcs12 -info -in "$file" -noout -passin pass:changeit 2>/dev/null && echo "    ✓ Accessible avec mot de passe par défaut" || echo "    ⚠ Mot de passe requis"
        done
    fi
    
    # Chercher les fichiers JKS
    JKS_FILES=$(find "$PROVIDER_DIR" -name "*.jks" 2>/dev/null)
    if [ -n "$JKS_FILES" ]; then
        echo "Fichiers JKS trouvés:"
        echo "$JKS_FILES" | while read -r file; do
            echo "  - $(basename "$file")"
        done
    fi
    
    # Chercher les certificats PEM
    PEM_FILES=$(find "$PROVIDER_DIR" -name "*.crt" -o -name "*.cer" -o -name "*.pem" 2>/dev/null)
    if [ -n "$PEM_FILES" ]; then
        echo "Certificats PEM trouvés:"
        echo "$PEM_FILES" | while read -r file; do
            echo "  - $(basename "$file")"
            # Afficher le sujet du certificat
            SUBJECT=$(openssl x509 -in "$file" -noout -subject 2>/dev/null | cut -d'=' -f2-)
            if [ -n "$SUBJECT" ]; then
                echo "    Sujet: $SUBJECT"
            fi
        done
    fi
}

# Traiter les commandes
case "$1" in
    "create-provider")
        create_provider "$2"
        ;;
    "list")
        list_providers
        ;;
    "validate-provider")
        validate_provider "$2"
        ;;
    *)
        echo "Usage: $0 {create-provider|list|validate-provider}"
        echo ""
        echo "Commandes:"
        echo "  create-provider <nom>   Créer un nouveau répertoire provider"
        echo "  list                    Lister tous les providers"
        echo "  validate-provider <nom> Valider un provider spécifique"
        exit 1
        ;;
esac
```

## 6. Utilisation et avantages

### Avantages de cette approche:

1. **Organisation par banque**: Chaque banque a son propre dossier avec ses certificats
2. **Flexibilité**: Support de différents formats (P12, JKS, PEM)
3. **Détection automatique**: L'application détecte automatiquement les certificats dans chaque dossier
4. **Validation spécifique**: Chaque certificat client est validé avec les certificats de confiance de sa banque
5. **Rechargement à chaud**: Possibilité de recharger les certificats sans redémarrage

### Utilisation:

1. **Créer un provider pour une nouvelle banque**:
   ```bash
   ./provider-manager.sh create-provider Natixis
   ```

2. **Ajouter les certificats de la banque**:
   ```bash
   cp natixis-qwac.p12 /provider/Natixis/
   cp natixis-ca.cert.pem /provider/Natixis/
   ```

3. **Valider le provider**:
   ```bash
   ./provider-manager.sh validate-provider Natixis
   ```

4. **Recharger les certificats via l'API**:
   ```bash
   curl -X POST https://localhost:8443/api/admin/providers/reload
   ```

5. **Lister les providers disponibles**:
   ```bash
   curl https://localhost:8443/api/admin/providers/list
   ```

Cette architecture permet une gestion flexible et organisée des certificats par banque, tout en conservant la simplicité d'utilisation pour les clients qui fournissent leurs certificats au format P12.
