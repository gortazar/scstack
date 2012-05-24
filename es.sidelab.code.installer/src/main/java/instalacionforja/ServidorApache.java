/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ServidorApache.java
 * Autor: Arek Klauza
 * Fecha: Mayo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package instalacionforja;

import es.sidelab.tools.commandline.ExecutionCommandException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;



/**
 * <p>Clase encargada de instalar el servidor web Apache con los
 * parámetros del fichero de configuración.</p>
 * @author Arek Klauza
 */
public class ServidorApache {

    public void instalar() throws ExecutionCommandException, IOException, NoSuchAlgorithmException {
        System.out.println("\n*** INSTALACIÓN SERVIDOR WEB APACHE ***\n");
        Instalacion.ejecutar("apt-get -y install apache2-mpm-prefork apache2-utils apache2.2-bin apache2.2-common libapache2-mod-php5 libapr1 libaprutil1 libaprutil1-dbd-sqlite3 libaprutil1-ldap php5-common php5");
        Instalacion.ejecutar("a2enmod ssl");
        Instalacion.ejecutar("a2enmod ldap");
        Instalacion.ejecutar("a2enmod authnz_ldap");
        Instalacion.ejecutar("sh ficherosInstalacion/servidorApache/a2ensite.sh");
        Instalacion.ejecutar("a2enmod rewrite");
        crearFicheroHOSTS("/etc/hosts");
        crearFicheroPORTS("/etc/apache2/ports.conf");
        crearFicheroDEFAULT("/etc/apache2/sites-available/default");
        crearFicheroDEFAULT_SSL("/etc/apache2/sites-available/default-ssl");
        Instalacion.ejecutar("touch /etc/apache2/sites-available/" + Instalacion.config.getProperty("ficheroProyectosSSL"));
        Instalacion.ejecutar("touch /etc/apache2/sites-available/" + Instalacion.config.getProperty("ficheroProyectos"));
        Instalacion.ejecutar("mkdir " + Instalacion.config.getProperty("pathCarpetas"));
        Instalacion.ejecutar("mkdir " + Instalacion.config.getProperty("pathCarpetaPublicaApache"));
        Instalacion.ejecutar("mkdir " + Instalacion.config.getProperty("pathCarpetaPrivadaWeb"));
        //display the list of opened ports and the listening services
        Instalacion.ejecutar("lsof -Pnl +M -i4");
        Instalacion.ejecutar("apache2ctl restart");
        System.out.println("**************************************************\n");
    }





    private void crearFicheroPORTS(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);
	
        pw.println("# If you just change the port or add more ports here, you will likely also");
        pw.println("# have to change the VirtualHost statement in");
        pw.println("# /etc/apache2/sites-enabled/000-default");
        pw.println("# This is also true if you have upgraded from before 2.2.9-3 (i.e. from");
        pw.println("# Debian etch). See /usr/share/doc/apache2.2-common/NEWS.Debian.gz and");
        pw.println("# README.Debian.gz");
        pw.println("");
        pw.println("#NameVirtualHost *:80");
        pw.println("Listen 127.0.0.1:80");
        pw.println("Listen 127.0.1.1:80");
        String stackIP = Instalacion.config.getProperty("ip");
        if (!stackIP.contentEquals("127.0.0.1") && !stackIP.contentEquals("127.0.1.1"))
        	pw.println("Listen " + stackIP + ":80");
    	else
    		System.out.println("Warning: The StackIP (" + stackIP + ") is the same as loopback's IP.");
        pw.println("");
        pw.println("<IfModule mod_ssl.c>");
        pw.println("    # If you add NameVirtualHost *:443 here, you will also have to change");
        pw.println("    # the VirtualHost statement in /etc/apache2/sites-available/default-ssl");
        pw.println("    # to <VirtualHost *:443>");
        pw.println("    # Server Name Indication for SSL named virtual hosts is currently not");
        pw.println("    # supported by MSIE on Windows XP.");
        pw.println("    Listen 127.0.0.1:443");
        pw.println("    Listen 127.0.1.1:443");
        if (!stackIP.contentEquals("127.0.0.1") && !stackIP.contentEquals("127.0.1.1"))
            pw.println("    Listen " + Instalacion.config.getProperty("ip") + ":443");
    	else
    		System.out.println("Warning: The StackIP (" + stackIP + ") is the same as loopback's IP.");
        pw.println("</IfModule>");
        pw.println("");
        pw.println("<IfModule mod_gnutls.c>");
        pw.println("    Listen 443");
        pw.println("</IfModule>");
        
        pw.close();
        file.close();
	}





	private void crearFicheroHOSTS(String ruta) throws IOException {
		String stackIP = Instalacion.config.getProperty("ip");
		
		FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);
        pw.println("127.0.0.1 localhost");
        pw.println("127.0.1.1 " + Instalacion.config.getProperty("dominio") 
        		+ " " + java.net.InetAddress.getLocalHost().getHostName());
        if (!stackIP.contentEquals("127.0.0.1") && !stackIP.contentEquals("127.0.1.1"))
        	pw.println(stackIP + " " + Instalacion.config.getProperty("dominio")
            		+ " " + java.net.InetAddress.getLocalHost().getHostName());
    	else
    		System.out.println("Warning: The StackIP (" + stackIP + ") is the same as loopback's IP.");
        pw.println("");
        pw.println("::1     localhost ip6-localhost ip6-loopback");
        pw.println("fe00::0 ip6-localnet");
        pw.println("ff00::0 ip6-mcastprefix");
        pw.println("ff02::1 ip6-allnodes");
        pw.println("ff02::2 ip6-allrouters");
        pw.println("ff02::3 ip6-allhosts");
        
        pw.close();
        file.close();
    }

    private void crearFicheroDEFAULT(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("<VirtualHost *:80>");
        pw.println("");
        pw.println("\tServerAdmin webmaster@localhost");
        pw.println("\tServerName " + Instalacion.config.getProperty("dominio") + ":80");
        pw.println("");
        pw.println("\tDocumentRoot /var/redmine/public/");
        pw.println("\t<Directory \"/var/redmine/public/\">");
        pw.println("\t\tOptions Indexes ExecCGI FollowSymLinks");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");        
        pw.println("\t\tAllowOverride all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tRedirectMatch ^" + Instalacion.config.getProperty("pathSVNWeb") + "(/.*)$ https://" + Instalacion.config.getProperty("dominio") + Instalacion.config.getProperty("pathSVNWeb") + "$1");
        pw.println("\tRedirectMatch ^" + Instalacion.config.getProperty("pathGITWeb") + "(/.*)$ https://" + Instalacion.config.getProperty("dominio") + Instalacion.config.getProperty("pathGITWeb") + "$1");
        pw.println("\tRedirectMatch ^/" + Instalacion.config.getProperty("pathCarpetaPrivadaWeb") + "(/.*)$ https://" + Instalacion.config.getProperty("dominio") + Instalacion.config.getProperty("pathCarpetaPrivadaWeb") + "$1");
        pw.println("");
        pw.println("\tInclude /etc/apache2/sites-available/" + Instalacion.config.getProperty("ficheroProyectos"));
        pw.println("");
        pw.println("\tAlias " + Instalacion.config.getProperty("pathCarpetaPublicaWeb") + " " + Instalacion.config.getProperty("pathCarpetaPublicaApache"));
        pw.println("");
        pw.println("\t<Directory \"" + Instalacion.config.getProperty("pathCarpetaPublicaApache") + "\">");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tScriptAlias /cgi-bin/ /usr/lib/cgi-bin/");
        pw.println("\t<Directory \"/usr/lib/cgi-bin\">");
        pw.println("\t\tAllowOverride None");
        pw.println("\t\tOptions +ExecCGI -MultiViews +SymLinksIfOwnerMatch");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tErrorLog /var/log/apache2/ForjaSoftware.error.log");
        pw.println("");
        pw.println("\tLogLevel warn");
        pw.println("");
        pw.println("\tCustomLog /var/log/apache2/ForjaSoftware-access.log combined");
        pw.println("");
        pw.println("\tAlias /doc/ \"/usr/share/doc/\"");
        pw.println("\t<Directory \"/usr/share/doc/\">");
        pw.println("\t\tOptions Indexes MultiViews FollowSymLinks");
        pw.println("\t\tAllowOverride None");
        pw.println("\t\tOrder deny,allow");
        pw.println("\t\tDeny from all");
        pw.println("\t\tAllow from 127.0.0.0/255.0.0.0 ::1/128");
        pw.println("\t</Directory>");
        pw.println("</VirtualHost>");
        
        pw.close();
        file.close();
    }

    private void crearFicheroDEFAULT_SSL(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("<IfModule mod_ssl.c>");
        pw.println("<VirtualHost *:443>");
        pw.println("");
        pw.println("\tServerAdmin webmaster@localhost");
        pw.println("\tServerName " + Instalacion.config.getProperty("dominio") + ":443");
        pw.println("");
        pw.println("\tDocumentRoot /var/redmine/public/");
        pw.println("\t<Directory \"/var/redmine/public/\">");
        pw.println("\t\tOptions Indexes ExecCGI FollowSymLinks");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");
        pw.println("\t\tAllowOverride all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tInclude /etc/apache2/sites-available/" + Instalacion.config.getProperty("ficheroProyectosSSL"));
        pw.println("");
        pw.println("\tAlias " + Instalacion.config.getProperty("pathCarpetaPublicaWeb") + " " + Instalacion.config.getProperty("pathCarpetaPublicaApache"));
        pw.println("");
        pw.println("\t<Directory \"" + Instalacion.config.getProperty("pathCarpetaPublicaApache") + "\">");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tScriptAlias /cgi-bin/ /usr/lib/cgi-bin/");
        pw.println("\t<Directory \"/usr/lib/cgi-bin\">");
        pw.println("\t\tAllowOverride None");
        pw.println("\t\tOptions +ExecCGI -MultiViews +SymLinksIfOwnerMatch");
        pw.println("\t\tOrder allow,deny");
        pw.println("\t\tAllow from all");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tErrorLog /var/log/apache2/ForjaSoftware.error.log");
        pw.println("");
        pw.println("\tLogLevel warn");
        pw.println("");
        pw.println("\tCustomLog /var/log/apache2/ForjaSoftware-access.log combined");
        pw.println("");
        pw.println("\tAlias /doc/ \"/usr/share/doc/\"");
        pw.println("\t<Directory \"/usr/share/doc/\">");
        pw.println("\t\tOptions Indexes MultiViews FollowSymLinks");
        pw.println("\t\tAllowOverride None");
        pw.println("\t\tOrder deny,allow");
        pw.println("\t\tDeny from all");
        pw.println("\t\tAllow from 127.0.0.0/255.0.0.0 ::1/128");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tSSLEngine on");
        pw.println("\tSSLCertificateFile    /etc/ldap/slapdcert.pem");
        pw.println("\tSSLCertificateKeyFile /etc/ldap/slapdkey.pem");
        pw.println("");
        pw.println("\t<FilesMatch \"\\.(cgi|shtml|phtml|php)$\">");
        pw.println("\t\tSSLOptions +StdEnvVars");
        pw.println("\t</FilesMatch>");
        pw.println("");
        pw.println("\t<Directory /usr/lib/cgi-bin>");
        pw.println("\t\tSSLOptions +StdEnvVars");
        pw.println("\t</Directory>");
        pw.println("");
        pw.println("\tBrowserMatch \"MSIE [2-6]\" \\");
        pw.println("\tnokeepalive ssl-unclean-shutdown \\");
        pw.println("\tdowngrade-1.0 force-response-1.0");
        pw.println("\tBrowserMatch \"MSIE [17-9]\" ssl-unclean-shutdown");
        pw.println("");
        pw.println("</VirtualHost>");
        pw.println("</IfModule>");
        
        pw.close();
        file.close();
    }
    
}
