# Class: openjdk
#
# This module manages openjdk
#
# Parameters: none
#
# Actions:
#
# Requires: see Modulefile
#
# Sample Usage:
#
class scstack::openjdk {

  exec { "apt-get update":
    command => "/usr/bin/apt-get update",
  }
  
  package { "openjdk-7-jdk":
    ensure => installed,
    require => Exec["apt-get update"],
  }
  
}
