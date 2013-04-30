# Class: backlogs_plugin
#
# This module manages Redmine plugins installation.
#
# Parameters: $installFolder: Redmine Installation Directory.
#
# Actions:
#
#   Install backlogs_plugin into existent redmine installation.
#
# Requires: Redmine Installation.
#
class puppet_plugins_redmine::backlogs_plugin ($installFolder) {
  Class["scstack::redmine"] -> Class["puppet_plugins_redmine::backlogs_plugin"]

  package { "libxslt-dev": ensure => installed, }

  package { "libxml2-dev":
    require => Package["libxslt-dev"],
    ensure  => installed,
  }

  # Download plugin
  exec { "download-backlogs":
    cwd       => "/tmp",
    command   => "/usr/bin/wget  -c  http://code.sidelab.es/public/sidelabcodestack/artifacts/0.4/redmine_backlogs.tar.gz",
    logoutput => true,
    require   => Package["libxml2-dev"],
  }

  # Unzip plugin
  exec { "extract-backlogs":
    cwd     => "/tmp",
    command => "/bin/tar -xvzf redmine_backlogs.tar.gz",
    require => Exec["download-backlogs"],
  }

  exec { "move-backlogs":
    cwd       => "/tmp",
    command   => "/bin/mv redmine_backlogs $installFolder/redmine/plugins/redmine_backlogs",
    require   => Exec["extract-backlogs"],
    logoutput => true,
  }

  # Configure Redmine
  exec { "bundle-install-redmine-plugin-backlogs":
    cwd         => "$installFolder/redmine",
    logoutput   => true,
    require     => Exec["move-backlogs"],
    command     => "/usr/local/bin/bundle install --path vendor/bundle --without postgresql development test rmagick sqlite",
    environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/vagrant_ruby/bin",
  }

  # Migrate plugin in redmine directory
  exec { "migrate-plugin-backlogs":
    cwd         => "$installFolder/redmine",
    require     => Exec["bundle-install-redmine-plugin-backlogs"],
    environment => ["RAILS_ENV=production"],
    command     => "/usr/local/bin/bundle exec rake redmine:plugins:migrate",
    logoutput   => true,
  }

  # Install backlogs plugin and define story and task tracker 
  # in params 'story_trackers= 1, 2, 3' and 'task_tracker=Task'
  exec { "bundle-configure-plugin-backlogs":
    cwd         => "$installFolder/redmine",
    require     => Exec["migrate-plugin-backlogs"],
    logoutput   => true,
    command     => "/usr/local/bin/bundle exec rake redmine:backlogs:install story_trackers=1,2,3 task_tracker=Task",
    environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/vagrant_ruby/bin",
  }

  # Update users and grop configuration
  file { "$installFolder/redmine/plugins/redmine_backlogs":
    owner   => www-data,
    group   => www-data,
    recurse => true,
    require => Exec["bundle-configure-plugin-backlogs"],
  }

}
