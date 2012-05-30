/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Proyecto.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package modeloDatos;

import apoyo.Utilidades;
import excepciones.modeloDatos.ExcepcionProyecto;
import modeloDatos.repositorios.Repositorio;
import java.util.ArrayList;


/**
 * <p>Clase que representa a un proyecto de la Forja Sidelab. Contiene todo lo
 * que se almacena acerca del proyecto en el directorio LDAP.</p>
 * @author Arek Klauza
 */
public class Proyecto {
    /** Nombre del proyecto */
    private String cn;
    /** Descripción textual del proyecto (es opcional) */
    private String description;
    /** GidNumber unívoco del proyecto */
    private String gidNumber;
    /** Lista de uids de administradores del proyecto (obligatorio al menos uno) */
    private ArrayList<String> administradores;
    /** Lista de uids de usuarios del proyecto (deberán aparecer también los
     * administradores del proyecto, que también son usuarios aparte. */
    private ArrayList<String> usuarios;
    /** Lista de objetos Repositorio que contiene el proyecto */
    private ArrayList<Repositorio> repositorios;
    /** Identifica el repositorio marcado por defecto para este proyecto */
    private String defaultRepositorio;


    /**
     * <p>Crea un proyecto nuevo del modelo de datos a partir de una orden del
     * usuario.</p>
     * @param cn Nombre del proyecto que cuadre con la expresión regular [_a-zA-z0-9]
     * @param description Descripción textual del proyecto
     * @param primerAdmin UID del primer admin que tendrá el proyecto
     * @param primerRepositorio Objeto Repositorio del proyecto (puede ser null
     * si el proyecto no tiene repositorios).
     * @throws ExcepcionProyecto Se lanza cuando el nombre del proyecto contiene
     * carácteres prohibidos (tildes y ñ) o cuando el UID del primerAdmin es null
     */
    public Proyecto(String cn, String description, String primerAdmin, Repositorio primerRepositorio) throws ExcepcionProyecto {
        // Comprobamos los parámetros: CN no contenga tildes ni ñ y que el primerAdmin no sea null
        if (Utilidades.containsIlegalCharsProyecto(cn))
            throw new ExcepcionProyecto("El Nombre (CN) del proyecto contiene carácteres prohibidos. Debe encajar con la Expresión Regular: [a-zA-Z0-9]+");
        if (primerAdmin == null)
            throw new ExcepcionProyecto("Se debe facilitar el uid de un administrador para el proyecto");
        
        this.cn = cn;
        this.description = description;
        this.administradores = new ArrayList<String>();
        this.administradores.add(primerAdmin);
        // Todos los admin deben estar también en la lista de usuarios
        this.usuarios = new ArrayList<String>();
        this.usuarios.add(primerAdmin);
        if (primerRepositorio != null) {
            this.repositorios = new ArrayList<Repositorio>();
            this.repositorios.add(primerRepositorio);
            this.defaultRepositorio = primerRepositorio.getTipo();
        }
    }


    /**
     * <p>Este constructor se utiliza cuando se construye un objeto Proyecto del
     * modelo de datos a partir de la información almacenada en el directorio LDAP</p>
     * @param cn Nombre del proyecto
     * @param description Descripción textual del proyecto
     * @param gidNumber Número unívoco de identificación del proyecto en LDAP
     * @param administradores Lista los UIDs de los administradores del proyecto
     * @param usuarios Lista de los UIDs de los miembros usuarios del proyecto
     * @param repositorios Lista de objetos Repositorio que contiene el proyecto
     * @param defaultRepositorio Nombre del repositorio por defecto
     */
    public Proyecto(String cn, String description, String gidNumber, ArrayList<String> administradores,
            ArrayList<String> usuarios, ArrayList<Repositorio> repositorios, String defaultRepositorio) {
        this.cn = cn;
        this.description = description;
        this.gidNumber = gidNumber;
        this.administradores = administradores;
        this.usuarios = usuarios;
        this.repositorios = repositorios;
        this.defaultRepositorio = defaultRepositorio;
    }


    


    /**
     * <p>Indica si un proyecto determinado tiene repositorios o no.</p>
     * @return true si el proyecto tiene al menos un repositorio, false si no
     * tiene ninguno.
     */
    public boolean tieneRepositorio() {
        if (this.repositorios == null || this.repositorios.isEmpty())
            return false;
        else
            return true;
    }


    /* GETTERS de la Clase */
    public ArrayList<String> getAdministradores() {
        return administradores;
    }
    public String getPrimerAdmin() {
        return administradores.get(0);
    }
    public String getCn() {
        return cn;
    }
    public String getDescription() {
        return description;
    }
    public ArrayList<Repositorio> getRepositorios() {
        return repositorios;
    }
    public Repositorio getPrimerRepositorio() {
        return repositorios.get(0);
    }
    public ArrayList<String> getUsuarios() {
        return usuarios;
    }
    public String getGidNumber() {
        return gidNumber;
    }
    public String getDefaultRepositorio() {
        return defaultRepositorio;
    }


    

    /* SETTERS de la Clase */
    public void setGidNumber(String gidNumber) {
        this.gidNumber = gidNumber;
    }
    public void setDefaultRepositorio(String defaultRepositorio) {
        this.defaultRepositorio = defaultRepositorio;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setRepositorios(ArrayList<Repositorio> repositorios) {
        this.repositorios = repositorios;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Proyecto other = (Proyecto) obj;
        if ((this.cn == null) ? (other.cn != null) : !this.cn.equals(other.cn)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if (this.administradores != other.administradores && (this.administradores == null || !this.administradores.equals(other.administradores))) {
            return false;
        }
        if (this.usuarios != other.usuarios && (this.usuarios == null || !this.usuarios.equals(other.usuarios))) {
            return false;
        }
        if (this.repositorios != other.repositorios && (this.repositorios == null || !this.repositorios.equals(other.repositorios))) {
            return false;
        }
        if ((this.defaultRepositorio == null) ? (other.defaultRepositorio != null) : !this.defaultRepositorio.equals(other.defaultRepositorio)) {
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
