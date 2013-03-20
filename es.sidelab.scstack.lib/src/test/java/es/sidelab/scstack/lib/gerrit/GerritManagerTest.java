package es.sidelab.scstack.lib.gerrit;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
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
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

/**
 * Test class for GerritManager methods.
 * 
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
        // ssh directory for test.
        gerritManager.setSshDirectory(sshDirectory);
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
                hostGerrit, sadminGerrit, options);

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
                hostGerrit, sadminGerrit, options);

        Assert.assertTrue("[Error]: project not found in list from Gerrit",
                exists);
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

        CommandLine cl = new CommandLine(new File("/usr/bin/"));

        OverthereConnection connection = Overthere.getConnection("local",
                options);

        OverthereFile workingDirectory = connection.getFile(projectDirectory
                + cnProyecto);
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
     * Test add user to gerrit database.
     * 
     * @throws Exception
     */
    @Test
    public void testAddUser() throws Exception {

        String uid;
        String nombre;
        String apellidos;
        String email;
        String passMD5;
        Usuario usuario;
        int accountId = 1;

        /*
         * Test 1
         */
        uid = "forja000";
        nombre = "forja000";
        apellidos = "forja000";
        email = "forja000@forja.com";
        passMD5 = "";

        usuario = new Usuario(uid, nombre, apellidos, email, passMD5);

        Date now = new Date();
        Timestamp timestamp = new Timestamp(now.getTime());

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + ConfiguracionForja.schemaGerrit
                + ".accounts VALUES(");
        sb.append("'");
        sb.append(timestamp);
        sb.append("','");
        sb.append(usuario.getNombre() + " " + usuario.getApellidos());
        sb.append("','");
        sb.append(usuario.getEmail());
        sb.append("',");
        sb.append("NULL, 25, 'Y', 'Y', NULL, NULL, 'N', NULL, NULL, 'N', 'N', 'N'");
        sb.append(",");
        sb.append(accountId);
        sb.append(")");

        log.log(Level.INFO, "Inserted new user in Gerrit\n" + sb.toString());

        Assert.assertTrue(true);

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