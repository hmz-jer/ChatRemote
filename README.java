    volumes:
      - ./ldap_data:/var/lib/ldap
      - ./ldap_config:/etc/ldap/slapd.d
      - ./ldap/init.ldif:/container/service/slapd/assets/config/bootstrap/ldif/50-bootstrap.ldif
      - ./bootstrap:/container/service/slapd/assets/config/bootstrap/ldif/custom
    command: 
      - --copy-service
      - --loglevel debug
    entrypoint: 
      - /bin/sh
      - -c
      - |
        /container/tool/run --copy-service --loglevel debug &
        sleep 10
        /container/service/slapd/assets/config/bootstrap/ldif/custom/setup.sh
