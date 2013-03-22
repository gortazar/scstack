/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Repositorio.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.dataModel.repos;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;

import java.io.PrintWriter;

/**
 *
 * @author Arek Klauza
 */
public abstract class Repositorio {
    /** Si el repositorio es público deberá ser true, si no false */
    private boolean esPublico;
    /** Ruta absoluta de la carpeta del repositorio dentro del servidor Apache */
    private String ruta;
    /** String que identifica el tipo de repositorio */
    private String tipo;
    /** Runtime de Java que utilizaremos para escribir en la consola de Linux */
    private Runtime consola;



    /**
     * <p>Constructor de la clase abstracta Repositorio que deberá ser invocado
     * por alguna de las clases hijas de este para construir una instancia concreta
     * de repositorio.</p>
     * @param esPublico true si el repositorio es público, false en caso de ser privado
     * @param ruta Ruta absoluta de la carpeta del repositorio dentro del Apache
     * @param tipo String que identifica el tipo de repositorio
     */
    public Repositorio(boolean esPublico, String ruta, String tipo) {
        this.esPublico = esPublico;
        this.ruta = ruta;
        this.tipo = tipo;
    }


    /**
     * <p>Convierte el objeto repositorio en un String formateado especialmente
     * para ser almacenado en el directorio LDAP en el atributo "repositorio".</p>
     * @return String con los datos del repositorio aplanados
     */
    public String aplanar() {
        String publico = "";
        if (this.esPublico)
            publico = "Publico";
        else
            publico = "Privado";
        return this.getTipo() + "###" + publico + "###" + this.ruta;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Repositorio other = (Repositorio) obj;
        if (!this.getTipo().equals(other.getTipo())) {
            return false;
        }
        return true;
    }






    /**
     * <p>Crea el repositorio especificado según el tipo de configurador que hayamos
     * creado en la llamada al constructor.
     * @param cnProyecto Nombre del proyecto.
     * @param uidAdminProyecto UID del usuario Administrador del proyecto
     * @param apiAbierta 
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    public abstract void crearRepositorio(String cnProyecto, String uidAdminProyecto, API_Abierta apiAbierta) throws ExcepcionConsola;


    /**
     * <p>Recibe el PrintWriter del fichero de configuración de Apache de proyectos
     * (fichero de proyectos en la carpeta sites-available de apache) e imprime al final
     * en dicho fichero la entrada correspondiente al repositorio.</p>
     * @param pwHTTPS PrintWriter del fichero de configuración SSL de Apache. Por defecto:
     * sites-available/dev.misidelab.es-ssl-projects
     * @param pwHTTP PrintWriter del fichero de configuración HTTP de Apache. Por
     * defecto: sites-available/dev.misidelab.es-projects
     * @param cn CN del proyecto a escribir
     * @param gidNumber gidNumber del proyecto a escribir
     */
    public abstract void escribirEntradaApache(PrintWriter pwHTTPS, PrintWriter pwHTTP, String cn, String gidNumber);




    public abstract void borrarRepositorio(String cnProyecto) throws ExcepcionConsola;



    

    /* GETTERs de la Clase */
    public boolean esPublico() {
        return esPublico;
    }
    public String getRuta() {
        return ruta;
    }
    public String getTipo() {
        return tipo;
    }
    public Runtime getConsola() {
        return consola;
    }

    /**
     * Se deberá usar este método cada vez que queramos escribir sobre la consola.
     */
    public void setConsola() {
        this.consola = Runtime.getRuntime();
    }


    

}