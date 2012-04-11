/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionConfiguradorApache.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.configuradorApache;

import excepciones.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indicará que ha sucedido algún tipo de problema
 * generalista durante la ejecución del GestorSVN. La excepción es padre de una
 * jerarquía de clases relacionadas con el GestorSVN.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionConfiguradorApache extends ExcepcionForja {

    public ExcepcionConfiguradorApache(Exception e) {
        super(e);
    }

    public ExcepcionConfiguradorApache(String msg) {
        super(msg);
    }

}
