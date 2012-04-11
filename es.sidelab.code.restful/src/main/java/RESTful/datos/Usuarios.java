/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Usuarios.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.datos;

import flexjson.JSONSerializer;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import org.restlet.ext.json.JsonRepresentation;

/**
 * <p>Representa una lista de String que puede contener tanto los uids de usuarios
 * como sus nombres completos.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "usuarios")
public class Usuarios extends BaseEntity {

    @XmlElements(@XmlElement(name = "usuario", type = String.class))
    public ArrayList<String> listaUsuarios;


    public Usuarios() {
        super();
    }




    /* GETTERS y SETTERS de la Clase */
    
    public void setListaUsuarios(ArrayList<String> usuarios) {
        this.listaUsuarios = usuarios;
    }



    /**
     * Las objetos con lista hay que sobreescribir el método porque por defecto
     * el JSON no serializa datos complejos del objeto principal.
     * @return
     */
    @Override
    public JsonRepresentation serializarJson() {
        return new JsonRepresentation(new JSONSerializer().include("listaUsuarios").serialize(this));
    }


}
