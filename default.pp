# Parte de la instalación
exec { "apt-update":
  command => "/usr/bin/apt-get update",
}
class { "scstack":
  # Superadmin password. Will be used to access the SidelabCode Stack Console
  sadminpass => "sadmin",
  # Or whatever IP specified in Vagrantfile
  ip => "127.0.0.1", 
  domain => "test.scstack.org",
  baseDN => "dc=code,dc=scstack,dc=org",
  # Your company/organization name
  compname => "SidelabCode Stack version 0.4",
  # A name to be displayed within Redmine
  codename => "SCStack ALM Tools",
}
