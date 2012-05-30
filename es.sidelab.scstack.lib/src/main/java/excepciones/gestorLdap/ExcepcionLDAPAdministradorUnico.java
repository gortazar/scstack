/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionLDAPAdministradorUnico.java
 * Autor: Arek Klauza
 * Fecha: Enero 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.gestorLdap;

/**
 * <p>Este tipo de excepción nos indicará que estamos intentando borrar a un
 * administrador único de un proyecto, lo cual está prohibido, ya que no puede
 * haber proyectos huérfanos de administrador.</p>
 * <p>Esta excepción hereda directamente de ExcepcionGestorLDAP.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionLDAPAdministradorUnico extends ExcepcionGestorLDAP {

    public ExcepcionLDAPAdministradorUnico(String e) {
        super(e);

    }

}