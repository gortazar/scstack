/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: Instalacion.java
 * Autor: Arek Klauza
 * Fecha: Mayo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package instalacionforja;

import es.sidelab.tools.commandline.CommandLine;
import es.sidelab.tools.commandline.CommandOutput;
import es.sidelab.tools.commandline.ExecutionCommandException;
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
	private static CommandLine consola = new CommandLine();

	public static void main(String[] args) {

		try {

			cargarConfiguracion();
			new DirectorioLdap().instalar();
			new SSH().instalar();
			new ServidorApache().instalar();
			new Repositorios().instalar();
			new Redmine().instalar();

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
