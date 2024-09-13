#!/bin/bash
ldapmodify -Y EXTERNAL -H ldapi:/// -f /container/service/slapd/assets/config/bootstrap/ldif/custom/custom.ldif
