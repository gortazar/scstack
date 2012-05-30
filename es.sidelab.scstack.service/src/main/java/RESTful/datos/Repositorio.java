/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Repositorio.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.datos;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>Clase que representa un repositorio concreto de un proyecto. Sin embargo,
 * no es independiente, debe colgar de un proyecto.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "repositorio")
public class Repositorio extends BaseEntity {
    /** Si el repositorio es público deberá ser true, si no false */
    private boolean esPublico;
    /** Ruta absoluta de la carpeta del repositorio dentro del servidor Apache */
    private String ruta;
    /** String que identifica el tipo de repositorio */
    private String tipo;
    /** Runtime de Java que utilizaremos para escribir en la consola de Linux */


    public Repositorio() {
        super();
    }

    public Repositorio(modeloDatos.repositorios.Repositorio repoAPI) {
        this();
        this.esPublico = repoAPI.esPublico();
        this.ruta = repoAPI.getRuta();
        this.tipo = repoAPI.getTipo();
    }

    public Repositorio(String tipo, boolean esPublico, String ruta) {
        this.esPublico = esPublico;
        this.ruta = ruta;
        this.tipo = tipo;
    }







    /* GETTERS de la Clase */

    @XmlAttribute
    public boolean isEsPublico() {
        return esPublico;
    }
    @XmlAttribute
    public String getRuta() {
        return ruta;
    }
    @XmlAttribute
    public String getTipo() {
        return tipo;
    }






    /* SETTERS de la Clase */

    public void setEsPublico(boolean esPublico) {
        this.esPublico = esPublico;
    }
    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }



}
