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

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Logger;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.gerrit.GerritException;
import es.sidelab.scstack.lib.gerrit.GerritManager;

/**
 * 
 * @author Arek Klauza
 */
public class RepositorioGIT extends Repositorio {

    /** String que identifica el tipo de repositorio */
    public static String tipo = "GIT";
    public Logger log = Logger.getLogger(RepositorioGIT.class.getName());

    private GerritManager gerritManager;

    /**
     * <p>
     * Crea el repositorio y la configuración básica.
     * </p>
     * 
     * @param esPublico
     *            Dar acceso público al repositorio.
     * @param ruta
     *            Ruta para alojar el repositorio.
     * @throws SCStackException
     *             Cuando se produce algún error durante la configuración de la
     *             API de la Forja.
     */
    public RepositorioGIT(boolean esPublico, String ruta)
            throws SCStackException {
        super(esPublico, ruta, tipo);

        // Inicializa el manager de Gerrit.
        try {
            this.gerritManager = new GerritManager(this.log);
        } catch (GerritException e) {
            throw new SCStackException("Failed to initialize gerrit manager: "
                    + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Recibe los PrintWriter de los ficheros de configuración de Apache de
     * proyectos (fichero de proyectos en la carpeta sites-available de apache)
     * e imprime al final en dicho fichero la entrada correspondiente al
     * repositorio SVN del proyecto.
     * </p>
     * <p>
     * En principio sólo se escribe en el pwHTTP cuando el repositorio es
     * público, si es privado, no hay que escribir nada en ningún fichero.
     * </p>
     * 
     * @param pwHTTPS
     *            PrintWriter del fichero de configuración SSL de Apache. Por
     *            defecto: sites-available/dev.misidelab.es-ssl-projects
     * @param pwHTTP
     *            PrintWriter del fichero de configuración HTTP de Apache. Por
     *            defecto: sites-available/dev.misidelab.es-projects
     * @param cn
     *            CN del proyecto a escribir
     */
    @Override
    public void escribirEntradaApache(PrintWriter pwHTTPS, PrintWriter pwHTTP,
            String cn, String gidNumber) {

        if (this.esPublico()) {
            pwHTTP.println("\n################# CONFIGURACIÓN PROYECTO: " + cn
                    + " #################\n");
            pwHTTP.println("##### Configuración GIT-Público");
            pwHTTP.println("Alias " + "git/" + cn + " " + this.getRuta() + "/"
                    + cn);
            pwHTTP.println("<Directory " + this.getRuta() + "/" + cn + "/>");
            pwHTTP.println("    Order allow,deny");
            pwHTTP.println("    Allow from all");
            pwHTTP.println("</Directory>");
            pwHTTP.println("");
        }
    }

    /**
     * <p>
     * Crea un repositorio GIT para el proyecto que estamos creando en la Forja.
     * </p>
     * <p>
     * Genera todas las carpetas necesarias para el repositorio, tanto si es
     * público como privado e inicializa el repositorio.
     * </p>
     * <p>
     * To create a repository, we first need a Gerrit group. All users for which
     * access to the repository should be granted must belong to this group.
     * Then we create a Gerrit project (which is really a git repository) with
     * two branches: master and develop, and an initial commit.
     * </p>
     * 
     * @param cnProyecto
     *            Nombre del proyecto
     * @param uidAdminProyecto
     *            UID del usuario Administrador del proyecto
     * @throws ExcepcionConsola
     *             Cuando se produce algún error durante el acceso del método a
     *             la consola Linux del servidor.
     */
    @Override
    public void crearRepositorio(String cnProyecto, String uidAdminProyecto,
            API_Abierta apiAbierta) throws ExcepcionConsola {

        ConnectionOptions options = new ConnectionOptions();
        options.set(ConnectionOptions.OPERATING_SYSTEM,
                OperatingSystemFamily.UNIX);

        CommandLine cl = new CommandLine(new File("/opt"));

        String hostGerrit = ConfiguracionForja.hostGerrit;
        String sadminGerrit = ConfiguracionForja.sadminGerrit;

        // Check if the group exists.
        boolean exists = gerritManager.checkExistingGerritGroup(cnProyecto,
                sadminGerrit, options);

        // Create gerrit group if not exists.
        if (!exists) {
            gerritManager.createGerritGroup(cnProyecto, uidAdminProyecto, cl,
                    sadminGerrit);
        }

        // Check if project exists.
        boolean projectExists = gerritManager.checkExistingGerritProject(
                cnProyecto, sadminGerrit, options);

        if (projectExists) {
            throw new ExcepcionConsola("Repository name already exists: "
                    + cnProyecto);
        }

        // Create git project using gerrit.
        gerritManager.createGerritProject(cnProyecto, cl, sadminGerrit);

        // We need to set several permissions for refs/heads/*, refs/tags/*,
        // refs/* to the group
        configureRepository(cnProyecto, sadminGerrit, cl, hostGerrit);
    }

    /**
     * <p>
     * Configure git repository.
     * </p>
     * 
     * @param cnProyecto
     *            Project name to check with existing groups.
     * @param sadminGerrit
     *            Administrator user of Gerrit.
     * @param cl
     *            Run command line.
     * @param hostGerrit
     *            Gerrit Host to configure repository.
     * @throws ExcepcionConsola
     */
    public void configureRepository(String cnProyecto, String sadminGerrit,
            CommandLine cl, String hostGerrit) throws ExcepcionConsola {

        /*
         * Clone repository
         */
        gerritManager.cloneGerritRepositoryCm(cnProyecto, sadminGerrit,
                hostGerrit, cl);

        /*
         * "/tmp" Working Directory
         */
        cl.setWorkDir(new File("/tmp/" + cnProyecto));

        /*
         * Fetch meta/config
         */
        gerritManager.fetchMetaConfigGerritCm(cl);

        /*
         * Checkout meta/config
         */
        gerritManager.checkoutMetaConfigGerritCm(cl);

        /*
         * Update git config file
         */
        gerritManager.udpateGitConfig("/tmp/", cnProyecto);

        /*
         * Add changes to git
         */
        gerritManager.addProjectConfigToGerrit(cl);

        /*
         * Commit changes
         */
        gerritManager.commitToGerrit(cl);

        /*
         * Push to repository
         */
        gerritManager.pushToGerrit(cl);
    }

    /**
     * <p>
     * Elimina definitivamente el repositorio de un proyecto determinado.
     * </p>
     * 
     * @param cnProyecto
     *            Nombre del proyecto cuyo repositorio queremos borrar
     * @throws ExcepcionConsola
     *             Cuando se produce algún error durante la ejecución de la
     *             consola
     */
    @Override
    public void borrarRepositorio(String cnProyecto) throws ExcepcionConsola {
        throw new ExcepcionConsola("Unsupported operation");
    }

}
