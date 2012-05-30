/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Fichero: ExcepcionForja.java
 * Autor: Arek Klauza
 * Fecha: Diciembre 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones;

/**
 * <p>Clase padre de todas las excepciones del proyecto. Hereda directamente de
 * la clase <i>Exception</i> de Java.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionForja extends Exception {

    public ExcepcionForja(Exception e) {
        super(e);
    }

    public ExcepcionForja(String msg) {
        super(msg);
    }



}
