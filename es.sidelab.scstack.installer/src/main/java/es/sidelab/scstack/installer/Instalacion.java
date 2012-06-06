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

	/**
	 * Main method of the SCStack installation.
	 * <p>Use {@code -Dtype=option} where option can be {@code all | tools | service}.
	 * <ul><li>If 'all' selected -> installing everything and set scstack-service as daemon.</li> 
	 * <li>If 'tools' selected -> install only the required software.</li>
	 * <li>If 'service' selected -> don not install, only set the service as a daemon.</li></ul>
	 * </p>
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String installType = System.getProperty("type", "all");
			boolean tools = false;
			boolean service = false;
			if (installType.equalsIgnoreCase("service")) {
				System.out.println("\n*** Installing only the service ***\n");
				service = true;
				new SCStackService().install();
			} else if (installType.equalsIgnoreCase("tools")) {
				System.out.println("\n*** Installing only the system dependencies (or tools) ***\n");
				tools = true;
			}
			if (!service && !tools) //no type specified or type=all
				System.out.println("\n*** Installing everything (tools & service) ***\n");
			if (!service) {
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
				if (!tools) //also the service
					new SCStackService().install();
			}
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
		//String ficheroConfiguracion = "ficherosInstalacion/configInstalacion.txt";
		String ficheroConfiguracion = "scstack.conf";
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
