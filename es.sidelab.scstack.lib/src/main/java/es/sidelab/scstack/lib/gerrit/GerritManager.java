package es.sidelab.scstack.lib.gerrit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

public class GerritManager {

	private Logger log;
	private Connection conection;
	private OverthereOutputHandler outputHandler;

    public static String gerritListGroups = "ls-groups";
    public static String gerritListProjects = "ls-projects";

	public GerritManager(Logger log) throws GerritException {
		this.log = log;

        outputHandler = new OverthereOutputHandler();

		try {
			conection = DriverManager.getConnection("jdbc:mysql://"
					+ ConfiguracionForja.hostMysql,
					ConfiguracionForja.usernameMysql,
					ConfiguracionForja.passMysql);
		} catch (SQLException e) {
			throw new GerritException("Can't connect to mysql: "
					+ e.getMessage());
		}

	}

	/**
	 * As of Gerrit 2.5 it is not possible to add a member to an existing
	 * project using the commandline utilities
	 * (http://gerrit-documentation.googlecode
	 * .com/svn/Documentation/2.5.1/cmd-index.html).
	 * 
	 * @param uid
	 *            The user uid in ldap
	 * @param cnProyecto
	 *            The project where the user should be added
	 * @throws GerritException
	 */
	public void addProjectMember(String uid, String projectId)
			throws GerritException {

		log.info("[Gerrit] Adding user " + uid + " to project " + projectId);

		int gerritUserId = findAccountIdByUid(uid);
		int gerritProjectId = findGroupIdByCn(projectId);

		try {
			Statement stmt = conection.createStatement();
			String query = "INSERT INTO " + ConfiguracionForja.schemaGerrit
					+ ".account_group_members VALUES(" + gerritUserId + ","
					+ gerritProjectId + ")";
			stmt.execute(query);
		} catch (SQLException e) {
			throw new GerritException("Problem adding user to group: "
					+ e.getMessage(), e);
		}

		try {
			Statement stmt = conection.createStatement();
			Date now = new Date();
			java.sql.Date sqlDate = new java.sql.Date(now.getTime());
			String query = "INSERT INTO " + ConfiguracionForja.schemaGerrit
					+ ".account_group_members_audit VALUES (1, NULL, NULL, "
					+ gerritUserId + ", " + gerritProjectId + ", "
					+ sqlDate.getTime();
			stmt.execute(query);
		} catch (SQLException e) {
			throw new GerritException("Problem adding user to group: "
					+ e.getMessage(), e);
		}

	}

	public void addUser(Usuario user) throws GerritException {

		log.info("[Gerrit] Adding user " + user.getUid() + " to Gerrit");

		int accountId;
		try {
			accountId = getNextId();
		} catch (SQLException e) {
			// No users??? INstaller should have added at least gerritadmin
			throw new GerritException(
					"Problem determining next user account_id: "
							+ e.getMessage(), e);
		}

		try {
			Statement stmt = conection.createStatement();
			Date now = new Date();
			java.sql.Date sqlDate = new java.sql.Date(now.getTime());
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO " + ConfiguracionForja.schemaGerrit
					+ ".accounts VALUES(");
			sb.append(sqlDate.getTime());
			sb.append(",");
			sb.append(user.getNombre() + " " + user.getApellidos());
			sb.append(",");
			sb.append(user.getEmail());
			sb.append(",");
			sb.append("NULL, 25, 'Y', 'Y', NULL, NULL, 'N', NULL, NULL, 'N', 'N', 'N'");
			sb.append(accountId);

			stmt.execute(sb.toString());

			stmt = conection.createStatement();
			stmt.execute("INSERT INTO " + ConfiguracionForja.schemaGerrit
					+ ".account_id VALUES (" + accountId + ")");

			PreparedStatement ps = conection.prepareStatement("INSERT INTO "
					+ ConfiguracionForja.schemaGerrit
					+ ".account_external_ids VALUES (?,?,?,?)");
			ps.setInt(1, accountId);
			ps.setString(2, user.getEmail());
			ps.setString(3, "null");
			ps.setString(4, "gerrit:" + user.getUid());
			ps.execute();

			ps.setInt(1, accountId);
			ps.setString(2, user.getEmail());
			ps.setString(3, "null");
			ps.setString(4, "username:" + user.getUid());
			ps.execute();
		} catch (SQLException e) {
			throw new GerritException("Problem updating gerrit database: "
					+ e.getMessage(), e);
		}

	}

	public int getNextId() throws SQLException, GerritException {

		Statement stmt = conection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MAX(account_id) FROM "
				+ ConfiguracionForja.schemaGerrit + ".accounts");
		if (rs.next()) {
			return rs.getInt(1) + 1;
		} else {
			throw new GerritException("Empty result");
		}

	}

	public void removeUser(String uid) throws GerritException {

		log.info("[Gerrit] Removing user " + uid + " from gerrit");

		String sshPrefix = "ssh -i /opt/ssh-keys/gerritadmin_rsa -l gerritadmin -p 29418 "
				+ ConfiguracionForja.hostRedmine;
		CommandLine cl = new CommandLine(new File("/opt"));
		try {
			CommandOutput co = cl.syncExec(sshPrefix
					+ " gerrit set-account --inactive " + uid);
		} catch (Exception e) {
			throw new GerritException("Problem changing account to inactive: "
					+ e.getMessage(), e);
		}
	}

	public void removeUserFromProject(String uid, String cnProyecto)
			throws GerritException {

		log.info("[Gerrit] Removing user " + uid + " from project "
				+ cnProyecto);

		try {
			Statement stmt = conection.createStatement();

			int account_id = findAccountIdByUid(uid);
			int group_id = findGroupIdByCn(cnProyecto);

			stmt.execute("DELETE FROM " + ConfiguracionForja.schemaGerrit
					+ ".account_group_members WHERE account_id=" + account_id
					+ " AND group_id=" + group_id);
		} catch (SQLException e) {
			throw new GerritException(
					"Couldn't remove user from project. User: " + uid
							+ "; Project: " + cnProyecto, e);
		}
	}

	private int findGroupIdByCn(String cnProyecto) throws GerritException {

		try {
			Statement stmt = conection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT "
					+ ConfiguracionForja.schemaGerrit + ".group_id FROM "
					+ ConfiguracionForja.schemaGerrit
					+ ".account_groups WHERE "
					+ ConfiguracionForja.schemaGerrit + ".name='" + cnProyecto
					+ "'");
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				throw new GerritException("Empty result");
			}
		} catch (SQLException e) {
			throw new GerritException("Problem finding groupId: " + cnProyecto,
					e);
		}
	}

	private int findAccountIdByUid(String uid) throws GerritException {

		try {
			Statement stmt = conection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT "
					+ ConfiguracionForja.schemaGerrit + ".account_id FROM "
					+ ConfiguracionForja.schemaGerrit
					+ ".account_external_ids WHERE "
					+ ConfiguracionForja.schemaGerrit + ".external_id='gerrit:"
					+ uid + "'");
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				throw new GerritException("Empty result");
			}
		} catch (SQLException e) {
			throw new GerritException("Couldn't find user id: " + uid, e);
		}

	}

	public void removeProject(Proyecto proyecto) {
		// We do nothing here. Project should be "removed" from gui

	}

    /**
     * <p>
     * Clone repository using sadminUID permissions.
     * </p>
     * 
     * @param cnProyecto
     *            project to clone.
     * @param sadminGerrit
     *            Gerrit super administrator user.
     * @param hostGerrit
     *            Gerrit host.
     * @param cl
     *            Run the command
     * @throws ExcepcionConsola
     */
    public void cloneGerritRepositoryCm(String cnProyecto, String sadminGerrit, String hostGerrit, CommandLine cl)
            throws ExcepcionConsola {

        String cmd = "git clone --config user.email="
                + sadminGerrit + "@"
                + hostGerrit + " --config user.name="
                + sadminGerrit + " ssh://"
                + sadminGerrit + "@"
                + hostGerrit + ":29418/" + cnProyecto
                + " /tmp/" + cnProyecto;
        log.info("[Gerrit] " + cmd);

        CommandOutput co;
        try {
            co = cl.syncExec(cmd);
            log.info("git clone: " + getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem with git clone: "
                    + e.getMessage());
        }
    }

    /**
     * <p>
     * Retrieve 'meta/config' from repository.
     * </p>
     * 
     * @param cl
     *            Run the command
     * @throws ExcepcionConsola
     */
    public void fetchMetaConfigGerritCm(CommandLine cl) throws ExcepcionConsola {
        try {
            String cmd = "git fetch origin refs/meta/config:refs/remotes/origin/meta/config";
            log.info("[Gerrit] " + cmd);

            CommandOutput co;
            co = cl.syncExec(cmd);
            log.info("git fetch origin: " + getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem with git fetch origin: "
                    + e.getMessage());
        }
    }

	/**
     * <p>
	 * Checkout 'meta/config' from cloned repository.
     * </p>
     * 
	 * @param cl Run the command
	 * @throws ExcepcionConsola
	 */
	public void checkoutMetaConfigGerritCm(CommandLine cl)
			throws ExcepcionConsola {
		try {
			String cmd = "git checkout meta/config";
			log.info("[Gerrit] " + cmd);

			CommandOutput co = cl.syncExec(cmd);

			log.info("git checkout meta/config: " + getCommandOutput(co));

		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with checkout meta/config: "
					+ e.getMessage());
		}
	}

	/**
     * <p>
	 * Retrieve executed command output.
     * </p>
	 * 
	 * @param co Command executed
	 * @return
	 */
	public String getCommandOutput(CommandOutput co) {
		return "[stdout:" + co.getStandardOutput() + ";stderr:"
				+ co.getErrorOutput() + "]";
	}

    /**
     * <p>
     * Update configuration file from selected project.
     * </p>
     * 
     * <p>
     * TODO: Update using git config command, see
     * {@link #gitConfigProject(ConnectionOptions, String, String, String)}.
     * </p>
     * 
     * @param cnProyecto
     *            project to update.
     * @throws ExcepcionConsola
     */
	public void udpateGitConfig(String projectDirectory, String cnProyecto) throws ExcepcionConsola {
		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
					projectDirectory, cnProyecto + "/project.config"), true));
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

    /**
     * <p>
     * Git project configuration updates project.config file usgin git config
     * command.
     * </p>
     * 
     * @param cl
     *            Run the command.
     * @param reference
     *            Reference to update permissions.
     * @param permission
     *            Type of permission assigned.
     * @param cnProyecto
     *            Project group to add permissions.
     * @throws ExcepcionConsola
     */
    public void gitConfigProject(ConnectionOptions options, String reference,
            String permission, String cnProyecto) throws ExcepcionConsola {

        CommandLine cl = new CommandLine(new File("/usr/bin/"));
        cl.setWorkDir(new File("/home/ricardo/tmp/" + cnProyecto));

        CommandOutput co;

        /*
         * git config -f project.config --add access.refs/*.Read
         * "group someproject-admin"
         */
        String cmd = "git config --file project.config --add access.";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(cmd);
        stringBuilder.append(reference);
        stringBuilder.append(".");
        stringBuilder.append(permission);
        stringBuilder.append(" \"group ");
        stringBuilder.append(cnProyecto);
        stringBuilder.append("\"");

        log.info("[Gerrit] " + stringBuilder.toString());
        try {
            
            co = cl.syncExec(stringBuilder.toString());
            log.info("git add: " + getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem with git config: "
                    + e.getMessage());
        }

        // log.info("[Gerrit] " + stringBuilder.toString());
        try {

            OverthereConnection conn = Overthere
                    .getConnection("local", options);
            OverthereFile workingDirectory = conn
                    .getFile("/home/ricardo/tmp/" + cnProyecto);
            conn.setWorkingDirectory(workingDirectory);

            StringBuilder groupBuilder = new StringBuilder();
            groupBuilder.append("group ");
            groupBuilder.append(cnProyecto);
            conn.execute(outputHandler, CmdLine.build("git", "config",
                    "-f", "project.config", "--add", "access."+reference+"."+permission,
                    groupBuilder.toString()));

            // co = cl.syncExec(stringBuilder.toString());
            // log.info("git add: " + getCommandOutput(co));
            log.info("[Gerrit] [stdout] " + outputHandler.getOut());
            log.info("[Gerrit] [err] " + outputHandler.getErr());

        } catch (Exception e) {
            throw new ExcepcionConsola("Problem with git config: "
                    + e.getMessage());
        }
    }

	/**
     * <p>
	 * Add changes to commit using git.
     * </p>
	 * 
	 * @param cl Run the command
	 * @throws ExcepcionConsola
	 */
	public void addProjectConfigToGerrit(CommandLine cl)
			throws ExcepcionConsola {
		CommandOutput co;
		String cmd = "git add project.config";
		log.info("[Gerrit] " + cmd);
		try {
			co = cl.syncExec(cmd);
			log.info("git add: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git add: "
					+ e.getMessage());
		}
	}

	/**
     * <p>
	 * Commit changes to git project.
     * </p>
	 * 
	 * @param cl Run the command
	 * @throws ExcepcionConsola
	 */
	public void commitToGerrit(CommandLine cl) throws ExcepcionConsola {
		CommandOutput co;
		String cmd = "git commit -m 'udpate'";
		log.info("[Gerrit] " + cmd);
		try {
			co = cl.syncExec(cmd);
			log.info("git commit: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git commit: "
					+ e.getMessage());
		}
	}

	/**
     * <p>
	 * Push changes to git project
     * </p>
	 * 
	 * @param cl Run the command
	 * @throws ExcepcionConsola
	 */
	public void pushToGerrit(CommandLine cl) throws ExcepcionConsola {

	    CommandOutput co;
		String cmd = "git push origin HEAD:refs/meta/config";
		try {
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
			log.info("git push: " + getCommandOutput(co));

		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git push: "
					+ e.getMessage());
		}
	}

    /**
     * <p>
     * Check existing Gerrit group using Gerrit jargon ssh commands.
     * </p>
     * 
     * <p>
     * Use {@link CommandLine} instead of {@link Overthere} because Overthere
     * retrieve groups wrong.
     * </p>
     * 
     * @param hostGerrit
     *            url where is Gerrit.
     * @param sadminGerrit
     *            Gerrit super administrator user.
     * @param cnProyecto
     *            Project name to check with existing groups.
     * @param sshDirectory
     *            ssh key directory prefix to run Gerrit command.
     * @param options
     *            Options to execute Gerrit command.
     * @return
     * @throws ExcepcionConsola
     */
    public boolean checkExistingGerritGroup(String cnProyecto,
            String hostGerrit, String sadminGerrit, String sshDirectory,
            ConnectionOptions options) throws ExcepcionConsola {

        boolean groupExists = false;

        String sshPrefix = "ssh -i " + sshDirectory + " -l " + sadminGerrit
                + " -p 29418 " + hostGerrit;

        CommandLine cl = new CommandLine(new File("/usr/bin/"));
        CommandOutput co;

        try {

            String cmd = sshPrefix + " gerrit ls-groups";
            log.info("[Gerrit] " + cmd);
            co = cl.syncExec(cmd);
            log.info(getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem creating Gerrit group: "
                    + e.getMessage());
        }

        StringTokenizer st = new StringTokenizer(
                getCommandOutput(co), "\n");
        while (!groupExists && st.hasMoreTokens()) {
            String name = st.nextToken();
            if (name.equals(cnProyecto)) {
                groupExists = true;
            }
        }

        return groupExists;
    }

    /**
     * <p>
     * Check existing Gerrit Project using Gerrit jargon ssh commands and
     * {@link OverthereConnection}.
     * </p>
     * 
     * @param hostGerrit
     *            url where is Gerrit.
     * @param sadminGerrit
     *            Gerrit super administrator user.
     * @param cnProyecto
     *            Project name to check with existing groups.
     * @param sshDirectory
     *            ssh key directory prefix to run Gerrit command.
     * @param options
     *            Options to execute Gerrit command.
     * @return
     * @throws ExcepcionConsola
     */
    public boolean checkExistingGerritProject(String cnProyecto,
            String hostGerrit, String sadminGerrit, String sshDirectory,
            ConnectionOptions options) throws ExcepcionConsola {

        boolean projectExists = checkExistingGerritConfiguration(cnProyecto,
                hostGerrit, sadminGerrit, sshDirectory, gerritListProjects,
                options);

        return projectExists;
    }

    /**
     * <p>
     * Check existing Gerrit configuration using Gerrit jargon ssh commands and
     * {@link OverthereConnection}.
     * </p>
     * 
     * <p>
     * Check existing groups or projects using 'gerritCommand' parameter.
     * </p>
     * 
     * @param cnProyecto
     *            Project name to check with existing groups.
     * @param hostGerrit
     *            url where is Gerrit.
     * @param sadminGerrit
     *            Gerrit super administrator user.
     * @param sshDirectory
     *            ssh key directory prefix to run Gerrit command.
     * @param gerritCommand
     *            Parameter to retrieve and check groups or projects:
     *            <ul>
     *            <li><b>ls-projects</b></li>
     *            <li><b>ls-groups</b></li>
     *            </ul>
     * @param options
     *            Options to execute Gerrit command.
     * @return
     * @throws ExcepcionConsola
     */
    public boolean checkExistingGerritConfiguration(String cnProyecto,
            String hostGerrit, String sadminGerrit, String sshDirectory,
            String gerritCommand, ConnectionOptions options)
            throws ExcepcionConsola {

        try {

            String sshPrefix = "ssh -i " + sshDirectory + " -l " + sadminGerrit
                    + " -p 29418 " + hostGerrit;
            String cmd = sshPrefix + " gerrit ls-projects";
            log.info("[Gerrit] " + cmd);

            OverthereConnection conn = Overthere
                    .getConnection("local", options);

            conn.execute(outputHandler, CmdLine.build("ssh", "-i",
                    sshDirectory, "-l", sadminGerrit, "-p",
                    "29418", hostGerrit, "gerrit", gerritCommand));

            log.info("[Gerrit] [stdout] " + outputHandler.getOut());
            log.info("[Gerrit] [err] " + outputHandler.getErr());

        } catch (Exception e) {
            throw new ExcepcionConsola("Problem listing Gerrit projects: "
                    + e.getMessage());
        }

        boolean exists = false;
        StringTokenizer st = new StringTokenizer(outputHandler.getOut(), "\n");
        while (!exists && st.hasMoreTokens()) {
            String name = st.nextToken();
            if (name.equals(cnProyecto)) {
                exists = true;
            }
        }
        return exists;
    }

    /**
     * <p>
     * Create git project in gerrit jargon.
     * </p>
     * 
     * @param cnProyecto
     *            Project name to check with existing groups.
     * @param cl
     *            Run the command
     * @param sshDirectory
     *            ssh key directory prefix to run Gerrit command.
     * @param sadminGerrit
     *            Gerrit super administrator user.
     * @param hostGerrit
     *            url where is Gerrit.
     * 
     * @throws ExcepcionConsola
     */
    public void createGerritProject(String cnProyecto, CommandLine cl,
            String sshDirectory, String sadminGerrit, String hostGerrit)
            throws ExcepcionConsola {

        String sshPrefix = "ssh -i " + sshDirectory + " -l " + sadminGerrit
                + " -p 29418 " + hostGerrit;

        // Now we are ready to create the repo (a project in Gerry jargon)
        try {
            String cmd = sshPrefix + " gerrit create-project --owner "
                    + cnProyecto
                    + " --branch master --branch develop --empty-commit "
                    + cnProyecto;
            log.info("[Gerrit] " + cmd);
            CommandOutput co = cl.syncExec(cmd);
            log.info("Creating project:" + getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem creating Gerrit project: "
                    + e.getMessage());
        }
    }

    /**
     * <p>
     * Create git group in gerrit jargon.
     * </p>
     * 
     * <p>
     * TODO: Create multiple users for group.
     * </P>
     * 
     * @param cnProyecto
     *            Project name.
     * @param uidAdminProyecto
     *            Project adminstrator.
     * @param cl
     *            Run the command.
     * @param sshPrefix
     *            SSH configuration for admin user.
     * @throws ExcepcionConsola
     */
    public void createGerritGroup(String cnProyecto, String uidAdminProyecto,
            CommandLine cl, String sshPrefix) throws ExcepcionConsola {
        // We need to create the group. uidAdminProyecto should already be a
        // member of Gerrit
        try {
            String cmd = sshPrefix + " gerrit create-group --member "
                    + uidAdminProyecto + " " + cnProyecto;
            log.info("[Gerrit] " + cmd);
            CommandOutput co = cl.syncExec(cmd);
            log.info(getCommandOutput(co));
        } catch (Exception e) {
            throw new ExcepcionConsola("Problem creating Gerrit group: "
                    + e.getMessage());
        }
    }
}
