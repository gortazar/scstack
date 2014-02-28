# scstack

A software forge ecosystem

[![Build Status](https://travis-ci.org/gortazar/scstack.png)](https://travis-ci.org/gortazar/scstack)

## Instalación

En la versión 0.4 de SidelabCode Stack se utiliza Puppet en el proceso de instalación. Ahora es extremadamente sencillo instalar SidelabCode Stack usando Puppet o Vagrant con el nuevo instalador.

Puppet nos permite automatizar la instalación en un servidor ubuntu mediante una sencilla configuración.

Además, el uso de Vagrant permite probar SidelabCode Stack en una máquina virtual en 5 minutos.

Give it a try! It's super easy.

### Requisitos

Para la instalación de SidelabCode Stack es necesario obtener los siguientes recursos.

Dependiendo de la instalación que se utilice Vagrant o Puppet se han de seguir instrucciones diferentes.

Descarga SidelabCode Stack
Descargar el instalador de SidelabCode Stack (módulo scstack) y sus dependencias de http://code.sidelab.es/public/sidelabcodestack/artifacts/0.4/puppet-installer-0.4-bin.tar.gz:

    cd $HOME
    mkdir tmp
    cd $HOME/tmp
    wget https://github.com/sidelab-urjc/scstack/archive/master.zip

Descomprimir y copiar a la carpeta modules:

    cd $HOME/tmp
    unzip master.zip

### Configuración de módulos puppet
  
Creamos un fichero default.pp en la carpeta tmp con el siguiente contenido:

    # Parte de la instalación
    exec { "apt-update":
      command => "/usr/bin/apt-get update",
    }
    class { "scstack":
      # Superadmin password. Will be used to access the SidelabCode Stack Console re@lity45
      sadminpass => "re@lity45",
      # Or whatever IP specified in Vagrantfile
      ip => "192.168.33.10", 
      domain => "sidelabcode03.scstack.org",
      baseDN => "dc=sidelabcode03,dc=scstack,dc=org",
      # Your company/organization name
      compname => "SidelabCode Stack version 0.4",
      # A name to be displayed within Redmine
      codename => "SCStack ALM Tools",
    }

### Apt Cacher

Dependiendo de la conexión de red, el proceso de instalación puede tardar más o menos. En general es buena idea configurar un proxy para los paquetes debian. Esta opción es recomendable en el proceso de instalación a través de Vagrant. Para ello, simplemente hay que instalar apt-cacher en el host:

    sudo apt-get install apt-cacher

Modificar las siguientes líneas del fichero /etc/apt-cacher/apt-cacher.conf:

    daemon_addr = 192.168.33.1 # No podemos usar localhost aquí si queremos que los clientes se puedan conectar
    allowed_hosts = * # Permitir a todos los clientes conectarse a este proxy. Alternativamente se podrían especificar direcciones IP
    generate_reports = 1 # Generar un informe cada 24h

Reiniciar el servicio:

    sudo /etc/init.d/apt-cacher restart

Modificar el fichero /etc/hosts con la ip del host y el dominio asociado que se define en el fichero de configuración default.pp y la ip y el dominio del cliente para la comunicación con apt-cacher definido en el parámetro daemon_addr del fichero /etc/apt-cacher/apt-cacher.conf:

    192.168.33.10    sidelabcode03.scstack.org
    192.168.33.1    host.scstack.es

Añadir el siguiente trozo de código en la primera línea del fichero default.pp para que utilice el apt-cacher creado:

    # Si hemos configardo apt-cacher como proxy, añadir las siguientes líneas:
    file { "/etc/apt/apt.conf.d/01proxy":
      content => 'Acquire::http::Proxy "http://192.168.33.1:3142/apt-cacher";',
    }

## Instalación con Vagrant

La instalación a través de Vagrant permite probar SidelabCode Stack en una máquina virtual en 5 minutos.

### Descripción del entorno de instalación

Básicamente lo que vamos a hacer es indicar a Vagrant que monte una red privada entre la máquina host y la máquina virtual donde instalaremos SidelabCode Stack. Esto nos permite tener acceso a la forja desde el host. Normalmente, Vagrant asigna, dentro de esa red privada, la IP 192.168.33.1 al host y la IP 192.168.33.10 a la máquina virtual. Estos valores se pueden cambiar como veremos posteriormente.

### Prerequisitos

Instalar Vagrant y Virtualbox

Añadir Vagrant al path

    gedit $HOME/.bashrc
    PATH=$PATH:/opt/vagrant/bin

### Arrancar la vm.

    git clone https://github.com/gortazar/scstack.git
    cd scstack
    vagrant up

## Instalación directamente con puppet

En este caso asumimos que tenemos una máquina con Ubuntu 12.04 64 bits instalado. 

### Prerequisitos

Puppet se ha de instalar mediante el gestor de paquetes de la distribución, en este caso apt para ubuntu:

    $ [sudo] apt-get install puppet

La instalación en otras distribuciones se puede consultar en el manual de puppet.

### Configuración

Copiar los módulos puppet al directorio de módulos definido:

    $ mkdir -p $HOME/puppet/modules
    $ cp -R puppet-installer-0.4/* $HOME/puppet/modules

### Provisionamiento

Ejecutar puppet para el proceso de instalación mediante sudo:

    $ sudo puppet apply --modulepath=$HOME/puppet/modules default.pp

Una vez finalizada la instalación, en la dirección http://test.scstack.org/redmine se mostrará Redmine. La consola es accesible a través de la dirección https://test.scstack.org:5555. Es posible administrar scstack accediendo con el usuario "sadmin" y la contraseña especificada en el parámetro sadminpass.

Nota: La consola de administración se ha comprobado el funcionamiento para los siguientes navegadores:

* Firefox.
* Chrome/Chromium

Para información sobre la administración de la forja, consultar la documentación de usuario.

## Post instalación

***Please read carefully this section to make your stack as secure as possible***

Después de la instalación automatizada del entorno se ha de acceder a las herramientas Redmine y Archiva para completar el proceso.

Antes de nada se ha de reiniciar la máquina para comprobar que se ejecutan todos los procesos en el inicio.

### Redmine

Modificar los permisos de las carpetas en /opt/redmine/tmp para que sean del usuario de apache:

    cd /opt/redmine/tmp && chown -R www-data:www-data *

Acceder a la URL de Redmine, con el usuario admin y el password admin:

    https://test.scstack.org/redmine

Cambiar la contraseña para que no utilice la genérica a través de la ruta:

Administración -> Users -> admin -> Authentication -> actualizar

Cambiar la API key para securizar el acceso a la API rest (scstack instala una por defecto, pero no es segura):

* Acceder como admin -> My account -> API access key -> Reset -> Show -> Copiar la key
* Pegar la key en el fichero /opt/scstack-service/scstack.conf
* Reiniciar el servicio scstack: sudo service scstack-service restart

### Gerrit

El primer usuario que accede a Gerrit obtiene privilegios de administrador. Al instalar la forja, se crea un usuario "gerritadmin" con la password especificada y se debe acceder con este usuario a Gerrit para hacerlo administrador. Este usuario se convertirá en administrador automáticamente al hacer login. A partir de este momento, este será el usuario con el que crear los grupos y proyectos (repositorios) en Gerrit.

Obtener la clave pública del servidor para asignarla al usuario gerritadmin:

Settings -> SSH Public Keys -> Add
Copiar la clave del fichero /opt/ssh-keys/gerritadmin_rsa.pub.

Configurar permisos para creación de proyectos:

Acceder a Projects -> List -> All-Projects.
Seleccionar Access:
Editar -> :
refs/ Add Permission -> añadir Push el grupo Administrators.
refs/meta/config Add Group -> añadir a Read el grupo Administrators.
Save Changes.

### Archiva

Acceder a la URL de Archiva:

    https://test.scstack.org/archiva

La primera vez que se configura archiva pide los datos del administrador. Apuntarlos convenientemente para posteriores necesidades de administración. Se recomienda utilizar la contraseña del administrador de la forja definida en el fichero de configuración default.pp.

#### Repositorios

Por defecto, Archiva trae configurado un repositorio internal que hace de proxy de Maven Central y java.net. Si hace falta añadir repositorios remotos adicionales, en Repositories, al final de la página se pueden añadir repositorios remotos.

Archiva trae configurado un repositorio de snapshots. Se recomienda crear uno de releases.

Para ello accedemos a la adminstración de Archiva Administration -> Repositories y añadimos uno nuevo con los siguientes parámetros:

    Identifier*: releases
    Name*:       Archiva Managed Releases Repository
    Directory*:  /opt/tomcat/data/repositories/releases
    ...
    Repository Purge By Days Older Than: 30
    
Se crea el repositorio. Si Tomcat nos muestra un error por pantalla al acceder a la URL https://test.scstack.org/archiva/admin/addRepository!commit.action relacionado con NullPointerException no nos debemos preocupar ya que el repositorio está creado correctamente siendo accesible y funcional. Se puede comprobar volviendo a visualizar la lista de repositorios de Archiva.

#### Usuarios

Archiva no lee los usuarios de OpenLDAP, por tanto es necesario añadirlos a mano. En principio, debería ser suficiente con un usuario de deploy para toda la organización, o como mucho, un usuario por proyecto o grupo de proyectos.

El usuario debe ser Observer de los tres repositorios (internal, snapshots, releases) y manager de snapshots y releases.

## FAQ

Posibles problemas que se pueden encontrar tras la instalación

### Error al crear un repositorio Git: Reininciar el servicio scstack-service

    $ sudo service scstack-service stop
    $ sudo service scstack-service start

### Error al acceder a Archiva o Jenkins 404 No encontrado:

Configurar el dominio de nombres en el ordenador para que acceda a através de la ip correspondiente:

    $ sudo vi /etc/hosts
    
Añadir la línea de conversión entre IP y nombre (elegido en la configuración del fichero default.pp):

    138.100.156.246    sidelabcode03.scstack.org
