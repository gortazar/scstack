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

import es.sidelab.commons.commandline.ExecutionCommandException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;



/**
 * <p>Clase encargada de instalar el servicio de directorios de LDAP con los
 * parámetros del fichero de configuración.</p>
 * @author Arek Klauza
 */
public class Repositorios {


    public void instalar() throws es.sidelab.commons.commandline.ExecutionCommandException, IOException, NoSuchAlgorithmException {
        System.out.println("\n*** INSTALACIÓN REPOSITORIOS GIT Y SVN ***\n");
        Instalacion.ejecutar("apt-get -y install libapache2-svn");
        Instalacion.ejecutar("apt-get -y install subversion");
        Instalacion.ejecutar("apt-get -y install subversion-tools");
        Instalacion.ejecutar("mkdir " + Instalacion.config.getProperty("pathSVNApache"));
        Instalacion.ejecutar("apt-get -y install git-core");
        Instalacion.ejecutar("mkdir " + Instalacion.config.getProperty("pathGITApache"));
        //display the list of opened ports and the listening services
        //Instalacion.ejecutar("lsof -Pnl +M -i4");
        Instalacion.ejecutar("/etc/init.d/apache2 restart");
        System.out.println("**************************************************\n");
    }
}
