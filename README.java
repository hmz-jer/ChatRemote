Si `cert.crt` est mentionné dans le contexte de l'option `--cert` avec `curl` et que vous l'avez décrit comme une clé privée, il semble qu'il y ait eu une confusion. Habituellement, un fichier avec l'extension `.crt` représente un certificat public, et non une clé privée. Les clés privées sont souvent stockées dans des fichiers avec des extensions telles que `.key` ou `.pem` et ne sont pas directement spécifiées dans les commandes `curl` utilisant `--cert` sans mentionner `--key`. Cependant, si votre `cert.crt` contient à la fois le certificat public et la clé privée (ce qui est moins courant mais possible, surtout dans des formats comme PEM), vous pouvez l'utiliser pour créer à la fois un keystore et un truststore.

Basé sur la clarification et en supposant que vous avez les éléments suivants :
- `ca.crt` : Certificat de l'Autorité de Certification (CA).
- `cert.crt` : Contient à la fois votre certificat public et votre clé privée, utilisés pour `curl`.

### Pour créer un truststore :

1. **Ajouter `ca.crt` au truststore (JKS)**

```bash
keytool -import -trustcacerts -file ca.crt -alias caAlias -keystore truststore.jks -storepass changeit
```

Cette commande ajoute le certificat CA (`ca.crt`) à un nouveau truststore nommé `truststore.jks` avec le mot de passe `changeit` (remplacez `changeit` par votre mot de passe de sécurité).

### Pour créer un keystore :

Si `cert.crt` contient la clé privée et le certificat public :

2. **Convertir `cert.crt` en format PKCS#12 (si nécessaire)**

D'abord, assurez-vous que `cert.crt` est au format PEM et contient à la fois la clé privée et le certificat public. Si ce n'est pas déjà le cas, vous aurez besoin de les combiner dans un seul fichier PEM (omis ici puisque vous avez indiqué avoir un seul fichier `cert.crt`).

Ensuite, convertissez le fichier PEM en PKCS#12 :

```bash
openssl pkcs12 -export -in cert.crt -out keystore.p12 -name mycert -passout pass:changeit
```

3. **Convertir PKCS#12 en JKS**

```bash
keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore keystore.jks -deststoretype JKS -deststorepass changeit -srcstorepass changeit
```

Cela crée un keystore JKS (`keystore.jks`) à partir du fichier PKCS#12 (`keystore.p12`). `mycert` est l'alias utilisé pour votre entrée de certificat et clé privée dans le keystore.

**Note :** Remplacez `changeit` par votre propre mot de passe sécurisé pour le keystore et le truststore.

Ces commandes vous permettent de configurer un environnement Java avec un keystore et un truststore qui peuvent être utilisés pour établir des connexions sécurisées, tant côté client que serveur, dans vos applications Java.
