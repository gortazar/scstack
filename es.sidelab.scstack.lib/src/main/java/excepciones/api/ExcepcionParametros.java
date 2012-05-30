/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionParametros.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.api;

import excepciones.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indicará que se ha intentado invocar un método
 * de la API sin los parámetros necesarios. Bien porque no seguían la sintaxis
 * correcta o porque faltaba alguno de ellos.sx</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionParametros extends ExcepcionForja {
    public ExcepcionParametros(Exception e) {
        super(e);
    }

    public ExcepcionParametros(String msg) {
        super(msg);
    }
}
