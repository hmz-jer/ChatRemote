ldapmodify -Y EXTERNAL -H ldapi:/// <<EOF
dn: olcDatabase={1}mdb,cn=config
changetype: modify
replace: olcAccess
olcAccess: to attrs=userPassword by anonymous read by self write by * none
olcAccess: to dn.base="" by * read
olcAccess: to * by self write by anonymous read by * read
EOF
