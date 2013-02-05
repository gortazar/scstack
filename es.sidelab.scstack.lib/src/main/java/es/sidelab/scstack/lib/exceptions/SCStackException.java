/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * 
 * Fichero: ExcepcionForja.java
 * Autor: Arek Klauza
 * Fecha: Diciembre 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package es.sidelab.scstack.lib.exceptions;

/**
 * <p>Clase padre de todas las excepciones del proyecto. Hereda directamente de
 * la clase <i>Exception</i> de Java.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class SCStackException extends Exception {

    public SCStackException(Exception e) {
        super(e);
    }

    public SCStackException(String msg) {
        super(msg);
    }

	public SCStackException(String msg, Throwable t) {
		super(msg, t);
	}



}
