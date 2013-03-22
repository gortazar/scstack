class scstack::svn {
  
  Class["scstack::scstack_apache"] -> Class["scstack::svn"]
  
  exec { "apt-get update svn":
    command => "/usr/bin/apt-get update",
  }

  package { "libapache2-svn": 
    require => Exec["apt-get update svn"],
    ensure => present,
  }

  package { "subversion": 
    require => Exec["apt-get update svn"],
    ensure => present,
  }

  package { "subversion-tools": 
    require => Exec["apt-get update svn"],
    ensure => present,
  }

}
