package es.sidelab.scstack.lib.gerrit;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

public class GerritManager {

	private Logger log;
	private Connection conection;

	public GerritManager(Logger log) throws GerritException {
		this.log = log;

		try {
			conection = DriverManager.getConnection("jdbc:mysql://" + ConfiguracionForja.hostMysql, ConfiguracionForja.usernameMysql,
					ConfiguracionForja.passMysql);
		} catch (SQLException e) {
			throw new GerritException("Can't connect to mysql: " + e.getMessage());
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
	public void addProjectMember(String uid, String projectId) throws GerritException {

		log.info("[Gerrit] Adding user " + uid + " to project " + projectId);

		int gerritUserId = findAccountIdByUid(uid);
		int gerritProjectId = findGroupIdByCn(projectId);

		try {
			Statement stmt = conection.createStatement();
			String query = "INSERT INTO " + ConfiguracionForja.schemaGerrit + ".account_group_members VALUES(" + gerritUserId + ","
					+ gerritProjectId + ")";
			stmt.execute(query);
		} catch (SQLException e) {
			throw new GerritException("Problem adding user to group: " + e.getMessage(), e);
		}

		try {
			Statement stmt = conection.createStatement();
			Date now = new Date();
			java.sql.Date sqlDate = new java.sql.Date(now.getTime());
			String query = "INSERT INTO " + ConfiguracionForja.schemaGerrit + ".account_group_members_audit VALUES (1, NULL, NULL, "
					+ gerritUserId + ", " + gerritProjectId + ", " + sqlDate.getTime();
			stmt.execute(query);
		} catch (SQLException e) {
			throw new GerritException("Problem adding user to group: " + e.getMessage(), e);
		}

	}

	public void addUser(Usuario user) throws GerritException {

		log.info("[Gerrit] Adding user " + user.getUid() + " to Gerrit");
		
		int accountId;
		try {
			accountId = getNextId();
		} catch (SQLException e) {
			// No users??? INstaller should have added at least gerritadmin
			throw new GerritException("Problem determining next user account_id: " + e.getMessage(), e);
		}

		try {
			Statement stmt = conection.createStatement();
			Date now = new Date();
			java.sql.Date sqlDate = new java.sql.Date(now.getTime());
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO " + ConfiguracionForja.schemaGerrit + ".accounts VALUES(");
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
			stmt.execute("INSERT INTO " + ConfiguracionForja.schemaGerrit + ".account_id VALUES (" + accountId + ")");

			PreparedStatement ps = conection.prepareStatement("INSERT INTO " + ConfiguracionForja.schemaGerrit
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
			throw new GerritException("Problem updating gerrit database: " + e.getMessage(), e);
		}

	}

	public int getNextId() throws SQLException, GerritException {

		Statement stmt = conection.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT MAX(account_id) FROM " + ConfiguracionForja.schemaGerrit + ".accounts");
		if (rs.next()) {
			return rs.getInt(1) + 1;
		} else {
			throw new GerritException("Empty result");
		}

	}

	public void removeUser(String uid) throws GerritException {

		log.info("[Gerrit] Removing user " + uid + " from gerrit");

		String sshPrefix = "ssh -i /opt/ssh-keys/gerritadmin_rsa -l gerritadmin -p 29418 " + ConfiguracionForja.hostRedmine;
		CommandLine cl = new CommandLine(new File("/opt"));
		try {
			CommandOutput co = cl.syncExec(sshPrefix + " gerrit set-account --inactive " + uid);
		} catch (Exception e) {
			throw new GerritException("Problem changing account to inactive: " + e.getMessage(), e);
		}
	}

	public void removeUserFromProject(String uid, String cnProyecto) throws GerritException {

		log.info("[Gerrit] Removing user " + uid + " from project " + cnProyecto);

		try {
			Statement stmt = conection.createStatement();

			int account_id = findAccountIdByUid(uid);
			int group_id = findGroupIdByCn(cnProyecto);

			stmt.execute("DELETE FROM " + ConfiguracionForja.schemaGerrit + ".account_group_members WHERE account_id=" + account_id
					+ " AND group_id=" + group_id);
		} catch (SQLException e) {
			throw new GerritException("Couldn't remove user from project. User: " + uid + "; Project: " + cnProyecto, e);
		}
	}

	private int findGroupIdByCn(String cnProyecto) throws GerritException {

		try {
			Statement stmt = conection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + ConfiguracionForja.schemaGerrit + ".group_id FROM "
					+ ConfiguracionForja.schemaGerrit + ".account_groups WHERE " + ConfiguracionForja.schemaGerrit + ".name='" + cnProyecto
					+ "'");
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				throw new GerritException("Empty result");
			}
		} catch (SQLException e) {
			throw new GerritException("Problem finding groupId: " + cnProyecto, e);
		}
	}

	private int findAccountIdByUid(String uid) throws GerritException {

		try {
			Statement stmt = conection.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT " + ConfiguracionForja.schemaGerrit + ".account_id FROM "
					+ ConfiguracionForja.schemaGerrit + ".account_external_ids WHERE " + ConfiguracionForja.schemaGerrit
					+ ".external_id='gerrit:" + uid + "'");
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
}
