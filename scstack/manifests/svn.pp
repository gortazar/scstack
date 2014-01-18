class scstack::svn {
  
  Class["scstack::scstack_apache"] -> Class["scstack::svn"]
  
  package { "libapache2-svn": 
    ensure => present,
  }

  package { "subversion": 
    ensure => present,
  }

  package { "subversion-tools": 
    ensure => present,
  }

}
