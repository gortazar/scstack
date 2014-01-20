
class scstack::redmine (
  $mysqlpass,
  $redminedb,
  $redminedbuser,
  $redminedbpass,
  $redminekey,
  $domain,
  $compname,
  $codename,
  $bindDN,
  $passBindDN,
  $baseDN,
  $installFolder,
  $redminePackage = "redmine-2.2.2",
){

  $targz = ".tar.gz"
  $redmineURL = "http://rubyforge.org/frs/download.php/76722/$redminePackage$targz"
  
  include mysql
  
  Class["scstack::scstack_ldap"] -> Class["scstack::redmine"]
  Class["scstack::scstack_apache"] -> Class["scstack::redmine"]
  
  class { "mysql::server": 
    config_hash => { 'root_password' => $mysqlpass },
  }

  package {

    "ruby1.9.1":
      require => Exec["apt-get update redmine"],
      ensure => installed;

    "ruby1.9.1-dev":
      require => Exec["apt-get update redmine"],
      ensure => installed;

    "rubygems1.9.1":
      require => Exec["apt-get update redmine"],
      ensure => installed;
  
  }

  exec { "update-alternatives":
    command => "/usr/bin/update-alternatives --install /usr/bin/ruby ruby /usr/bin/ruby1.9.1 400 --slave /usr/share/man/man1/ruby.1.gz ruby.1.gz /usr/share/man/man1/ruby1.9.1.1.gz",
    require => Package["ruby1.9.1", "ruby1.9.1-dev", "rubygems1.9.1"],
  }

  exec { "set-ruby-update-alternatives":
    command => "/usr/bin/update-alternatives --set ruby /usr/bin/ruby1.9.1",
    require => Exec["update-alternatives"],
  }

  exec { "set-gem-update-alternatives":
    command => "/usr/bin/update-alternatives --set gem /usr/bin/gem1.9.1",
    require => Exec["update-alternatives"],
  }

  class { 'mysql::ruby': }

  class { 'mysql::java': }

  exec { "create-${redminedb}-db":
      unless => "/usr/bin/mysql -u${redminedbuser} -p${redminedbpass} ${redminedb}",
      command => "/usr/bin/mysql -uroot -p$mysqlpass -e \"create database ${redminedb}; grant all on ${redminedb}.* to ${redminedbuser}@localhost identified by '$redminedbpass';\"",
      require => Service["mysqld"],
  }

  exec { "apt-get update redmine":
    command => "/usr/bin/apt-get update",
  }

  package { 
    "libmysqlclient-dev":
      require => Exec["apt-get update redmine"],
      ensure => installed;

    "libfcgi-dev":
      require => Exec["apt-get update redmine"],
      ensure => installed;

    "build-essential":
      require => Exec["apt-get update redmine"],
      ensure => installed;

  }

  apache::mod {"fcgid":}

  exec {"download-redmine":
    cwd => "/tmp",
    command => "/usr/bin/wget -c $redmineURL",
    logoutput => true,
  }

  exec { "extract-redmine":
    command => "tar xvzf /tmp/$redminePackage$targz",
    path => ["/bin", "/usr/bin"],
    cwd => "$installFolder",
    require => Exec["download-redmine"],
  }

#  file { "$installFolder/redmine-2.1.2.tar.gz":
#    source => "puppet:///modules/scstack/redmine/redmine-2.1.2.tar.gz",
#  }
#
#  exec { "extract-redmine":
#    command => "tar xvzf $installFolder/redmine-2.1.2.tar.gz",
#    path => ["/bin", "/usr/bin"],
#    cwd => "$installFolder",
#    require => File["$installFolder/redmine-2.1.2.tar.gz"],
#  }

  exec { "rename-redmine":
    command => "/bin/mv $installFolder/$redminePackage $installFolder/redmine",
    require => Exec["extract-redmine"],
  }

 # This is a patch, because rake fails with an error due to a different mysql version installed
 # At some point in time, bundle started to install mysql 2.9.0 instead of mysql 2.8.1
 # This patch forces installation of mysql 2.8.1
 file { "$installFolder/redmine/Gemfile":
   source => "puppet:///modules/scstack/redmine/Gemfile",
   require => Exec["rename-redmine"],
 }

  file { "$installFolder/redmine/config/database.yml":
    content => template('scstack/redmine/database.yml.erb'),
    require => Exec["rename-redmine"],
  }

  package { "bundler":
    provider => "gem",
    ensure => "1.2.3",
    before => Exec["redmine-install"],
    require => Exec["set-gem-update-alternatives"],
  }

  package { "rake":
    provider => "gem",
    ensure => installed,
    before => Exec["redmine-install"],
    require => Exec["set-gem-update-alternatives"],
  }
  
  package { "fcgi":
    provider => gem,
    ensure => installed,
    require => Package["rake"],
  }

  exec { "redmine-install": 
    cwd => "$installFolder/redmine",
    logoutput => true,
    command => "/usr/local/bin/bundle install --without development test rmagick postgresql sqlite",
    # environment => "PATH=/opt/vagrant_ruby/bin:/usr/bin"
    environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/vagrant_ruby/bin",
    # path => ["/opt/vagrant_ruby/bin", "/usr/bin"],
    # path => ["/usr/local/bin"],
    require => [
      Exec["rename-redmine"], 
      Package["libmysqlclient-dev"], 
      Package["libfcgi-dev"], 
      Package["libapache2-mod-fcgid"], 
      Package["build-essential"],
      File["$installFolder/redmine/Gemfile"]],
  }

  exec { "generate_secret_token":
    cwd => "$installFolder/redmine",
    command => "rake generate_secret_token",
    # path => ["/opt/vagrant_ruby/bin"],
    path => ["/usr/local/bin"],
    # environment => "PATH=/opt/vagrant_ruby/bin",
    require => Exec["redmine-install"],
  }

  exec { "redmine-db-init":
    cwd => "$installFolder/redmine",
    # environment => ["RAILS_ENV=production", "PATH=/opt/vagrant_ruby/bin:/usr/bin:/bin"],
    environment => ["RAILS_ENV=production"],
    command => "rake db:migrate",
    logoutput => true,
    path => ["/usr/local/bin"],
    # path => ["/opt/vagrant_ruby/bin"],
    require => [File["$installFolder/redmine/config/database.yml"], Exec["create-${redminedb}-db"], Exec["generate_secret_token"]],
  }

  exec { "redmine-load-data":
    cwd => "$installFolder/redmine",
    # environment => ["RAILS_ENV=production", "REDMINE_LANG=en", "PATH=/opt/vagrant_ruby/bin"],
    environment => ["RAILS_ENV=production", "REDMINE_LANG=en"],
    command => "rake redmine:load_default_data",
    # path => ["/opt/vagrant_ruby/bin"],
    path => ["/usr/local/bin"],
    require => Exec["redmine-db-init"],
  }
  
#  exec {"delete-Gemfile.local":
#    cwd => "$installFolder/redmine",
#    command => "/bin/rm $installFolder/redmine/Gemfile.local",
#    logoutput => true,
#    require => Exec["redmine-load-data"],
#  }
  
  file {"/tmp/redmine-ldap.sql":
    content => template('scstack/redmine/redmine-ldap.sql.erb'),
  }

  if !empty($redminedbpass) {
    exec {"mysql-redmine-ldap-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser -p$redminedbpass < /tmp/redmine-ldap.sql",
      require => [File["/tmp/redmine-ldap.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }
  } else {
    exec {"mysql-redmine-ldap-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser < /tmp/redmine-ldap.sql",
      require => [File["/tmp/redmine-ldap.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }
  }

  file {"/tmp/redmine-settings.sql":
    content => template('scstack/redmine/redmine-settings.sql.erb'),
  }

  if !empty($redminedbpass) {
    exec {"mysql-redmine-settings-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser -p$redminedbpass < /tmp/redmine-settings.sql",
      require => [File["/tmp/redmine-settings.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }
  } else {
    exec {"mysql-redmine-settings-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser < /tmp/redmine-settings.sql",
      require => [File["/tmp/redmine-settings.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }
  }

  file {"/tmp/redmine-apikey.sql":
    content => template('scstack/redmine/redmine-apikey.sql.erb'),
  }

  if !empty($redminedbpass) {
    exec {"mysql-redmine-apikey-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser -p$redminedbpass < /tmp/redmine-apikey.sql",
      require => [File["/tmp/redmine-apikey.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }    
  } else {
    exec {"mysql-redmine-apikey-setup":
      cwd => "$installFolder/redmine",
      command => "/usr/bin/mysql -u$redminedbuser < /tmp/redmine-apikey.sql",
      require => [File["/tmp/redmine-apikey.sql"],Class["mysql"], Exec["redmine-load-data"]],
    }
  }
  
  file { "$installFolder/redmine/tmp":
    ensure => directory,
    owner => www-data,
    group => www-data,
    mode => 0755,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/.htaccess"],
  }

  file { "$installFolder/redmine/tmp/pdf":
    ensure => directory,
    owner => www-data,
    group => www-data,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/Gemfile.lock"],
  }

  file { "$installFolder/redmine/Gemfile.lock":
    ensure => file,
    owner => www-data,
    group => www-data,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/plugin_assets"],
  }
  
  file { "$installFolder/redmine/public/plugin_assets":
    ensure => directory,
    owner => www-data,
    group => www-data,
    mode => 0755,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/.htaccess"],
  }

  file { "$installFolder/redmine/files":
    ensure => directory,
    owner => www-data,
    group => www-data,
    mode => 0755,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/.htaccess"],
  }

  file { "$installFolder/redmine/log":
    ensure => directory,
    owner => www-data,
    group => www-data,
    mode => 0766,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/.htaccess"],
  }

  file { "$installFolder/redmine/log/production.log":
    ensure => present,
    owner => www-data,
    group => www-data,
    mode => 0766,
    require => Exec["redmine-load-data"],
    before => File["$installFolder/redmine/public/.htaccess"],
  }

  file { "$installFolder/redmine/config/environment.rb":
    replace => true,
    mode => 0755,
    source => "puppet:///modules/scstack/redmine/environment.rb",
    require => Exec["redmine-load-data"],
  }

  file { "$installFolder/redmine/public/dispatch.fcgi":
    content => template("scstack/redmine/dispatch.fcgi.erb"),
    mode => 0755,
    require => Exec["redmine-load-data"],
  }

  file { "$installFolder/redmine/public/.htaccess":
    ensure => present,
    source => "puppet:///modules/scstack/redmine/.htaccess",
    owner => www-data,
    group => www-data,
    mode => 0700,
    require => [File["$installFolder/redmine/config/environment.rb"],File["$installFolder/redmine/public/dispatch.fcgi"]],
    notify => Service['httpd'],
  }

# Install Postfix to enable mail sending in Redmine

  file { "/var/cache/debconf/postfix.preseed":
    content => template('scstack/redmine/postfix.preseed.erb'),
    require => File["$installFolder/redmine/public/.htaccess"],
  }

  exec { "apt-get update postfix":
    command => "/usr/bin/apt-get update",
  }

  package { "postfix":
    ensure => installed,
    responsefile => "/var/cache/debconf/postfix.preseed",
    require => [Exec["apt-get update postfix"],File["/var/cache/debconf/postfix.preseed"]],
  }

  file { "$installFolder/redmine/config/configuration.yml":
    source => "puppet:///modules/scstack/redmine/configuration.yml",
    require => Package["postfix"],
    notify => Service['httpd'],
  }
 
  
  # Install redmine plugin for mylyn
  
  file { "/tmp/redmine_mylyn_connector-stable-2.8.tar.gz":
    source => "puppet:///modules/scstack/redmine/redmine_mylyn_connector-stable-2.8.tar.gz",
  }
  
  exec {"extract-connector":
    cwd => "/tmp",
    command => "/bin/tar -xvzf redmine_mylyn_connector-stable-2.8.tar.gz",
    require => File["/tmp/redmine_mylyn_connector-stable-2.8.tar.gz"],
  }
  
  exec {"move-connector":
    cwd => "/tmp",
    command => "/bin/mv redmine_mylyn_connector-stable-2.8 $installFolder/redmine/plugins/redmine_mylyn_connector",
    require => [Exec["extract-connector"],Exec["rename-redmine"]],
  }
  
  file {"$installFolder/redmine/plugins/redmine_mylyn_connector":
    owner => www-data,
    group => www-data,
    recurse => true,
    require => Exec["move-connector"],
  }
    
#  exec {"clone-redmine-mylyn-connector":
#    cwd => "$installFolder/redmine/plugins",
#    require => [Package["git"],File["$installFolder/redmine/public/.htaccess"]],
#    command => "/usr/bin/git clone git://github.com/danmunn/redmine_mylyn_connector.git",
#  }
  
  exec {"migrate-plugin":
    cwd => "$installFolder/redmine",
    #require => Exec["clone-redmine-mylyn-connector"],
    require => [File["$installFolder/redmine/plugins/redmine_mylyn_connector"]],
    environment => ["RAILS_ENV=production"],
    command => "rake db:migrate_plugins",
    logoutput => true,
    # path => ["/opt/vagrant_ruby/bin"],
    path => ["/usr/local/bin"],
  }
  
  exec {"bundle-install":
    cwd => "$installFolder/redmine",
    logoutput => true,
    require => Exec["migrate-plugin"],
    command => "/usr/local/bin/bundle install --without development test rmagick postgresql sqlite",
    #environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/vagrant_ruby/bin",
    environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games",
  }

}
