/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ServicioREST.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.log.Log;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.service.LogService;
import org.restlet.util.Series;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.service.restlets.LoginResource;
import es.sidelab.scstack.service.restlets.ServiciosResource;
import es.sidelab.scstack.service.restlets.projects.ProyectoAdminsResource;
import es.sidelab.scstack.service.restlets.projects.ProyectoMiembrosResource;
import es.sidelab.scstack.service.restlets.projects.ProyectoRepositorioResource;
import es.sidelab.scstack.service.restlets.projects.ProyectoResource;
import es.sidelab.scstack.service.restlets.projects.ProyectosResource;
import es.sidelab.scstack.service.restlets.users.UsuarioActivadoResource;
import es.sidelab.scstack.service.restlets.users.UsuarioProyectosResource;
import es.sidelab.scstack.service.restlets.users.UsuarioResource;
import es.sidelab.scstack.service.restlets.users.UsuarioUsuariosResource;
import es.sidelab.scstack.service.restlets.users.UsuariosResource;


/**
 * <p>Clase main encargada de crear y lanzar el servidor sobre el que atiende
 * peticiones el servicio RESTful.</p>
 * @author Arek Klauza
 */
public final class ServicioREST extends Application {
	/** Path and name of the configuration file for the REST service. */
	public static final String CONFIG_FILE = "scstack.conf";
	
    /* PARÁMETROS CONFIGURACIÓN SERVIDOR */
    public static String rootHTML;
    private String rootServicio;
    private String host;
    private int port;
    private String protocolo;


    @Override
    public synchronized Restlet createRoot() {
        Router router = new Router(getContext().createChildContext());
        
        // Define las rutas a los distintos recursos
        router.attach(rootServicio + "/servicios", ServiciosResource.class);
        router.attach(rootServicio + "/login", LoginResource.class);
        router.attach(rootServicio + "/logout", new Directory(getContext().createChildContext(), rootHTML + "/logout.html"));
        router.attach(rootServicio + "/usuarios", UsuariosResource.class);
        router.attach(rootServicio + "/usuarios/{uid}", UsuarioResource.class);
        router.attach(rootServicio + "/usuarios/{uid}/activado", UsuarioActivadoResource.class);
        router.attach(rootServicio + "/usuarios/{uid}/proyectos", UsuarioProyectosResource.class);
        router.attach(rootServicio + "/usuarios/{uid}/usuarios", UsuarioUsuariosResource.class);

        router.attach(rootServicio + "/proyectos", ProyectosResource.class);
        router.attach(rootServicio + "/proyectos/{cn}", ProyectoResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/miembros", ProyectoMiembrosResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/miembros/{uid}", ProyectoMiembrosResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/admins", ProyectoAdminsResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/admins/{uid}", ProyectoAdminsResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/repos", ProyectoRepositorioResource.class);
        router.attach(rootServicio + "/proyectos/{cn}/repos/{tipo}", ProyectoRepositorioResource.class);

        // Servir los ficheros
        router.attach("/" + rootServicio, new Directory(getContext().createChildContext(), rootHTML));

        return router;
    }



    /**
     * Método para lanzar el servicio en MODO STANDALONE
     * @throws Exception 
     */
    public ServicioREST() throws Exception {
        super();

    	Logger log = Logger.getLogger(ServicioREST.class.getName());
    	log.setLevel(Level.ALL);
    	log.info("Loading configuration");
        // Necesario antes de lanzar el servidor
        cargarConfiguracion();

        log.info("Initializing web server");
        // Creamos un nuevo componente (servidor web)        
        Component component = new Component();

        LogService logService = new LogService(true);
        logService.setLoggerName("REST Server");
        logService.start();
        
        component.setLogService(logService);

        log.info("Configuring web server");
        // Configuramos el tipo de protocolo del Servidor Web
        if (protocolo.equalsIgnoreCase("HTTPS")) {
        	log.info("Configuring HTTPS");
            // Modo HTTPS (excluye HTTP)
            component.getClients().add(Protocol.HTTPS);
            Server server = component.getServers().add(Protocol.HTTPS, host, port);
            component.getClients().add(Protocol.FILE);
            Series<Parameter> parameters = server.getContext().getParameters();
            parameters.add("SSLContextFactory", "org.jsslutils.sslcontext.SSLContextFactory");
            // Generado con keytool -genkey -v -alias SidelabCode -dname "CN=SidelabCode,OU=IT,O=JPC,C=GB"
            // -keypass sidelab2010 -keystore SidelabCode.jks -storepass sidelab2010 -keyalg "RSA" -sigalg "MD5withRSA"
            // -keysize 2048 -validity 3650
            parameters.add("keystorePath", "SidelabCode.jks");
            parameters.add("keystorePassword", "sidelab2010");
            parameters.add("keyPassword", "sidelab2010");
            parameters.add("keystoreType", "JKS");

        } else {
        	log.info("Configuring HTTP");
            // Modo HTTP (excluye HTTPS)
            component.getClients().add(Protocol.HTTP);
            component.getServers().add(Protocol.HTTP, host, port);
            component.getClients().add(Protocol.FILE);
        }

        // Intentamos lanzar el servidor web de esta aplicación
        try {
        	log.info("Deploying web service into server");
             // Añadimos esta aplicación al componente dentro del Host por defecto
            this.setContext(component.getContext().createChildContext());
            component.getDefaultHost().attach(this);
            component.start();
            log.info("Web service started");
        } catch (Exception e) {
            // Si no se puede lanzar, anunciamos el error.
            System.err.println("ERROR!!! El servidor no ha podido ser lanzado!");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
    


    public static void main(String[] args) {
    	
    	Logger log = Logger.getLogger(ServicioREST.class.getName());

    	try {
        	
        	log.setLevel(Level.ALL);
        	
        	log.info("Loading SCStack API");
            API_Abierta api = new API_Abierta(CONFIG_FILE); 

            log.info("Obtaining project list");
            ArrayList listaProy = api.getListaCnProyectos();
            if (listaProy == null || !listaProy.contains(ConfiguracionForja.groupSuperadmin)) {
//                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//                System.out.println("Indroduzca el UID de superadmin: ");
//                String uid = br.readLine();
//                System.out.println("Indroduzca la contraseña del superadmin: ");
//                String pass = br.readLine();
            	if(listaProy == null) {
            		log.info("No project found");
            	} else {
            		log.info("Projects found: " + listaProy);
            	}
            	log.info("Could not find superadmin group: " + ConfiguracionForja.groupSuperadmin + ". Creating superadmin.");
                String uid = ConfiguracionForja.sadminUID;
                String pass = ConfiguracionForja.sadminPass;
                api.inicializaForja(uid, pass);
            }
            
            log.info("Starting REST service");
            new ServicioREST();

        } catch (Throwable ex) {
            log.severe(ex.getMessage());
        }
        
        Log.info("REST service started");
    }




    /**
     * Método encargado de cargar desde el fichero de configuración todos los
     * parámetros necesarios para lanzar el servicio web REST.
     */
    private void cargarConfiguracion() {
        try {
        	Properties props = loadPropertiesFromFile("scstack.conf");
            host = props.getProperty("hostRest", "localhost");
            port = Integer.valueOf(props.getProperty("puertoRest", "5555"));
            protocolo = props.getProperty("protocoloRest", "HTTPS");
            rootServicio = props.getProperty("rootServicio", "");

            // Extraer y configurar el path del HTML dependiendo si es root relativo o absoluto
            rootHTML = props.getProperty("carpetaHtml", "src/main/www");
            if (!rootHTML.startsWith("file:///"))
                rootHTML = "file://" + System.getProperty("user.dir") + "/" + rootHTML;

        } catch (IOException e) {
            System.err.println("Se ha producido un error durante la carga del fichero: " + CONFIG_FILE +
                    " - Asegúrese que está en la ruta raíz del JAR");
        }
    }

    /**
     * Loads all pairs of key - value from a file into a {@link java.util.Properties} object.
     * @param filePath
     * @return the Properties object
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static Properties loadPropertiesFromFile(String filePath) throws FileNotFoundException, IOException {
    	Properties props = new Properties();
    	props.load(new FileInputStream(filePath));
    	return props;
    }
}
