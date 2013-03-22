# Class: wysiwyg_textile_plugin
#
# This module manages Redmine plugins installation.
#
# Parameters: $installFolder: Redmine Installation Directory.
#
# Actions:
#
#   Install redmine_wysiwyg_plugin into existent redmine installation.
#
# Requires: Redmine Installation.
#
class puppet_plugins_redmine::wysiwyg_textile_plugin (
  $installFolder) {
  
  Class["scstack::redmine"] -> Class["puppet_plugins_redmine::wysiwyg_textile_plugin"]

  # Download plugin
  exec { "download-wysiwyg":
    cwd       => "/tmp",
    command   => "/usr/bin/wget  -c  http://code.sidelab.es/public/sidelabcodestack/artifacts/0.3/redmine_wysiwyg_textile.tar.gz",
    logoutput => true,
  }

  # Unzip plugin
  exec { "extract-wysiwyg":
    cwd     => "/tmp",
    command => "/bin/tar -xvzf redmine_wysiwyg_textile.tar.gz",
    require => Exec["download-wysiwyg"],
  }

  exec { "move-wysiwyg":
    cwd     => "/tmp",
    command => "/bin/mv redmine_wysiwyg_textile $installFolder/redmine/plugins/redmine_wysiwyg_textile",
    require => Exec["extract-wysiwyg"],
    logoutput   => true,
  }

  file {"$installFolder/redmine/plugins/redmine_wysiwyg_textile":
    owner   => www-data,
    group   => www-data,
    recurse => true,
    require => Exec["move-wysiwyg"],
  }

  # Migrate plugin in redmine directory
  exec {"migrate-plugin-wysiwyg":
    cwd         => "$installFolder/redmine",
    require     => File["$installFolder/redmine/plugins/redmine_wysiwyg_textile"],
    environment => ["RAILS_ENV=production"],
    command     => "rake redmine:plugins:migrate",
    logoutput   => true,
    path        => ["/usr/local/bin"],
  }
  
  exec {"bundle-install-plugin-wysiwyg":
    cwd         => "$installFolder/redmine",
    logoutput   => true,
    require     => Exec["migrate-plugin-wysiwyg"],
    command     => "/usr/local/bin/bundle install --without development test rmagick postgresql sqlite",
    environment => "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/vagrant_ruby/bin",
  }

}
