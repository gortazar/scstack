/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: BaseEntity.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.data;

import flexjson.JSONSerializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import org.restlet.ext.json.JsonRepresentation;

/**
 * <p>Esta es la estructura de datos básica de la cual heredan todas las de la
 * jerarquía del modelo de datos.
 * @author Arek Klauza
 */
public class BaseEntity {

    private String uri;
    private String name;

    public BaseEntity() {
        super();
    }

    public BaseEntity(String name) {
        this();
        this.name = name;        
    }


    /* GETTERS y SETTERS de la Clase */

    @XmlAttribute(name = "uri")
    public String getUri() {
        return uri;
    }
    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public void setName(String name) {
        this.name = name;
    }





    // ///////////////// MÉTODOS XML
    /**
     * Devuelve una representación en XML de la entidad.
     *
     * @return Una cadena de XML formateada que representa la entidad o null si
     *         hubiese algún error.
     */
    public String serializarXml() {
        try {
            JAXBContext jc = JAXBContext.newInstance("es.sidelab.scstack.service.data");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            m.marshal(this, output);
            return output.toString();
        } catch (JAXBException e) {
            System.err.println("Excepción al serializar en XML -- Imposible serializar objetos de la clase: "
                    + this.getClass().getName());
            return null;
        }
    }

    /**
     * Devuelve una entidad a partir de una representación en XML o null si
     * sucede algún error en la deserialización.
     *
     * @param XML
     *            Representación válida de una entidad de la aplicación
     * @return Un objeto java o null si ocurrió algún error.
     */
    public static Object deserializarXml(String s) {
        Object obj = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("es.sidelab.scstack.service.data");
            Unmarshaller u = jc.createUnmarshaller();
            obj = u.unmarshal(new ByteArrayInputStream(s.getBytes()));
        } catch (JAXBException e) {
            System.err.println("Excepción al deserializar -- Contenido XML no reconocido.");
            return null;
        }
        return obj;
    }


    

    /**
     * <p>Devuelve una entidad en formato JSON.</p>
     * @return JsonRepresentation de la entidad
     */
    public JsonRepresentation serializarJson() {
        return new JsonRepresentation(new JSONSerializer().serialize(this));
    }


}
