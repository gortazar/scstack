/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GestorMysql.java
 * Autor: Arek Klauza
 * Fecha: Febrero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.redmine;

import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.repos.RepositorioGIT;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionMysql;

import java.sql.*;
import java.util.Locale;
import java.util.logging.Logger;


/**
 * <p>Se utilizará este sencillo gestor para las tareas que requieran manipular
 * directamente la Base de Datos Mysql de Redmine.</p>
 * @author Arek Klauza
 */
public class GestorMysql {
    private Connection conexion;
    private Statement stmt;
	private Logger log;


    /**
     * <p>Construye un sencillo gestor para poder ejecutar consultas y sentencias
     * sobre la base de datos Mysql de Redmine.</p>
     * @param log 
     * @throws ExcepcionMysql Cuando se ha producido algún error al intentar
     * establecer la conexión con el servidor de Mysql.
     */
    public GestorMysql(Logger log) throws ExcepcionMysql {
    	this.log = log;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conexion = DriverManager.getConnection("jdbc:mysql://" + ConfiguracionForja.hostMysql, ConfiguracionForja.usernameMysql, ConfiguracionForja.passMysql);
            stmt = conexion.createStatement();
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL: imposible conectar con la BD de Redmine: " + e.getMessage());
        } catch (Exception e) {
            throw new ExcepcionMysql("Error durante la conexión a MySQL: " + e.getMessage());
        }
    }



    /**
     * <p>Se encarga de activar el login mediante LDAP para los nuevos usuarios
     * creados.</p>
     * @param uid UID del usuario que queremos activar
     * @throws ExcepcionMysql
     */
    public void activarLoginUsuarioLDAP(String uid) throws ExcepcionMysql {
        String consulta = "UPDATE " + ConfiguracionForja.schemaRedmine + ".users SET auth_source_id=1 WHERE login='" + uid + "'";
        try {
        	log.info("Running sql statement: " + consulta);
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }


    /**
     * <p>Se utilizará para que por defecto los proyectos no sean públicos, sino privados.</p>
     * @param cnProyecto CN del proyecto que acabamos de crear
     * @throws ExcepcionMysql
     */
    public void desactivarPublicidadProyectos(String cnProyecto) throws ExcepcionMysql {
        String consulta = "UPDATE " + ConfiguracionForja.schemaRedmine + ".projects SET is_public=0 WHERE name='" + cnProyecto + "'";
        try {
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }


    /**
     * <p>Método encargado de añadir un administrador a un proyecto determinado
     * en Redmine.</p>
     * @param uid UID del usuario a añadir como admin
     * @param cnProyecto CN del proyecto donde añadirle
     * @throws ExcepcionMysql
     */
    public void addAdministradorAProyecto(String uid, String cnProyecto) throws ExcepcionMysql {
        this.addRoleAProyecto(uid, cnProyecto, "3");
        this.addRoleAProyecto(uid, cnProyecto, "4");
    }



    /**
     * <p>Método encargado de añadir un miembro a un proyecto determinado
     * en Redmine.</p>
     * @param uid UID del usuario a añadir como admin
     * @param cnProyecto CN del proyecto donde añadirle
     * @throws ExcepcionMysql
     */
    public void addMiembroAProyecto(String uid, String cnProyecto) throws ExcepcionMysql {
        this.addRoleAProyecto(uid, cnProyecto, "4");
    }


    /**
     * <p>Método que cambia los roles de un administrador de un proyecto a miembro.</p>
     * @param uid UID del administrador que se convertirá en miembro
     * @param cnProyecto CN del proyecto a modificar
     * @throws ExcepcionMysql
     */
    public void deleteAdministradorProyecto(String uid, String cnProyecto) throws ExcepcionMysql {
        // Primero recuperamos los datos para el manejo de la BBDD Redmine
        String userID = this.getUserID(uid);
        String projectID = this.getProjectID(cnProyecto);

        // Borra solo el rol de administrador (3)
        String consulta = "DELETE FROM " + ConfiguracionForja.schemaRedmine + ".member_roles WHERE role_id=3 AND "
                + "member_id=(SELECT id FROM " + ConfiguracionForja.schemaRedmine + ".members WHERE user_id=" + userID + " AND project_id=" + projectID + ")";
        try {
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }


    /**
     * <p>Elimina un miembro de un proyecto determinado.</p>
     * @param uid UID del miembro a quitar del proyecto
     * @param cn CN del proyecto implicado
     * @throws ExcepcionMysql
     */
    public void deleteMiembroProyecto(String uid, String cn) throws ExcepcionMysql {
        // Primero recuperamos los datos para el manejo de la BBDD Redmine
        String userID = this.getUserID(uid);
        String projectID = this.getProjectID(cn);

        String consulta = "";
        try {
            // Si le borramos como miembro, implícitamente también como administrador
            consulta = "DELETE FROM " + ConfiguracionForja.schemaRedmine + ".member_roles "
                  + "WHERE member_id=(SELECT id FROM " + ConfiguracionForja.schemaRedmine + ".members WHERE user_id=" + userID + " AND project_id=" + projectID + ")";
            stmt.executeUpdate(consulta);
            consulta = "DELETE FROM " + ConfiguracionForja.schemaRedmine + ".members WHERE user_id=" + userID + " AND project_id=" + projectID;
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }


    /**
     * <p>Elimina a un usuario determinado de la BBDD de Redmine.</p>
     * @param uid UID del usuario a borrar
     * @throws ExcepcionMysql
     */
    public void deleteUsuario(String uid) throws ExcepcionMysql {
        String consulta = "DELETE FROM " + ConfiguracionForja.schemaRedmine + ".users WHERE login='"+ uid +"'";
        try {
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }



    /**
     * <p>Manipula directamente la base de datos Mysql de Redmine para añadir
     * un repositorio a un proyecto determinado.</p>
     * <p>Es preciso destacar que en Redmine sólo puede haber un repositorio a
     * la vez, por tanto, se establecerá siempre en defaultRepositorio.</p>
     * @param proyecto Objeto Proyecto del modelo de datos de la Forja
     * @param id Identificador que tiene el proyecto en la tabla de Redmine. Nos
     * lo devuelve la API REST de Redmine al crear el proyecto.
     * @throws ExcepcionMysql Se lanza cuando se ha producido algún error durante
     * la ejecución de la sentencia de insertado en Mysql.
     */
    public void addRepositorio(Proyecto proyecto, Integer id) throws ExcepcionMysql {
        String consulta = "";
        try {
            if (proyecto.getDefaultRepositorio().equals(RepositorioGIT.tipo)) {
                consulta = String.format(
                		Locale.US, 
                		"INSERT INTO %s.repositories VALUES(%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d)", 
                		ConfiguracionForja.schemaRedmine,
                		"null",
                		id,
                		"'file://" + ConfiguracionForja.pathGITApache + "/" + proyecto.getCn() + "'",
                		"''",
                		"''",
                		"''",
                		"'Repository::Git'",
                		"null",
                		"null",
                		"null",
                		"'" + proyecto.getCn() + "'",
                		1);
            } else {
                consulta = String.format(
                		Locale.US, 
                		"INSERT INTO %s.repositories VALUES(%s,%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%d)", 
                		ConfiguracionForja.schemaRedmine,
                		"null",
                		id,
                		"'file://" + ConfiguracionForja.pathSVNApache + "/" + proyecto.getCn() + "'",
                		"''",
                		"''",
                		"''",
                		"'Repository::Subversion'",
                		"null",
                		"null",
                		"null",
                		"'" + proyecto.getCn() + "'",
                		1);
            }
            log.info("Executing query: " + consulta);
            stmt.execute(consulta);
            
        } catch (SQLException e) {
            throw new ExcepcionMysql(e);
        }
    }


    /**
     * <p>Manipula directamente la base de datos Mysql de Redmine para modificar
     * el repositorio de un proyecto determinado.</p>
     * <p>Es preciso destacar que en Redmine sólo puede haber un repositorio a
     * la vez, por tanto, se establecerá siempre en defaultRepositorio.</p>
     * @param proyecto Objeto Proyecto del modelo de datos de la Forja
     * @param id Identificador que tiene el proyecto en la tabla de Redmine. Nos
     * lo devuelve la API REST de Redmine al crear el proyecto.
     * @throws ExcepcionMysql Se lanza cuando se ha producido algún error durante
     * la ejecución de la sentencia de actualización en Mysql.
     */
    public void updateRepositorio(Proyecto proyecto, Integer id) throws ExcepcionMysql {
        String consulta = "";
        try {
        	
            if (proyecto.getDefaultRepositorio().equals(RepositorioGIT.tipo)) {
            	consulta = String.format(
            			Locale.US,
            			"UPDATE %s.repositories SET url=%s, type=%s WHERE project_id=%d",
            			ConfiguracionForja.schemaRedmine,
            			"'file://" + ConfiguracionForja.pathGITApache + "/" + proyecto.getCn() + "'",
            			"Repositories::Git",
            			id
            			);
            } else {
            	consulta = String.format(
            			Locale.US,
            			"UPDATE %s.repositories SET url=%s, type=%s WHERE project_id=%d",
            			ConfiguracionForja.schemaRedmine,
            			"'file://" + ConfiguracionForja.pathSVNApache + "/" + proyecto.getCn() + "'",
            			"Repositories::Subversion",
            			id
            			);
            }
            
            log.info("Executing query: " + consulta);
            stmt.executeUpdate(consulta);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }





    /**
     * <p>Añade a un usuario determinado como miembro de un proyecto determinado,
     * tanto en la tabla "members" como "member_role".</p>
     * @param uid UID del usuario a añadir
     * @param cn CN del proyecto
     * @param role 3 para administrador y 4 para desarrollador
     * @throws ExcepcionMysql
     */
    private void addRoleAProyecto(String uid, String cn, String role) throws ExcepcionMysql {
        
    	log.info("Adding role " + role + " to project " + cn + " for user " + uid);
    	
    	// Primero recuperamos los datos para el manejo de la BBDD Redmine
        String userID = this.getUserID(uid);
        String projectID = this.getProjectID(cn);

        // Solo insertamos si no existe ese miembro con ese rol
        String consulta = "SELECT * FROM " + ConfiguracionForja.schemaRedmine + ".members WHERE user_id='" + userID + "' AND project_id='" + projectID + "'";
        try {
        	log.info("Executing query: " + consulta);
            ResultSet resultado = stmt.executeQuery(consulta);

            if (resultado.next()) {
            	
                String member_id = resultado.getString("id");
                consulta = "SELECT * FROM " + ConfiguracionForja.schemaRedmine + ".member_roles WHERE role_id=" + role + " AND member_id='"+member_id+"'";
            	log.info("Executing query: " + consulta);
                resultado = stmt.executeQuery(consulta);
                // Si ya existe el usuario con ese rol en ese proyecto, no hay que hacer nada
                if (resultado.next()) {
                	log.info("User " + uid + " already has role " + role);
                    return;
                } else {
                    // Si existe el usuario en el proyecto pero con otro rol, añadimos
                	consulta = "INSERT INTO " + ConfiguracionForja.schemaRedmine + ".member_roles VALUES(null, " + member_id + ", " + role + ", null)";
                	log.info("Executing query: " + consulta);
                    stmt.execute(consulta);
                }
            // Si no existe aún el usuario en el proyecto
            } else {
                consulta = "INSERT INTO " + ConfiguracionForja.schemaRedmine + ".members "
                    + "VALUES(null, '" + userID + "', '" + projectID + "', null, 0)";
            	log.info("Executing query: " + consulta);
                stmt.execute(consulta);
                consulta = "INSERT INTO " + ConfiguracionForja.schemaRedmine + ".member_roles "
                    + "VALUES(null, (SELECT id FROM " + ConfiguracionForja.schemaRedmine + ".members WHERE user_id=" + userID + " AND project_id=" + projectID + "), " + role + ", null)";
            	log.info("Executing query: " + consulta);
                stmt.execute(consulta);
            }
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }
    
    
    /**
     * <p>Devuelve el UserID almacenado en la BBDD de Redmine.</p>
     * @param uid UID del usuario
     * @return UserID
     * @throws ExcepcionMysql
     */
    private String getUserID(String uid) throws ExcepcionMysql {
        String consulta = "SELECT id FROM " + ConfiguracionForja.schemaRedmine + ".users WHERE login='" + uid + "'";
        try {
            ResultSet resultado = stmt.executeQuery(consulta);
            if (resultado.next())
                return resultado.getString("id");
            else
                throw new ExcepcionMysql("No se pudo encontrar en la BBDD de Redmine el UserID de : " + uid);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }


    /**
     * <p>Devuelve el ProjectID almacenado en la BBDD de Redmine.</p>
     * @param cn Nombre del proyecto
     * @return ProjectID
     * @throws ExcepcionMysql
     */
    private String getProjectID(String cn) throws ExcepcionMysql {
        String consulta = "SELECT id FROM " + ConfiguracionForja.schemaRedmine + ".projects WHERE name='" + cn + "'";
        try {
            ResultSet resultado = stmt.executeQuery(consulta);
            if (resultado.next())
                return resultado.getString("id");
            else
                throw new ExcepcionMysql("No se pudo encontrar en la BBDD de Redmine el ProjectID de : " + cn);
        } catch (SQLException e) {
            throw new ExcepcionMysql("Error de MySQL al intentar realizar la consulta: " + consulta);
        }
    }
}