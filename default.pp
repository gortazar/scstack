# Uncomment this if you have apt-cacher:
#file { "/etc/apt/apt.conf.d/01proxy":
#  content => 'Acquire::http::Proxy "http://192.168.33.1:3142/apt-cacher";',
#}

# Ensure apt-update has been run:
exec { "apt-update":
  command => "/usr/bin/apt-get update",
}

class { "scstack":
  # Superadmin password. Will be used to access the SidelabCode Stack Console
  sadminpass => "sadmin",
  # Or whatever IP specified in Vagrantfile
  ip => "127.0.0.1", 
  # Uncomment this if you are using vagrant and wish to have a guest-host virtual network
  # Note that this requires to modify the Vagrantfile accordingly
  #ip => "192.168.33.10",
  domain => "code.scstack.org",
  # Note that your baseDN must match EXACTLY with your domain
  baseDN => "dc=code,dc=scstack,dc=org",
  passBindDN => "changeme",
  # MySQL info
  mysqlpass => "changeme",
  redminedb => "redminedb",
  redminedbpass => "changeme",
  archivadbpass => "changeme", 
  gerritdbpass => "changeme",
  gerritAdminPass => "changeme",
  # Your company/organization name
  compname => "SidelabCode Stack version 0.4",
  # A name to be displayed within Redmine
  codename => "SCStack ALM Tools",
}
