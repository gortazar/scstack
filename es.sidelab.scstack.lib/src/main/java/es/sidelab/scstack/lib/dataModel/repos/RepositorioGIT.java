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

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.ssh.SshConnectionType;

/**
 * 
 * @author Arek Klauza
 */
public class RepositorioGIT extends Repositorio {
	
	
	private static class OverthereOutputHandler implements OverthereProcessOutputHandler {

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
	
	
	/** String que identifica el tipo de repositorio */
	public static String tipo = "GIT";
	public Logger log = Logger.getLogger(RepositorioGIT.class.getName());

	public RepositorioGIT(boolean esPublico, String ruta) {
		super(esPublico, ruta, tipo);
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
	public void escribirEntradaApache(PrintWriter pwHTTPS, PrintWriter pwHTTP, String cn, String gidNumber) {
		
		if (this.esPublico()) {
			pwHTTP.println("\n################# CONFIGURACIÓN PROYECTO: " + cn + " #################\n");
			pwHTTP.println("##### Configuración GIT-Público");
			pwHTTP.println("Alias " + "git/" + cn + " " + this.getRuta() + "/" + cn);
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
	public void crearRepositorio(String cnProyecto, String uidAdminProyecto, API_Abierta apiAbierta) throws ExcepcionConsola {

		ConnectionOptions options = new ConnectionOptions();
		options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
		
		String sshPrefix = "ssh -i /opt/ssh-keys/gerritadmin_rsa -l gerritadmin -p 29418 " + apiAbierta.getConfiguration().hostGerrit;

		CommandLine cl = new CommandLine(new File("/opt"));
		CommandOutput co = null;
		OverthereOutputHandler outputHandler = null;
		try {
			String cmd = sshPrefix + " gerrit ls-groups";
			log.info("[Gerrit] " + cmd);
			OverthereConnection conn = Overthere.getConnection("local", options);
			outputHandler = new OverthereOutputHandler();
			conn.execute(outputHandler, CmdLine.build(
					"ssh",
					"-i",
					"/opt/ssh-keys/gerritadmin_rsa",
					"-l",
					"gerritadmin",
					"-p",
					"29418",
					apiAbierta.getConfiguration().hostGerrit,
					"gerrit",
					"ls-groups"));
//			co = cl.syncExec(cmd);
//			log.info("[Gerrit] [stdout] " + co.getStandardOutput());
//			log.info("[Gerrit] [err] " + co.getErrorOutput());
			log.info("[Gerrit] [stdout] " + outputHandler.getOut());
			log.info("[Gerrit] [err] " + outputHandler.getErr());
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem listing Gerrit groups: " + e.getMessage());
		}

		// Check for group existence
		boolean exists = false;
//		StringTokenizer st = new StringTokenizer(co.getStandardOutput(), "\n");
		StringTokenizer st = new StringTokenizer(outputHandler.getOut(), "\n");
		while (!exists && st.hasMoreTokens()) {
			String groupName = st.nextToken();
			if (groupName.equals(cnProyecto)) {
				exists = true;
			}
		}

		if (!exists) {
			// We need to create the group. uidAdminProyecto should already be a
			// member of Gerrit
			try {
				String cmd = sshPrefix + " gerrit create-group --member " + uidAdminProyecto + " " + cnProyecto;
				log.info("[Gerrit] " + cmd);
				co = cl.syncExec(cmd);
				log.info(getCommandOutput(co));
			} catch (Exception e) {
				throw new ExcepcionConsola("Problem creating Gerrit group: " + e.getMessage());
			}
		}

		// Check for project existence
		try {
			String cmd = sshPrefix + " gerrit ls-projects";
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem creating Gerrit group: " + e.getMessage());
		}

		boolean projectExists = false;
		st = new StringTokenizer(co.getStandardOutput());
		while (!projectExists && st.hasMoreTokens()) {
			String groupName = st.nextToken();
			if (groupName.equals(cnProyecto)) {
				projectExists = true;
			}
		}

		if (projectExists) {
			throw new ExcepcionConsola("Repository name already exists: " + cnProyecto);
		}

		// Now we are ready to create the repo (a project in Gerry jargon)
		try {
			String cmd = sshPrefix + " gerrit create-project --owner " + cnProyecto
					+ " --branch master --branch develop --empty-commit " + cnProyecto;
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
			log.info("Creating project:" + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem creating Gerrit project: " + e.getMessage());
		}

		// We need to set several permissions for refs/heads/*, refs/tags/*,
		// refs/* to the group
		String sshAgentPrefix = "ssh-agent bash -c 'ssh-add /opt/ssh-keys/gerritadmin_rsa ; ";
//		cl = new CommandLine(new File("/opt"));
		try {
			String cmd = sshAgentPrefix
					+ "git clone --config user.email=gerritadmin@gmail.com --config user.name=gerritadmin ssh://gerritadmin@"
					+ apiAbierta.getConfiguration().hostGerrit + ":29418/" + cnProyecto + "'";
			log.info("[Gerrit] " + cmd);
//			co = cl.syncExec(cmd);
			
			OverthereConnection connection = Overthere.getConnection("local", options);
			outputHandler = new OverthereOutputHandler();
			connection.execute(outputHandler, CmdLine.build(
					"ssh-agent",
					"bash",
					"-c",
					"'ssh-add /opt/ssh-keys/gerritadmin_rsa ; " 
							+ "git clone --config user.email=gerritadmin@gmail.com --config user.name=gerritadmin ssh://gerritadmin@"
							+ apiAbierta.getConfiguration().hostGerrit + ":29418/" + cnProyecto + "'"));
//			log.info("git clone: " + getCommandOutput(co));
			log.info("git clone: " + outputHandler.getOut());
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem cloning repository: " + e.getMessage(), e);
		}

//		cl = new CommandLine(new File("/opt", cnProyecto));
		try {
			String cmd = sshAgentPrefix + "git fetch origin refs/meta/config:refs/remotes/origin/meta/config'";
			log.info("[Gerrit] " + cmd);
//			co = cl.syncExec(cmd);
			OverthereConnection connection = Overthere.getConnection("local", options);
			outputHandler = new OverthereOutputHandler();
			connection.execute(outputHandler, CmdLine.build(
					"ssh-agent bash",
					"bash",
					"-c",
					"'ssh-add /opt/ssh-keys/gerritadmin_rsa ; " 
							+ "git fetch origin refs/meta/config:refs/remotes/origin/meta/config'"));
//			log.info("git fetch: " + getCommandOutput(co));
			log.info("git fetch: " + outputHandler.getOut());
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem fetching meta/config: " + e.getMessage());
		}

		try {
			String cmd = sshAgentPrefix + "git checkout meta/config'";
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
			log.info("Cheking out meta/config: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with checkout meta/config: " + e.getMessage());
		}

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/opt", cnProyecto + "/project.config"), true));
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
			throw new ExcepcionConsola("Problem writing permissions: " + e.getMessage());
		}

		try {
			co = cl.syncExec("git add project.config");
			log.info("git add: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git add: " + e.getMessage());
		}
		
		try {
			co = cl.syncExec("git commit -m 'updated permissions by scstack'");
			log.info("git commit: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git commit: " + e.getMessage());
		}
		
		try {
			String cmd = sshAgentPrefix + "git push origin meta/config:meta/config'";
			log.info("[Gerrit] " + cmd);
			co = cl.syncExec(cmd);
			log.info("git push: " + getCommandOutput(co));
		} catch (Exception e) {
			throw new ExcepcionConsola("Problem with git push: " + e.getMessage());
		}

	}

	private String getCommandOutput(CommandOutput co) {
		return "[stdout:" + co.getStandardOutput() + ";stderr:" + co.getErrorOutput() + "]";
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
