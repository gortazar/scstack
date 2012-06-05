/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Usuario.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.dataModel;

import es.sidelab.scstack.lib.commons.Utilidades;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;

/**
 * <p>Objeto que representa un registro de usuario en el directorio LDAP.</p>
 * @author Arek Klauza
 */
public class Usuario {
    /** UID unívoco de usuario */
    private String uid;
    /** Nombre (CN) del usuario */
    private String nombre;
    /** Apellidos (SN) del usuario */
    private String apellidos;
    /** Dirección email del usuario */
    private String email;
    /** Contraseña codificada en MD5 */
    private String passMD5;

    
    /**
     * <p>Crea un usuario de la forja (puede ser administrador o usuario estándar).</p>
     * @param uid UID unívoco de usuario
     * @param nombre Nombre (CN - Common Name) del usuario
     * @param apellidos Apellidos (SN - SurName) del usuario
     * @param email Dirección Email del usuario
     * @param passMD5 Contraseña codificada en MD5 del usuario
     */
    public Usuario(String uid, String nombre, String apellidos, String email, String passMD5) throws ExcepcionUsuario {
        // Primero comprobamos que el UID no contenga tildes ni ñ
        if (Utilidades.containsIlegalCharsUsuario(uid))
            throw new ExcepcionUsuario("El UID de usuario contiene carácteres prohibidos (no cumple [_a-z0-9]+)");
        this.uid = uid;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.passMD5 = passMD5;
        this.email = email;
    }






    /* GETTERS de la Clase */

    public String getApellidos() {
        return apellidos;
    }
    public String getEmail() {
        return email;
    }
    public String getNombre() {
        return nombre;
    }
    public String getPassMD5() {
        return passMD5;
    }
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
    public void setPassMD5(String passMD5) {
        this.passMD5 = passMD5;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Usuario other = (Usuario) obj;
        if ((this.uid == null) ? (other.uid != null) : !this.uid.equals(other.uid)) {
            return false;
        }
        if ((this.nombre == null) ? (other.nombre != null) : !this.nombre.equals(other.nombre)) {
            return false;
        }
        if ((this.apellidos == null) ? (other.apellidos != null) : !this.apellidos.equals(other.apellidos)) {
            return false;
        }
        if ((this.email == null) ? (other.email != null) : !this.email.equals(other.email)) {
            return false;
        }
        if ((this.passMD5 == null) ? (other.passMD5 != null) : !this.passMD5.equals(other.passMD5)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }








    


}
