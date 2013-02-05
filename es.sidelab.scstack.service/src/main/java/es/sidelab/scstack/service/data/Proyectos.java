/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Proyectos.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.data;

import flexjson.JSONSerializer;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.restlet.ext.json.JsonRepresentation;

/**
 * <p>Representa una lista de String con los nombres de los distintos proyectos
 * de la Forja.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "proyectos")
public class Proyectos extends BaseEntity {

    @XmlElements(@XmlElement(name = "proyecto", type = String.class))
    public ArrayList<String> listaProyectos;


    public Proyectos() {
        super();
    }




    /* GETTERS y SETTERS de la Clase */

    public void setListaProyectos(ArrayList<String> proyectos) {
        this.listaProyectos = proyectos;
    }


    /**
     * Los objetos con lista hay que sobreescribir el método porque por defecto
     * el JSON no serializa datos complejos del objeto principal.
     * @return
     */
    @Override
    public JsonRepresentation serializarJson() {
        return new JsonRepresentation(new JSONSerializer().include("listaProyectos").serialize(this));
    }

}
