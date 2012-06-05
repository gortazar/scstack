/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ServidorApache.java
 * Autor: Arek Klauza
 * Fecha: Mayo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.installer;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.ExecutionCommandException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;



/**
 * <p>Clase encargada de instalar el servicio de directorios de LDAP con los
 * parámetros del fichero de configuración.</p>
 * @author Arek Klauza
 */
public class Redmine {
    private static CommandLine consola;



    public Redmine() {
        consola = new CommandLine();
    }


    public void instalar() throws ExecutionCommandException, IOException, NoSuchAlgorithmException {
        System.out.println("\n*** INSTALACIÓN REDMINE ***\n");
        Instalacion.ejecutar("apt-get -y install mysql-client-5.1 libmysqlclient-dev");
        Instalacion.ejecutar("apt-get -y install rubygems mongrel ruby1.8-dev rake libopenssl-ruby1.8");
        Instalacion.ejecutar("gem install rails -v=2.3.5");
        Instalacion.ejecutar("gem install rack -v=1.0.1");
        Instalacion.ejecutar("gem install mysql");
        Instalacion.ejecutar("gem install i18n -v=0.4.2");
        Instalacion.ejecutar("cp -R ficherosInstalacion/redmine/redmine /var/redmine");
        crearFicheroCONFIG_MYSQL("/tmp/configMysql.sh");
        Instalacion.ejecutar("sh /tmp/configMysql.sh");
        crearFicheroDATABASE_YML("/var/redmine/config/database.yml");
        crearFicheroPRODUCTION("/tmp/initDBRedmine.sh");
        Instalacion.ejecutar("sh /tmp/initDBRedmine.sh");
        Instalacion.ejecutar("apt-get -y install libapache2-mod-fcgid");
        Instalacion.ejecutar("apt-get -y install build-essential");
        Instalacion.ejecutar("apt-get -y install libfcgi-dev");
        Instalacion.ejecutar("gem install fcgi");
        Instalacion.ejecutar("chown -R www-data:www-data /var/redmine/files /var/redmine/log /var/redmine/tmp /var/redmine/vendor");
        Instalacion.ejecutar("chmod -R 755 /var/redmine/files /var/redmine/log /var/redmine/tmp");
        Instalacion.ejecutar("cp /var/redmine/public/dispatch.fcgi.example /var/redmine/public/dispatch.fcgi");
        Instalacion.ejecutar("chmod 555 /var/redmine/public/dispatch.fcgi");
        crearFicheroHTTACCESS("/var/redmine/public/.htaccess");
        crearFicheroSettingsRedmine("/tmp/settingsRedmine.sh");
        Instalacion.ejecutar("sh /tmp/settingsRedmine.sh");
        
        Instalacion.ejecutar("sh ficherosInstalacion/postfix_install.sh");
        crearFicheroEmailYml("/var/redmine/config/email.yml");
        
        Instalacion.ejecutar("/etc/init.d/apache2 restart");
        //Instalacion.ejecutar("apache2ctl graceful");

        System.out.println("**************************************************\n");
    }

    private void crearFicheroEmailYml(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("production:");
        pw.println("  delivery_method: :sendmail");
        pw.println("  smtp_settings:");
        pw.println("    address: 127.0.0.1");
        pw.println("    port: 25");

        pw.close();
        file.close();
	}


	private void crearFicheroSettingsRedmine(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("mysql -u root -p" + Instalacion.config.getProperty("passMysql") + " redmine<< EOF");
        pw.println("INSERT INTO settings (name, value) VALUES ('rest_api_enabled', 1);");
        pw.println("exit");
        pw.println("EOF");

        pw.close();
        file.close();
				
	}

	private void crearFicheroCONFIG_MYSQL(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("mysql -u root -p" + Instalacion.config.getProperty("passMysql") + " << EOF");
        pw.println("CREATE DATABASE redmine DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;");
        pw.println("USE redmine;");
        pw.println("GRANT ALL ON redmine.* TO redmine@localhost IDENTIFIED BY 'password';");
        pw.println("exit");
        pw.println("EOF");

        pw.close();
        file.close();
    }

    private void crearFicheroDATABASE_YML(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("# MySQL (default setup).");
        pw.println("");
        pw.println("production:");
        pw.println("  adapter: mysql");
        pw.println("  database: redmine");
        pw.println("  host: localhost");
        pw.println("  username: root");
        pw.println("  password: " + Instalacion.config.getProperty("passMysql"));
        pw.println("  encoding: utf8");
        pw.println("");
        pw.println("development:");
        pw.println("  adapter: mysql");
        pw.println("  database: redmine_development");
        pw.println("  host: localhost");
        pw.println("  username: root");
        pw.println("  password:");
        pw.println("  encoding: utf8");
        pw.println("");
        pw.println("test:");
        pw.println("  adapter: mysql");
        pw.println("  database: redmine_test");
        pw.println("  host: localhost");
        pw.println("  username: root");
        pw.println("  password:");
        pw.println("  encoding: utf8");
        pw.println("");
        pw.println("test_pgsql:");
        pw.println("  adapter: postgresql");
        pw.println("  database: redmine_test");
        pw.println("  host: localhost");
        pw.println("  username: postgres");
        pw.println("  password: \"postgres\"");
        pw.println("");
        pw.println("test_sqlite3:");
        pw.println("  adapter: sqlite3");
        pw.println("  database: db/test.db");

        pw.close();
        file.close();
    }
    
    private void crearFicheroPRODUCTION(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("cd /var/redmine");
        pw.println("RAILS_ENV=production rake config/initializers/session_store.rb");
        pw.println("rake db:migrate RAILS_ENV=production");

        pw.close();
        file.close();
    }
    
    private void crearFicheroHTTACCESS(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("<IfModule mod_fcgid.c>");
        pw.println("\tAddHandler fcgid-script .fcgi");
        pw.println("</IfModule>");
        pw.println("");
        pw.println("Options +FollowSymLinks +ExecCGI");
        pw.println("RewriteEngine On");
        pw.println("RewriteRule ^$ index.html [QSA]");
        pw.println("RewriteRule ^([^.]+)$ $1.html [QSA]");
        pw.println("RewriteCond %{REQUEST_FILENAME} !-f");
        pw.println("");
        pw.println("<IfModule mod_fcgid.c>");
        pw.println("\tRewriteRule ^(.*)$ dispatch.fcgi [QSA,L]");
        pw.println("</IfModule>");
        pw.println("ErrorDocument 500 \"<h2>Application error</h2>Rails application failed to start properly\"");

        pw.close();
        file.close();
    }
 
  
 
 
}
