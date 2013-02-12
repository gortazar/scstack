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

import static org.junit.Assert.assertNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcessOutputHandler;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
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

	private String hostGerrit = "redmine.scstack.org";

	/* PARÁMETROS DE CONFIGURACIÓN */
	private String uidSuperAdmin = "sadmin";

	/* OutputHandler */
	private OverthereOutputHandler outputHandler;
	private ConnectionOptions options;
	private String sshDirectory;

	private static class OverthereOutputHandler implements
			OverthereProcessOutputHandler {

		StringBuilder out = new StringBuilder();
		StringBuilder err = new StringBuilder();

		@Override
		public void handleErrorLine(String arg0) {
			err.append(arg0);
		}

		@Override
		public void handleOutput(char arg0) {
			// Do nothing!
		}

		@Override
		public void handleOutputLine(String arg0) {
			out.append(arg0);
		}

		public String getErr() {
			return err.toString();
		}

		public String getOut() {
			return out.toString();
		}
	}

	@Before
	public void setUp() throws Exception {

		this.log = Logger.getLogger(GerritManagerTest.class.getName());

		options = new ConnectionOptions();
		options.set(ConnectionOptions.OPERATING_SYSTEM,
				OperatingSystemFamily.UNIX);

		sshDirectory = "$HOME/.ssh/gerritadmin_rsa";

	}

	/**
	 * Test to check gerrit ssh commands.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGerritListGroups() throws Exception {

		ConnectionOptions options = new ConnectionOptions();
		options.set(ConnectionOptions.OPERATING_SYSTEM,
				OperatingSystemFamily.UNIX);

		String sshPrefix = "ssh -i " + sshDirectory + " -l sadmin -p 29418 "
				+ hostGerrit;

		outputHandler = new OverthereOutputHandler();

		try {
			String cmd = sshPrefix + " gerrit ls-groups";
			log.info("[Gerrit] " + cmd);
			OverthereConnection conn = Overthere
					.getConnection("local", options);
			conn.execute(outputHandler, CmdLine.build("ssh", "-i",
					sshDirectory, "-l", "sadmin", "-p", "29418", hostGerrit,
					"gerrit", "ls-groups"));

			log.info("[Gerrit] [stdout] " + outputHandler.getOut());
			log.info("[Gerrit] [err] " + outputHandler.getErr());
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem listing Gerrit groups: "
					+ e.getMessage());
		}

		assertNotNull("Error receiving group list from Gerrit",
				outputHandler.getOut());

	}

	/**
	 * Create project using gerrit ssh command.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateGerritProject() throws Exception {

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

		// sshAgentPrefix
		String sshAgentPrefix = "ssh-agent bash -c '" + sshDirectory + " ; ";

		CommandLine cl = new CommandLine(new File("/usr/bin/"));

		OverthereConnection connection = Overthere.getConnection("local",
				options);

		/*
		 * Clone repository
		 */
		cloneRepositoryCm(cnProyecto, sshAgentPrefix, connection, cl);
		// cloneRepositoryGerrit(cnProyecto, sshAgentPrefix, connection);

		cl.setWorkDir(new File("/home/ricardo/tmp/" + cnProyecto));

		/*
		 * Fetch meta/config
		 */
		// fetchMetaConfigGerrit(sshAgentPrefix, connection);
		fetchMetaConfigGerritCm(cnProyecto, cl);

		/*
		 * Checkout meta/config
		 */
		// checkoutMetaConfigGerrit(sshAgentPrefix, connection);
		checkoutMetaConfigGerritCm(cnProyecto, cl);

		/*
		 * Update git config file
		 */
		 udpateGitConfig(cnProyecto);

		/*
		 * Add changes to git
		 */
		 addProjectConfigToGerrit(cl);

		/*
		 * Commit changes
		 */
		 commitToGerrit(cl);

		/*
		 * Push to repository
		 */
		 pushToGerrit(cl, sshAgentPrefix);
	}

	public void cloneRepositoryGerrit(String cnProyecto, String sshAgentPrefix,
			OverthereConnection connection) throws ExcepcionConsola {
		try {
			String cmd = sshAgentPrefix + "git clone --config user.email="
					+ uidSuperAdmin + "@" + hostGerrit + " --config user.name="
					+ uidSuperAdmin + " ssh://" + uidSuperAdmin + "@"
					+ hostGerrit + ":29418/" + cnProyecto + "'";
			log.info("[Gerrit] " + cmd);

			outputHandler = new OverthereOutputHandler();

			connection.execute(
					outputHandler,
					CmdLine.build("ssh-agent", "bash", "-c", "'ssh-add "
							+ sshDirectory + " ; "
							+ "git clone --config user.email=" + uidSuperAdmin
							+ "@" + hostGerrit + " --config user.name="
							+ uidSuperAdmin + " ssh://" + uidSuperAdmin + "@"
							+ hostGerrit + ":29418/" + "/home/ricardo/tmp/"
							+ cnProyecto));
			log.info("git clone: " + outputHandler.getOut());
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem cloning repository: "
					+ e.getMessage());
		}
	}

	public void cloneRepositoryCm(String cnProyecto, String sshAgentPrefix,
			OverthereConnection connection, CommandLine cl)
			throws ExcepcionConsola {

		try {
			String cmd = sshAgentPrefix + "git clone --config user.email="
					+ uidSuperAdmin + "@" + hostGerrit + " --config user.name="
					+ uidSuperAdmin + " ssh://" + uidSuperAdmin + "@"
					+ hostGerrit + ":29418/" + cnProyecto + " $HOME/tmp/"
					+ cnProyecto + "'";
			log.info("[Gerrit] " + cmd);

			CommandOutput co;
			try {
				co = cl.syncExec("git clone --config user.email="
						+ uidSuperAdmin + "@" + hostGerrit
						+ " --config user.name=" + uidSuperAdmin + " ssh://"
						+ uidSuperAdmin + "@" + hostGerrit + ":29418/"
						+ cnProyecto + " /home/ricardo/tmp/" + cnProyecto);
				log.info("git clone: " + getCommandOutput(co));
			} catch (Exception e) {
				throw new ExcepcionConsola("Problem with git clone: "
						+ e.getMessage());
			}

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

	public void fetchMetaConfigGerritCm(String cnProyecto, CommandLine cl)
			throws ExcepcionConsola {
		try {
			String cmd = "git fetch origin refs/meta/config:refs/remotes/origin/meta/config";
			log.info("[Gerrit] " + cmd);

			CommandOutput co;
			co = cl.syncExec("git fetch origin refs/meta/config:refs/remotes/origin/meta/config");
			log.info("git fetch origin: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git fetch origin: "
					+ e.getMessage());
		}
	}

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

	public void checkoutMetaConfigGerritCm(String cnProyecto, CommandLine cl)
			throws ExcepcionConsola {
		try {
			String cmd = "git checkout meta/config";
			log.info("[Gerrit] " + cmd);

			CommandOutput co = cl
					.syncExec("git checkout meta/config");

			log.info("git checkout meta/config: " + getCommandOutput(co));

		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with checkout meta/config: "
					+ e.getMessage());
		}
	}

	private String getCommandOutput(CommandOutput co) {
		return "[stdout:" + co.getStandardOutput() + ";stderr:"
				+ co.getErrorOutput() + "]";
	}

	public void udpateGitConfig(String cnProyecto) throws ExcepcionConsola {
		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					"/home/ricardo/tmp", cnProyecto + "/project.config"), true));
			bw.write("[access \"refs/*\"]");
			bw.newLine();
			bw.write("\tpushMerge = group " + cnProyecto);
			bw.newLine();
			bw.write("[access \"refs/heads/*\"]");
			bw.newLine();
			bw.write("\tread = group " + cnProyecto);
			bw.newLine();
			bw.write("\tcreate = group " + cnProyecto);
			bw.newLine();
			bw.write("\tpush = group " + cnProyecto);
			bw.newLine();
			bw.write("[access \"refs/tags/*\"]");
			bw.newLine();
			bw.write("\tpushTag = group " + cnProyecto);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			throw new ExcepcionConsola("Problem writing permissions: "
					+ e.getMessage());
		}
	}

	public void addProjectConfigToGerrit(CommandLine cl)
			throws ExcepcionConsola {
		CommandOutput co;
		try {
			co = cl.syncExec("git add project.config");
			log.info("git add: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git add: "
					+ e.getMessage());
		}
	}

	public void commitToGerrit(CommandLine cl) throws ExcepcionConsola {
		CommandOutput co;
		try {
			co = cl.syncExec("git commit -m \"updated permissions by scstack\"");
			log.info("git commit: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git commit: "
					+ e.getMessage());
		}
	}

	public void pushToGerrit(CommandLine cl, String sshAgentPrefix)
			throws ExcepcionConsola {
		CommandOutput co;
		try {
			String cmd = sshAgentPrefix
					+ "git push origin meta/config:meta/config'";
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
			log.info("git push: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git push: "
					+ e.getMessage());
		}
	}

}