/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Usuario.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.data;

import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Clase que representa a las entidades usuario de la Forja.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "usuario")
public class Usuario extends BaseEntity {
    /** UID unívoco de usuario */
    private String uid;
    /** Nombre (CN) del usuario */
    private String nombre;
    /** Apellidos (SN) del usuario */
    private String apellidos;
    /** Dirección email del usuario */
    private String email;
    /** Contraseña codificada en MD5 */
    private String pass;


    public Usuario() {
        super();
    }

    
    public Usuario(String uid, String nombre, String apellidos, String email, String pass) {
        this();
        this.uid = uid;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.pass = pass;
    }

    public Usuario(es.sidelab.scstack.lib.dataModel.Usuario api_user) {
        this();
        this.uid = api_user.getUid();
        this.nombre = api_user.getNombre();
        this.apellidos = api_user.getApellidos();
        this.email = api_user.getEmail();
        this.pass = api_user.getPassMD5();
    }


    public es.sidelab.scstack.lib.dataModel.Usuario toUsuarioAPI() throws ExcepcionUsuario {
        return new es.sidelab.scstack.lib.dataModel.Usuario(uid, nombre, apellidos, email, pass);
    }






    /* GETTERS de la Clase */

    @XmlElement
    public String getApellidos() {
        return apellidos;
    }
    @XmlElement
    public String getEmail() {
        return email;
    }
    @XmlElement
    public String getNombre() {
        return nombre;
    }
    @XmlElement
    public String getPass() {
        return pass;
    }
    @XmlElement
    public String getUid() {
        return uid;
    }





    /* SETTERS de la Clase */

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
}
