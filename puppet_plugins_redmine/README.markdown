# redmine plugin installer #

This is the redmine plugin installer module.

Plugin list:

	* Redmine wysiwyg plugin.

Install
========

# Compile module:

	$ puppet-module build
	=============================================================================
	Building ~/puppet_plugins_redmine for release
	-----------------------------------------------------------------------------
	Done. Built: pkg/puppet_plugins_redmine-00.00.01.tar.gz
# Configure module directory, show puppet module directory:

	$ puppet master --configprint modulepath
	/etc/puppet/modules:/usr/share/puppet/modules
# Create directory folders:

	$ sudo mkdir  /usr/share/puppet
	$ sudo mkdir  /usr/share/puppet/modules
	$ cd /usr/share/puppet/modules
	$ sudo puppet-module install [fichero tar.gz generado por la orden build]
# Create file to launch: @/etc/puppet/manifests/site.pp@.

	node default {
		
	  class { "puppet_plugins_redmine":
	    installFolder => "/opt",
	  }
	}
# Run puppet 

	$ puppet apply manifests/site.pp
