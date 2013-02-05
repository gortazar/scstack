/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionLogin.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package es.sidelab.scstack.lib.exceptions.api;

import es.sidelab.scstack.lib.exceptions.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indicará que ha sucedido algún tipo de problema
 * durante la verificación de credenciales del usuario que invoca un método de
 * la API.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionLogin extends ExcepcionForja {
    public ExcepcionLogin(Exception e) {
        super(e);
    }

    public ExcepcionLogin(String msg) {
        super(msg);
    }
}
