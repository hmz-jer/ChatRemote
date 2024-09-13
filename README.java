dn: olcDatabase={1}hdb,cn=config
changetype: modify
add: olcAccess
olcAccess: to * by * read

volumes:
      - ./anonymous_access.ldif:/container/service/slapd/assets/config/bootstrap/ldif/custom/anonymous_access.ldif:ro
