dn: olcDatabase={1}mdb,cn=config
changetype: modify
add: olcAccess
olcAccess: to * by dn.exact=gidNumber=0+uidNumber=0,cn=peercred,cn=external,cn=auth manage by * break
olcAccess: to attrs=userPassword,shadowLastChange by self write by anonymous auth by dn="cn=admin,dc=interne,dc=cartes,dc=com" write by * none
olcAccess: to * by self read by dn="cn=admin,dc=interne,dc=cartes,dc=com" write by anonymous read by * none
