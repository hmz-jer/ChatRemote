 Ok, je comprends. Nous allons créer notre propre PKI depuis zéro pour tester le flux d'authentification MTLS dans le cadre du système de vérification IBAN.

Voici comment nous pouvons procéder étape par étape :

1. **Créer notre propre Autorité de Certification (CA)**

```bash
# Créer un répertoire pour notre PKI
mkdir -p pki_test/ca pki_test/certs

# Générer la clé privée de la CA
openssl genrsa -out pki_test/ca/ca.key 4096

# Générer le certificat auto-signé de la CA
openssl req -x509 -new -nodes -key pki_test/ca/ca.key -sha256 -days 1825 -out pki_test/ca/ca.crt -subj "/C=FR/O=Test PKI/OU=CA/CN=Test Root CA"
```

2. **Créer un certificat QWAC pour notre client**

```bash
# Générer une clé privée pour le certificat QWAC
openssl genrsa -out pki_test/certs/qwac.key 2048

# Créer un fichier de configuration pour notre CSR
cat > pki_test/certs/qwac.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = FR
O = Votre Société
OU = 0002 552120222
CN = dsp2.votresociete.fr
L = PARIS
2.5.4.97 = PSDFR-ACPR-30003

[v3_req]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = dsp2.votresociete.fr
EOF

# Générer la demande de certificat (CSR)
openssl req -new -key pki_test/certs/qwac.key -out pki_test/certs/qwac.csr -config pki_test/certs/qwac.conf

# Signer la demande avec notre CA
openssl x509 -req -in pki_test/certs/qwac.csr -CA pki_test/ca/ca.crt -CAkey pki_test/ca/ca.key -CAcreateserial -out pki_test/certs/qwac.crt -days 730 -sha256 -extensions v3_req -extfile pki_test/certs/qwac.conf
```

3. **Créer un certificat pour le serveur VOP**

```bash
# Générer une clé privée pour le serveur VOP
openssl genrsa -out pki_test/certs/vop_server.key 2048

# Créer un fichier de configuration pour le serveur VOP
cat > pki_test/certs/vop_server.conf << EOF
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req
prompt = no

[req_distinguished_name]
C = FR
O = PSP Externe VOP
OU = Test
CN = vop.test.local

[v3_req]
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = vop.test.local
DNS.2 = localhost
IP.1 = 127.0.0.1
EOF

# Générer la demande de certificat (CSR)
openssl req -new -key pki_test/certs/vop_server.key -out pki_test/certs/vop_server.csr -config pki_test/certs/vop_server.conf

# Signer la demande avec notre CA
openssl x509 -req -in pki_test/certs/vop_server.csr -CA pki_test/ca/ca.crt -CAkey pki_test/ca/ca.key -CAcreateserial -out pki_test/certs/vop_server.crt -days 730 -sha256 -extensions v3_req -extfile pki_test/certs/vop_server.conf
```

4. **Créer les formats PKCS#12 pour utilisation**

```bash
# Créer un PKCS#12 pour le client (QWAC)
openssl pkcs12 -export -out pki_test/certs/qwac.p12 -inkey pki_test/certs/qwac.key -in pki_test/certs/qwac.crt -certfile pki_test/ca/ca.crt -name "QWAC Test Cert" -passout pass:password

# Créer un PKCS#12 pour le serveur VOP
openssl pkcs12 -export -out pki_test/certs/vop_server.p12 -inkey pki_test/certs/vop_server.key -in pki_test/certs/vop_server.crt -certfile pki_test/ca/ca.crt -name "VOP Server" -passout pass:password
```

5. **Créer un Truststore Java pour le serveur VOP**

```bash
# Convertir le certificat CA en format JKS
keytool -import -trustcacerts -alias testca -file pki_test/ca/ca.crt -keystore pki_test/certs/truststore.jks -storepass password -noprompt
```

6. **Mettre en place un serveur mock VOP**

Créez un projet Spring Boot simple avec la configuration suivante:

```java
// VopApplication.java
@SpringBootApplication
public class VopApplication {
    public static void main(String[] args) {
        SpringApplication.run(VopApplication.class, args);
    }
}

// VopController.java
@RestController
public class VopController {
    
    private static final Logger logger = LoggerFactory.getLogger(VopController.class);
    
    @PostMapping("/ibancheck")
    public ResponseEntity<?> checkIban(@RequestBody Map<String, Object> request, HttpServletRequest servletRequest) {
        // Récupérer le certificat client
        X509Certificate[] certs = (X509Certificate[]) servletRequest
            .getAttribute("javax.servlet.request.X509Certificate");
        
        if (certs == null || certs.length == 0) {
            logger.error("Aucun certificat client trouvé");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", "ERROR", "message", "Certificat manquant"));
        }
        
        // Analyser le certificat
        X509Certificate clientCert = certs[0];
        String subject = clientCert.getSubjectX500Principal().getName();
        logger.info("Certificat client reçu: {}", subject);
        
        // Extraire le certificateEmitterId (dans ce cas, on utilise l'identifiant PSDFR-ACPR-XXXXX)
        String certificateEmitterId = "";
        Pattern pattern = Pattern.compile("2\\.5\\.4\\.97=([^,]+)");
        Matcher matcher = pattern.matcher(subject);
        if (matcher.find()) {
            certificateEmitterId = matcher.group(1);
            logger.info("CertificateEmitterId extrait: {}", certificateEmitterId);
        }
        
        // Simuler la vérification d'IBAN
        String iban = (String) request.get("iban");
        boolean isValid = iban != null && iban.startsWith("FR") && iban.length() >= 20;
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", isValid ? "SUCCESS" : "ERROR");
        response.put("valid", isValid);
        response.put("certificateEmitterId", certificateEmitterId);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
```

```properties
# application.properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:vop_server.p12
server.ssl.key-store-password=password
server.ssl.key-store-type=PKCS12
server.ssl.trust-store=classpath:truststore.jks
server.ssl.trust-store-password=password
server.ssl.client-auth=need
logging.level.org.springframework.web=DEBUG
```

7. **Tester l'authentification MTLS**

```bash
# Test avec curl
curl --cert pki_test/certs/qwac.p12:password --cert-type p12 --cacert pki_test/ca/ca.crt -k https://localhost:8443/ibancheck -H "Content-Type: application/json" -d '{"iban":"FR7630001007941234567890185"}'
```

8. **Simuler l'API Gateway**

Pour un test plus complet, vous pourriez utiliser Nginx comme proxy pour simuler l'API Gateway:

```
# nginx.conf
server {
    listen 443 ssl;
    server_name api-gateway.test.local;
    
    ssl_certificate /path/to/pki_test/certs/api_gateway.crt;
    ssl_certificate_key /path/to/pki_test/certs/api_gateway.key;
    ssl_client_certificate /path/to/pki_test/ca/ca.crt;
    ssl_verify_client on;
    
    location /ibancheck_outbound {
        proxy_pass https://localhost:8443/ibancheck;
        proxy_ssl_certificate /path/to/pki_test/certs/qwac.crt;
        proxy_ssl_certificate_key /path/to/pki_test/certs/qwac.key;
        proxy_ssl_trusted_certificate /path/to/pki_test/ca/ca.crt;
        proxy_ssl_verify on;
    }
}
```

Cette approche vous permettra de créer un environnement complet pour tester l'authentification MTLS dans le flux IBAN check, en partant de votre propre PKI. Vous pourrez ensuite adapter les certificats selon vos besoins spécifiques et tester différents scénarios.
