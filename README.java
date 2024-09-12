dn: olcDatabase={1}mdb,cn=config
changetype: modify
add: olcAccess
olcAccess: to attrs=userPassword by self write by anonymous auth by * none
olcAccess: to * by self read by anonymous read by * none
