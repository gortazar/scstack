# Class: scstack::tomcat
#
# This module manages tomcat
#
# Parameters:
#   $tomcat apache file name (should end up in tar.gz)
#   $tomcatdir apache tomcat dir after uncompressing
#   $javahome JAVA_HOME used to build the daemon
#
# Actions:
#
# Requires: see Modulefile
#
# Sample Usage:
#
class scstack::tomcat (
  $tomcatfile = "apache-tomcat-7.0.32.tar.gz",
  $tomcatdir  = "apache-tomcat-7.0.32",
  $javahome   = "/usr/lib/jvm/java-7-openjdk-amd64",
  $mysqlrootpass,
  $archivadbpass,
  $gerritdbpass,
  $gerritAdminPass,
  $domain,
  $bindDN,
  $passBindDN,
  $searchDN,
  $searchProjectDN,
  $ip,
  $installFolder) {
  #  file { "$installFolder/$tomcatfile":
  #    source => "puppet:///modules/scstack/tomcat/$tomcatfile",
  #  }

  exec { "download-tomcat":
    cwd       => "$installFolder",
    command   => "/usr/bin/wget http://archive.apache.org/dist/tomcat/tomcat-7/v7.0.32/bin/$tomcatfile",
    logoutput => true,
  }

  exec { "unzip-tomcat":
    cwd     => "$installFolder",
    command => "/bin/tar xvzf $tomcatfile",
    #    require => File["$installFolder/$tomcatfile"],
    require => Exec["download-tomcat"],
  }

  file { "$installFolder/$tomcatdir":
    mode    => 775,
    owner   => "tomcat",
    group   => "tomcat",
    recurse => true,
    require => [Exec["unzip-tomcat"], User["tomcat"]],
  }

  exec { "uncompress-daemon-src":
    cwd     => "$installFolder/$tomcatdir/bin",
    command => "/bin/tar xvzf commons-daemon-native.tar.gz",
    require => Exec["unzip-tomcat"],
  }

  # Package build-essential should already be installed. Redmine installs it so it is ok to run this after Redmine installation.
  exec { "configure-build-daemon":
    cwd     => "$installFolder/$tomcatdir/bin/commons-daemon-1.0.10-native-src/unix",
    command => "$installFolder/$tomcatdir/bin/commons-daemon-1.0.10-native-src/unix/configure --with-java=$javahome",
    require => Exec["uncompress-daemon-src"],
  }

  exec { "make-build-daemon":
    cwd     => "$installFolder/$tomcatdir/bin/commons-daemon-1.0.10-native-src/unix",
    command => "/usr/bin/make",
    require => Exec["configure-build-daemon"],
  }

  exec { "copy-jsvc":
    cwd     => "$installFolder/$tomcatdir/bin/commons-daemon-1.0.10-native-src/unix",
    command => "/bin/cp jsvc ../..",
    require => Exec["make-build-daemon"],
  }

  file { "/etc/init.d/tomcat":
    content => template("scstack/tomcat/tomcat.erb"),
    mode    => 0755,
    owner   => "root",
    group   => "root",
  }

  group { "tomcat": gid => 6001, }

  user { "tomcat":
    shell   => "/bin/false",
    uid     => 6001,
    gid     => "tomcat",
    home    => "/home/tomcat",
    require => Group["tomcat"],
  }

  file { "/home/tomcat":
    ensure  => directory,
    owner   => "tomcat",
    group   => "tomcat",
    mode    => 750,
    require => [User["tomcat"], Group["tomcat"]],
  }

  file { "/home/tomcat/.ssh":
    ensure  => directory,
    owner   => "tomcat",
    group   => "tomcat",
    mode    => 700,
    require => File["/home/tomcat/"],
  }

  #  file { "/opt/tomcat":
  #    target => "/opt/$tomcatdir",
  #    mode => 775,
  #    require => File["/opt/$tomcatdir"],
  #  }

  file { "$installFolder/tomcat":
    target  => "$installFolder/$tomcatdir",
    mode    => 0775,
    owner   => "tomcat",
    group   => "tomcat",
    ensure => link,
    require => [File["$installFolder/$tomcatdir"], User["tomcat"]],
  }

  #  exec {"start-tomcat":
  #    cwd => "/etc/init.d",
  #    command => "/usr/sbin/service tomcat start",
  #    require => [Exec["copy-jsvc"], File["/etc/init.d/tomcat"], User["tomcat"]],
  #  }

  file { "$installFolder/tomcat/conf/tomcat-users.xml":
    source  => "puppet:///modules/scstack/tomcat/tomcat-users.xml",
    owner   => "tomcat",
    group   => "tomcat",
    mode    => 0750,
    require => [File["$installFolder/tomcat"], User["tomcat"]],
  }

  file { "$installFolder/tomcat/conf/server.xml":
    source  => "puppet:///modules/scstack/tomcat/server.xml",
    owner   => "tomcat",
    group   => "tomcat",
    mode    => 0750,
    require => [File["$installFolder/tomcat"], User["tomcat"]],
  }

  service { "tomcat":
    ensure  => running,
    enable  => true, 
    require => [
      File["$installFolder/tomcat/conf/tomcat-users.xml"],
      File["/etc/init.d/tomcat"],
      Exec["copy-jsvc"],
      File["$installFolder/tomcat"],
      File["$installFolder/tomcat/conf/server.xml"]],
  }

  ##########################
  # Deploy archiva on Tomcat
  ##########################

  $archiva = "apache-archiva-1.3.5.war"
  $archivashort = "archiva.war"

  exec { "download-archiva":
    cwd       => "/opt",
    command   => "/usr/bin/wget http://archive.apache.org/dist/archiva/binaries/$archiva",
    logoutput => true,
    require   => File["/opt/tomcat"],
  }

  file { "$installFolder/archiva-db-setup.sql": content => template('scstack/tomcat/archiva-db-setup.sql.erb'), }

  exec { "mysql-archiva-setup":
    cwd     => "$installFolder",
    command => "/usr/bin/mysql -uroot -p$mysqlrootpass < archiva-db-setup.sql",
    require => [File["$installFolder/archiva-db-setup.sql"], Class["mysql"]],
  }

  file { "$installFolder/tomcat/lib/mysql-connector-java-5.1.22-bin.jar":
    source  => "puppet:///modules/scstack/tomcat/mysql-connector-java-5.1.22-bin.jar",
    owner   => "tomcat",
    group   => "tomcat",
    require => File["$installFolder/tomcat"],
  }

  exec { "rename-archiva":
    cwd     => "$installFolder",
    command => "/bin/mv $archiva $archivashort",
    #    require => File["$installFolder/$archiva"],
    require => Exec["download-archiva"],
  }

  file { "$installFolder/tomcat/conf/Catalina":
    ensure => directory,
    owner  => "tomcat",
    group  => "tomcat",
  }

  file { "$installFolder/tomcat/conf/Catalina/localhost":
    ensure  => directory,
    owner   => "tomcat",
    group   => "tomcat",
    require => File["$installFolder/tomcat/conf/Catalina"],
  }

  file { "$installFolder/tomcat/conf/Catalina/localhost/archiva.xml":
    content => template('scstack/tomcat/archiva.xml.erb'),
    require => File["$installFolder/tomcat/conf/Catalina/localhost"],
  }

  exec { "copy-archiva":
    cwd     => "$installFolder",
    command => "/bin/mv $archivashort $installFolder/tomcat/webapps/",
    require => [
      Exec["rename-archiva"],
      File["$installFolder/tomcat"],
      #      File["$installFolder/tomcat/lib/derby-10.1.3.1.jar"],
      File["$installFolder/tomcat/lib/activation-1.1.jar"],
      File["$installFolder/tomcat/lib/mail-1.4.jar"],
      Exec["mysql-archiva-setup"],
      File["$installFolder/tomcat/lib/mysql-connector-java-5.1.22-bin.jar"],
      File["$installFolder/tomcat/conf/Catalina/localhost/archiva.xml"]],
  }

  #  file { "$installFolder/tomcat/lib/derby-10.1.3.1.jar":
  #    source => "puppet:///modules/scstack/tomcat/derby-10.1.3.1.jar",
  #    owner => "tomcat",
  #    group => "tomcat",
  #  }

  file { "$installFolder/tomcat/lib/activation-1.1.jar":
    source => "puppet:///modules/scstack/tomcat/activation-1.1.jar",
    owner  => "tomcat",
    group  => "tomcat",
  }

  file { "$installFolder/tomcat/lib/mail-1.4.jar":
    source => "puppet:///modules/scstack/tomcat/mail-1.4.jar",
    owner  => "tomcat",
    group  => "tomcat",
  }

  ##########################
  # Deploy Jenkins on Tomcat
  ##########################

  $jenkins = "jenkins.war"

  exec { "download-jenkins":
    cwd       => "$installFolder",
    command   => "/usr/bin/wget http://mirrors.jenkins-ci.org/war/1.491/jenkins.war",
    logoutput => true,
  }

  #  file { "$installFolder/$jenkins":
  #    source => "puppet:///modules/scstack/tomcat/$jenkins",
  #  }

  file { "$installFolder/jenkins":
    ensure => directory,
    owner  => "tomcat",
    group  => "tomcat",
  }

  file { "$installFolder/tomcat/conf/Catalina/localhost/jenkins.xml":
    content => template('scstack/tomcat/jenkins.xml.erb'),
    require => File["$installFolder/tomcat/conf/Catalina/localhost"],
  }

  file { "$installFolder/jenkins/config.xml":
    content => template('scstack/tomcat/config.xml.erb'),
    require => File["$installFolder/jenkins"],
  }

  exec { "copy-jenkins":
    cwd     => "$installFolder",
    command => "/bin/mv $jenkins $installFolder/tomcat/webapps/",
    require => [
      File["$installFolder/tomcat"],
      #      File["$installFolder/$jenkins"],
      Exec["download-jenkins"],
      File["$installFolder/jenkins"],
      File["$installFolder/jenkins/config.xml"],
      File["$installFolder/tomcat/conf/Catalina/localhost/jenkins.xml"],
      Exec["copy-archiva"]],
    notify  => Service["tomcat"],
    before  => Package["git"],
  }

  ##########################
  # Deploy gerrit on Tomcat
  ##########################

  $gerrit = "gerrit-full-2.5.war"

  file { "/etc/ldap/gerritadmin.ldif":
    content => template("scstack/tomcat/gerritadmin.ldif.erb"),
  }
  
  exec { "ldapadd gerritadmin":
    require => [File["/etc/ldap/gerritadmin.ldif"],Service["slapd"]],
    logoutput => true,
    command => "/usr/bin/ldapadd -x -D ${bindDN} -w ${passBindDN} -f /etc/ldap/gerritadmin.ldif",
  }
  
  exec { "rm-gerritadmin.ldif":
    cwd => "/etc/ldap",
    command => "/bin/rm gerritadmin.ldif",
    require => Exec["ldapadd gerritadmin"],
  }

  package { "git": ensure => installed, }

  file { "$installFolder/gerrit-db-setup.sql": content => template('scstack/tomcat/gerrit-db-setup.sql.erb'), }

  exec { "mysql-gerrit-setup":
    cwd     => "$installFolder",
    command => "/usr/bin/mysql -uroot -p$mysqlrootpass < gerrit-db-setup.sql",
    require => [File["$installFolder/gerrit-db-setup.sql"], Class["mysql"]],
  }
  
  file { "/opt/ssh-keys/": 
    ensure => directory,
    owner  => "tomcat",
    group  => "tomcat",
  }

  exec { "exec gerritadmin-ssh-key":
    cwd => "$installFolder/ssh-keys",
    command => "/usr/bin/ssh-keygen -t rsa -C 'gerritadmin@$domain' -N '' -f /opt/ssh-keys/gerritadmin_rsa",
    creates => ["/opt/ssh-keys/gerritadmin_rsa", "/opt/ssh-keys/gerritadmin_rsa.pub"],
  }
   
  # SSH Config parameters 
  $gerritadmin = 'gerritadmin'
  $gerritadminssh = '/opt/ssh-keys/gerritadmin_rsa'
  
  file { "/root/.ssh/": 
    ensure => directory,
    owner  => "root",
    group  => "root",
  }

  file {"/root/.ssh/config": 
    content => template('scstack/ssh/config.erb'),
    require => [File["/opt/ssh-keys/"], Exec["exec gerritadmin-ssh-key"], File["/root/.ssh/"]],
  }

  file {"$installFolder/gerrit-db-admin-setup.sql": 
    content => template('scstack/tomcat/gerrit-db-admin-setup.sql.erb'),
    require => [File["/opt/ssh-keys/"], File["/root/.ssh/config"]],
  }
  
  exec { "mysql-gerrit-admin-setup":
    cwd => "$installFolder",
    command => "/usr/bin/mysql -u root -p$mysqlrootpass < gerrit-db-admin-setup.sql",
    require => [Exec["mysql-gerrit-setup"],File["$installFolder/gerrit-db-admin-setup.sql"]],
  }

  file { "$installFolder/gerrit": 
    ensure => directory,
    owner  => "tomcat",
    group  => "tomcat",
  }

  file { "$installFolder/gerrit/etc":  
    ensure => directory,
    owner  => "tomcat",
    group  => "tomcat",
  }

  file { "$installFolder/gerrit/etc/gerrit.config":
    content => template('scstack/tomcat/gerrit.config.erb'),
    require => File["$installFolder/gerrit/etc"],
  }

  file { "$installFolder/gerrit/etc/secure.config":
    content => template('scstack/tomcat/secure.config.erb'),
    require => File["$installFolder/gerrit/etc"],
  }

  exec { "download-gerrit":
    cwd       => "/opt",
    command   => "/usr/bin/wget http://gerrit.googlecode.com/files/gerrit-full-2.5.war",
    logoutput => true,
  }

  file { "$installFolder/gerrit-init.sh":
    source => "puppet:///modules/scstack/tomcat/gerrit-init.sh",
    mode   => 0750,
  }

  exec { "gerrit-init":
    cwd     => "$installFolder",
    command => "$installFolder/gerrit-init.sh $javahome/bin/java $gerrit $installFolder/gerrit",
    require => [
      Exec["download-gerrit"],
      Package["git"],
      File["$installFolder/gerrit-init.sh"],
      File["$installFolder/gerrit/etc/gerrit.config"],
      File["$installFolder/gerrit/etc/secure.config"]],
  }

  file { "/etc/init/gerrit.conf": content => template("scstack/tomcat/gerrit.conf.erb"), }

  service { "gerrit":
    ensure  => running,
    require => [File["/etc/init/gerrit.conf"], Exec["gerrit-init"]],
  }

}
