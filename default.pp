# Si hemos configardo apt-cacher como proxy, añadir las siguientes líneas:
#file { "/etc/apt/apt.conf.d/01proxy":
#  content => 'Acquire::http::Proxy "http://192.168.33.1:3142/apt-cacher";',
#}

# Parte de la instalación
exec { "apt-update":
  command => "/usr/bin/apt-get update",
}

#class { 'ruby':
#  version         => 'latest',
#  ruby_package => 'ruby1.9.3',
#  gems_version     => 'latest',
#  before => Class["scstack"],
#}

class { "scstack":
  # Superadmin password. Will be used to access the SidelabCode Stack Console
  sadminpass => "sadmin",
  # Or whatever IP specified in Vagrantfile
  ip => "127.0.0.1", 
  domain => "code.scstack.org",
  baseDN => "dc=code,dc=scstack,dc=org",
  # MySQL info
  mysqlpass => "",
  redminedb => "redminedb",
  redminedbuser => "travis",
  redminedbpass => "",
  # Your company/organization name
  compname => "SidelabCode Stack version 0.4",
  # A name to be displayed within Redmine
  codename => "SCStack ALM Tools",
}
