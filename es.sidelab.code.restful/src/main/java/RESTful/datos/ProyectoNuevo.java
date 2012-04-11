/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Proyecto.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.datos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import modeloDatos.repositorios.FactoriaRepositorios;
import modeloDatos.repositorios.FactoriaRepositorios.TipoRepositorio;

/**
 * <p>Clase que representa los datos necesarios para crear un nuevo proyecto.</p>
 * @author Arek Klauza
 */
@XmlRootElement(name = "proyectoNuevo")
public class ProyectoNuevo extends BaseEntity {
    /** Nombre del proyecto */
    private String cn;
    /** Descripción textual del proyecto */
    private String descripcion;
    /** UID del admin al que se va a asociar el proyecto en primer lugar */
    private String primerAdmin;
    /** Tipo de Repositorio a crear con el proyecto */
    private TipoRepositorio tipoRepo;
    /** Indica si el repositorio es público o privado */
    private boolean esRepoPublico;
    /** Ruta del primer repositorio que crearemos */
    private String rutaRepo;



    public ProyectoNuevo() {
        super();
    }

    
    public ProyectoNuevo(String cn, String description, String primerAdmin, String tipoRepo, String esRepoPublico, String rutaRepo) {
        this();
        this.cn = cn;
        this.descripcion = description;
        this.primerAdmin = primerAdmin;
        if (tipoRepo != null)
            this.tipoRepo = FactoriaRepositorios.getTipo(tipoRepo);
        if (esRepoPublico != null && esRepoPublico.equalsIgnoreCase("true"))
            this.esRepoPublico = true;
        else
            this.esRepoPublico = false;
        this.setRutaRepo(rutaRepo);
    }

    




    /* GETTERS de la Clase */

    @XmlElement(required=true)
    public String getCn() {
        return cn;
    }
    @XmlElement
    public String getDescripcion() {
        return descripcion;
    }
    @XmlElement(type=boolean.class)
    public boolean isEsRepoPublico() {
        return esRepoPublico;
    }
    @XmlElement
    public String getPrimerAdmin() {
        return primerAdmin;
    }
    @XmlElement
    public String getRutaRepo() {
        return rutaRepo;
    }
    @XmlElement
    public TipoRepositorio getTipoRepo() {
        return tipoRepo;
    }


    

    /* SETTERS de la Clase */
    
    public void setCn(String cn) {
        this.cn = cn;
    }
    public void setDescripcion(String description) {
        this.descripcion = description;
    }
    public void setEsRepoPublico(boolean esRepoPublico) {
        this.esRepoPublico = esRepoPublico;
    }
    public void setPrimerAdmin(String primerAdmin) {
        this.primerAdmin = primerAdmin;
    }
    public void setRutaRepo(String rutaRepo) {
        if (rutaRepo == null || rutaRepo.equalsIgnoreCase("null"))
            this.rutaRepo = null;
        else
            this.rutaRepo = rutaRepo;
    }
    public void setTipoRepo(TipoRepositorio tipoRepo) {
        this.tipoRepo = tipoRepo;
    }

}
