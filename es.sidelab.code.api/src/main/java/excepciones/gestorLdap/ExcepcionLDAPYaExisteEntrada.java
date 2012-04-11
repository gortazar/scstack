/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ExcepcionLDAPYaExisteEntrada.java
 * Autor: Arek Klauza
 * Fecha: Diciembre 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package excepciones.gestorLdap;

/**
 * <p>Este tipo de excepción nos indicará que hemos intentado agregar una entrada
 * al directorio LDAP que ya existe.</p>
 * <p>Esta excepción hereda directamente de ExcepcionGestorLDAP.</p>
 * <p>Consiste básicamente en un wrapper sobre la clase <i>Exception</i> nativa
 * de Java para tener mejor clasificado el origen de la Excepción.</p>
 * @author Arek Klauza
 */
public class ExcepcionLDAPYaExisteEntrada extends ExcepcionGestorLDAP {

    public ExcepcionLDAPYaExisteEntrada(Exception e) {
        super(e);

    }

}