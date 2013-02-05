/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Proyecto.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.data;

import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionProyecto;
import flexjson.JSONSerializer;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.restlet.ext.json.JsonRepresentation;

/**
 * <p>Clase que representa a las entidades proyecto de la Forja.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "proyecto")
public class Proyecto extends BaseEntity {
    /** Nombre del proyecto */
    private String cn;
    /** Descripción textual del proyecto (es opcional) */
    private String descripcion;
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
    


    public Proyecto() {
        super();
    }


    public Proyecto(es.sidelab.scstack.lib.dataModel.Proyecto api_proy) {
        this();
        this.cn = api_proy.getCn();
        this.administradores = api_proy.getAdministradores();
        this.usuarios = api_proy.getUsuarios();
        this.descripcion = api_proy.getDescription();
        this.gidNumber = api_proy.getGidNumber();
        this.defaultRepositorio = api_proy.getDefaultRepositorio();

        // Convierte los repositorios de la API a los de REST
        ArrayList<Repositorio> repos = new ArrayList<Repositorio>();
        for (es.sidelab.scstack.lib.dataModel.repos.Repositorio repoAPI : api_proy.getRepositorios()) {
            repos.add(new Repositorio(repoAPI));
        }
        this.repositorios = repos;
    }
    

    public Proyecto(String cn, String description, String defaultRepositorio) {
        this();
        this.cn = cn;
        this.descripcion = description;
        this.defaultRepositorio = defaultRepositorio;
    }


    public es.sidelab.scstack.lib.dataModel.Proyecto toProyectoAPI() throws ExcepcionProyecto {
        es.sidelab.scstack.lib.dataModel.Proyecto proy_api = new es.sidelab.scstack.lib.dataModel.Proyecto(cn, descripcion, "null", null);
        proy_api.setDefaultRepositorio(defaultRepositorio);
        return proy_api;
    }



    /**
     * Los objetos con lista hay que sobreescribir el método porque por defecto
     * el JSON no serializa datos complejos del objeto principal.
     * @return
     */
    @Override
    public JsonRepresentation serializarJson() {
        return new JsonRepresentation(new JSONSerializer().include("administradores", "usuarios", "repositorios").serialize(this));
    }








    /* GETTERS de la Clase */

    @XmlElement
    public String getCn() {
        return cn;
    }    
    @XmlElement
    public String getDescripcion() {
        return descripcion;
    }
    @XmlElement
    public String getGidNumber() {
        return gidNumber;
    }
    @XmlElementWrapper(name = "administradores")
    @XmlElements(@XmlElement(name = "uid", type = String.class))
    public ArrayList<String> getAdministradores() {
        return administradores;
    }
    @XmlElementWrapper(name = "miembros")
    @XmlElements(@XmlElement(name = "uid", type = String.class))
    public ArrayList<String> getUsuarios() {
        return usuarios;
    }
    @XmlElement
    public String getDefaultRepositorio() {
        return defaultRepositorio;
    }
    @XmlElementWrapper(name = "repositorios")
    @XmlElements(@XmlElement(name = "repositorio", type = Repositorio.class))
    public ArrayList<Repositorio> getRepositorios() {
        return repositorios;
    }




    
    /* SETTERS de la Clase */

    public void setAdministradores(ArrayList<String> administradores) {
        this.administradores = administradores;
    }
    public void setCn(String cn) {
        this.cn = cn;
    }
    public void setDefaultRepositorio(String defaultRepositorio) {
        this.defaultRepositorio = defaultRepositorio;
    }
    public void setDescripcion(String description) {
        this.descripcion = description;
    }
    public void setGidNumber(String gidNumber) {
        this.gidNumber = gidNumber;
    }
    public void setRepositorios(ArrayList<Repositorio> repositorios) {
        this.repositorios = repositorios;
    }
    public void setUsuarios(ArrayList<String> usuarios) {
        this.usuarios = usuarios;
    }

}
