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
  
  exec { "apt-get update ldap":
    command => "/usr/bin/apt-get update",
    before => Class["ldap"],
  }

  class { "ldap":
    server => 'true',
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
  ldap::define::domain { "${domain}":
    basedn => "${baseDN}",
    rootdn => "${cnBindDN}",
    rootpw => "${passBindDN}",
    before => [Exec["ldapadd construir.ldif"],Exec["ldapadd sadmin"]],
  }

  file { "/etc/ldap/construir.ldif":
    content => template('scstack/ldap/construir.ldif.erb'),
  }
  
#  file { "/etc/ldap/sc-stack-projects.ldif":
#    ensure => file,
#    before => Exec['ldapadd sc-stack-projects.ldif'],
#  }

  exec { "ldapadd construir.ldif":
    require => [File["/etc/ldap/construir.ldif"], Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/construir.ldif",
  }
  
  exec {"sadminpass to file":
    cwd => "/tmp",
    command => "/bin/echo $sadminpass > sadminpass",
    require => Class["ldap"],
  }
  
  exec { "to md5 sadmin pass":
    cwd => "/tmp",
    command => "/usr/sbin/slappasswd -h {MD5}  -T /tmp/sadminpass > sadminpass.md5",
    logoutput => true,
    require => Exec["sadminpass to file"],
  }
  
  file { "/etc/ldap/superadmin.ldif":
    content => template("scstack/ldap/superadmin.ldif.erb"),
  }
  
  exec {"add md5 pass":
    cwd => "/etc/ldap",
    command => "/bin/cat /tmp/sadminpass.md5 >> superadmin.ldif",
    require => [File["/etc/ldap/superadmin.ldif"], Exec["to md5 sadmin pass"]],
  }
  
  exec { "ldapadd sadmin":
    require => [Exec["add md5 pass"],Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/superadmin.ldif",
  }
  
  exec { "rm-superadmin.ldif":
    cwd => "/etc/ldap",
    command => "/bin/rm superadmin.ldif",
    require => Exec["ldapadd sadmin"],
  }
  
  exec { "rm sadminpass sadminpass.md5":
    cwd => "/tmp",
    command => "/bin/rm sadminpass sadminpass.md5",
    require => Exec["ldapadd sadmin"],
  }
  
#  exec { 'ldapadd sc-stack-projects.ldif':
#    require => File['/etc/ldap/sc-stack-projects.ldif'],
#    logoutput => true,
#    command => "/usr/bin/ldapmodify -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/sc-stack-projects.ldif",
##    before => Exec['ldapadd tls_config.ldif']
#  }

#  file { "/etc/ldap/replicante2.airfit.es_rapidssl.crt":
#    ensure => file,
#    owner => 'openldap',
#    group => 'openldap',
#    mode => 0600,
#  }

#  file { "/etc/ldap/replicante2.airfit.es.key":
#    ensure => file,
#    owner => 'openldap',
#    group => 'openldap',
#    mode => 0600,
#  }

#  file { "/etc/ldap/tls_config.ldif":
#    ensure => file,
#    content => template('scstack/tls_config.ldif.erb'),
#  }

#  exec { "ldapadd tls_config.ldif":
#    require => [File["/etc/ldap/tls_config.ldif"],File["/etc/ldap/replicante2.airfit.es.key"], File["/etc/ldap/replicante2.airfit.es_rapidssl.crt"]],
#    logoutput => true,
#    command => "/usr/bin/ldapmodify -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/tls_config.ldif",
#  }
  

}
