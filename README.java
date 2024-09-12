# Racine de l'organisation
dn: dc=interne,dc=cartes,dc=com
objectClass: top
objectClass: dcObject
objectClass: organization
o: Interne Cartes
dc: interne

# Unité organisationnelle pour les utilisateurs
dn: ou=users,dc=interne,dc=cartes,dc=com
objectClass: organizationalUnit
ou: users

# Unité organisationnelle pour les groupes
dn: ou=groups,dc=interne,dc=cartes,dc=com
objectClass: organizationalUnit
ou: groups

# Utilisateur Admin
dn: cn=admin,dc=interne,dc=cartes,dc=com
objectClass: simpleSecurityObject
objectClass: organizationalRole
cn: admin
description: LDAP administrator
userPassword: {SSHA}UCPz/VK/wavAeAP090jYvEWqJnrHBArK

# Utilisateur 1
dn: uid=user1,ou=users,dc=interne,dc=cartes,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: user1
cn: User One
sn: One
givenName: User
userPassword: {SSHA}5SyXYOXEzxc91m3ZbvMA2VUDxQL8BcUC
loginShell: /bin/bash
uidNumber: 10000
gidNumber: 10000
homeDirectory: /home/user1
mail: user1@interne.cartes.com

# Utilisateur 2
dn: uid=user2,ou=users,dc=interne,dc=cartes,dc=com
objectClass: inetOrgPerson
objectClass: posixAccount
objectClass: shadowAccount
uid: user2
cn: User Two
sn: Two
givenName: User
userPassword: {SSHA}5SyXYOXEzxc91m3ZbvMA2VUDxQL8BcUC
loginShell: /bin/bash
uidNumber: 10001
gidNumber: 10000
homeDirectory: /home/user2
mail: user2@interne.cartes.com

# Groupe Utilisateurs
dn: cn=users,ou=groups,dc=interne,dc=cartes,dc=com
objectClass: posixGroup
cn: users
gidNumber: 10000
memberUid: user1
memberUid: user2

# Groupe Administrateurs
dn: cn=admins,ou=groups,dc=interne,dc=cartes,dc=com
objectClass: posixGroup
cn: admins
gidNumber: 10001
memberUid: user1
