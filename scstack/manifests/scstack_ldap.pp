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
class scstack::scstack_ldap (
  $baseDN,
  $bindDN,
  $cnBindDN,
  $passBindDN,
  $searchDN,
  $searchProjectDN,
  $domain,
  $sadmin,
  $sadminpass,
) {
  
  class { "ldap":
    server => true,
  #  ssl => "true",
  #  ssl_ca => '??'
  #  ssl_cert => '/etc/ldap/replicante2.airfit.es_rapidssl.crt',
  #  ssl_key => '/etc/ldap/replicante2.airfit.es.key',
  }

  # Define SCStack schema
  ldap::define::schema { "scstack":
    ensure => "present",
    source => "puppet:///modules/scstack/ldap/sc-stack-projects2.ldif",
  }
  
  # Install OpenLDAP and configure domain and structural info
  ldap::define::domain { $domain:
    basedn => $baseDN,
    rootdn => $cnBindDN,
    rootpw => $passBindDN,
    before => [Exec["ldapadd construir.ldif"],Exec["ldapadd sadmin"]],
  }

  file { "/etc/ldap/construir.ldif":
    content => template('scstack/ldap/construir.ldif.erb'),
  }
  
  exec { "ldapadd construir.ldif":
    require => [File["/etc/ldap/construir.ldif"], Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/construir.ldif",
  }
  
  
  file { "/etc/ldap/superadmin.ldif":
    content => template("scstack/ldap/superadmin.ldif.erb"),
  }
  
  exec { "ldapadd sadmin":
    require => [File["/etc/ldap/superadmin.ldif"], Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/superadmin.ldif",
  }
  
  exec { "rm-superadmin.ldif":
    cwd => "/etc/ldap",
    command => "/bin/rm superadmin.ldif",
    require => Exec["ldapadd sadmin"],
  }

}
