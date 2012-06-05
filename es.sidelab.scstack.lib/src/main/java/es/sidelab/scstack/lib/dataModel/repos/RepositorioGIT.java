/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: RepositorioGIT.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.dataModel.repos;

import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

import java.io.PrintWriter;


/**
 *
 * @author Arek Klauza
 */
public class RepositorioGIT extends Repositorio {
    /** String que identifica el tipo de repositorio */
    public static String tipo = "GIT";

    public RepositorioGIT(boolean esPublico, String ruta) {
        super(esPublico, ruta, tipo);
    }




    /**
     * <p>Recibe los PrintWriter de los ficheros de configuración de Apache de proyectos
     * (fichero de proyectos en la carpeta sites-available de apache) e imprime al final
     * en dicho fichero la entrada correspondiente al repositorio SVN del proyecto.</p>
     * <p>En principio sólo se escribe en el pwHTTP cuando el repositorio es
     * público, si es privado, no hay que escribir nada en ningún fichero.</p>
     * @param pwHTTPS PrintWriter del fichero de configuración SSL de Apache. Por defecto:
     * sites-available/dev.misidelab.es-ssl-projects
     * @param pwHTTP PrintWriter del fichero de configuración HTTP de Apache. Por
     * defecto: sites-available/dev.misidelab.es-projects
     * @param cn CN del proyecto a escribir
     */
    @Override
    public void escribirEntradaApache(PrintWriter pwHTTPS, PrintWriter pwHTTP, String cn, String gidNumber) {
        pwHTTPS.println("##### Configuración GIT-Privado");
        pwHTTPS.println("Alias " + ConfiguracionForja.pathGITWeb + "/" + cn + " " + this.getRuta() + "/" + cn);
        pwHTTPS.println("<Directory " + this.getRuta() + "/" + cn + "/>");
        pwHTTPS.println("    AuthName \"Repositorio GIT del proyecto: " + cn + "\"");
        pwHTTPS.println("    AuthType Basic");
        pwHTTPS.println("    AuthBasicProvider ldap");
        pwHTTPS.println("    AuthzLDAPAuthoritative off");
        pwHTTPS.println("    AuthLDAPURL \"ldap://" + ConfiguracionForja.hostLDAP + ":389/" + ConfiguracionForja.baseDN + "?uid\"");
        pwHTTPS.println("    AuthLDAPBindDN " + ConfiguracionForja.bindDN);
        pwHTTPS.println("    AuthLDAPBindPassword " + ConfiguracionForja.passBindDN);
        pwHTTPS.println("    AuthzLDAPAuthoritative on");
        pwHTTPS.println("    AuthLDAPGroupAttributeIsDN off");
        pwHTTPS.println("    AuthLDAPGroupAttribute memberUid");
        pwHTTPS.println("    Require ldap-group cn=" + cn + ",ou=" + ConfiguracionForja.ouProyectos + "," + ConfiguracionForja.baseDN);
        pwHTTPS.println("    Require ldap-attribute gidNumber=" + gidNumber);
        pwHTTPS.println("</Directory>");
        pwHTTPS.println("");

        if (this.esPublico()) {
            pwHTTP.println("\n################# CONFIGURACIÓN PROYECTO: " + cn + " #################\n");
            pwHTTP.println("##### Configuración GIT-Público");
            pwHTTP.println("Alias " + ConfiguracionForja.pathReposPublicosWeb + "/" + cn + " " + this.getRuta() + "/" + cn);
            pwHTTP.println("<Directory " + this.getRuta() + "/" + cn + "/>");
            pwHTTP.println("    Order allow,deny");
            pwHTTP.println("    Allow from all");
            pwHTTP.println("</Directory>");
            pwHTTP.println("");
        }
    }


    /**
     * <p>Crea un repositorio GIT para el proyecto que estamos creando en la
     * Forja.</p>
     * <p>Genera todas las carpetas necesarias para el repositorio, tanto si es
     * público como privado e inicializa el repositorio.</p>
     * @param cnProyecto Nombre del proyecto
     * @param uidAdminProyecto UID del usuario Administrador del proyecto
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    @Override
    public void crearRepositorio(String cnProyecto, String uidAdminProyecto) throws ExcepcionConsola {
        // Primero creamos la carpeta del repositorio
        try {
            this.setConsola();
            this.getConsola().exec("mkdir " + this.getRuta() + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error durante la creación de la carpeta del repositorio GIT en la ruta: " +
                    this.getRuta() + " - " + e.getMessage());
        }

        // Asignamos los permisos correspondientes a la carpeta del proyecto GIT
        try {
            this.getConsola().exec("chown " + uidAdminProyecto + ":" + cnProyecto + " " + this.getRuta() + "/" + cnProyecto).waitFor();
            if (this.esPublico())
                this.getConsola().exec("chmod 775 " + this.getRuta() + "/" + cnProyecto).waitFor();
            else
                this.getConsola().exec("chmod 770 " + this.getRuta() + "/" + cnProyecto).waitFor();

        } catch (Exception e) {
            throw new ExcepcionConsola("Error durante la asignación de permisos a la carpeta del repositorio GIT - " + e.getMessage());
        }

        // Inicializamos el repositorio GIT
        try {
            this.getConsola().exec("git init " + this.getRuta() + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error durante la inicialización del repositorio GIT - " + e.getMessage());
        }

        // Si es público hay que activar el acceso al repositorio desde el navegador web
        if (this.esPublico()) {
            try {
                this.getConsola().exec("mv " + this.getRuta() + "/" + cnProyecto + "/hooks/post-update.sample " +
                        this.getRuta() + "/" + cnProyecto + "/hooks/post-update").waitFor();
                this.getConsola().exec("git update-server-info " + this.getRuta() + "/" + cnProyecto).waitFor();
            } catch (Exception e) {
                throw new ExcepcionConsola("Error durante la configuración del acceso web al repositorio GIT - " + e.getMessage());
            }
        }
    }

    /**
     * <p>Elimina definitivamente el repositorio de un proyecto determinado.</p>
     * @param cnProyecto Nombre del proyecto cuyo repositorio queremos borrar
     * @throws ExcepcionConsola Cuando se produce algún error durante la ejecución
     * de la consola
     */
    @Override
    public void borrarRepositorio(String cnProyecto) throws ExcepcionConsola{
        try {
            this.setConsola();
            this.getConsola().exec("rm -rf " + this.getRuta() + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló el borrado del repositorio GIT " + e.getMessage());
        }
    }

}
