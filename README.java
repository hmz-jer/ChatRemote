 L'erreur "in position 10 of 12 in FilterChainProxy" indique un problème dans la chaîne de filtres de sécurité Spring Security lors de votre test avec curl. Cette erreur se produit souvent lors de l'authentification client mutuelle (mTLS). Voici comment résoudre ce problème étape par étape:

## 1. Analyse de l'erreur

Cette erreur peut avoir plusieurs causes:

1. **Certificat client non fourni**: Vous n'avez pas fourni de certificat client avec votre requête curl
2. **Certificat client invalide**: Le certificat client est fourni mais n'est pas valide/n'est pas approuvé
3. **Problème dans la configuration du truststore**: Le truststore du serveur ne contient pas l'AC qui a signé le certificat client
4. **Problème de format ou de chemin des certificats**: Les chemins vers les certificats dans la commande curl sont incorrects
5. **Problème de configuration SSL côté serveur**: La configuration SSL du serveur est incorrecte

## 2. Vérifier les logs du serveur

Vérifiez les logs détaillés du serveur pour identifier la cause exacte:

```bash
mock-vop logs 100 | grep -E "FilterChainProxy|X509|SSL|TLS|Certificate"
```

## 3. Correction des problèmes courants

### 3.1. Configuration SSL du serveur

Vérifiez et mettez à jour la configuration SSL dans votre fichier `application.yml`:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: file:/opt/mock-client-vop/certs/keystore/server.jks
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-store-type: JKS
    key-alias: server
    trust-store: file:/opt/mock-client-vop/certs/truststore/truststore.jks
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD:changeit}
    trust-store-type: JKS
    client-auth: need  # Assurez-vous que c'est "need" pour authentification obligatoire
    # Ajouter ces paramètres pour plus de détails sur les erreurs
    debug: true
    enabled-protocols: TLSv1.2,TLSv1.3
```

### 3.2. Ajuster la commande curl

Vérifiez que votre commande curl est correcte. Voici la syntaxe recommandée:

```bash
curl -v \
  --cert /chemin/vers/certificat_client.crt \
  --key /chemin/vers/cle_privee_client.key \
  --cacert /chemin/vers/ca_root.crt \
  https://10.55.8.12:8443/api/status
```

Si vous utilisez un format PKCS12 (fichier .p12):

```bash
curl -v \
  --cert-type P12 \
  --cert /chemin/vers/client.p12:mot_de_passe \
  --cacert /chemin/vers/ca_root.crt \
  https://10.55.8.12:8443/api/status
```

### 3.3. Configurer Spring Security pour le débogage

Ajoutez cette configuration à votre fichier `application.yml` pour activer le débogage Spring Security:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.web: DEBUG
    org.apache.tomcat.util.net: DEBUG
    org.apache.coyote.http11: DEBUG
```

### 3.4. Vérifier les certificats côté serveur

Vérifiez que le truststore du serveur contient bien la chaîne de confiance du certificat client:

```bash
keytool -list -v -keystore /opt/mock-client-vop/certs/truststore/truststore.jks -storepass changeit
```

### 3.5. Créer une classe FilterChainDebugFilter pour identifier l'erreur exacte

```java
package com.example.mockclientvop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;

@Configuration
public class DebugConfig {

    private static final Logger logger = LoggerFactory.getLogger(DebugConfig.class);

    @Bean
    public OncePerRequestFilter certificateDebugFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
                
                if (certs != null && certs.length > 0) {
                    logger.info("Certificat client trouvé: Subject={}, Issuer={}",
                            certs[0].getSubjectX500Principal().getName(),
                            certs[0].getIssuerX500Principal().getName());
                } else {
                    logger.warn("Aucun certificat client trouvé dans la requête");
                }
                
                // Capturer les en-têtes pour le débogage
                logger.info("En-têtes de la requête:");
                request.getHeaderNames().asIterator().forEachRemaining(headerName -> 
                    logger.info("  {} = {}", headerName, request.getHeader(headerName))
                );
                
                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public FilterChainDecorator filterChainDecorator(FilterChainProxy filterChainProxy) {
        return new FilterChainDecorator(filterChainProxy);
    }

    public static class FilterChainDecorator {
        private static final Logger logger = LoggerFactory.getLogger(FilterChainDecorator.class);

        public FilterChainDecorator(FilterChainProxy filterChainProxy) {
            // Afficher tous les filtres dans la chaîne
            int chainCount = 0;
            for (SecurityFilterChain chain : filterChainProxy.getFilterChains()) {
                logger.info("Chaîne de filtres #{}", chainCount++);
                chain.getFilters().forEach(filter -> 
                    logger.info("  Filtre: {}", filter.getClass().getName())
                );
            }
        }
    }
}
```

## 4. Script de débogage pour tester la connexion SSL

Créez un script pour tester la connexion SSL avec OpenSSL:

```bash
#!/bin/bash

# debug-ssl.sh - Script pour déboguer la connexion SSL au mock-client-VOP

SERVER_HOST="10.55.8.12"
SERVER_PORT="8443"
CLIENT_CERT="/chemin/vers/client.crt"
CLIENT_KEY="/chemin/vers/client.key"
CA_CERT="/chemin/vers/ca.crt"

echo "╔═══════════════════════════════════════════════════════╗"
echo "║          DÉBOGAGE DE LA CONNEXION SSL/TLS             ║"
echo "╚═══════════════════════════════════════════════════════╝"

# Tester la connectivité de base
echo "Test de connectivité de base..."
nc -zv $SERVER_HOST $SERVER_PORT 2>&1 || { echo "Erreur: Impossible de se connecter au serveur"; exit 1; }

# Tester avec OpenSSL pour voir les détails du certificat serveur
echo -e "\nCertificat serveur présenté:"
echo "==========================="
echo | openssl s_client -connect $SERVER_HOST:$SERVER_PORT -showcerts

# Tester avec OpenSSL avec le certificat client
echo -e "\nTest de connexion avec certificat client:"
echo "==================================="
openssl s_client -connect $SERVER_HOST:$SERVER_PORT \
  -cert $CLIENT_CERT \
  -key $CLIENT_KEY \
  -CAfile $CA_CERT \
  -state -debug

# Tester avec curl (en mode verbose)
echo -e "\nTest avec curl (verbose):"
echo "======================="
curl -v \
  --cert $CLIENT_CERT \
  --key $CLIENT_KEY \
  --cacert $CA_CERT \
  https://$SERVER_HOST:$SERVER_PORT/api/status

echo -e "\nTests terminés."
```

## 5. Configuration avancée pour résoudre les problèmes de filtre

Ajoutez cette configuration à votre classe `SecurityConfig.java`:

```java
package com.example.mockclientvop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .requestMatchers("/error").permitAll() // Permettre l'accès aux pages d'erreur
                .requestMatchers("/api/public/**").permitAll() // Endpoints publics si nécessaire
                .anyRequest().authenticated()
            .and()
            .x509()
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .userDetailsService(userDetailsService())
            .and()
            .csrf().disable();
        
        // Ajouter le filtre de débogage au début de la chaîne
        http.addFilterBefore(certificateDebugFilter(), BasicAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return subjectDN -> {
            // Accepte tous les certificats clients valides et attribue le rôle CLIENT
            return new User(subjectDN, "", 
                    AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_CLIENT"));
        };
    }
    
    @Bean
    public OncePerRequestFilter certificateDebugFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                
                X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
                
                if (certs != null && certs.length > 0) {
                    logger.info("Certificat client trouvé: Subject={}, Issuer={}",
                            certs[0].getSubjectX500Principal().getName(),
                            certs[0].getIssuerX500Principal().getName());
                } else {
                    logger.warn("Aucun certificat client trouvé dans la requête");
                }
                
                filterChain.doFilter(request, response);
            }
        };
    }
}
```

## 6. Créer un endpoint de test public

Ajoutez un contrôleur avec un endpoint public pour tester la connectivité SSL de base sans authentification:

```java
package com.example.mockclientvop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Le serveur est en cours d'exécution");
        return response;
    }
}
```

## 7. Mise à jour du script mock-vop.sh pour plus de diagnostic

Ajoutez une fonction de vérification SSL au script `mock-vop.sh`:

```bash
# Fonction pour tester la configuration SSL
test_ssl() {
  SERVER_HOST=${1:-"localhost"}
  SERVER_PORT=${2:-"8443"}
  
  echo "╔═══════════════════════════════════════════════════════╗"
  echo "║          TEST DE LA CONFIGURATION SSL/TLS             ║"
  echo "╚═══════════════════════════════════════════════════════╝"
  
  # Tester la connectivité de base
  echo "Test de connectivité de base..."
  nc -zv $SERVER_HOST $SERVER_PORT 2>&1 || { echo "Erreur: Impossible de se connecter au serveur"; return 1; }
  
  # Tester avec OpenSSL pour voir les détails du certificat serveur
  echo -e "\nCertificat serveur présenté:"
  echo "==========================="
  echo | openssl s_client -connect $SERVER_HOST:$SERVER_PORT -showcerts | grep -E "subject|issuer|verify"
  
  # Vérifier si le serveur demande un certificat client
  echo -e "\nVérification de la demande de certificat client:"
  echo "======================================="
  echo | openssl s_client -connect $SERVER_HOST:$SERVER_PORT -state | grep -E "Certificate chain|Acceptable client certificate"
  
  echo -e "\nTests terminés."
}

# Dans le case du script, ajoutez:
case "$1" in
  # [autres cas existants]
  "test-ssl")
    test_ssl "$2" "$3"
    ;;
  # [autres cas existants]
esac
```

## 8. Solutions spécifiques selon l'erreur exacte

Maintenant que vous avez des outils pour diagnostiquer le problème, voici les solutions pour les erreurs les plus courantes:

### 8.1. Si le serveur ne reconnaît pas l'AC qui a signé le certificat client

```bash
# Importez l'AC racine dans le truststore
keytool -importcert -file /chemin/vers/ca_racine.crt -alias ca-racine \
  -keystore /opt/mock-client-vop/certs/truststore/truststore.jks -storepass changeit -noprompt

# Si nécessaire, importez également l'AC intermédiaire
keytool -importcert -file /chemin/vers/ca_intermediaire.crt -alias ca-intermediaire \
  -keystore /opt/mock-client-vop/certs/truststore/truststore.jks -storepass changeit -noprompt

# Redémarrez le serveur
mock-vop restart
```

### 8.2. Si les versions de TLS ne sont pas compatibles

Modifiez la configuration SSL dans `application.yml`:

```yaml
server:
  ssl:
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_AES_128_GCM_SHA256,TLS_AES_256_GCM_SHA384
```

### 8.3. Si le certificat client n'est pas au bon format

Convertissez le certificat client au format approprié:

```bash
# Convertir un certificat PEM en PKCS12
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name "client"

# Utiliser le certificat PKCS12 avec curl
curl -v --cert-type P12 --cert client.p12:password --cacert ca.crt https://10.55.8.12:8443/api/status
```

## 9. Vérifiez également ces erreurs fréquentes

1. **Noms DNS/IP incorrects**: Assurez-vous que le nom d'hôte ou l'IP utilisé dans l'URL correspond bien à un nom ou IP inclus dans le certificat serveur (dans le champ CN ou SAN).

2. **Clés privées protégées par mot de passe**: Si la clé privée est protégée par un mot de passe, assurez-vous de l'inclure dans la commande curl.

3. **Dates de validité**: Vérifiez que les certificats (serveur et client) sont valides et n'ont pas expiré.

4. **Ordre de la chaîne de certificats**: L'ordre des certificats dans la chaîne peut être important. Assurez-vous que le certificat client est suivi de tout certificat intermédiaire, puis de l'AC racine.

En appliquant ces corrections et outils de diagnostic, vous devriez pouvoir identifier et résoudre le problème d'erreur "in position 10 of 12 in FilterChainProxy" lors de l'utilisation de curl pour tester votre mock-client-VOP.
