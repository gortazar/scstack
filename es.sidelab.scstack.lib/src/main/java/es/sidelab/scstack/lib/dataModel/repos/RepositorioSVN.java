/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: RepositorioSVN.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.dataModel.repos;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

import java.io.PrintWriter;

/**
 *
 * @author Arek Klauza
 */
public class RepositorioSVN extends Repositorio {
    /** String que identifica el tipo de repositorio */
    public static String tipo = "SVN";

    public RepositorioSVN(boolean esPublico, String ruta) {
        super(esPublico, ruta, tipo);
    }


    /**
     * <p>Este método se encarga de inicializar el repositorio SVN del proyecto
     * que estamos creando.</p>
     * <p>Así mismo, crea automáticamente las carpetas correspondientes a dicho
     * repositorio.</p>
     * @param cnProyecto Nombre del proyecto
     * @param uidAdminProyecto UID del usuario Administrador del proyecto (para
     * este método puede ser null)
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    @Override
    public void crearRepositorio(String cnProyecto, String uidAdminProyecto, API_Abierta apiAbierta) throws ExcepcionConsola {        
        try {
            this.setConsola();
            // Se pone un waitFor() para esperar a que se haga el svnadmin antes del chown
            this.getConsola().exec("svnadmin create " + this.getRuta() + "/" + cnProyecto).waitFor();
            this.getConsola().exec("chown -R www-data:www-data " + this.getRuta() + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló la creación del repositorio SVN "
                    + "(svnadmin create ó chown -R www-data) - " + e.getMessage());
        }
    }


    /**
     * <p>Recibe los PrintWriter de los ficheros de configuración de Apache de proyectos
     * (fichero de proyectos en la carpeta sites-available de apache) e imprime al final
     * en dicho fichero la entrada correspondiente al repositorio SVN del proyecto.</p>
     * <p>Si es un repositorio privado se escribe solo en el pwHTTPS, mientras que
     * si tiene acceso público, se escribe también en el pwHTTP.</p>
     * @param pwHTTPS PrintWriter del fichero de configuración SSL de Apache. Por defecto:
     * sites-available/dev.misidelab.es-ssl-projects
     * @param pwHTTP PrintWriter del fichero de configuración HTTP de Apache. Por
     * defecto: sites-available/dev.misidelab.es-projects
     * @param cn CN del proyecto a escribir
     * @param gidNumber gidNumber del proyecto a escribir
     */
    @Override
    public void escribirEntradaApache(PrintWriter pwHTTPS, PrintWriter pwHTTP, String cn, String gidNumber) {
        pwHTTPS.println("##### Configuración SVN-Privado");
        pwHTTPS.println("<Location " + ConfiguracionForja.pathSVNWeb + "/" + cn + ">");
        pwHTTPS.println("    DAV svn");
        pwHTTPS.println("    SVNPath " + this.getRuta() + "/" + cn);
        pwHTTPS.println("    Options Indexes FollowSymLinks");
        pwHTTPS.println("    AuthType Basic");
        pwHTTPS.println("    AuthBasicProvider ldap");
        pwHTTPS.println("    AuthName \"Repositorio SVN de " + cn + "\"");
        pwHTTPS.println("    AuthLDAPURL \"ldap://" + ConfiguracionForja.hostLDAP + ":389/" + ConfiguracionForja.baseDN + "?uid\"");
        pwHTTPS.println("    AuthLDAPBindDN " + ConfiguracionForja.bindDN);
        pwHTTPS.println("    AuthLDAPBindPassword " + ConfiguracionForja.passBindDN);
        pwHTTPS.println("    AuthzLDAPAuthoritative on");
        pwHTTPS.println("    AuthLDAPGroupAttributeIsDN off");
        pwHTTPS.println("    AuthLDAPGroupAttribute memberUid");
        pwHTTPS.println("    Require ldap-group cn=" + cn + ",ou=" + ConfiguracionForja.ouProyectos + "," + ConfiguracionForja.baseDN);
        pwHTTPS.println("    Require ldap-attribute gidNumber=" + gidNumber);
        pwHTTPS.println("</Location>");
        pwHTTPS.println("");
        if (this.esPublico()) {
            pwHTTP.println("\n################# CONFIGURACIÓN PROYECTO: " + cn + " #################\n");
            pwHTTP.println("##### Configuración SVN-Público");
            pwHTTP.println("<Location " + ConfiguracionForja.pathReposPublicosWeb + "/" + cn + ">");
            pwHTTP.println("    DAV svn");
            pwHTTP.println("    SVNPath " + this.getRuta() + "/" + cn);
            pwHTTP.println("    Options Indexes FollowSymLinks");
            pwHTTP.println("    <LimitExcept GET PROPFIND OPTIONS REPORT>");
            pwHTTP.println("        Deny from all");
            pwHTTP.println("    </LimitExcept>");
            pwHTTP.println("</Location>");
            pwHTTP.println("");
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
            throw new ExcepcionConsola("Error del terminal Linux: falló el borrado del repositorio SVN " + e.getMessage());
        }
    }


    
}
