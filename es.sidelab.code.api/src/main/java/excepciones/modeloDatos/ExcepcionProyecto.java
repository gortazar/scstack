/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionProyecto.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package excepciones.modeloDatos;

import excepciones.ExcepcionForja;

/**
 * <p>Este tipo de excepción nos indica que hemos ha sucedido algún error durante
 * el manejo de un objeto Proyecto como modelo de datos.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionProyecto extends ExcepcionForja {
    public ExcepcionProyecto(Exception e) {
        super(e);
    }

    public ExcepcionProyecto(String msg) {
        super(msg);
    }
}
