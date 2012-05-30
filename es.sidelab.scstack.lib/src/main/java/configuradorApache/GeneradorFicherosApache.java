/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GeneradorFicherosApache.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package configuradorApache;

import config.ConfiguracionForja;
import modeloDatos.Proyecto;
import modeloDatos.Usuario;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import excepciones.configuradorApache.ExcepcionGeneradorFicherosApache;
import java.io.RandomAccessFile;
import modeloDatos.repositorios.Repositorio;

/**
 * <p>Esta clase será la encargada de generar los ficheros de configuración necesarios
 * para el correcto funcionamiento de la Forja sobre Apache.</p>
 * @author Arek Klauza
 */
public class GeneradorFicherosApache {
    /** Ruta de la carpeta sites-Available de Apache */
    private String pathSitesAvailableApache;
    /** Nombre del ficheroHTTPS de configuración de proyectos en /sites-available */
    private String ficheroProyectosSSL;
    /** Nombre del ficheroHTTP de configuración de proyectos en /sites-available */
    private String ficheroProyectos;
    /** Ruta virtual de la carpeta privada de proyectos */
    private String pathCarpetaPrivadaWeb;
    /** Ruta física de la carpeta privada de proyectos en el Apache */
    private String pathCarpetaPrivadaApache;
    /** Host del servidor LDAP */
    private String hostLDAP;
    /** baseDN del servidor LDAP */
    private String baseDN;
    /** bindDN con el DN completo del usuario que hará las consultas */
    private String bindDN;
    /** Contraseña en claro del bindDN para realizar las consultas */
    private String passBindDN;
    /** OU (Organizational Unit) de los proyectos */
    private String ouProyectos;
    /** Ruta absoluta / ficheroHTTPS de configuración del SSH (jaula) */
    private String ficheroConfigSSH;





    /**
     * <p>Constructor de la clase Generador de ficheros que nos construye dicho
     * objeto a partir de la configuración especificada en la clase de configuración
     * de la Forja.</p>
     * <p>Esta clase nos ayuda a generar el ficheroHTTPS de configuración de los proyectos
     * de la Forja para el servidor Apache.</p>
     */
    public GeneradorFicherosApache() {
        this.pathSitesAvailableApache = ConfiguracionForja.pathsitesAvailableApache;
        this.ficheroProyectosSSL = ConfiguracionForja.ficheroProyectosSSL;
        this.ficheroProyectos = ConfiguracionForja.ficheroProyectos;
        this.pathCarpetaPrivadaWeb = ConfiguracionForja.pathCarpetaPrivadaWeb;
        this.pathCarpetaPrivadaApache = ConfiguracionForja.pathCarpetaPrivadaApache;
        this.hostLDAP = ConfiguracionForja.hostLDAP;
        this.baseDN = ConfiguracionForja.baseDN;
        this.bindDN = ConfiguracionForja.bindDN;
        this.passBindDN = ConfiguracionForja.passBindDN;
        this.ouProyectos = ConfiguracionForja.ouProyectos;
        this.ficheroConfigSSH = ConfiguracionForja.ficheroConfigSSH;
    }

    


    /**
     * <p>Método que genera desde cero el ficheroHTTPS de configuración de proyectos
     * necesario para el servidor Apache, tanto el del canal HTTP como HTTPS.
     * Además también enjaula a los usuarios de dichos proyectos.</p>
     * <p>En concreto, por cada proyecto contendrá una entrada que configure la
     * carpeta privada del proyecto y la entrada correspondiente a su tipo de
     * repositorio: Sin repositorio, SVN-público, SVN-privado, GIT-público...</p>
     * @param lista Lista de proyectos que tiene la Forja en el momento actual.
     * A partir de esta lista se generará el ficheroHTTPS de configuración.
     * @throws ExcepcionGeneradorFicherosApache Se lanza cuando se produce algún
     * error durante la escritura del ficheroHTTPS de texto de configuración de Apache.
     */
    public void generarFicherosProyectos(Proyecto[] lista) throws ExcepcionGeneradorFicherosApache {
        FileWriter ficheroHTTPS = null;
        FileWriter ficheroHTTP = null;
        try {
            ficheroHTTPS = new FileWriter(this.pathSitesAvailableApache + "/" + this.ficheroProyectosSSL);
            ficheroHTTP = new FileWriter(this.pathSitesAvailableApache + "/" + this.ficheroProyectos);
            PrintWriter pwHTTP = new PrintWriter(ficheroHTTP);
            PrintWriter pwHTTPS = new PrintWriter(ficheroHTTPS);

            if (lista == null)
                throw new ExcepcionGeneradorFicherosApache("Error del generador de ficheros de Apache: Parace que no hay "
                        + "proyectos en la Forja. Lista de proyectos vacía");

            /* Por cada proyecto que haya en la Forja hay que generar una serie
             * de entradas de configuración */
            for (int i = 0; i < lista.length; i++) {
                pwHTTPS.println("\n################# CONFIGURACIÓN PROYECTO: " + lista[i].getCn() + " #################\n");
                // Primero escribimos la entrada de la Carpeta privada del proyecto
                this.escribirEntradaCarpetaPrivada(pwHTTPS, lista[i].getCn(), lista[i].getGidNumber());

                // Luego escribimos las entradas correspondientes a los repositorios del proyecto
                if (lista[i].tieneRepositorio()) {
                    for(Repositorio repo : lista[i].getRepositorios()) {
                        repo.escribirEntradaApache(pwHTTPS, pwHTTP, lista[i].getCn(), lista[i].getGidNumber());
                    }
                }

                pwHTTPS.println("");
            }
            ficheroHTTPS.close();
            ficheroHTTP.close();

        } catch (IOException ex) {
            throw new ExcepcionGeneradorFicherosApache("Error durante la generación del fichero de configuración de Apache: " + ex.getMessage());
        }
    }




    /**
     * <p>Genera el ficheroHTTPS para enjaular los usuarios en el directorio de carpeta
     * pública y privada para que no puedan trastear por el resto de carpetas del
     * Apache.</p>
     * @param users Lista de Proyectos que hay en la Forja
     * @throws ExcepcionGeneradorFicherosApache Se lanza cuando se produce algún
     * error durante la escritura del ficheroHTTPS de texto de configuración de SSH.
     */
    public void generarFicheroJaulaUsuariosSSH(String[] users) throws ExcepcionGeneradorFicherosApache {
        RandomAccessFile fichero = null;
        try {
            fichero = new RandomAccessFile(this.ficheroConfigSSH, "rw");

            /* Trunca el ficheroHTTPS quitando todas las entradas de proyecto, preparándolo
             * así para la inserción de nuevas entradas de proyectos. */
            for (;;) {
                String linea = fichero.readLine();
                
                if(linea == null){
                	throw new ExcepcionGeneradorFicherosApache("The file "+this.ficheroConfigSSH+" doesn't not contains the \"SSH cage marker\" (marcador jaula SSH): "+ConfiguracionForja.marcadorJaulaSSH);
                }
                
                if (linea.equals(ConfiguracionForja.marcadorJaulaSSH)) {
                    fichero.setLength(fichero.getFilePointer());                    
                    break;
                }
            }
            
            fichero.close();

            /* Por cada proyecto que haya en la Forja hay que generar una entrada
             * de configuración para SSH */
            FileWriter fileWr = new FileWriter(this.ficheroConfigSSH, true);
            PrintWriter pw = new PrintWriter(fileWr);
            for (int i = 0; i < users.length; i++) {
                pw.println("Match User " + users[i]);
                pw.println("    ChrootDirectory " + ConfiguracionForja.pathCarpetas);
                pw.println("    AllowTCPForwarding no");
                pw.println("    X11Forwarding no");
                pw.println("    ForceCommand internal-sftp");
                pw.println("");
            }
            fileWr.close();
            
        } catch (IOException ex) {
            throw new ExcepcionGeneradorFicherosApache("Error durante la generación del fichero de jaula SSH: " + ex.getMessage());
        }
    }






    /**
     * <p>Recibe el PrintWriter del ficheroHTTPS de configuración de Apache de proyectos
     * (ficheroHTTPS de proyectos en la carpeta sites-available de apache) e imprime al final
     * en dicho ficheroHTTPS la entrada correspondiente a la carpeta privada del proyecto.</p>
     * @param pw PrintWriter del ficheroHTTPS de configuración Apache. Por defecto:
     * sites-available/dev.misidelab.es-ssl-projects
     * @param cn CN del proyecto a escribir
     * @param gidNumber gidNumber del proyecto a escribir
     */
    private void escribirEntradaCarpetaPrivada(PrintWriter pw, String cn, String gidNumber) {
        pw.println("##### Configuración Carpeta Privada");
        pw.println("Alias " + this.pathCarpetaPrivadaWeb + "/" + cn + " " + this.pathCarpetaPrivadaApache + "/" + cn);
        pw.println("<Directory " + this.pathCarpetaPrivadaApache + "/" + cn + "/>");
        pw.println("    AuthName \"Carpeta Privada del proyecto: " + cn + "\"");
        pw.println("    AuthType Basic");        
        pw.println("    AuthBasicProvider ldap");
        pw.println("    AuthzLDAPAuthoritative off");
        pw.println("    AuthLDAPURL \"ldap://" + this.hostLDAP + ":389/" + this.baseDN + "?uid\"");
        pw.println("    AuthLDAPBindDN " + this.bindDN);
        pw.println("    AuthLDAPBindPassword " + this.passBindDN);
        pw.println("    AuthzLDAPAuthoritative on");
        pw.println("    AuthLDAPGroupAttributeIsDN off");
        pw.println("    AuthLDAPGroupAttribute memberUid");
        pw.println("    Require ldap-group cn=" + cn + ",ou=" + this.ouProyectos + "," + this.baseDN);
        pw.println("    Require ldap-attribute gidNumber=" + gidNumber);
        pw.println("</Directory>");
        pw.println("");
    }


    
}
