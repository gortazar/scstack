# Class: scstack
#
# This module manages scstack, the puppet-based installation of SidelabCode Stack
#
# Parameters: 
#
# Actions:
#
# Requires: see Modulefile
#
# Sample Usage:
#
class scstack(
  $sadmin = "sadmin",
  $sadminpass,
  $ip,
  $domain,
  $baseDN,
  $passBindDN="re@lity45",
  # MySQL info
  $mysqlpass = "re@lity45",
  $redminedb = "redminedb",
  $redminedbuser = "redminedbuser",
  $redminedbpass = "r3dm1n3",
  $archivadbpass = "t0rc0zu310", 
  $gerritdbpass = "t0rc0zu310",
  $gerritAdminPass = "t0rc0zu310",
  # Generic info (used in Redmine, for instance)
  $compname = "SidelabCode Stack",
  $codename = "Code",
  $rubygemsProxy = "",
) {
    
  # LDAP params
#  $baseDN="dc=scstack,dc=sidelab,dc=es"
  $cnBindDN="cn=admin"
  $bindDN="cn=admin,${baseDN}"
  $passBindDNMD5=md5($passBindDN)
  $searchDN="ou=people,${baseDN}"
  $searchProjectDN="ou=projects,${baseDN}"
  $ouPeople = "people"
  $ouProjects = "projects"
  
  # Apache certificates
  $cert_apache = "code.tscompany.es.server.cnf-crt.pem"
  $key_apache = "code.tscompany.es.server.cnf-key.pem"
  $cacert_apache = "code.tscompany.es-cacert.pem"
  
  # Folder structure
  $installFolder = "/opt"
  $filesPath="$installFolder/files"
  $privateFolder="$filesPath/private"
  $publicFolder="$filesPath/public"
  $svnFolder="$installFolder/svn"
  $svnSubURI="/svn"

  $adminmail = "admin@$domain"
  
  # MySQL params
  $old_pw = ''
  
  # Redmine params
  $redminekey = "bc8f104416bbb259a87d411ad0228adafa4d348a"

  file { $installFolder:
    ensure => directory,
  }
  
  file { $filesPath:
    ensure => directory,  
  }
  
  file { $privateFolder:
    ensure => directory,  
  }

  file { $publicFolder:
    ensure => directory,  
  }
  
  file { $svnFolder:
    ensure => directory,
  }
  
  class { "scstack::openjdk": }

  class { "scstack::scstack_ldap": 
    baseDN => $baseDN,
    bindDN => $bindDN,
    cnBindDN => $cnBindDN,
    passBindDN => $passBindDN,
    searchDN => $searchDN,
    searchProjectDN => $searchProjectDN,
    domain => $domain,
    sadmin => $sadmin,
    sadminpass => $sadminpass,
  }

# Install SSH and configure sftp server and ldap-based authentication
# The ssh module has been modified to allow jailed sftp access

  class { "scstack::scstack_ssh":
    baseDN => $baseDN, 
    bindDN => $bindDN,
    passBindDN => $passBindDN,
    searchDN => $searchDN,
    searchProjectDN => $searchProjectDN
  }

# Install Apache

  class { "scstack::scstack_apache": 
    domain => $domain,
    ip => $ip,
    cert_apache => $cert_apache,
    key_apache => $key_apache,
    cacert_apache => $cacert_apache,
    adminmail => $adminmail,
    installFolder => $installFolder
  }
  
# Install Redmine

  class {"scstack::redmine": 
    mysqlpass => $mysqlpass,
    redminedb => $redminedb,
    redminedbuser => $redminedbuser,
    redminedbpass => $redminedbpass,
    redminekey => $redminekey,
    domain => $domain,
    codename => $codename,
    compname => $compname,
    baseDN => $baseDN,
    bindDN => $bindDN,
    passBindDN => $passBindDN,
    installFolder => $installFolder,
    rubygemsProxy => $rubygemsProxy,
  }

# Install Redmine WYSIWYG plugin

  class { "puppet_plugins_redmine":
    installFolder => $installFolder,
    require       => Class["scstack::redmine"],
  }

  # Touch file redmine restart.
  exec { "touch-restart-redmine":
    command => "/usr/bin/touch $installFolder/redmine/tmp/restart.txt",
    require => Class["puppet_plugins_redmine"],
    notify  => Service['httpd'],
  }

# Install svn
 
  class {"scstack::svn": }
  
# Install tomcat, Jenkins, Archiva and Gerrit
  
  class {"scstack::tomcat":
    domain => $domain,
    mysqlrootpass => $mysqlpass,
    archivadbpass => $archivadbpass,
    gerritdbpass => $gerritdbpass,
    gerritAdminPass => $gerritAdminPass,
    bindDN => $bindDN,
    passBindDN => $passBindDN,
    searchDN => $searchDN,
    searchProjectDN => $searchProjectDN,
    ip => $ip,
    installFolder => $installFolder,
    # Redmine configura MySQL:
    require => Class["scstack::redmine"], 
  }
  
  class {"scstack::scstack_service":
    installFolder => $installFolder,
    bindDN => $bindDN,
    passBindDN => $passBindDN,
    ouProjects => $ouProjects,
    ouPeople => $ouPeople,
    sadmin => $sadmin,
    sadminpass => $sadminpass,
    filesPath => $filesPath,
    privateFolder => $privateFolder,
    publicFolder => $publicFolder,
    svnFolder => $svnFolder,
    svnSubURI => $svnSubURI,
    mysqlpass => $mysqlpass,
    redminedb => $redminedb,
    redminekey => $redminekey,
    require => [Class["scstack::redmine"], Class["scstack::scstack_ldap"]],
  } 

  file {
      '/opt/backup':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/mysql':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/ldap':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/svn':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/sftp-files':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/redmine':
          ensure  => directory,
          owner   => 'root',
          group   => 'root',
          mode    => '0660';
  }

  file {
      '/opt/backup/backup.sh':
          require => File["/opt/backup"],
          ensure  => file,
          content  => template('scstack/backup/backup.sh.erb'),
          owner   => 'root',
          group   => 'root',
          mode    => '0550';
  }

  cron {
      'run-backup':
          ensure   => present,
          command  => '/opt/backup/backup.sh',
          user     => 'root',
          month    => '*',
          monthday => '*',
          hour     => '2',
          minute   => '0',
          #provider => crontab;
  }
}

