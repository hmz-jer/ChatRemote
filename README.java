  version: '3'

services:
  openldap:
    image: osixia/openldap:1.5.0
    container_name: openldap
    environment:
      LDAP_ORGANISATION: "Organisation Interne Carte"
      LDAP_DOMAIN: "interne.cartes.com"
      LDAP_BASE_DN: "dc=interne,dc=cartes,dc=com"
      LDAP_ADMIN_PASSWORD: "admin_password"
    ports:
      - "389:389"
      - "636:636"
    volumes:
      - ./ldap:/container/service/slapd/assets/config/bootstrap/ldif/custom

  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: myapp
      MYSQL_USER: myuser
      MYSQL_PASSWORD: mypassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  phpldapadmin:
    image: osixia/phpldapadmin:latest
    container_name: phpldapadmin
    environment:
      PHPLDAPADMIN_LDAP_HOSTS: "openldap"
      PHPLDAPADMIN_HTTPS: "false"
    ports:
      - "8080:80"
    depends_on:
      - openldap

volumes:
  mysql_data:
