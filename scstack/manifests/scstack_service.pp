class scstack::scstack_service(
  $installFolder,
  $bindDN,
  $passBindDN,
  $ouProjects,
  $ouPeople,
  $sadmin,
  $sadminpass,
  $filesPath,
  $privateFolder,
  $publicFolder,
  $svnFolder,
  $svnSubURI,
  $mysqlpass,
  $redminedb,
  $redminekey
){
  
# Since code.sidelab.es is down, we need to use the embedded binary
#  exec { "get-scstack-service":
#    cwd => "/tmp",
#    command => "/usr/bin/wget -c  http://code.sidelab.es/public/sidelabcodestack/artifacts/0.4/scstack-service-bin.tar.gz",
#    logoutput => true,
#  }
  
 file { "/tmp/scstack-service-bin.tar.gz":
   source => "puppet:///modules/scstack/service/scstack-service-bin.tar.gz",
 }
  
  exec { "unzip-scstack-service":
    cwd => "/tmp",
    command => "/bin/tar -xvzf scstack-service-bin.tar.gz",
#    require => File["/tmp/scstack-service-bin.tar.gz"],
    require => File["/tmp/scstack-service-bin.tar.gz"],
  }

  exec { "cp-scstack-service":
    cwd => "$installFolder",
    command => "/bin/cp -R /tmp/scstack-service $installFolder",
    require => Exec["unzip-scstack-service"],
  }
    
  file { "$installFolder/scstack-service/scstack.conf":
    content => template("scstack/service/scstack.conf.erb"),
    require => Exec["cp-scstack-service"],
  }
  
  file { "/etc/init.d/scstack-service":
    content => template("scstack/service/scstack-service.erb"),
    mode => 750,
    require => Exec["cp-scstack-service"],
  }
  
  file {"/etc/init/scstack-service.conf":
    content => template("scstack/service/scstack-service.conf.erb"),
    require => Exec["cp-scstack-service"],
  }
  
  service { "scstack-service":
    ensure => running,
    provider => "upstart",
    require => [
      File["/etc/init.d/scstack-service"],
      File["/etc/init/scstack-service.conf"],
      Class["scstack::scstack_ldap"],
      Class["scstack::scstack_ssh"],
    ]
  }
  
}