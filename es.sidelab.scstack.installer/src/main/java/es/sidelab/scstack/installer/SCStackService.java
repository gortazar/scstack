package es.sidelab.scstack.installer;

import java.io.IOException;

import es.sidelab.commons.commandline.ExecutionCommandException;

/**
 * Provides a method to install the REST service and web console of 
 * the SidelabCode's Stack as a system daemon.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class SCStackService {
	
	/**
	 * @throws IOException 
	 * @throws ExecutionCommandException 
	 * 
	 */
	public void install() throws ExecutionCommandException, IOException {
		 System.out.println("\n*** INSTALLING REST SERVICE***\n");
		 Instalacion.ejecutar("unzip scstack-service-bin.zip");
		 
		 //copy the configuration file (overwriting it if necessary)
		 Instalacion.ejecutar("cp scstack.conf scstack-service/");
		 Instalacion.ejecutar("chown root:root scstack-service/daemon/scstack-service.conf");
		 Instalacion.ejecutar("chmod 644 scstack-service/daemon/scstack-service.conf");
		 Instalacion.ejecutar("cp scstack-service/daemon/scstack-service.conf /etc/init/");
		 
		 Instalacion.ejecutar("chown root:root scstack-service/daemon/scstack-service");
		 Instalacion.ejecutar("chmod 755 scstack-service/daemon/scstack-service");
		 Instalacion.ejecutar("cp scstack-service/daemon/scstack-service /etc/init.d/");
		 
		 Instalacion.ejecutar("start scstack-service");
		 Instalacion.ejecutar("status scstack-service");
		 System.out.println("**************************************************\n");
	}

}
