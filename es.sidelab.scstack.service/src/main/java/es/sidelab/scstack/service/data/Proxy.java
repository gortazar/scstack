/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Proxy.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.data;

import es.sidelab.scstack.lib.api.API_Segura;
import es.sidelab.scstack.lib.commons.Utilidades;
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
import java.util.logging.Logger;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;



/**
 * <p>Clase encargada de realizar las conversiones necesarias entre los datos
 * que maneja la API Java de la Forja y el modelo de datos del servicio RESTlet.</p>
 * <p>Esta clase actúa de wrapper sobre la API de la Forja.</p>
 * <p>Además, esta clase también provee un patrón Singleton sobre la API de la Forja,
 * de modo que siempre haya una única instancia de Proxy en el servicio web,
 * facilitando la posibilidad de controlar la concurrencia.</p>
 * @author Arek Klauza
 */
public class Proxy {
    public API_Segura api;

	private Logger log;

    /**
     * Patrón Singleton
     */
    private static Proxy instance = new Proxy(Logger.getLogger(Proxy.class.getName()));


    private Proxy(Logger logger) {
    	this.log = logger;
        try {
            api = new API_Segura();
        } catch (ExcepcionForja e) {
            System.err.println(e.getMessage());
        }
    }


    /**
     * Devuelve la instancia del Proxy
     */
    public static Proxy getInstance() {
        return instance;
    }



    public String doLogin(String user, String pass) throws ExcepcionLDAPNoExisteRegistro, ExcepcionParametros, ExcepcionLogin, ExcepcionGestorLDAP {
    	
   		return api.doLogin(user, pass);
   		
    }


    public Usuarios getUsuariosXUid(String user, String pass) 
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoUidsUsuarios(user, pass));
        return users;
    }


    public Usuarios getUsuariosXNombre(String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoNombresUsuarios(user, pass));
        return users;
    }

    
    public Usuarios getEmailsUsuarios(String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoEmailsUsuarios(user, pass));
        return users;
    }


    public Usuario getUsuario(String uid, String user, String pass) 
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        es.sidelab.scstack.lib.dataModel.Usuario api_user = api.getDatosUsuario(uid, user, pass);
        return new Usuario(api_user);
    }


    synchronized public void postUsuario(Usuario usuario, String user, String pass)
            throws ExcepcionUsuario, ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, NoSuchAlgorithmException, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
        api.crearUsuario(usuario.getUid(), usuario.getNombre(), usuario.getApellidos(), usuario.getEmail(), usuario.getPass(), user, pass);
    }

    
    synchronized public void putUsuario(Usuario usuario, String user, String pass)
            throws NoSuchAlgorithmException, ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorRedmine, ExcepcionUsuario, ExcepcionGestorLDAP, ExcepcionParametros {
        // En la edición no se codifica a MD5 la contraseña en la API,
        // Como lo tiene que recibir codificado, lo tenemos que hacer a mano
        if (usuario.getPass() != null && !usuario.getPass().isEmpty())
            usuario.setPass(Utilidades.toMD5(usuario.getPass()));
        api.editarDatosUsuario(usuario.toUsuarioAPI(), user, pass);
            
    }


    synchronized public void deleteUsuario(String uid, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionConsola, ExcepcionGeneradorFicherosApache, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
        api.eliminarUsuario(uid, user, pass);
    }


    synchronized public void desactivarUsuario(String uid, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        api.bloquearUsuario(uid, user, pass);
    }


    synchronized public void activarUsuario(String uid, String nuevaPass, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, NoSuchAlgorithmException, ExcepcionGestorLDAP, ExcepcionParametros {
        api.desbloquearUsuario(uid, nuevaPass, user, pass);
    }


    public Proyectos getListaProyectosParticipados(String uid, String user, String pass) 
            throws ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorLDAP, ExcepcionParametros {
        Proyectos proyectos = new Proyectos();
        proyectos.setListaProyectos(api.getListadoProyectosParticipados(uid, user, pass));
        return proyectos;
    }


    public Proyectos getListaProyectosAdministrados(String uid, String user, String pass)
            throws ExcepcionLDAPNoExisteRegistro, ExcepcionLogin, ExcepcionGestorLDAP, ExcepcionParametros {
        Proyectos proyectos = new Proyectos();
        proyectos.setListaProyectos(api.getListadoProyectosAdministrados(uid, user, pass));
        return proyectos;
    }


    public Usuarios getListaUsuariosAdministrados(String uid, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoUsuariosAdministrados(uid, user, pass));
        return users;
    }


    public Proyectos getListaProyectos(String user, String pass) 
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Proyectos proyectos = new Proyectos();
        proyectos.setListaProyectos(api.getListadoProyectos(user, pass));
        return proyectos;
    }


    public Proyecto getProyecto(String cn, String user, String pass) 
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        es.sidelab.scstack.lib.dataModel.Proyecto api_proy = api.getDatosProyecto(cn, user, pass);
        return new Proyecto(api_proy);
    }


    synchronized public void postProyecto(ProyectoNuevo p, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionProyecto, ExcepcionGeneradorFicherosApache,
            ExcepcionConsola, ExcepcionRepositorio, ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionParametros {
        api.crearProyecto(p.getCn(), p.getDescripcion(), p.getPrimerAdmin(), p.getTipoRepo() , p.isEsRepoPublico(), p.getRutaRepo(), user, pass);
    }


    synchronized public void putProyecto(Proyecto proyecto, String user, String pass)
            throws ExcepcionProyecto, ExcepcionLogin, ExcepcionGestorRedmine, ExcepcionLDAPNoExisteRegistro,
            ExcepcionParametros, ExcepcionGestorLDAP, ResourceException {
        if (proyecto.getCn() != null && proyecto.getDescripcion() != null)
            api.editarDatosProyecto(proyecto.toProyectoAPI(), user, pass);
        else
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "El XML recibido no contiene CN y/o descripcion del Proyecto");
    }


    synchronized public void deleteProyecto(String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache, ExcepcionConsola,
            ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionParametros {
        api.eliminarProyecto(cn, user, pass);
    }


    public Usuarios getMiembrosProyecto(String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoUsuariosPorProyecto(cn, user, pass));
        return users;
    }


    synchronized public void putMiembroProyecto(String uid, String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
        api.addUsuarioAProyecto(uid, cn, user, pass);
    }


    synchronized public void deleteMiembroProyecto(String uid, String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionConsola, ExcepcionGeneradorFicherosApache,
            ExcepcionLDAPAdministradorUnico, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
        api.eliminarUsuarioDeProyecto(uid, cn, user, pass);
    }


    public Usuarios getAdminsProyecto(String cn, String user, String pass) 
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros {
        Usuarios users = new Usuarios();
        users.setListaUsuarios(api.getListadoAdministradoresPorProyecto(cn, user, pass));
        return users;
    }


     synchronized public void putAdminProyecto(String uid, String cn, String user, String pass)
             throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
         api.addAdminAProyecto(uid, cn, user, pass);
    }


    synchronized public void deleteAdminProyecto(String uid, String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache,
            ExcepcionLDAPAdministradorUnico, ExcepcionConsola, ExcepcionGestorLDAP, ExcepcionParametros, ExcepcionGestorRedmine {
        api.eliminarAdminDeProyecto(uid, cn, user, pass);
    }


    synchronized public void postRepositorio(Repositorio repo, String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionRepositorio, ExcepcionGeneradorFicherosApache,
            ExcepcionParametros, ExcepcionConsola, ExcepcionGestorLDAP {
        api.addRepositorioAProyecto(repo.getTipo(), repo.isEsPublico(), repo.getRuta(), cn, user, pass);
    }


    synchronized public void deleteRepositorio(String tipo, String cn, String user, String pass)
            throws ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache, ExcepcionConsola,
            ExcepcionParametros, ExcepcionGestorLDAP {
        api.eliminarRepositorioDeProyecto(tipo, cn, user, pass);
    }

}
