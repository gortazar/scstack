/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: API_Abierta.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.api;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.scstack.lib.commons.Utilidades;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.config.apache.ConfiguradorApache;
import es.sidelab.scstack.lib.config.apache.GeneradorFicherosApache;
import es.sidelab.scstack.lib.dataModel.*;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios;
import es.sidelab.scstack.lib.dataModel.repos.Repositorio;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios.TipoRepositorio;
import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConfiguradorApache;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionGeneradorFicherosApache;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionProyecto;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPAdministradorUnico;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import es.sidelab.scstack.lib.gerrit.GerritManager;
import es.sidelab.scstack.lib.gerrit.GerritException;
import es.sidelab.scstack.lib.ldap.GestorLDAP;
import es.sidelab.scstack.lib.redmine.GestorRedmine;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.exolab.castor.util.CommandLineOptions;

/**
 * <p>
 * Esta clase supone la capa superior de la API, aquella que deberá de utilizar
 * el usuario de la API.
 * </p>
 * 
 * @author Arek Klauza
 */
public class API_Abierta {
	private Logger log;
	private GestorLDAP gestorLdap;
	private ConfiguradorApache configApache;
	private GestorRedmine gestorRedmine;
	private ConfiguracionForja config;
	private GerritManager gerritManager;

	public API_Abierta(String configFile) throws SCStackException {
		this(Logger.getLogger(API_Abierta.class.getName()), configFile);
	}

	/**
	 * <p>
	 * Crea la API de la Forja e inicializa todos los componentes necesarios
	 * para su correcto funcionamiento.
	 * </p>
	 * 
	 * @param ficheroConfiguracion
	 *            Ruta y nombre del fichero que contiene los parámetros de
	 *            configuración de la Forja
	 * @throws SCStackException
	 *             Cuando se produce algún error durante la configuración de la
	 *             API de la Forja.
	 */
	public API_Abierta(Logger log, String ficheroConfiguracion) throws SCStackException {

		if (log == null) {
			throw new IllegalArgumentException("Log may not be null");
		}

		this.log = log;

		try {
			this.config = new ConfiguracionForja(ficheroConfiguracion);
		} catch (Exception e) {
			throw new SCStackException("Failed loading the stack's configuration from " + ficheroConfiguracion + ": " + e.getMessage(), e);
		}
		try {
			this.gestorLdap = new GestorLDAP();
		} catch (ExcepcionGestorLDAP ex) {
			throw new SCStackException("Error durante la creación y configuración de la API de la Forja: " + ex.getMessage(), ex);
		}
		try {
			this.configApache = new ConfiguradorApache(this.log);
		} catch (Exception e) {
			throw new SCStackException("Failed loading apache's config from " + ficheroConfiguracion + ": " + e.getMessage(), e);
		}
		try {
			this.gestorRedmine = new GestorRedmine(this.log);
		} catch (Exception e) {
			throw new SCStackException("Failed loading Redmine's config from " + ficheroConfiguracion + ": " + e.getMessage(), e);
		}

		try {
			this.gerritManager = new GerritManager(this.log);
		} catch (GerritException e) {
			throw new SCStackException("Failed to initialize gerrit manager: " + e.getMessage(), e);
		}
	}

	public ConfiguracionForja getConfiguration() {
		return this.config;
	}

	public void addProyecto(String cn, String description, String primerAdmin, TipoRepositorio tipoRepositorio, boolean esRepoPublico,
			String rutaRepo) throws SCStackException {

		try {
			Repositorio repo = FactoriaRepositorios.crearRepositorio(tipoRepositorio, esRepoPublico, rutaRepo);
			Proyecto proyecto = new Proyecto(cn, description, primerAdmin, repo);
			this.gestorLdap.addProyecto(proyecto);
			this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), proyecto, gestorLdap.getListaUIdsUsuario(), this);
			this.gestorRedmine.crearProyecto(proyecto);
			this.gestorRedmine.desactivarPublicidadProyectos(cn);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public void addUsuario(String uid, String nombre, String apellidos, String email, String pass) throws SCStackException {

		log.info("Adding user...");

		// Hay que codificar la contraseña a MD5 de LDAP
		String passMD5 = null;

		try {
			passMD5 = Utilidades.toMD5(pass);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

		// Los emails son clave en Redmine, hay que comprobar que no haya 2
		// iguales
		try {

			log.info("Testing email for duplicates (not allowed)");

			ArrayList<String> listaEmails = this.gestorLdap.getListaEmailsUsuarios();
			if (listaEmails != null && listaEmails.contains(email))
				throw new ExcepcionUsuario("El email del usuario ya existe, no puede haber duplicados. Introduzca otro email por favor");

		} catch (ExcepcionGestorLDAP ex) {
			throw new SCStackException("No se ha podido comprobar que el email de usuario es único: " + ex.getMessage(), ex);
		}

		Usuario user = new Usuario(uid, nombre, apellidos, email, passMD5);

		try {
			log.info("Adding user to ldap server");
			this.gestorLdap.addUsuario(user);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

		try {
			log.info("Adding user to Redmine server");
			this.gestorRedmine.crearUsuario(user, pass);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

		log.info("Adding user to Gerrit server");
		try {
			this.gerritManager.addUser(user);
		} catch (GerritException e) {
			log.severe("[API] " + e.getMessage());
			throw new SCStackException(e);
		}

		log.info("Adding user to SSH jail");
		GeneradorFicherosApache apache = new GeneradorFicherosApache(log);
		try {
			apache.generarFicheroJaulaUsuariosSSH(gestorLdap.getListaUIdsUsuario().toArray(new String[] {}));
		} catch (Exception e) {
			log.severe(e.getMessage());
			throw new SCStackException(e);
		}

		CommandLine commandLine = new CommandLine();
		try {
			log.info("Restarting SSH server");
			CommandOutput co = commandLine.syncExec("/etc/init.d/ssh restart");
			log.info("SSH restarted: [/etc/init.d/ssh restart] -> " + co.getStandardOutput());
		} catch (IOException e) {
			throw new SCStackException(e);
		} catch (ExecutionCommandException e) {
			log.severe("Couldn't restart SSH server. Err: " + e.getErrorOutput());
			log.severe("Couldn't restart SSH server. Std: " + e.getStandardOutput());
			throw new SCStackException(e);
		}
	}

	public void addUsuarioAProyecto(String uid, String cnProyecto) throws SCStackException {

		try {
			this.gestorLdap.addUsuarioAProyecto(cnProyecto, uid);
			this.gestorRedmine.addMiembroAProyecto(uid, cnProyecto);

			this.gerritManager.addProjectMember(uid, cnProyecto);
		} catch (Exception e) {
			throw new SCStackException("Problem adding user to project: " + e.getMessage(), e);
		}
	}

	public void addAdministradorAProyecto(String uidAdmin, String cnProyecto) throws SCStackException {
		try {
			this.gestorLdap.addAdminAProyecto(cnProyecto, uidAdmin);
			this.gestorRedmine.addAdministradorAProyecto(uidAdmin, cnProyecto);
			this.gerritManager.addProjectMember(uidAdmin, cnProyecto);
		} catch (Exception e) {
			throw new SCStackException("Problem adding user to project: " + e.getMessage(), e);
		}
	}

	public void addRepositorioAProyecto(TipoRepositorio tipoRepo, boolean esPublico, String rutaRepo, String cnProyecto)
			throws SCStackException {

		try {
			Repositorio repo = FactoriaRepositorios.crearRepositorio(tipoRepo, esPublico, rutaRepo);
			this.gestorLdap.addRepositorioAProyecto(repo, cnProyecto);
			// Cuando se añaden repositorios hay que regenerar los ficheros
			// Apache
			this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto),
					gestorLdap.getListaUIdsUsuario(), this);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public Usuario getUsuario(String uid) throws SCStackException {
		try {
			return this.gestorLdap.getUsuario(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public ArrayList<String> getListaProyectosAdministrados(String uid) throws SCStackException {
		try {
			return this.gestorLdap.getListaProyectosAdministradosXUid(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public ArrayList<String> getListaUsuariosAdministrados(String uid) throws SCStackException {
		try {
			return this.gestorLdap.getListaUsuariosAdministradosXUid(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public ArrayList<String> getListaProyectosParticipados(String uid) throws SCStackException {
		try {
			return this.gestorLdap.getListaProyectosMiembroXUid(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public ArrayList<String> getListaUsuariosPorProyecto(String cnProyecto) throws SCStackException {
		try {
			return this.gestorLdap.getListaUsuariosXProyecto(cnProyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public ArrayList<String> getListaAdministradoresPorProyecto(String cnProyecto) throws SCStackException {
		try {
			return this.gestorLdap.getListaAdministradoresXProyecto(cnProyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public Proyecto getProyecto(String cnProyecto) throws SCStackException {
		try {
			return this.gestorLdap.getProyecto(cnProyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public ArrayList<String> getListaUidsUsuarios() throws SCStackException {
		try {
			return this.gestorLdap.getListaUIdsUsuario();
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public ArrayList<String> getListaNombresUsuarios() throws SCStackException {
		try {
			return this.gestorLdap.getListaNombresCompletosUsuarios();
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public ArrayList<String> getListaEmailsUsuarios() throws SCStackException {
		try {
			return this.gestorLdap.getListaEmailsUsuarios();
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public ArrayList<String> getListaCnProyectos() throws SCStackException {
		try {
			return this.gestorLdap.getListaNombresProyectos();
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public void editUsuario(Usuario user) throws SCStackException {
		// Los emails son clave en Redmine, hay que comprobar que no haya 2
		// iguales
		try {
			ArrayList<String> listaEmails = this.gestorLdap.getListaEmailsUsuarios();
			if (listaEmails.contains(user.getEmail()) && !gestorLdap.getUsuario(user.getUid()).getEmail().equals(user.getEmail()))
				throw new ExcepcionUsuario(
						"El email del usuario está siendo utilizado por otro usuario, no puede haber duplicados. Introduzca otro email por favor");
		} catch (ExcepcionGestorLDAP ex) {
			throw new SCStackException("Fallo LDAP: No se ha podido comprobar que el email de usuario es único: " + ex.getMessage(), ex);
		}

		try {
			this.gestorLdap.editUsuario(user);
			this.gestorRedmine.editarUsuario(user);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public void editProyecto(Proyecto proyecto) throws SCStackException {
		try {
			this.gestorLdap.editProyecto(proyecto);
			this.gestorRedmine.editarProyecto(proyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}
	}

	public void bloquearUsuario(String uid) throws SCStackException {
		try {
			this.gestorLdap.bloquearUsuario(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void desbloquearUsuario(String uid, String nuevaPass) throws SCStackException {
		try {
			// Hay que codificar la contraseña a MD5 de LDAP
			String nuevaPassMD5 = Utilidades.toMD5(nuevaPass);
			this.gestorLdap.desbloquearUsuario(uid, nuevaPassMD5);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void deleteUsuarioDeProyecto(String uid, String cnProyecto) throws SCStackException {
		try {
			this.gestorLdap.deleteUsuarioDeProyecto(uid, cnProyecto);
			// Cuando borramos a un usuario que también es administrador hay que
			// regenerar los ficheros Apache
			this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto),
					gestorLdap.getListaUIdsUsuario(), this);
			this.gestorRedmine.deleteMiembroDeProyecto(uid, cnProyecto);
			this.gerritManager.removeUserFromProject(uid, cnProyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void deleteAdministradorDeProyecto(String uid, String cnProyecto) throws SCStackException {

		try {
			this.gestorLdap.deleteAdministradorDeProyecto(uid, cnProyecto);
			// Cuando borramos a un administrador hay que regenerar los ficheros
			// Apache
			this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto),
					gestorLdap.getListaUIdsUsuario(), this);
			this.gestorRedmine.deleteAdministradorDeProyecto(uid, cnProyecto);
			this.gerritManager.removeUserFromProject(uid, cnProyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void deleteRepositorioDeProyecto(String tipoRepo, String cnProyecto) throws SCStackException {

		try {
			// Borramos la carpeta del repositorio
			Proyecto proyecto = gestorLdap.getProyecto(cnProyecto);
			ArrayList<Repositorio> repos = proyecto.getRepositorios();
			for (Repositorio repo : repos)
				if (repo.getTipo().equalsIgnoreCase(tipoRepo))
					if (Utilidades.existeCarpeta(repo.getRuta(), proyecto.getCn()))
						repo.borrarRepositorio(proyecto.getCn());
			// Borramos del directorio LDAP
			this.gestorLdap.deleteRepositorioDeProyecto(tipoRepo, cnProyecto);
			// Cuando borramos un repositorio hay que regenerar los ficheros
			// Apache
			this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto),
					gestorLdap.getListaUIdsUsuario(), this);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void deleteProyecto(String cnProyecto) throws SCStackException {

		try {
			Proyecto proyecto = this.gestorLdap.getProyecto(cnProyecto);
			this.gestorLdap.deleteProyecto(cnProyecto);
			this.configApache.borrarProyecto(gestorLdap.getListaProyectos(), proyecto);
			this.gestorRedmine.borrarProyecto(proyecto);
			this.gerritManager.removeProject(proyecto);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	public void deleteUsuario(String uid) throws SCStackException {

		try {
			// Borra al usuario de todos los proyectos que administra y en los
			// que
			// participa
			for (String proyecto : this.gestorLdap.getListaProyectosMiembroXUid(uid)) {
				this.deleteUsuarioDeProyecto(uid, proyecto);
			}
			this.gestorLdap.deleteUsuario(uid);
			this.gestorRedmine.deleteUsuario(uid);
			this.gerritManager.removeUser(uid);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

	/**
	 * <p>
	 * Este método se debe ejecutar para crear el grupo de superadministradores
	 * y el Primer administrador de la Forja.
	 * </p>
	 * 
	 * @param groupSuperadmins
	 * @param uidSuperadmin
	 * @param passSuperadmin
	 * @throws SCStackException
	 */
	@Deprecated
	public void inicializaForja(String uidSuperadmin, String passSuperadmin) throws NoSuchAlgorithmException, SCStackException {

		try {
			this.addUsuario(uidSuperadmin, "Usuario Superadministrador", "Permanente", "info@info.com", passSuperadmin);
			this.addProyecto(ConfiguracionForja.groupSuperadmin, "Proyecto que agrupa a los superadministradores de la Forja",
					uidSuperadmin, null, false, null);
		} catch (Exception e) {
			throw new SCStackException(e);
		}

	}

}