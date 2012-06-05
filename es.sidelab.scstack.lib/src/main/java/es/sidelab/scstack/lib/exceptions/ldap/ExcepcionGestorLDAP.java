/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionGestorLDAP.java
 * Autor: Arek Klauza
 * Fecha: Diciembre 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package es.sidelab.scstack.lib.exceptions.ldap;

import es.sidelab.scstack.lib.exceptions.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indicará que ha sucedido algún tipo de problema
 * generalista durante la ejecución del GestorLDAP. La excepción es padre de una
 * jerarquía de clases relacionadas con el GestorLDAP.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionGestorLDAP extends ExcepcionForja {
    
    public ExcepcionGestorLDAP(Exception e) {
        super(e);
    }

    public ExcepcionGestorLDAP(String msg) {
        super(msg);
    }

}
