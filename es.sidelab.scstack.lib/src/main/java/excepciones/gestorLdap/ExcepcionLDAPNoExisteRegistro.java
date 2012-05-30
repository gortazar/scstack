/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionYaExisteEntrada.java
 * Autor: Arek Klauza
 * Fecha: Enero 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.gestorLdap;

/**
 * <p>Este tipo de excepción nos indicará que estamos intentando realizar una
 * operación con un usuario (UID) que no existe en el directorio LDAP.</p>
 * <p>Esta excepción hereda directamente de ExcepcionGestorLDAP.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionLDAPNoExisteRegistro extends ExcepcionGestorLDAP {

    public ExcepcionLDAPNoExisteRegistro(String e) {
        super(e);

    }

}