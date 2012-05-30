/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionConsola.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.configuradorApache;

/**
 * <p>Este tipo de excepción nos indicará que ha ocurrido un error durante la
 * ejecución de algún comando sobre la terminal Linux del servidor.</p>
 * <p>Esta excepción hereda directamente de ExcepcionConfiguradorApache.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionConsola extends ExcepcionConfiguradorApache {

    public ExcepcionConsola(String e) {
        super(e);

    }

}