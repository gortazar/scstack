/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Utilidades.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package apoyo;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>Clase de apoyo con varios métodos útiles para las tareas de la Forja.</p>
 * @author Arek Klauza
 */
public class Utilidades {

    /**
     * <p>Comprueba que la cadena enviada como parámetro está unicamente formada
     * por carácteres válidos que cuadren con la Regexp: "[a-zA-Z0-9\-]+".</p>
     * @param s String a comprobar
     * @return false si la cadena es correcta, true si no
     */
    public static boolean containsIlegalCharsProyecto(String s) {
        if (s.matches("[a-zA-Z0-9\\-]+"))
            return false;
        else
            return true;
    }


    /**
     * <p>Comprueba que la cadena enviada como parámetro está unicamente formada
     * por carácteres válidos que cuadren con la Regexp: "[_a-z0-9]+".</p>
     * @param s String a comprobar
     * @return false si la cadena es correcta, true si no
     */
    public static boolean containsIlegalCharsUsuario(String s) {
        if (s.matches("[_a-z0-9]+")) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * <p>Pasándole un String nos indica si dicho String pertenece a la lista que
     * pasamos como parámetro.</p>
     * @param cadena Cadena String a buscar en la lista
     * @param lista Lista de Strings donde buscar
     * @return true si está en la lista, false si no está.
     */
    public static boolean estaEnLista(String cadena, String[] lista) {
        if (lista == null || cadena == null)
            return false;
        for (int i = 0; i < lista.length; i++)
            if (lista[i].equalsIgnoreCase(cadena))
                return true;
        return false;
    }


    /**
     * <p>Convierte un array de Strings a un ArrayList de Strings.</p>
     * @param array String[] a convertir
     * @return ArrayList<String> con el contenido del array de parámetro
     */
    public static ArrayList<String> convertirALista(String[] array) {
        ArrayList<String> lista = new ArrayList<String>();
        lista.addAll(Arrays.asList(array));
        return lista;
    }




    /**
     * <p>Comprueba si existe una carpeta o repositorio que se le pasa como
     * parámetro.</p>
     * @param ruta Ruta absoluta de la carpeta
     * @param cn Nombre de la carpeta
     * @return true si existe la carpeta, false si no
     */
    public static boolean existeCarpeta(String ruta, String cn) {
        if (new File(ruta + "/" + cn).exists())
            return true;
        else
            return false;
    }


    /**
     * <p>Convierte una cadena en claro a su encriptación MD5.</p>
     * @param claveEnClaro String con la clave en claro a encriptar
     * @return Cadena con el MD5 encriptado
     * @throws NoSuchAlgorithmException Se lanza cuando no se ha encontrado el
     * algoritmo que convierte a MD5 en las librerías de Java.
     */
    public static String toMD5(String claveEnClaro) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(claveEnClaro.getBytes());

        int size = b.length;
        StringBuilder h = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int u = b[i] & 255;
            if (u < 16) {
                h.append("0").append(Integer.toHexString(u));
            } else {
                h.append(Integer.toHexString(u));
            }
        }
        return Md5.getBase64FromHEX(h.toString());
    }


    /**
     *<p>Función que comprueba que una cadena de entrada cumple la RegExp de un email.</p>
     * @param email String con el email a comprobar
     * @return true si cumple, false si es inválido
     */
    public static boolean checkEmail(String s) {
        if (s.matches(".+@.+\\.[a-z]+"))
            return true;
        else
            return false;
    }



    
}
