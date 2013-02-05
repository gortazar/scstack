# Class: scstack-ssh
#
# This module manages scstack-ssh
#
# Parameters: required parameters for configuring ldap connection when installing libnss-ldap.
#
# Actions:
#
# Requires: see Modulefile
#
# Sample Usage:
#
class scstack::scstack_ssh (
  $baseDN,
  $bindDN,
  $passBindDN,
  $searchDN,
  $searchProjectDN
) {

  include ssh

  file {"/var/cache/debconf/libnss-ldap.preseed":
    ensure => present,
    mode => 600,
    content => template('scstack/ssh/libnss-ldap.preseed.erb'),
    before => Package["libnss-ldap"],
  }

  package { "libnss-ldap":
    ensure => present,
    responsefile => "/var/cache/debconf/libnss-ldap.preseed",
    require => File["/var/cache/debconf/libnss-ldap.preseed"],
    before => [File["/etc/nsswitch.conf"],File["/etc/pam.d/sshd"]],
  }

  file { "/etc/nsswitch.conf":
    ensure => present,
    source => "puppet:///modules/scstack/ssh/nsswitch.conf",
    notify => Service[$ssh::params::service_name],
  }

  file { "/etc/pam.d/sshd":
    ensure => present,
    source => "puppet:///modules/scstack/ssh/sshd",
    notify => Service[$ssh::params::service_name],
  }
  
  file {"/var/cache/debconf/nslcd.preseed":
    content => template('scstack/ssh/nslcd.preseed.erb'),
  }
  
  package {"nslcd":
    ensure => installed,
    responsefile => "/var/cache/debconf/nslcd.preseed",
    require => [File["/var/cache/debconf/nslcd.preseed"],Class["ldap"],Package["libnss-ldap"]],
  }

  file { "/etc/ldap.conf":
    ensure => file,
    content => template('scstack/ssh/ldap.conf.erb'),
    require => Package["nslcd"],
  }

  file { "/etc/ssh/sshd_config.scstak":
    source => "puppet:///modules/scstack/ssh/sshd_config",
    require => Package[$ssh::params::server_package_name],
  }
  
  exec { "rename-sshd_config":
    cwd => "/etc/ssh",
    require => File["/etc/ssh/sshd_config.scstak"],
    command => "/bin/mv sshd_config.scstak sshd_config",
    notify => Service[$ssh::params::service_name],
  }
  

}
