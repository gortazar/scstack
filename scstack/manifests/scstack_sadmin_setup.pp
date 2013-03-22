# Class: scstack_ldap
#
# This module manages scstack-ldap
#
# Parameters:
#   [*baseDN*]          - The LDAP base distingished name of the server.
#   [*bindDN*]          - The user to connect to the OpenLDAP server
#   [*cnBindDN*]        - The distingished name of the OpenLDAP administrator (for instance, cn=admin,dc=test,dc=scstack,dc=org)
#   [*passBindDN*]      - The password for the bindDN account 
#   [*searchDN*]        - The entry where users will be looked for (i.e., "ou=people,dc=test,dc=scstack,dc=org"). 
#   [*searchProjectDN*] - The entry where projects will be looked for (i.e., "ou=projects,dc=test,dc=scstack,dc=org"). 
#   [*sadmin*]          - Superadmin username (i.e., "sadmin") 
#   [*sadminpass*]      - Superadmin password in md5 (i.e., "pass") 
#
# Actions:
#
# Requires: see Modulefile
#
# Sample Usage:
#
class scstack::scstack_sadmin_setup (
  $baseDN,
  $bindDN,
  $passBindDN,
  $domain,
  $sadmin,
  $sadminPassMd5,
) {
  
  file { "/etc/ldap/superadmin.ldif":
    content => template("scstack/ldap/superadmin.ldif.erb"),
  }
  
  exec { "ldapadd sadmin":
    require => [File["/etc/ldap/superadmin.ldif"],Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/superadmin.ldif",
  }
  
  exec { "rm-superadmin.ldif":
    cwd => "/etc/ldap",
    command => "/bin/rm superadmin.ldif",
    require => Exec["ldapadd sadmin"],
  }
  

}
