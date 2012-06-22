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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
	private static es.sidelab.commons.commandline.CommandLine consola = new CommandLine();
	private static final String configFilename = "scstack.conf";

	/**
	 * Main method of the SCStack installation.
	 * <p>Use {@code -Dtype=option} where option can be {@code all | tools | service}.
	 * <ul><li>If 'all' selected -> installing everything and set scstack-service as daemon.</li> 
	 * <li>If 'tools' selected -> install only the required software.</li>
	 * <li>If 'service' selected -> do not install, only set the service as a daemon.</li></ul>
	 * </p>
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String installType = System.getProperty("type", "all");
			boolean tools = false;
			boolean service = false;
			cargarConfiguracion(null);
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
				System.out.println("\n*** Updating the repositories list ***\n");
				ejecutar("apt-get -y update");
				System.out.println("**************************************************\n");
				new DirectorioLdap().instalar();
				new SSH().instalar();
				new ServidorApache().instalar();
				new Repositorios().instalar();
				new Redmine().instalar();
				//Instalacion.ejecutar("/etc/init.d/apache2 restart");
				if (!tools) {//also the service
//					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//					System.out.print("Setup Redmine and then copy its API REST key here: ");
//					String apiKey = br.readLine();
					new SCStackService().install();
				}
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
	 * @param resourcePath the folder where the config file resides. Accepts null value, in which
	 * case the current dir is considered.
	 * @throws Exception 
	 */
	public static Properties cargarConfiguracion(String resourcePath) throws Exception {
		config = new Properties();
		//String ficheroConfiguracion = "ficherosInstalacion/configInstalacion.txt";
		try {
			File configFile = new File(resourcePath, configFilename);
			config.load(new FileInputStream(configFile));
		} catch (IOException e) {
			System.err
			.println("Se ha producido un error durante la carga del fichero: "
					+ configFilename
					+ " - Asegúrese que está en la ruta raíz del proyecto");
			throw new Exception(e.getMessage());
		}
		return config;
	}

	public static void ejecutar(String comando)
			throws ExecutionCommandException, IOException {
		System.out.println("# " + comando);
		consola.syncExec(comando);
	}

	/**
	 * Writes a new value for the specified key into the default configuration file.
	 * @param key the name of the key
	 * @param newValue the new value
	 * @throws Exception
	 */
	public static void overwriteConfigValue(String key, String newValue) throws Exception {
		try {
			config.put(key, newValue);
			config.store(new FileOutputStream(configFilename), null);
		} catch (NullPointerException e) {
			System.err.println("Key or value argument is null.");
			throw new Exception(e.getMessage());
		} catch (IOException e) {
			System.err
			.println("Error while writing new value to key '" + key + "' into file " 
					+ configFilename
					+ " inside the root folder.");
			throw new Exception(e.getMessage());
		}
	}
}
