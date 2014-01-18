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

  package { "openjdk-7-jdk":
    ensure => installed,
  }
  
}
