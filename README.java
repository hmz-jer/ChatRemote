dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcAccess
olcAccess: to attrs=userPassword by anonymous auth by self write by * none
olcAccess: to dn.base="" by * read
olcAccess: to *
  by self write
  by users read
  by anonymous read
  by * none
