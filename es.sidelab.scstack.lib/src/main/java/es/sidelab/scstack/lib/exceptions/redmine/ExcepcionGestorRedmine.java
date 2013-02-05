/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionGestorRedmine.java
 * Autor: Arek Klauza
 * Fecha: Febrero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package es.sidelab.scstack.lib.exceptions.redmine;

import es.sidelab.scstack.lib.exceptions.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indicará que ha sucedido algún tipo de problema
 * durante el uso de la clase GestorRedmine.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionGestorRedmine extends ExcepcionForja {

    public ExcepcionGestorRedmine(Exception e) {
        super(e);
    }

    public ExcepcionGestorRedmine(String msg) {
        super(msg);
    }

}
