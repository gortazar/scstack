/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: API_Segura.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.api;

import es.sidelab.scstack.lib.commons.Utilidades;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios.TipoRepositorio;
import es.sidelab.scstack.lib.exceptions.ExcepcionForja;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionGeneradorFicherosApache;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionLogin;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionParametros;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionProyecto;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPAdministradorUnico;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * <p>Esta clase recubre la API original dotándola de una capa de control de
 * permisos y privilegios para la ejecución de los distintos métodos de la API.</p>
 * @author Arek Klauza
 */
public class API_Segura {
	public enum NivelSeguridad {
		USUARIO, ADMIN, SUPERADMIN
	}
	private API_Abierta api;
	private Logger log;

	/**
	 * <p>Constructor que por defecto usa como fichero de configuración el fichero
	 * "configuracion.txt" de la raíz del proyecto.</p>
	 * @throws ExcepcionForja
	 */
	public API_Segura() throws ExcepcionForja {
		//this("configuracion.txt");
		this("scstack.conf");
	}


	/**
	 * <p>Constructor al cual se le debe indicar la ruta y nombre del fichero de
	 * configuración que se quiere utilizar.</p>
	 * @param ficheroConfig Ruta relativa o absoluta y nombre del fichero
	 * @throws ExcepcionForja
	 */
	public API_Segura(String ficheroConfig) throws ExcepcionForja {
		this.log = Logger.getLogger(API_Segura.class.getName());
		this.api = new API_Abierta(this.log, ficheroConfig);
	}



	/**
	 * <p>Este método se debe utilizar para crear el grupo de superadministradores y
	 * el primer Superadministrador la primera vez que se vaya a usar la Forja.</p>
	 * @param uid UID del primer Superadministrador de la Forja
	 * @param pass Contraseña en claro del primer Superadministrador de la Forja
	 * @throws ExcepcionForja Cuando se ha producido algún error durante la inicialización
	 * @throws NoSuchAlgorithmException Cuando se produce erro al codificar a MD5
	 * la contraseña suministrada
	 */
	public void inicializarForja(String uid, String pass) throws ExcepcionForja, NoSuchAlgorithmException {
//		this.api = new API_Abierta("configuracion.txt");
		this.api = new API_Abierta("scstack.conf");
		ArrayList listaProy = api.getListaCnProyectos();
		if (listaProy == null || !listaProy.contains(ConfiguracionForja.groupSuperadmin))
			this.api.inicializaForja(uid, pass);
		else
			throw new ExcepcionForja("La Forja ya contiene un grupo de Superadministradores denominado: " + ConfiguracionForja.groupSuperadmin);
	}



	/**************************************************************************/

	/************************ CASOS DE USO DE USUARIO *************************/

	/**************************************************************************/


	/**
	 * <p>Método que a partir de un UID de usuario y una contraseña, verifica que
	 * éstos sean correctos y luego devuelve un String indicando el tipo de rol
	 * que tiene dicho usuario en la Forja.</p>
	 * @param uid UID del usuario
	 * @param pass Contraseña del usuario
	 * @return String que puede ser {"superadmin", "admin" ó "usuario"} en función
	 * del rol que desempeñe el UID suministrado en la Forja.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public String doLogin(String uid, String pass) throws ExcepcionParametros, ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uid,pass}");

		if (!checkLogin(uid, pass))
			return null;
		else if (esSuperAdmin(uid))
			return "superadmin";
		else if (esAdminDeAlgunProyecto(uid))
			return "admin";
		else
			return "usuario";
	}


	/**
	 * <p>Consulta los datos personales de un usuario concreto almacenados en el
	 * directorio LDAP de la Forja.</p>
	 * <p>Lo puede invocar: el propio usuario, un administrador de alguno de sus
	 * proyectos y cualquier superadmin.</p>
	 * @param uidConsulta UID del usuario cuyos datos queremos recuperar
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña en claro de quien realiza la consulta
	 * @return Objeto Usuario con los datos personales
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public Usuario getDatosUsuario(String uidConsulta, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidConsulta,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidConsulta,uid,pass}");

		if (this.checkCredentialsUsuario(uidConsulta, uid, pass, NivelSeguridad.USUARIO))
			return api.getUsuario(uidConsulta);
		else
			return null;
	}



	/**
	 * <p>Edita los datos almacenados en la Forja de un usuario determinado.
	 * Edita tanto la entrada en el directorio LDAP como en Redmine.</p>
	 * <p>Si no se quiere modificar la contraseña, deberá mandarse el campo pass
	 * del usuario a nulo o "".</p>
	 * <p>Lo puede invocar: el propio usuario, un administrador de alguno de sus
	 * proyectos y cualquier superadmin.</p>
	 * @param user Objeto estructurado con los datos nuevos del usuario
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña en claro de quien realiza la consulta
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionGestorRedmine Cuando existe algún problema durante la 
	 * modificación de los datos en Redmine
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void editarDatosUsuario(Usuario user, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionUsuario {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {user,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {user,uid,pass}");

		if (this.checkCredentialsUsuario(user.getUid(), uid, pass, NivelSeguridad.USUARIO))
			api.editUsuario(user);
	}



	/**
	 * <p>Devuelve una lista de los proyectos en los que participa como miembro
	 * un usuario determinado.</p>
	 * <p>Todo administrador es a su vez miembro de un proyecto.</p>
	 * <p>Lo puede invocar: un usuario acerca de sus proyectos, un administrador
	 * de cualquiera de sus usuarios administrados y cualquier superadmin.</p>
	 * @param uidConsulta UID del usuario cuya lista de proyectos queremos consultar
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña en claro de quien realiza la consulta
	 * @return ArrayList de Strings con los nombres de proyectos participados por
	 * el usuario
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoProyectosParticipados(String uidConsulta, String uid, String pass) 
			throws ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidConsulta,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidConsulta,uid,pass}");

		if (this.checkCredentialsUsuario(uidConsulta, uid, pass, NivelSeguridad.USUARIO))
			return api.getListaProyectosParticipados(uidConsulta);
		else
			return null;
	}



	/**
	 * <p>Devuelve una lista de los proyectos en los que es administrador 
	 * un usuario determinado.</p>
	 * <p>Lo puede invocar: un usuario acerca de sus proyectos, un administrador
	 * de cualquiera de sus usuarios administrados y cualquier superadmin.</p>
	 * @param uidConsulta UID del usuario cuya lista de proyectos queremos consultar
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña en claro de quien realiza la consulta
	 * @return ArrayList de Strings con los nombres de proyectos administrados por
	 * el usuario
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoProyectosAdministrados(String uidConsulta, String uid, String pass)
			throws ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidConsulta,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidConsulta,uid,pass}");

		if (this.checkCredentialsUsuario(uidConsulta, uid, pass, NivelSeguridad.USUARIO))
			return api.getListaProyectosAdministrados(uidConsulta);
		else
			return null;
	}


	/**
	 * <p>Devuelve una lista de los UID de los usuarios miembros de los proyectos
	 * en los que es administrador un usuario determinado.</p>
	 * <p>Lo puede invocar: un administrador de cualquier proyecto y cualquier
	 * superadmin.</p>
	 * @param uidConsulta UID del usuario administrador
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña en claro de quien realiza la consulta
	 * @return ArrayList de Strings con los UIDs de los usuarios miembros de los
	 * proyectos del administrador cuyo UID se pasa como parámetro.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el usuario buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoUsuariosAdministrados(String uidConsulta, String uid, String pass)
			throws ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidConsulta,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidConsulta,uid,pass}");

		if (this.checkCredentialsUsuario(uidConsulta, uid, pass, NivelSeguridad.ADMIN))
			return api.getListaUsuariosAdministrados(uidConsulta);
		else
			return null;
	}


	/**
	 * <p>Devuelve la lista de UIDs de los usuarios que participan en un proyecto
	 * determinado de la Forja.</p>
	 * <p>Lo puede invocar: un usuario acerca de sus proyectos, un administrador
	 * de cualquiera de sus proyectos administrados y cualquier superadmin.</p>
	 * @param cnProyecto Nombre del proyecto cuya lista de usuarios buscamos
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña del usuario que realiza la consulta
	 * @return Lista de UIDs de usuarios que participan en el proyecto
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el proyecto buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoUsuariosPorProyecto(String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.USUARIO))
			return api.getListaUsuariosPorProyecto(cnProyecto);
		else
			return null;
	}



	/**
	 * <p>Devuelve la lista de UIDs de los administradores de un proyecto
	 * determinado de la Forja.</p>
	 * <p>Lo puede invocar: un usuario acerca de sus proyectos, un administrador
	 * de cualquiera de sus proyectos administrados y cualquier superadmin.</p>
	 * @param cnProyecto Nombre del proyecto cuya lista de usuarios buscamos
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña del usuario que realiza la consulta
	 * @return Lista de UIDs de administradores del proyecto
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el proyecto buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoAdministradoresPorProyecto(String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.USUARIO))
			return api.getListaAdministradoresPorProyecto(cnProyecto);
		else
			return null;
	}



	/**
	 * <p>Devuelve un objeto estructurado con todos los datos del proyecto
	 * buscado.</p>
	 * <p>Lo puede invocar: un usuario acerca de sus proyectos, un administrador
	 * de cualquiera de sus proyectos administrados y cualquier superadmin.</p>
	 * @param cnProyecto Nombre del proyecto
	 * @param uid UID del usuario que realiza la consulta
	 * @param pass Contraseña del usuario que realiza la consulta
	 * @return Objeto Proyecto con los datos del mismo
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el proyecto buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public Proyecto getDatosProyecto(String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.USUARIO))
			return api.getProyecto(cnProyecto);
		else
			return null;
	}







	/**************************************************************************/

	/************** CASOS DE USO DE ADMINISTRADOR DE PROYECTOS ****************/

	/**************************************************************************/


	/**
	 * <p>Este método devuelve el listado de todos los UIDs de usuarios que hay
	 * en la Forja, recuperándolos del directorio LDAP.</p>
	 * <p>Lo puede invocar: cualquier administrador y cualquier superadmin.</p>
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @return Lista de UIDs de todos los usuarios que hay en la Forja
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoUidsUsuarios(String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uid,pass}");

		if (this.checkCredentialsGeneral(uid, pass, NivelSeguridad.ADMIN))
			return api.getListaUidsUsuarios();
		else
			return null;
	}


	/**
	 * <p>Este método devuelve el listado de todos los nombres y apellidos de
	 * usuarios que hay en la Forja, recuperándolos del directorio LDAP.</p>
	 * <p>Lo puede invocar: cualquier administrador y cualquier superadmin.</p>
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @return Lista de Nombres y apellidos de todos los usuarios que hay en la Forja
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoNombresUsuarios(String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uid,pass}");

		if (this.checkCredentialsGeneral(uid, pass, NivelSeguridad.ADMIN))
			return api.getListaNombresUsuarios();
		else
			return null;
	}



	/**
	 * <p>Este método devuelve el listado de todos los emails de los
	 * usuarios que hay en la Forja, recuperándolos del directorio LDAP.</p>
	 * <p>Lo puede invocar: cualquier administrador y cualquier superadmin.</p>
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @return Lista de emails de todos los usuarios que hay en la Forja
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoEmailsUsuarios(String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uid,pass}");

		if (this.checkCredentialsGeneral(uid, pass, NivelSeguridad.ADMIN))
			return api.getListaEmailsUsuarios();
		else
			return null;
	}



	/**
	 * <p>Añade un usuario ya existente en la Forja como administrador de un
	 * proyecto determinado.</p>
	 * <p>Lo puede invocar: un administrador del proyecto implicado y cualquier
	 * superadmin.</p>
	 * @param uidAdmin UID de usuario que queremos hacer administrador de proyecto
	 * @param cnProyecto Nombre del proyecto donde añadir al administrador
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void addAdminAProyecto(String uidAdmin, String cnProyecto, String uid, String pass) 
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidAdmin,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidAdmin,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			api.addAdministradorAProyecto(uidAdmin, cnProyecto);
	}



	/**
	 * <p>Añade un usuario ya existente en la Forja como miembro de un
	 * proyecto determinado.</p>
	 * <p>Lo puede invocar: un administrador del proyecto implicado y cualquier
	 * superadmin.</p>
	 * @param uidUsuario UID de usuario que queremos añadir al proyecto
	 * @param cnProyecto Nombre del proyecto donde añadir al usuario
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void addUsuarioAProyecto(String uidUsuario, String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			api.addUsuarioAProyecto(uidUsuario, cnProyecto);
	}



	/**
	 * <p>Crea y añade un nuevo repositorio a un proyecto de la Forja.</p>
	 * <p>Puede invocarlo: un administrador del proyecto implicado y cualquier
	 * supeardmin.</p>
	 * @param tipoRepo Tipo de repositorio a crear ("SVN", "GIT"...)
	 * @param esPublico true si queremos que el repositorio tenga vista pública,
	 * false si no.
	 * @param rutaRepo Ruta absoluta donde queremos que se cree el repositorio
	 * en Apache. Null si lo queremos en la ruta por defecto.
	 * @param cnProyecto Nombre del proyecto
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionRepositorio Cuando se intenta crear un repositorio con
	 * parámetros incorrectos.
	 * @throws ExcepcionGeneradorFicherosApache Cuando no se ha podido regenerar
	 * la configuración de Apache para el proyecto.
	 * @throws ExcepcionConsola Cuando se produce un error durante la configuración
	 * de Apache por consola.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void addRepositorioAProyecto(String tipoRepo, boolean esPublico, String rutaRepo, String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionRepositorio, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {tipoRepo,esPublico,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {tipoRepo,esPublico,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			if (tipoRepo.equalsIgnoreCase("GIT"))
				api.addRepositorioAProyecto(TipoRepositorio.GIT, esPublico, rutaRepo, cnProyecto);
			else
				api.addRepositorioAProyecto(TipoRepositorio.SVN, esPublico, rutaRepo, cnProyecto);
	}



	/**
	 * <p>Devuelve una lista de los nombres de todos los proyectos que hay en la
	 * Forja.</p>
	 * <p>Puede invocarlo: cualquier administrador y cualquier superadmin.</p>
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @return Lista de los nombres de todos los proyectos que hay en la Forja
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public ArrayList<String> getListadoProyectos(String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uid,pass}");

		if (this.checkCredentialsGeneral(uid, pass, NivelSeguridad.ADMIN))
			return api.getListaCnProyectos();
		else
			return null;
	}



	/**
	 * <p>Modifica los datos de descripción o defaultRepositorio de un proyecto
	 * determinado.</p>
	 * <p>Puede invocarlo: El administrador del proyecto o cualquier superadmin.</p>
	 * @param proyecto Objeto estructurado Proyecto con los datos del mismo
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionGestorRedmine Cuando se produce algún error durante la
	 * modificación de los datos del proyecto guardados en Redmine.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void editarDatosProyecto(Proyecto proyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionGestorRedmine, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {proyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {proyecto,uid,pass}");

		if (this.checkCredentialsProyecto(proyecto.getCn(), uid, pass, NivelSeguridad.ADMIN)) {
			Proyecto pInterno = api.getProyecto(proyecto.getCn());
			proyecto.setRepositorios(pInterno.getRepositorios());
			api.editProyecto(proyecto);
		}
	}



	/**
	 * <p>Elimina a un usuario determinado como miembro del proyecto, borrándole
	 * como administrador del mismo si también lo era.</p>
	 * <p>No puede eliminarse miembros de un proyecto que a su vez sean el único
	 * administrador del mismo, ya que ello implicaría dejar al proyecto sin
	 * ningún administrador. Por ello para borrar a estos individuos, primero hay
	 * que añadir más administradores al proyecto.</p>
	 * <p>Puede invocarlo: El administrador del proyecto o cualquier superadmin.</p>
	 * @param uidUsuario UID de quien queremos borrar del proyecto
	 * @param cnProyecto Nombre del proyecto
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionGestorRedmine Cuando se produce algún error durante la
	 * modificación de los datos del proyecto guardados en Redmine.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionGeneradorFicherosApache Cuando al reconfigurar Apache, no
	 * se pudo regenerar sus ficheros de configuración
	 * @throws ExcepcionConsola Cuando no se pueden realizar determinadas órdenes
	 * de consola de Apache.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void eliminarUsuarioDeProyecto(String uidUsuario, String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionLDAPAdministradorUnico, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			api.deleteUsuarioDeProyecto(uidUsuario, cnProyecto);
	}



	/**
	 * <p>Elimina a un administrador determinado de un proyecto, dejándolo como
	 * miembro usuario del mismo.</p>
	 * <p>Puede invocarlo: El administrador del proyecto o cualquier superadmin.</p>
	 * @param uidUsuario UID de quien queremos borrar del proyecto
	 * @param cnProyecto Nombre del proyecto
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionGestorRedmine Cuando se produce algún error durante la
	 * modificación de los datos del proyecto guardados en Redmine.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionGeneradorFicherosApache Cuando al reconfigurar Apache, no
	 * se pudo regenerar sus ficheros de configuración
	 * @throws ExcepcionConsola Cuando no se pueden realizar determinadas órdenes
	 * de consola de Apache.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void eliminarAdminDeProyecto(String uidAdmin, String cnProyecto, String uid, String pass)
			throws ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionConsola,
			ExcepcionGeneradorFicherosApache, ExcepcionGestorLDAP, ExcepcionLogin, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidAdmin,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidAdmin,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			api.deleteAdministradorDeProyecto(uidAdmin, cnProyecto);
	}



	/**
	 * <p>Elimina un repositorio de un determinado de un proyecto.</p>
	 * <p>Puede invocarlo: El administrador del proyecto o cualquier superadmin.</p>
	 * @param tipoRepo Nombre del tipo de repositorio que queremos borrar del
	 * proyecto ("SVN", "GIT"...)
	 * @param cnProyecto Nombre del proyecto
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionGestorRedmine Cuando se produce algún error durante la
	 * modificación de los datos del proyecto guardados en Redmine.
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionGeneradorFicherosApache Cuando al reconfigurar Apache, no
	 * se pudo regenerar sus ficheros de configuración
	 * @throws ExcepcionConsola Cuando no se pueden realizar determinadas órdenes
	 * de consola de Apache.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void eliminarRepositorioDeProyecto(String tipoRepo, String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {tipoRepo,cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {tipoRepo,cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(cnProyecto, uid, pass, NivelSeguridad.ADMIN))
			api.deleteRepositorioDeProyecto(tipoRepo, cnProyecto);
	}








	/**************************************************************************/

	/************************ CASOS DE USO SUPERADMIN *************************/

	/**************************************************************************/

	/**
	 * <p>Agrega un nuevo usuario a la Forja.</p>
	 * <p>La contraseña se le pasa en claro y es el propio método quien la convierte
	 * en MD5 de LDAP en base64.</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param uidUsuario UID unívoco que cumpla la Regexp: [A-Za-z0-9]
	 * @param nombre Nombre del usuario
	 * @param apellidos Apellido del usuario
	 * @param email Dirección email del usuario
	 * @param passUser Contraseña en claro del nuevo usuario
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionUsuario Cuando el uid del usuario no cumple las condiciones
	 * de la expresión regular: [a-zA-z0-9]+
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void crearUsuario(String uidUsuario, String nombre, String apellidos, String email, String passUser, String uid, String pass)
			throws ExcepcionUsuario, ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, NoSuchAlgorithmException, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario, nombre, apellidos, email, passUser, uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario, nombre, apellidos, email, passUser, uid,pass}");

		// Verificación de la sintaxis del email
		if (email == null || email.isEmpty() || !Utilidades.checkEmail(email))
			throw new ExcepcionUsuario("El dato email introducido (" + email + ") es incorrecto.");

		if (this.checkCredentialsUsuario(uidUsuario, uid, pass, NivelSeguridad.SUPERADMIN))
			api.addUsuario(uidUsuario, nombre, apellidos, email, passUser);
	}



	/**
	 * <p>Bloquea temporalmente a un usuario determinado impidiéndole acceder a
	 * sus funciones dentro de la Forja.</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param uidUsuario UID del usuario que queremos bloquear
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void bloquearUsuario(String uidUsuario, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario,uid,pass}");

		if (this.checkCredentialsUsuario(uidUsuario, uid, pass, NivelSeguridad.SUPERADMIN))
			api.bloquearUsuario(uidUsuario);
	}



	/**
	 * <p>Desbloquea a un usuario previamente bloqueado reestableciendo todos
	 * sus privilegios dentro de la Forja.</p>
	 * <p>El propio método es encargado de codificar la contraseña a MD5 de LDAP.</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param uidUsuario UID del usuario que queremos bloquear
	 * @param nuevaPass Nueva contraseña del usuario en claro
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void desbloquearUsuario(String uidUsuario, String nuevaPass, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, NoSuchAlgorithmException, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario, nuevaPass, uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario, nuevaPass, uid,pass}");

		if (this.checkCredentialsUsuario(uidUsuario, uid, pass, NivelSeguridad.SUPERADMIN))
			api.desbloquearUsuario(uidUsuario, nuevaPass);
	}



	/**
	 * <p>Elimina definitivamente a un usuario determinado dentro de la Forja,
	 * siempre y cuando este no sea administrador único de ningún proyecto.</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param uidUsuario UID del usuario que queremos borrar de la Forja
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionLDAPAdministradorUnico Cuando estamos intentando borrar
	 * a un usuario que es administrador único de algún proyecto de la Forja
	 * @throws ExcepcionConsola Tras eliminar al usuario hay que reconfigurar
	 * Apache si este usuario era administrador de algún proyecto de la Forja.
	 * @throws ExcepcionGeneradorFicherosApache Tras eliminar al usuario hay que
	 * reconfigurar Apache si este usuario era administrador de algún proyecto
	 * de la Forja.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void eliminarUsuario(String uidUsuario, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionLDAPAdministradorUnico, ExcepcionConsola, ExcepcionGeneradorFicherosApache, ExcepcionParametros, ExcepcionGestorRedmine {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {uidUsuario,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {uidUsuario,uid,pass}");

		if (this.checkCredentialsUsuario(uidUsuario, uid, pass, NivelSeguridad.SUPERADMIN))
			api.deleteUsuario(uidUsuario);
	}



	/**
	 * <p>Añade un nuevo proyecto a la Forja, siguiendo el siguiente orden de
	 * acciones:<br/>
	 * 1-Crea el objeto Repositorio del modelo (si es que lo hay) <br/>
	 * 2-Crea el objeto Proyecto del modelo<br/>
	 * 3-Añade el Proyecto al directorio LDAP<br/>
	 * 4-Regenera y reconfigura los ficheros de Apache<br/>
	 * 5-Añade el Proyecto a Redmine</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param cn Nombre del Proyecto. Deberá ser acorde a la expresión [a-zA-Z0-9]+
	 * @param description Pequeño párrafo descriptivo del proyecto
	 * @param primerAdmin UID del usuario que va a ser primer administrador del Proyecto
	 * @param tipoRepositorio Tipo de Repositorio que va a tener el proyecto en
	 * primer lugar. Si no queremos que tenga repositorio, dejamos este campo a null
	 * (es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios.TipoRepositorio)
	 * @param esRepoPublico true si queremos que el primer repositorio del proyecto
	 * sea público, false si no (también false si no hay repositorio).
	 * @param rutaRepo Ruta absoluta de la carpeta donde se debe encontrar el
	 * primer repositorio en Apache. Si no tiene repositorio, o queremos que sea
	 * la ruta por defecto lo dejamos a null.
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionProyecto Cuando alguno de los parámetros del proyecto es
	 * incorrecto (el nombre no sigue la sintaxis, falta el primerAdmin, etc...)
	 * @throws ExcepcionGestorLDAP Cuando se produce algún error de acceso al
	 * directorio LDAP, cuando el proyecto ya existe en el directorio o cuando
	 * alguno de los parámetros del Proyecto presenta una sintaxis incorrecta.
	 * @throws ExcepcionGeneradorFicherosApache Cuando ha habido algún error
	 * durante la generación de los ficheros de configuración de Apache
	 * @throws ExcepcionConsola Cuando durante la generación de los ficheros de
	 * Apache, Linux nos ha devuelto algún tipo de error al escribir en su consola.
	 * @throws ExcepcionRepositorio Cuando el Repositorio que estamos creando
	 * tiene parámetros incorrectos.
	 * @throws ExcepcionGestorRedmine Cuando se produce algún error durante la
	 * creación del proyecto en Redmine.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void crearProyecto(String cn, String description, String primerAdmin, TipoRepositorio tipoRepositorio, boolean esRepoPublico, String rutaRepo, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionProyecto, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionRepositorio, ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {cn,description,primerAdmin,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {cn,description,primerAdmin,uid,pass}");

		if (this.checkCredentialsProyecto(null, uid, pass, NivelSeguridad.SUPERADMIN))
			api.addProyecto(cn, description, primerAdmin, tipoRepositorio, esRepoPublico, rutaRepo);
	}



	/**
	 * <p>Elimina definitivamente un proyecto determinado de la Forja.</p>
	 * <p>Puede invocarlo: superadmin.</p>
	 * @param cnProyecto Nombre del proyecto a eliminar
	 * @param uid UID de quien invoca el método
	 * @param pass Contraseña de quien invoca el método
	 * @throws ExcepcionLogin Se produce cuando el login suministrado no es correcto
	 * o no existe el usuario cuyo login se facilita o este no puede consultar
	 * dicha información.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando el recurso buscado no existe
	 * en la Forja
	 * @throws ExcepcionGestorLDAP Cuando no se puede conectar con el directorio
	 * LDAP.
	 * @throws ExcepcionGeneradorFicherosApache Cuando no se pudieron regenerar
	 * correctamente los ficheros de configuración de Apache
	 * @throws ExcepcionConsola Cuando no se pudo reconfigurar Apache
	 * @throws ExcepcionGestorRedmine Cuando no se pudo eliminar el proyecto de
	 * la base de datos de Redmine.
	 * @throws ExcepcionParametros Se lanza cuando se llama al método con algún parámetro
	 * a null que no puede ser nulo.
	 */
	public void eliminarProyecto(String cnProyecto, String uid, String pass)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionParametros {

		// Comprobación de los parámetros de invocación del parámetro
		Object[] paramsRequeridos = {cnProyecto,uid,pass};
		if (!this.checkParamsOk(paramsRequeridos))
			throw new ExcepcionParametros("Faltan los parámetros necesarios para poder invocar este método: {cnProyecto,uid,pass}");

		if (this.checkCredentialsProyecto(null, uid, pass, NivelSeguridad.SUPERADMIN))
			api.deleteProyecto(cnProyecto);
	}





























	/**
	 * <p>Método que sirve para controlar quién puede invocar determinadoss
	 * métodos enfocados a usuarios de la Forja.</p>
	 * <p>Además, verifica también que el nombre de usuario y contraseñas facilitadas
	 * sean también correctas.</p>
	 * @param uidConsulta UID del usuario sobre el que queremos realizar la operación
	 * @param uid UID del usuario que solicita el método
	 * @param pass Contraseña del usuario que solicita el método
	 * @param nivel NivelSeguridad que queremos comprobar. Depende del método que
	 * lo esté invocando.
	 * @return true si el usuario dispone de los privilegios necesarios
	 * @throws ExcepcionLogin Cuando la contraseña suministrada no es correcta,
	 * no existe el usuario solicitante o sus privilegios son insuficientes.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe alguno de los usuarios
	 * involucrados
	 * @throws ExcepcionGestorLDAP Cuando hay problemas con la conexión a LDAP
	 */
	private boolean checkCredentialsUsuario(String uidConsulta, String uid, String pass, NivelSeguridad nivel)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		// Primero comprueba los datos del solicitante
		checkLogin(uid,pass);

		// Comprueba en función del nivel de la consulta si puede realizarla o no
		switch(nivel) {
		case USUARIO:
			if (esUsuario(uid, uidConsulta) || esAdminDeUser(uid, uidConsulta) || esSuperAdmin(uid))
				return true;
		case ADMIN:
			if (esAdminDeUser(uid, uidConsulta) || esSuperAdmin(uid))
				return true;
		case SUPERADMIN:
			if (esSuperAdmin(uid))
				return true;
		}

		// Si no ha salido del método en el switch es que no tiene privilegios
		throw new ExcepcionLogin("El usuario " + uid + " no tiene privilegios para realizar esta operación sobre " + uidConsulta);
	}



	/**
	 * <p>Método que sirve para saber quién puede realizar operaciones sobre un
	 * proyecto determinado.</p>
	 * <p>Además, verifica también que el nombre de usuario y contraseñas facilitadas
	 * sean también correctas.</p>
	 * @param cnProyecto Nombre del proyecto en el que se va a realizar la operación
	 * @param uid UID del usuario que invoca el método
	 * @param pass Contraseña del usuario que invoca el método
	 * @param nivel NivelSeguridad que queremos comprobar. Depende del método que
	 * lo esté invocando.
	 * @return true si el usuario dispone de los privilegios necesarios
	 * @throws ExcepcionLogin Cuando la contraseña suministrada no es correcta,
	 * no existe el usuario solicitante o sus privilegios son insuficientes.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe alguno de los usuarios
	 * involucrados
	 * @throws ExcepcionGestorLDAP Cuando hay problemas con la conexión a LDAP
	 */
	private boolean checkCredentialsProyecto(String cnProyecto, String uid, String pass, NivelSeguridad nivel)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		// Primero comprueba los datos del solicitante
		checkLogin(uid,pass);

		// Comprueba en función del nivel de la consulta si puede realizarla o no
		switch(nivel) {
		case USUARIO:
			if (perteneceAProyecto(cnProyecto, uid) || esSuperAdmin(uid))
				return true;
		case ADMIN:
			if (esAdminDeProyecto(cnProyecto, uid) || esSuperAdmin(uid))
				return true;
		case SUPERADMIN:
			if (esSuperAdmin(uid))
				return true;
		}

		// Si no ha salido del método en el switch es que no tiene privilegios
		throw new ExcepcionLogin("El usuario " + uid + " no tiene privilegios para realizar esta operación sobre " + cnProyecto);
	}



	/**
	 * <p>Método que sirve para comprobar quién puede realizar invocaciones sobre
	 * métodos generalistas de la Forja. Es decir, aquellas restringidas a admins
	 * y superadmins.</p>
	 * <p>Además, verifica también que el nombre de usuario y contraseñas facilitadas
	 * sean también correctas.</p>
	 * @param uid UID del usuario que invoca el método
	 * @param pass Contraseña del usuario que invoca el método
	 * @param nivel NivelSeguridad que queremos comprobar. Depende del método que
	 * lo esté invocando.
	 * @return true si el usuario dispone de los privilegios necesarios
	 * @throws ExcepcionLogin Cuando la contraseña suministrada no es correcta,
	 * no existe el usuario solicitante o sus privilegios son insuficientes.
	 * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe alguno de los usuarios
	 * involucrados
	 * @throws ExcepcionGestorLDAP Cuando hay problemas con la conexión a LDAP
	 */
	private boolean checkCredentialsGeneral(String uid, String pass, NivelSeguridad nivel)
			throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		// Primero comprueba los datos del solicitante
		checkLogin(uid,pass);

		// Comprueba en función del nivel de la consulta si puede realizarla o no
		switch(nivel) {
		case ADMIN:
			if (esAdminDeAlgunProyecto(uid) || esSuperAdmin(uid))
				return true;
		case SUPERADMIN:
			if (esSuperAdmin(uid))
				return true;
		}

		// Si no ha salido del método en el switch es que no tiene privilegios
		throw new ExcepcionLogin("El usuario " + uid + " no tiene privilegios para realizar esta operación");

	}



	/**
	 * <p>Comprueba si la contraseña suministrada coincide con la que tiene el
	 * usuario almacenada en el directorio LDAP.</p>
	 * @param uid UID del usuario
	 * @param pass Contraseña en claro del usuario
	 * @return true si la contraseña es correcta, sino lanza una excepción indicando
	 * que es incorrecta.
	 * @throws ExcepcionLogin Cuando se produce algún error durante el login de
	 * usuario.
	 */
	private boolean checkLogin(String uid, String pass) throws ExcepcionLogin {
		try {
			String passAlmacenada = api.getUsuario(uid).getPassMD5();
			String passSuministrada = Utilidades.toMD5(pass);
			if (passAlmacenada == null)
				throw new ExcepcionLogin("Error de login: Usuario " + uid + " bloqueado. Contacte con su administrador.");
			else if(passAlmacenada.equals(passSuministrada)) {
				return true;
			} else {
				throw new ExcepcionLogin("Error de Login: La contraseña suministrada es incorrecta.");
			}
		} catch (ExcepcionLDAPNoExisteRegistro ex) {
			throw new ExcepcionLogin("Error de Login: " + ex.getMessage());
		} catch (ExcepcionGestorLDAP ex) {
			throw new ExcepcionLogin("Error de Login: " + ex.getMessage());
		} catch (NoSuchAlgorithmException ex) {
			throw new ExcepcionLogin("Error de Login: No se puede encontrar el algoritmo MD5 en las librerías de Java - " + ex.getMessage());
		}
	}


	private boolean esUsuario(String uidUser, String uidConsulta) {
		return uidUser.equals(uidConsulta);
	}

	private boolean esAdminDeUser(String uidAdmin, String uidUser) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		ArrayList<String> proyectosAdmin = api.getListaProyectosAdministrados(uidAdmin);
		ArrayList<String> proyectosParticipados = api.getListaProyectosParticipados(uidUser);

		for (String proyectoAdmin : proyectosAdmin) {
			if (proyectosParticipados.contains(proyectoAdmin))
				return true;
		}
		return false;
	}

	private boolean esSuperAdmin(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		if (api.getListaProyectosParticipados(uid).contains(ConfiguracionForja.groupSuperadmin))
			return true;
		else
			return false;
	}

	private boolean perteneceAProyecto(String cnProyecto, String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		if (api.getListaProyectosParticipados(uid).contains(cnProyecto))
			return true;
		else
			return false;
	}

	private boolean esAdminDeProyecto(String cnProyecto, String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		if (api.getListaProyectosAdministrados(uid).contains(cnProyecto))
			return true;
		else
			return false;
	}

	private boolean esAdminDeAlgunProyecto(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
		if (api.getListaProyectosAdministrados(uid).isEmpty())
			return false;
		else
			return true;
	}



	/**
	 * <p>Comprueba si en la lista de parámetros recibidos hay alguno que sea nulo.</p>
	 * <p>Se utiliza para comprobar que se invoca correctamente los distintos métodos.</p>
	 * @param parametros Lista de los parámetros a comprobar que no sean nulos
	 * @return true si no hay ningun parámetro nulo, false si hay alguno nulo
	 */
	private boolean checkParamsOk(Object[] parametros) {
		for (Object aux : parametros) {
			if (aux == null)
				return false;
		}
		return true;
	}
}
