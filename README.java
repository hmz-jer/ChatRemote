
# Configuration LDAP
spring.ldap.urls=ldap://localhost:389
spring.ldap.base=dc=interne,dc=carte,dc=com
spring.ldap.username=cn=admin,dc=interne,dc=carte,dc=com
spring.ldap.password=admin_password

# Configuration de l'authentification LDAP
spring.security.ldap.authentication-strategy=simple
spring.security.ldap.user-search-base=ou=users
spring.security.ldap.user-search-filter=(uid={0})
spring.security.ldap.group-search-base=ou=groups
spring.security.ldap.group-search-filter=(member={0})

# Activer la sécurité de Spring
spring.security.enabled=true
