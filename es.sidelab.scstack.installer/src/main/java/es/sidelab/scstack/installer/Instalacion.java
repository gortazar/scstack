/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Instalacion.java
 * Autor: Arek Klauza
 * Fecha: Mayo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.installer;

import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.CommandOutput;
import es.sidelab.commons.commandline.ExecutionCommandException;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Aplicación encargada de la instalación de la Forja Software del grupo
 * Sidelab.
 * </p>
 * 
 * @author Arek Klauza
 */
public class Instalacion {
	public static Properties config;
	public static int tot = 58;
	private static es.sidelab.commons.commandline.CommandLine consola = new CommandLine();

	public static void main(String[] args) {

		try {

			cargarConfiguracion();
	        System.out.println("\n*** Updating the repositories list ***\n");
			ejecutar("apt-get -y update");
	        System.out.println("**************************************************\n");
			new DirectorioLdap().instalar();
			new SSH().instalar();
			new ServidorApache().instalar();
			new Repositorios().instalar();
			new Redmine().instalar();
			//Instalacion.ejecutar("/etc/init.d/apache2 restart");

		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.getLogger(Instalacion.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	/**
	 * Método encargado de cargar desde el fichero de configuración todos los
	 * parámetros necesarios para la instalación.
	 */
	public static void cargarConfiguracion() {
		config = new Properties();
		String ficheroConfiguracion = "ficherosInstalacion/configInstalacion.txt";
		try {
			config.load(new FileInputStream(ficheroConfiguracion));

		} catch (IOException e) {
			System.err
					.println("Se ha producido un error durante la carga del fichero: "
							+ ficheroConfiguracion
							+ " - Asegúrese que está en la ruta raíz del proyecto");

		}
	}

	public static void ejecutar(String comando)
			throws ExecutionCommandException, IOException {
		System.out.println("# " + comando);
		CommandOutput co = consola.syncExec(comando);
	}

}
