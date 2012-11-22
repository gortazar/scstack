class scstack::scstack_apache(
  $domain,
  $ip,
  $cert_apache,
  $key_apache,
  $cacert_apache,
  $adminmail,
  $installFolder
) {
    
  include apache

#  apache::mod {"ssl":}
  apache::mod {"ldap":}
  apache::mod {"authnz_ldap":}
  apache::mod {"rewrite":}
  apache::mod {"proxy":}
  apache::mod {"proxy_http":}
  apache::mod {"ssl":}

#  apache::vhost {$domain:
#    port => "80",
#    docroot => "/var/redmine/public",
#    options => "Indexes ExecCGI FollowSymLinks",
#    override => ["All"],
#  }
  
  file { "/etc/hosts":
    content => template('scstack/apache/hosts.erb'),
  }

  file { "/etc/apache2/ports.conf":
    content => template('scstack/apache/ports.conf.erb'),
  }

  file { "/etc/apache2/sites-available/configProjects":
    ensure => file,
    require => Package['httpd'],
  }

  file { "/etc/apache2/sites-available/configProjects-ssl":
    ensure => file,
    require => Package['httpd'],
  }

  file { "/etc/ssl/certs/$cert_apache":
    source => "puppet:///modules/scstack/apache/$cert_apache",
    require => Package['httpd'],
  }

  file { "/etc/ssl/private/$key_apache":
    source => "puppet:///modules/scstack/apache/$key_apache",
    require => Package['httpd'],
  }

  file { "/etc/ssl/certs/$cacert_apache":
    source => "puppet:///modules/scstack/apache/$cacert_apache",
    require => Package['httpd'],
  }

  exec { "disable-other-hosts":
    command => "/bin/rm /etc/apache2/sites-enabled/000-default",
    onlyif => "/usr/bin/test -f /etc/apache2/sites-enabled/000-default",
    require => Package['httpd'],
  }
  
  file { "/etc/apache2/sites-available/code.tscompany.es":
    replace => true,
    content => template('scstack/apache/code.tscompany.es.erb'),
    require => [Exec["disable-other-hosts"],File["/etc/apache2/sites-available/configProjects"],File["/etc/apache2/sites-available/configProjects-ssl"]],
  }

  file { "/etc/apache2/sites-available/code.tscompany.es-ssl":
    replace => true,
    content => template('scstack/apache/code.tscompany.es-ssl.erb'),
    require => [
  Exec["disable-other-hosts"],
  File["/etc/apache2/sites-available/configProjects"],
  File["/etc/apache2/sites-available/configProjects-ssl"],
  File["/etc/ssl/certs/$cert_apache"],
  File["/etc/ssl/private/$key_apache"],
  File["/etc/ssl/certs/$cacert_apache"]],
  }

  exec { "enable-default-host":
    command => "/usr/sbin/a2ensite code.tscompany.es",
    notify => Service['httpd'],
    require => [File["/etc/apache2/sites-available/code.tscompany.es"],Package['httpd']],
  }

  exec { "enable-ssl-host":
    command => "/usr/sbin/a2ensite code.tscompany.es-ssl",
    notify => Service['httpd'],
    require => [File["/etc/apache2/sites-available/code.tscompany.es-ssl"],Package['httpd']],
  }

  
}
