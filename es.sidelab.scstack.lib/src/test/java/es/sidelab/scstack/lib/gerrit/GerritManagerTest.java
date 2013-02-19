package es.sidelab.scstack.lib.gerrit;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GerritManagerTest.java
 * Autor: -
 * Fecha: -
 * Revisión: -
 * Versión: -
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import java.io.File;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

/**
 * Esta clase de pruebas es la encargada de probar el correcto funcionamiento de
 * los distintos métodos ofrecidos por la API de la Forja.
 * 
 * @author Arek Klauza
 */
public class GerritManagerTest {

    private Logger log;

    private GerritManager gerritManager;

    /* PARÁMETROS DE CONFIGURACIÓN */
    private String sadminGerrit;
    private String hostGerrit;

    /* OutputHandler */
    private OverthereOutputHandler outputHandler;
    private ConnectionOptions options;
    private String sshDirectory;

    @Before
    public void setUp() throws Exception {

        this.log = Logger.getLogger(GerritManagerTest.class.getName());

        options = new ConnectionOptions();
        options.set(ConnectionOptions.OPERATING_SYSTEM,
                OperatingSystemFamily.UNIX);
        sadminGerrit = "sadmin";
        hostGerrit = "redmine.scstack.org";
        sshDirectory = "/home/ricardo/.ssh/gerritadmin_rsa";

        gerritManager = new GerritManager(this.log);
    }

    /**
     * Test to check existing Gerrit groups.
     * 
     * @throws Exception
     */
    @Test
    public void testCheckExistingGerritGroup() throws Exception {

        String cnProyecto = "gittest";

        boolean exists = gerritManager.checkExistingGerritGroup(cnProyecto,
                hostGerrit, sadminGerrit, sshDirectory, options);

        Assert.assertTrue(
                "[Error]: receiving group not found in list from Gerrit",
                exists);

    }

    /**
     * Test to check existing Gerrit projects.
     * 
     * @throws Exception
     */
    @Test
    public void testCheckExistingGerritProject() throws Exception {

        String cnProyecto = "gittest";

        boolean exists = gerritManager.checkExistingGerritProject(cnProyecto,
                hostGerrit, sadminGerrit, sshDirectory, options);

        Assert.assertTrue("[Error]: project not found in list from Gerrit",
                exists);
    }

    /**
     * Test to check existing update project.config git file.
     * 
     * @throws Exception
     */
    @Test
    public void testGitConfigProject() {

        String cnProyecto = "gittest";
        String reference = "refs/*";
        String permission = "read";

        /*
         * Command to launch in test:
         * 
         * git config -f project.config --add access.refs/*.Read
         * "group someproject-admin"
         */
        try {
            gerritManager.gitConfigProject(options, reference, permission,
                    cnProyecto);
        } catch (ExcepcionConsola e) {
            Assert.assertTrue(
                    "[Error]: Exception updatign permission in project:\t"
                            + e.getStackTrace(), true);
        }
        Assert.assertTrue(true);
    }
    
    /**
     * Configuration for a new project. Update repository properties.
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateRepositoryProperties() throws Exception {

        /*
         * Configure project gittest
         */
        String cnProyecto = "gittest";

        String projectDirectory = "/tmp/";

        // sshAgentPrefix
        String sshAgentPrefix = "ssh-agent bash -c '" + sshDirectory + " ; ";

        CommandLine cl = new CommandLine(new File("/usr/bin/"));

        OverthereConnection connection = Overthere.getConnection("local",
                options);

        OverthereFile workingDirectory = connection
                .getFile(projectDirectory + cnProyecto);
        connection.setWorkingDirectory(workingDirectory);

        /*
         * Clone repository
         */
        gerritManager.cloneGerritRepositoryCm(cnProyecto, sadminGerrit,
                hostGerrit, cl);

        /*
         * "/home/ricardo/tmp" Working Directory
         */
        cl.setWorkDir(new File(projectDirectory + cnProyecto));

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
        gerritManager.udpateGitConfig(projectDirectory, cnProyecto);

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

    @Deprecated
    public void cloneRepositoryGerrit(String cnProyecto, String sshAgentPrefix,
            OverthereConnection connection) throws ExcepcionConsola {
        try {
            String cmd = sshAgentPrefix + "git clone --config user.email="
                    + sadminGerrit + "@" + hostGerrit + " --config user.name="
                    + sadminGerrit + " ssh://" + sadminGerrit + "@"
                    + hostGerrit + ":29418/" + cnProyecto + "'";
            log.info("[Gerrit] " + cmd);

            outputHandler = new OverthereOutputHandler();

            connection.execute(
                    outputHandler,
                    CmdLine.build("ssh-agent", "bash", "-c", "'ssh-add "
                            + sshDirectory + " ; "
                            + "git clone --config user.email=" + sadminGerrit
                            + "@" + hostGerrit + " --config user.name="
                            + sadminGerrit + " ssh://" + sadminGerrit + "@"
                            + hostGerrit + ":29418/" + "/home/ricardo/tmp/"
                            + cnProyecto));
            log.info("git clone: " + outputHandler.getOut());
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem cloning repository: "
                    + e.getMessage());
        }
    }

    @Deprecated
    public void fetchMetaConfigGerrit(String sshAgentPrefix,
            OverthereConnection connection) throws ExcepcionConsola {
        try {
            String cmd = sshAgentPrefix
                    + "git fetch origin refs/meta/config:refs/remotes/origin/meta/config'";
            log.info("[Gerrit] " + cmd);

            outputHandler = new OverthereOutputHandler();

            connection
                    .execute(
                            outputHandler,
                            CmdLine.build(
                                    "ssh-agent",
                                    "bash",
                                    "-c",
                                    "'ssh-add "
                                            + sshDirectory
                                            + " ; "
                                            + "git fetch origin refs/meta/config:refs/remotes/origin/meta/config'"));
            log.info("git fetch: " + outputHandler.getOut());
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem fetching meta/config: "
                    + e.getMessage());
        }
    }

    @Deprecated
    public void checkoutMetaConfigGerrit(String sshAgentPrefix,
            OverthereConnection connection) throws ExcepcionConsola {
        try {
            String cmd = sshAgentPrefix + "git checkout meta/config'";
            log.info("[Gerrit] " + cmd);

            outputHandler = new OverthereOutputHandler();

            connection.execute(
                    outputHandler,
                    CmdLine.build("ssh-agent", "bash", "-c", "'ssh-add "
                            + sshDirectory + " ; "
                            + "git checkout meta/config'"));
            log.info("git fetch: " + outputHandler.getOut());

        } catch (Exception e) {
            throw new ExcepcionConsola("Problem with checkout meta/config: "
                    + e.getMessage());
        }
    }

}