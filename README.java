 Je vais vous aider à concevoir ce mock-client-VOP pour la validation des certificats QWAC dans le cadre d'un flux outbound. Je vais structurer ma réponse pour couvrir les différentes parties de votre demande.

## 1. Génération des certificats avec OpenSSL

### 1.1 Création d'une AC racine de test

```bash
# Création du répertoire pour stocker les clés et certificats
mkdir -p pki/{certs,private,newcerts,crl}
touch pki/index.txt
echo 1000 > pki/serial

# Configuration de l'AC racine
cat > pki/openssl-ca.cnf << 'EOF'
[ ca ]
default_ca = CA_default

[ CA_default ]
dir = ./pki
certs = $dir/certs
crl_dir = $dir/crl
database = $dir/index.txt
new_certs_dir = $dir/newcerts
certificate = $dir/certs/ca.cert.pem
serial = $dir/serial
crlnumber = $dir/crlnumber
crl = $dir/crl/ca.crl.pem
private_key = $dir/private/ca.key.pem
RANDFILE = $dir/private/.rand
name_opt = ca_default
cert_opt = ca_default
default_days = 3650
default_crl_days = 30
default_md = sha256
preserve = no
policy = policy_strict

[ policy_strict ]
countryName = supplied
stateOrProvinceName = supplied
organizationName = supplied
organizationalUnitName = optional
commonName = supplied
emailAddress = optional

[ req ]
default_bits = 4096
default_md = sha256
distinguished_name = req_distinguished_name
string_mask = utf8only
x509_extensions = v3_ca

[ req_distinguished_name ]
countryName = Country Name (2 letter code)
stateOrProvinceName = State or Province Name
localityName = Locality Name
organizationName = Organization Name
organizationalUnitName = Organizational Unit Name
commonName = Common Name
emailAddress = Email Address

[ v3_ca ]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true
keyUsage = critical, digitalSignature, cRLSign, keyCertSign

[ v3_intermediate_ca ]
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always,issuer
basicConstraints = critical, CA:true, pathlen:0
keyUsage = critical, digitalSignature, cRLSign, keyCertSign
EOF

# Génération de la clé privée pour l'AC racine
openssl genrsa -out pki/private/ca.key.pem 4096
chmod 400 pki/private/ca.key.pem

# Création du certificat de l'AC racine
openssl req -config pki/openssl-ca.cnf -key pki/private/ca.key.pem -new -x509 -days 7300 -sha256 -extensions v3_ca -out pki/certs/ca.cert.pem -subj "/C=FR/ST=Paris/L=Paris/O=Test Payment CA/CN=Test Root CA"
chmod 444 pki/certs/ca.cert.pem

# Vérification du certificat de l'AC racine
openssl x509 -noout -text -in pki/certs/ca.cert.pem
```

### 1.2 Génération d'un certificat serveur pour le mock

```bash
# Génération de la clé serveur
openssl genrsa -out pki/private/server.key.pem 2048
chmod 400 pki/private/server.key.pem

# Création de la demande de certificat pour le serveur
openssl req -config pki/openssl-ca.cnf -key pki/private/server.key.pem -new -sha256 -out pki/certs/server.csr.pem -subj "/C=FR/ST=Paris/L=Paris/O=Mock VOP Server/CN=mock-vop-server.example.com"

# Configuration pour le certificat serveur
cat > pki/openssl-server.cnf << 'EOF'
[ server_cert ]
basicConstraints = CA:FALSE
nsCertType = server
nsComment = "OpenSSL Generated Server Certificate"
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
EOF

# Signature du certificat serveur par l'AC racine
openssl ca -batch -config pki/openssl-ca.cnf -extensions server_cert -days 3650 -notext -md sha256 -in pki/certs/server.csr.pem -out pki/certs/server.cert.pem -extfile pki/openssl-server.cnf -extensions server_cert

chmod 444 pki/certs/server.cert.pem

# Vérification du certificat serveur
openssl x509 -noout -text -in pki/certs/server.cert.pem
```

### 1.3 Création d'un certificat QWAC pour l'API Gateway avec extensions PSD2

```bash
# Génération de la clé pour le certificat QWAC
openssl genrsa -out pki/private/qwac.key.pem 2048
chmod 400 pki/private/qwac.key.pem

# Configuration pour le certificat QWAC
cat > pki/openssl-qwac.cnf << 'EOF'
[ req ]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[ req_distinguished_name ]
C = FR
ST = Paris
L = Paris
O = API Gateway Client
CN = api-gateway.example.com
organizationIdentifier = PSDFR-ACPR-15930

[ v3_req ]
basicConstraints = CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, serverAuth
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
certificatePolicies = @policysection
qcStatements = critical, @qcstatements

[ policysection ]
policyIdentifier = 0.4.0.19495.3.1
userNotice.1 = @notice

[ notice ]
explicitText = "PSD2 Qualified Website Authentication Certificate"

[ qcstatements ]
id-etsi-psd2-qcStatement = DER:30:64:31:10:30:0E:06:03:2B:06:01:05:05:07:0C:01:01:01:01:01:FF:04:04:50:53:44:32:31:25:30:23:06:0A:2A:06:01:04:01:97:55:01:03:01:01:30:15:0C:09:50:53:44:46:52:2D:41:43:50:52:0C:08:31:35:39:33:30:31:29:30:27:06:08:2B:06:01:05:05:07:0C:02:30:1B:1E:19:68:74:74:70:73:3A:2F:2F:70:73:64:32:2E:65:78:61:6D:70:6C:65:2E:63:6F:6D
EOF

# Création de la demande de certificat QWAC
openssl req -new -config pki/openssl-qwac.cnf -key pki/private/qwac.key.pem -out pki/certs/qwac.csr.pem

# Configuration pour les extensions du certificat QWAC
cat > pki/openssl-qwac-ext.cnf << 'EOF'
[ qwac_cert ]
basicConstraints = CA:FALSE
keyUsage = critical, digitalSignature, keyEncipherment
extendedKeyUsage = clientAuth, serverAuth
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid,issuer:always
certificatePolicies = @policysection
qcStatements = critical, @qcstatements

[ policysection ]
policyIdentifier = 0.4.0.19495.3.1
userNotice.1 = @notice

[ notice ]
explicitText = "PSD2 Qualified Website Authentication Certificate"

[ qcstatements ]
id-etsi-psd2-qcStatement = DER:30:64:31:10:30:0E:06:03:2B:06:01:05:05:07:0C:01:01:01:01:01:FF:04:04:50:53:44:32:31:25:30:23:06:0A:2A:06:01:04:01:97:55:01:03:01:01:30:15:0C:09:50:53:44:46:52:2D:41:43:50:52:0C:08:31:35:39:33:30:31:29:30:27:06:08:2B:06:01:05:05:07:0C:02:30:1B:1E:19:68:74:74:70:73:3A:2F:2F:70:73:64:32:2E:65:78:61:6D:70:6C:65:2E:63:6F:6D
EOF

# Signature du certificat QWAC par l'AC racine
openssl ca -batch -config pki/openssl-ca.cnf -extensions qwac_cert -days 730 -notext -md sha256 -in pki/certs/qwac.csr.pem -out pki/certs/qwac.cert.pem -extfile pki/openssl-qwac-ext.cnf -extensions qwac_cert

chmod 444 pki/certs/qwac.cert.pem

# Vérification du certificat QWAC
openssl x509 -noout -text -in pki/certs/qwac.cert.pem
```

### 1.4 Création des truststores nécessaires

```bash
# Création du truststore au format PKCS12 pour le mock-client-VOP
openssl pkcs12 -export -in pki/certs/ca.cert.pem -out pki/truststore.p12 -nokeys -name "Root CA" -passout pass:changeit

# Création du truststore au format JKS (pour application Java)
keytool -importkeystore -srckeystore pki/truststore.p12 -srcstoretype PKCS12 -srcstorepass changeit -destkeystore pki/truststore.jks -deststoretype JKS -deststorepass changeit

# Création du keystore pour le serveur mock au format PKCS12
openssl pkcs12 -export -in pki/certs/server.cert.pem -inkey pki/private/server.key.pem -certfile pki/certs/ca.cert.pem -out pki/keystore-server.p12 -name "server" -passout pass:changeit

# Conversion en JKS pour application Java
keytool -importkeystore -srckeystore pki/keystore-server.p12 -srcstoretype PKCS12 -srcstorepass changeit -destkeystore pki/keystore-server.jks -deststoretype JKS -deststorepass changeit

# Création du keystore pour l'API Gateway au format PKCS12 (pour le client QWAC)
openssl pkcs12 -export -in pki/certs/qwac.cert.pem -inkey pki/private/qwac.key.pem -certfile pki/certs/ca.cert.pem -out pki/keystore-qwac.p12 -name "qwac" -passout pass:changeit

# Conversion en JKS pour application Java
keytool -importkeystore -srckeystore pki/keystore-qwac.p12 -srcstoretype PKCS12 -srcstorepass changeit -destkeystore pki/keystore-qwac.jks -deststoretype JKS -deststorepass changeit
```

### 1.5 Validation des certificats générés

```bash
# Validation du certificat serveur
openssl verify -CAfile pki/certs/ca.cert.pem pki/certs/server.cert.pem

# Validation du certificat QWAC
openssl verify -CAfile pki/certs/ca.cert.pem pki/certs/qwac.cert.pem

# Extraction et vérification du certificateOwnerId (PSD2 identifiant)
openssl x509 -in pki/certs/qwac.cert.pem -noout -subject | grep "organizationIdentifier"
```

## 2. Configuration du mock-client-VOP

Je vais maintenant vous fournir une configuration type pour votre application Spring Boot (application.yml).

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore-server.jks
    key-store-password: changeit
    key-store-type: JKS
    key-alias: server
    trust-store: classpath:truststore.jks
    trust-store-password: changeit
    trust-store-type: JKS
    client-auth: need # Rend l'authentification mutuelle obligatoire

spring:
  application:
    name: mock-client-vop

# Configuration personnalisée pour le mock-client-VOP
mock-vop:
  # Configuration pour la validation des certificats QWAC
  qwac:
    validation:
      enabled: true
      # Vérification de la chaîne de certificats
      certificate-chain-validation: true
      # Vérification de la période de validité
      validity-period-validation: true
      # Vérification des extensions PSD2
      psd2-extensions-validation: true
      # OID pour l'identifiant d'organisation PSD2
      organization-identifier-oid: "2.5.4.97"
  
  # Configuration pour le routage basé sur le certificateOwnerId
  routing:
    enabled: true
    # Expression régulière pour extraire l'ID du PSP à partir du certificateOwnerId
    certificate-owner-id-pattern: "PSDFR-ACPR-(\\d+)"
    # Mappings des PSPs (ajoutez autant de mappings que nécessaire)
    psp-mappings:
      "15930": "https://backend-15930.example.com"
      "default": "https://default-backend.example.com"

logging:
  level:
    root: INFO
    com.example.mockclientvop: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
```

## 3. Implémentation du mock-client-VOP (Spring Boot)

### 3.1 Structure du projet

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── mockclientvop/
│   │               ├── MockClientVopApplication.java
│   │               ├── config/
│   │               │   ├── SecurityConfig.java
│   │               │   └── SSLConfig.java
│   │               ├── controller/
│   │               │   └── MockController.java
│   │               ├── service/
│   │               │   ├── CertificateService.java
│   │               │   └── RoutingService.java
│   │               └── util/
│   │                   └── CertificateUtils.java
│   └── resources/
│       ├── application.yml
│       ├── keystore-server.jks
│       └── truststore.jks
```

### 3.2 Code source du mock-client-VOP

#### MockClientVopApplication.java

```java
package com.example.mockclientvop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MockClientVopApplication {
    public static void main(String[] args) {
        SpringApplication.run(MockClientVopApplication.class, args);
    }
}
```

#### SecurityConfig.java

```java
package com.example.mockclientvop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
            .anyRequest().authenticated()
            .and()
            .x509()
            .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
            .and()
            .csrf().disable();
        return http.build();
    }
}
```

#### SSLConfig.java

```java
package com.example.mockclientvop.config;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SSLConfig {

    @Value("${server.ssl.trust-store}")
    private String trustStore;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> sslConfigCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            SSLHostConfig sslHostConfig = connector.findSSlHostConfigs()[0];
            sslHostConfig.setTruststoreFile(trustStore.replace("classpath:", ""));
            sslHostConfig.setTruststorePassword(trustStorePassword);
            sslHostConfig.setCertificateVerification("required");
        });
    }
}
```

#### CertificateUtils.java

```java
package com.example.mockclientvop.util;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.springframework.stereotype.Component;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CertificateUtils {

    private static final ASN1ObjectIdentifier ORGANIZATION_IDENTIFIER = new ASN1ObjectIdentifier("2.5.4.97");
    
    public Optional<String> extractOrganizationIdFromCertificate(X509Certificate certificate) {
        try {
            X500Name x500name = new JcaX509CertificateHolder(certificate).getSubject();
            RDN[] rdns = x500name.getRDNs(ORGANIZATION_IDENTIFIER);
            
            if (rdns.length > 0) {
                return Optional.of(rdns[0].getFirst().getValue().toString());
            }
            
            return Optional.empty();
        } catch (CertificateEncodingException e) {
            return Optional.empty();
        }
    }
    
    public Optional<String> extractPSPIdFromOrganizationId(String organizationId, String pattern) {
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(organizationId);
        
        if (matcher.find() && matcher.groupCount() >= 1) {
            return Optional.of(matcher.group(1));
        }
        
        return Optional.empty();
    }
    
    public boolean validateQWACCertificate(X509Certificate certificate) {
        // Validation de base (période de validité)
        try {
            certificate.checkValidity();
            
            // Vérification des extensions PSD2 (à personnaliser selon vos besoins)
            // Cette implémentation est simplifiée
            
            // 1. Vérifier la présence de l'identifiant d'organisation
            Optional<String> orgId = extractOrganizationIdFromCertificate(certificate);
            if (orgId.isEmpty()) {
                return false;
            }
            
            // 2. Vérifier le format de l'identifiant d'organisation (PSDFR-ACPR-XXXXX)
            String pattern = "PSDFR-ACPR-(\\d+)";
            Optional<String> pspId = extractPSPIdFromOrganizationId(orgId.get(), pattern);
            return pspId.isPresent();
            
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### CertificateService.java

```java
package com.example.mockclientvop.service;

import com.example.mockclientvop.util.CertificateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Optional;

@Service
public class CertificateService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateService.class);
    
    @Autowired
    private CertificateUtils certificateUtils;
    
    @Value("${mock-vop.qwac.validation.enabled}")
    private boolean validationEnabled;
    
    @Value("${mock-vop.qwac.validation.certificate-chain-validation}")
    private boolean certificateChainValidation;
    
    @Value("${mock-vop.qwac.validation.validity-period-validation}")
    private boolean validityPeriodValidation;
    
    @Value("${mock-vop.qwac.validation.psd2-extensions-validation}")
    private boolean psd2ExtensionsValidation;
    
    @Value("${mock-vop.routing.certificate-owner-id-pattern}")
    private String certificateOwnerIdPattern;

    public boolean validateQWACCertificate(X509Certificate certificate) {
        if (!validationEnabled) {
            return true;
        }
        
        logger.debug("Validating QWAC Certificate: {}", certificate.getSubjectX500Principal());
        
        if (validityPeriodValidation) {
            try {
                certificate.checkValidity();
            } catch (Exception e) {
                logger.error("Certificate validity check failed", e);
                return false;
            }
        }
        
        if (psd2ExtensionsValidation) {
            boolean isValid = certificateUtils.validateQWACCertificate(certificate);
            if (!isValid) {
                logger.error("PSD2 extensions validation failed");
                return false;
            }
        }
        
        return true;
    }
    
    public Optional<String> extractPSPIdFromCertificate(X509Certificate certificate) {
        Optional<String> organizationId = certificateUtils.extractOrganizationIdFromCertificate(certificate);
        if (organizationId.isPresent()) {
            logger.debug("Extracted organization identifier: {}", organizationId.get());
            return certificateUtils.extractPSPIdFromOrganizationId(organizationId.get(), certificateOwnerIdPattern);
        }
        return Optional.empty();
    }
}
```

#### RoutingService.java

```java
package com.example.mockclientvop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Optional;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);
    
    @Autowired
    private CertificateService certificateService;
    
    @Value("${mock-vop.routing.enabled}")
    private boolean routingEnabled;
    
    @Value("#{${mock-vop.routing.psp-mappings}}")
    private Map<String, String> pspMappings;
    
    public String determineTargetUrl(X509Certificate certificate) {
        if (!routingEnabled) {
            return pspMappings.getOrDefault("default", "https://default-backend.example.com");
        }
        
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(certificate);
        if (pspId.isPresent()) {
            String targetUrl = pspMappings.getOrDefault(pspId.get(), pspMappings.get("default"));
            logger.debug("Routing request for PSP ID {} to {}", pspId.get(), targetUrl);
            return targetUrl;
        }
        
        logger.warn("Could not determine PSP ID from certificate, using default route");
        return pspMappings.getOrDefault("default", "https://default-backend.example.com");
    }
}
```

#### MockController.java

```java
package com.example.mockclientvop.controller;

import com.example.mockclientvop.service.CertificateService;
import com.example.mockclientvop.service.RoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MockController {

    private static final Logger logger = LoggerFactory.getLogger(MockController.class);
    
    @Autowired
    private CertificateService certificateService;
    
    @Autowired
    private RoutingService routingService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        Map<String, Object> response = new HashMap<>();
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Extraction du PSP ID
        Optional<String> pspId = certificateService.extractPSPIdFromCertificate(clientCert);
        
        // Construction de la réponse
        response.put("status", "success");
        response.put("message", "Valid QWAC certificate");
        response.put("subject", clientCert.getSubjectX500Principal().toString());
        
        if (pspId.isPresent()) {
            response.put("pspId", pspId.get());
            response.put("targetUrl", routingService.determineTargetUrl(clientCert));
        }
        
        return ResponseEntity.ok(response);
    }
    
    @RequestMapping("/**")
    public ResponseEntity<Map<String, Object>> handleRequest(HttpServletRequest request) {
        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        
        Map<String, Object> response = new HashMap<>();
        
        if (certs == null || certs.length == 0) {
            logger.error("No client certificate provided");
            response.put("status", "error");
            response.put("message", "No client certificate provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        X509Certificate clientCert = certs[0];
        
        // Validation du certificat QWAC
        boolean isValid = certificateService.validateQWACCertificate(clientCert);
        if (!isValid) {
            logger.error("Invalid QWAC certificate");
            response.put("status", "error");
            response.put("message", "Invalid QWAC certificate");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Construction de la réponse pour simuler un forward
        String targetUrl = routingService.determineTargetUrl(clientCert);
        
        response.put("status", "success");
        response.put("message", "Request would be forwarded");
        response.put("originalPath", request.getRequestURI());
        response.put("method", request.getMethod());
        response.put("targetUrl", targetUrl);
        
        logger.info("Simulating forwarding {} request to {}", request.getMethod(), targetUrl);
        
        return ResponseEntity.ok(response);
    }
}
```

## 4. Test du mock-client-VOP

### 4.1 Préparer l'environnement de test

1. Placez les fichiers JKS générés dans le répertoire `src/main/resources`
2. Exécutez l'application Spring Boot

### 4.2 Tester avec curl

```bash
# Test avec curl en utilisant le certificat QWAC
curl --cert-type P12 --cert pki/keystore-qwac.p12:changeit \
     --cacert pki/certs/ca.cert.pem \
     -v https://localhost:8443/api/status
```

### 4.3 Tester avec un client Java

Voici un exemple simple de client Java pour tester la connexion:

```java
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class QwacTestClient {
    
    public static void main(String[] args) throws Exception {
        // Chemin vers les keystores et truststores
        String keystorePath = "pki/keystore-qwac.jks";
        String keystorePassword = "changeit";
        String truststorePath
