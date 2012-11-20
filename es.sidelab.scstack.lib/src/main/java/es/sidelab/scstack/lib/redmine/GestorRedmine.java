/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GestorRedmine.java
 * Autor: Arek Klauza
 * Fecha: Febrero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.redmine;

import java.util.logging.Logger;

import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionMysql;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

/**
 * <p>Clase encargada de la comunicación con la API de Redmine. A través de ésta
 * podemos gestionar proyectos y usuarios de Redmine de forma sencilla.</p>
 * @author Arek Klauza
 */
public class GestorRedmine {
    private String redmineHost;
    private String apiAccessKey;
    private RedmineManager gestorAPI;
	private Logger log;


    /**
     * <p>Crea una clase GestorRedmine para poder gestionar los proyectos y
     * usuarios de Redmine. Utilizando para ello los parámetros de las variables
     * estáticas de la Forja.</p>
     * @param log 
     */
    public GestorRedmine(Logger log) throws ExcepcionMysql {
    	this.log = log;
        this.redmineHost = ConfiguracionForja.protocolRedmine + ConfiguracionForja.hostRedmine;
        this.apiAccessKey = ConfiguracionForja.keyRedmineAPI;
        this.gestorAPI = new RedmineManager(redmineHost, apiAccessKey);
    }


    /**
     * <p>Crea un nuevo proyecto en la aplicación Redmine.</p>
     * @param proyecto Objeto Proyecto de la Forja
     * @throws ExcepcionGestorRedmine Se produce cuando ha habido errores durante
     * la comunicación o ejecución de la API de Redmine.
     */
    public void crearProyecto(Proyecto proyecto) throws ExcepcionGestorRedmine {
    	
    	log.info("Creating project " + proyecto.getCn());
        // Transformamos el Proyecto de la Forja en Project de Redmine
        Project proyectoRedmine = new Project();

        // Como restricción el identifier debe ser todo en minúsculas y puede contener números.
        proyectoRedmine.setIdentifier(proyecto.getCn().toLowerCase());
        proyectoRedmine.setName(proyecto.getCn());
        proyectoRedmine.setDescription(proyecto.getDescription());
        try {
        	log.info("Creating project in Redmine");
            Project proyectoCreado = gestorAPI.createProject(proyectoRedmine);
            
            GestorMysql gestorMysql = new GestorMysql(this.log);

            // Solo si el proyecto tiene repositorio se lo añadimos en Redmine
            if (proyecto.tieneRepositorio()) {
            	log.info("Adding repository to redmine db");
                gestorMysql.addRepositorio(proyecto, proyectoCreado.getId());
            }

            // Añade al primer administrador del proyecto en Redmine
            log.info("Adding admin to project");
            gestorMysql.addAdministradorAProyecto(proyecto.getPrimerAdmin(), proyecto.getCn());
        } catch (ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error de Mysql en el GestorRedmine: " + ex.getMessage());
        } catch (RedmineException ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error al ejecutar la API Redmine: " + ex.getMessage());
        }
    }


    /**
     * <p>Actualiza los datos de configuración de un proyecto en Redmine.</p>
     * @param proyecto Objeto Proyecto de la Forja
     * @throws ExcepcionGestorRedmine Se produce cuando ha habido errores durante
     * la comunicación o ejecución de la API de Redmine.
     */
    public void editarProyecto(Proyecto proyecto) throws ExcepcionGestorRedmine {
        // Unificamos el Proyecto de la Forja con el Project de Redmine
        try {
            Project proyectoRedmine = gestorAPI.getProjectByKey(proyecto.getCn());
            proyectoRedmine.setDescription(proyecto.getDescription());
            gestorAPI.update(proyectoRedmine);
            if (proyecto.tieneRepositorio())
                new GestorMysql(this.log).updateRepositorio(proyecto, proyectoRedmine.getId());
        } catch (ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error de Mysql en el GestorRedmine: " + ex.getMessage());
        } catch (NotFoundException ex) {
            throw new ExcepcionGestorRedmine("El proyecto " + proyecto.getCn() + " no existe en Redmine: " + ex.getMessage());
        } catch (RedmineException ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error al ejecutar la API Redmine: " + ex.getMessage());
        }
    }


    /**
     * <p>Elimina definitivamente un proyecto determinado de Redmine.</p>
     * @param proyecto Objeto Proyecto de la Forja
     * @throws ExcepcionGestorRedmine Se produce cuando ha habido errores durante
     * la comunicación o ejecución de la API de Redmine.
     */
    public void borrarProyecto(Proyecto proyecto) throws ExcepcionGestorRedmine {
        try {
            gestorAPI.deleteProject(proyecto.getCn());
        } catch (NotFoundException ex) {
            throw new ExcepcionGestorRedmine("El proyecto " + proyecto.getCn() + " no existe en Redmine: " + ex.getMessage());
        } catch (RedmineException ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error al ejecutar la API Redmine: " + ex.getMessage());
        }
    }



    /**
     * <p>Crea un nuevo usuario en Redmine.</p>
     * @param userForja Objeto Usuario de la Forja
     * @param pass Constraseña en claro del usuario, porque hay que mandársela en
     * claro a Redmine.
     * @throws ExcepcionGestorRedmine Se produce cuando ha habido errores durante
     * la comunicación o ejecución de la API de Redmine.
     */
    public void crearUsuario(Usuario userForja, String pass) throws ExcepcionGestorRedmine {
        // Transformamos el Usuario de la Forja en User de Redmine
        User userRed = new User();

        // Cargamos el userRed con los datos del userForja
        userRed.setLogin(userForja.getUid());
        userRed.setFirstName(userForja.getNombre());
        userRed.setLastName(userForja.getApellidos());
        userRed.setMail(userForja.getEmail());
        userRed.setPassword(pass);
        
        try {          
        	log.info("Creating Redmine user");
            User userCreado = gestorAPI.createUser(userRed);
        } catch (RedmineException ex) {
        	log.severe("Couldn't create Redmine user: " + ex.getMessage());
            throw new ExcepcionGestorRedmine("Se ha producido un error al ejecutar la API Redmine: " + ex.getMessage());
        }
   
        try {
            log.info("Activating user login by writing directly to SQL");
            new GestorMysql(this.log).activarLoginUsuarioLDAP(userForja.getUid());
        } catch (ExcepcionMysql ex) {
        	log.severe("Couldn't activate Redmine user login: " + ex.getMessage());
            throw new ExcepcionGestorRedmine("Se ha producido un error al intentar activar el login por LDAP en Redmine: " + ex.getMessage());
        }
    }



    /**
     * <p>Actualiza los datos personales de un usuario concreto en Redmine.</p>
     * @param user Objeto Usuario de la Forja
     * @throws ExcepcionGestorRedmine Se produce cuando ha habido errores durante
     * la comunicación o ejecución de la API de Redmine.
     */
    public void editarUsuario(Usuario user) throws ExcepcionGestorRedmine {
        // Como la API no devuelve un usuario por su login, tenemos que buscarlo a mano
        try {
            // Establecemos el número de usuarios que nos devolverá la API por petición
            // Este número debe ser superior al total de usuarios de la Forja.
            gestorAPI.setObjectsPerPage(1000);
            for (User userRed : gestorAPI.getUsers()) {
                // Hemos encontrado al que buscamos, ahora le actualizamos
                if (userRed.getLogin().equals(user.getUid())) {
                    userRed.setFirstName(user.getNombre());
                    userRed.setLastName(user.getApellidos());
                    userRed.setMail(user.getEmail());
                    // Solo cambia la contraseña si se ha mandado algo
                    if (user.getPassMD5() != null && !user.getPassMD5().isEmpty())
                        userRed.setPassword(user.getPassMD5());

                    gestorAPI.update(userRed);
                    break;
                }
            }
        } catch (NotFoundException ex) {
            throw new ExcepcionGestorRedmine("El usuario " + user.getUid() + " no existe en Redmine: " + ex.getMessage());
        } catch (RedmineException ex) {
            throw new ExcepcionGestorRedmine("Se ha producido un error al ejecutar la API Redmine: " + ex.getMessage());
        }
    }



    /*  MÉTODOS PROXY DEL GESTOR REDMINE */

    public void activarLoginUsuarioLDAP(String uid) throws ExcepcionGestorRedmine {
        try {
             new GestorMysql(this.log).activarLoginUsuarioLDAP(uid);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void desactivarPublicidadProyectos(String cnProyecto) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).desactivarPublicidadProyectos(cnProyecto);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void addAdministradorAProyecto(String uid, String cnProyecto) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).addAdministradorAProyecto(uid, cnProyecto);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void addMiembroAProyecto(String uid, String cnProyecto) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).addMiembroAProyecto(uid, cnProyecto);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void deleteAdministradorDeProyecto(String uid, String cnProyecto) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).deleteAdministradorProyecto(uid, cnProyecto);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void deleteMiembroDeProyecto(String uid, String cnProyecto) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).deleteMiembroProyecto(uid, cnProyecto);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

    public void deleteUsuario(String uid) throws ExcepcionGestorRedmine {
        try {
            new GestorMysql(this.log).deleteUsuario(uid);
        } catch(ExcepcionMysql ex) {
            throw new ExcepcionGestorRedmine("Error durante el manejo de la BBDD Redmine: " + ex.getMessage());
        }
    }

}
