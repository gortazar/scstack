# scstack

A software forge ecosystem

[![Build Status](https://travis-ci.org/gortazar/scstack.png)](https://travis-ci.org/gortazar/scstack)

Scstack is a stack of open source solutions aimed at supporting software development for a team, with a single and simple to use web interface for configuration.

Solutions in the stack are deployed with puppet. These include issue management (redmine), scm (svn & git), code revision (gerrit), ci (jenkins), and public & private folders for projects accessible through https & sftp.

The full stack is configured through a web interface (the scstack service) in a transparent way. For this purpose we defined the concept of "project". By creating a project a team obtains:

* A project in redmine for project management (this project can hold issues, a wiki and a code repository browser)
* A source code repository
* Public & private folders for documents, artifacts and any other file that you want to keep with your project

## Installation

There are two ways of installing:

* Using [Vagrant](http://www.vagrantup.com/) (easiest)
* Manually (a couple of commands away from using vagrant)

## Installing using vagrant

Clone repo, cd, run vagrant up:

    git clone https://github.com/gortazar/scstack.git
    cd scstack
    vagrant up

## Installing manually

I have provided a script to bootstrap the installation process. In order to install, assuming you are within the host where the forge is to be installed, you have to follow these steps:

    sudo apt-get update
    sudo apt-get -y install git-core
    git clone https://github.com/gortazar/scstack.git
    cd scstack
    sudo ./bootstrap.sh

Once installed, open http://<default.pp domain>/redmine in a browser. Redmine will show up. scstack console can be found at https://test.scstack.org:5555. Access is granted to user sadmin with the sadminpass password specified in default.pp file.

Note that scstack console has been tested with Chrome and Firefox.

## Customize installation
  
The forge installed by scstack can be configured by modifiying the default.pp file in the root of the git repository:

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

## Post installation steps

*Please read carefully this section to make your stack as secure as possible*

### Redmine

Change admin redmine password (currently admin):

    https://code.scstack.org/redmine

Administration -> Users -> admin -> Authentication -> update

Change API key to secure redmine api access (scstack installs an api key, but it is not recommended to use it in production):

* Accessing as admin -> My account -> API access key -> Reset -> Show -> Copy key to clipboard
* Paste key in /opt/scstack-service/scstack.conf file (parameter keyRedmineAPI)
* Restart scstack-service: 
    sudo service scstack-service restart

### Gerrit

First user to access gerrit gains admin priviledges. So login as gerritadmin with gerrit password from default.pp. Gerrit is available at https://code.scstack.org/gerrit/ (trailing backslash is needed).

Copy public ssh key from server to become the ssh key of gerrit admin:

    Settings -> SSH Public Keys -> Add
    Paste key from /opt/ssh-keys/gerritadmin_rsa.pub.

Configure access to allow scstack-service to create projects:

    Projects -> List -> All-Projects.
    In Access:
      Edit -> :
        refs/ Add Permission -> añadir Push el grupo Administrators.
        refs/meta/config Add Group -> añadir a Read el grupo Administrators.
    Save Changes.

### Archiva

Archiva is available at:

    https://code.scstack.org/archiva

Archiva asks for admin data first time we access it.

#### Repositories

By default, archiva comes with a preconfigured repository "internal" that works as a proxy for Maven Central and java.net. If necessary, any additional remote repository can be added. 

Archiva already comes with a snapshots repository. However, it is recommended to also create a releases repository. In order to do so, under "Archiva Administration" ->  "Repositories", add a new repository like this one:

    Identifier*: releases
    Name*:       Archiva Managed Releases Repository
    Directory*:  /opt/tomcat/data/repositories/releases
    ...
    Repository Purge By Days Older Than: 30
    
When creating the repository it is possible that Tomcat complains when accessing https://code.scstack.org/archiva/admin/addRepository!commit.action wirh a NullPointerException. Don't worry, the repository has been created and can be used. You can test it by listing the repositories.

#### Users

Archiva users are not retrieved from OpenLDAP. So new users must be added manually to archiva. However, it should suffice with a user per project to be added to archiva, and let that user to upload files to archiva repositories. Sometimes this user is jenkins, as a result of a success build.

Note that users added must have Observer permissions on internal, snapshots, and releases repositories, and manager permissions on snapshots and releases.

## Some optimizations: Apt Cacher

Depending on your internet connection, the installation process may last a bit. I assume that it can last 15 minutes with a good connection. However, if you plan to test scstack on your machine to give it a try, it could be a good idea to use a cache for .deb packages (apt-cacher will do the work). By doing this, you can boost further installations. 

The following instructions assume you are using vagrant to setup scstack. To install apt-cacher issue the following command *in the host machine*:

    sudo apt-get install apt-cacher

Add the following lines in /etc/apt-cacher/apt-cacher.conf:

    daemon_addr = 192.168.33.1 # This is the ip vagrant vms will use to connect to host
    allowed_hosts = * 
    generate_reports = 1 

Restart:

    sudo /etc/init.d/apt-cacher restart

Modify the /etc/hosts file:

    192.168.33.10   acme.scstack.org
    192.168.33.1    host.scstack.org

Uncomment the following lines in default.pp (note that we specify the ip of the host as it is seen from the vm):

    # Si hemos configardo apt-cacher como proxy, añadir las siguientes líneas:
    file { "/etc/apt/apt.conf.d/01proxy":
      content => 'Acquire::http::Proxy "http://192.168.33.1:3142/apt-cacher";',
    }



## FAQ

Some problemas that may arise after installation

### Errors when creating git repositories

Restart scstack-service:

    $ sudo service scstack-service stop
    $ sudo service scstack-service start

### "404 Not found" error when accessing Archiva or Jenkins:

Change your /etc/hosts file so that it uses the external ip:

    $ sudo vi /etc/hosts
    
Add this line to tell your host to use the ip configured in your default.pp when asked for the server name:

    138.100.156.246    sidelabcode03.scstack.org
