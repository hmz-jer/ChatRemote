Voici un mail détaillant les étapes d'installation et de configuration de RSLOG :

---

**Objet : Configuration de RSLOG - Étapes d'installation et packages requis**

Bonjour,

Voici les étapes de configuration de RSLOG comme discuté :

1. **Création du fichier de configuration** :
   - Créer un fichier dans `/etc/rsyslog.d/` (exemple : `ibcproxy.conf`)
   - Contenu du fichier :
     ```
     $template log_ersb_v7,"%TIMESTAMP:1:10:date-rfc3339% %TIMESTAMP:12:23:date-rfc3339%|%HOSTNAME%|%msg:::drop-last-lf%\n"
     $template rslogger,"%TIMESTAMP:1:10:date-rfc3339% %TIMESTAMP:12:23:date-rfc3339%|%HOSTNAME%|%msg:::drop-last-lf%\n"
     $FileCreateMode 0640
     if ( $programname == 'IBC') then /var/log/log2trap.log;rslogger
     & @proxylog
     & ~
     ```

2. **Modification du fichier principal** :
   - Modifier le fichier `/etc/rsyslog.conf`
   - Ajouter la ligne : `include (file="/etc/rsyslog.d/*.conf" mode="optional")`

3. **Gestion du service** :
   - Arrêter le service : `service rsyslog stop`
   - Démarrer le service : `service rsyslog start`

4. **Notes importantes** :
   - Si `$programname == 'IBC'`, IBC est le nom de l'application Spring Boot
   - Utiliser `tail -f /var/log/log2trap.log` pour surveiller les logs

5. **Test de fonctionnement** :
   - Exécuter : `logger -a 505 -A IBC -C IBC_STATUS_REQUEST -m 0001 -t msg_type -i E -c 9999 -M "test_message"`

**Packages installés** (versions détectées sur le système) :
- rsyslog-1.1.0-1.el8.x86_64
- librslog-1.1.1-2.el8b.x86_64
- python3-rslog-0.8.0-1.el8b.x86_64
- rslogger-0.1.4-0.el8bx.noarch

N'hésitez pas à me contacter si vous avez des questions ou besoin d'aide supplémentaire pour cette configuration.

Cordialement,

---
