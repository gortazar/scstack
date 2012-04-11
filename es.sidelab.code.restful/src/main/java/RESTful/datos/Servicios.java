/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Servicios.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.datos;

import config.ConfiguracionForja;
import flexjson.JSONSerializer;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.restlet.ext.json.JsonRepresentation;

/**
 * <p>Clase que representa los Servicios ofrecidos por la Forja.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "servicios")
public class Servicios extends BaseEntity {
    /** Host de Redmine */
    private String hostRedmine;
    /** Path web de los repositorios SVN */
    private String pathSVN;
    /** Path web de los repositorios GIT */
    private String pathGIT;
    /** Path web de los repositorios públicos */
    private String pathReposPublicos;
    /** Path web del proyecto en Redmine */
    private String pathProyectoRedmine;
    


    public Servicios() {
        super();
        hostRedmine = ConfiguracionForja.hostRedmine;
        pathSVN = ConfiguracionForja.pathSVNWeb;
        pathGIT = ConfiguracionForja.pathGITWeb;
        pathReposPublicos = ConfiguracionForja.pathReposPublicosWeb;
        pathProyectoRedmine = "/projects/";
    }





    /**
     * Serializa la estructura de datos a JSON
     * @return
     */
    @Override
    public JsonRepresentation serializarJson() {
        return new JsonRepresentation(new JSONSerializer().serialize(this));
    }





    /* GETTERS de la Clase */
    @XmlElement
    public String getHostRedmine() {
        return hostRedmine;
    }
    @XmlElement
    public String getPathGIT() {
        return pathGIT;
    }
    @XmlElement
    public String getPathProyectoRedmine() {
        return pathProyectoRedmine;
    }
    @XmlElement
    public String getPathReposPublicos() {
        return pathReposPublicos;
    }
    @XmlElement
    public String getPathSVN() {
        return pathSVN;
    }



    /* SETTERS de la Clase */

    public void setHostRedmine(String hostRedmine) {
        this.hostRedmine = hostRedmine;
    }

    public void setPathGIT(String pathGIT) {
        this.pathGIT = pathGIT;
    }

    public void setPathProyectoRedmine(String pathProyectoRedmine) {
        this.pathProyectoRedmine = pathProyectoRedmine;
    }

    public void setPathReposPublicos(String pathReposPublicos) {
        this.pathReposPublicos = pathReposPublicos;
    }

    public void setPathSVN(String pathSVN) {
        this.pathSVN = pathSVN;
    }

}
