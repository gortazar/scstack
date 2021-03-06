# Class: puppet_plugins_redmine
#
# This module manages Redmine plugins installation.
#
# Parameters: $installFolder: Redmine Installation Directory.
#
# Actions:
#
#   Install redmine_backlogs_plugin into existent redmine installation.
#
# Requires: Redmine Installation.
#
class puppet_plugins_redmine ($installFolder) {

  # Install Redmine Backlogs Plugin
  class { "puppet_plugins_redmine::backlogs_plugin":
    installFolder => $installFolder,
  }

}
