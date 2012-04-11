/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ConfiguracionForja.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * <p>Clase que contiene todos los parámetros de configuración de la ForjaSidelab
 * para su correcto funcionamiento.</p>
 * @author Arek Klauza
 */
public final class ConfiguracionForja {

    /**************** PARÁMETROS DE CONEXIÓN AL SERVIDOR LDAP ******************/

    /** Host en el que se encuentra el servidor LDAP.
     * (por defecto: "localhost") */
    public static String hostLDAP;

    /** Puerto para la conexión segura.
     * (por defecto: 636) */
    public static int puertoLDAP;

    /** Será el DN del Servidor que usará para realizar las consultas.
     * (por defecto: "cn=admin,dc=sidelab,dc=es") */
    public static String bindDN;

    /** Contraseña en claro del bindDN */
    public static String passBindDN;

    /** DN base sobre el que se realizarán las consultas.
     * (por defecto: "dc=sidelab,dc=es") */
    public static String baseDN;

    /** Nombre del OU (Organizational Unit) de usuarios.
     * (por defecto: "people") */
    public static String ouUsuarios;

    /** Nombre del OU (Organizational Unit) de proyectos.
     * (por defecto: "group") */
    public static String ouProyectos;

    /** Nombre del OU (Organizational Unit) de configuracion.
     * (por defecto: "configuracion") */
    public static String ouConfiguracion;

    /** Nombre del group al que pertenecen los superadmins.
     * (por defecto: "superadmins") */
    public static String groupSuperadmin;



    /***************** PARÁMETROS DE CONFIGURACIÓN DE APACHE *******************/

    /** Ruta absoluta donde alojamos los repositorios SVN dentro del servidor Apache.
     * (por defecto: "/var/svn") */
    public static String pathSVNApache;

    /** Ruta relativa web para los repositorios SVN.
     * (por defecto: "/svn") */
    public static String pathSVNWeb;

    /** Ruta absoluta donde alojamos los repositorios GIT dentro del servidor Apache.
     * (por defecto: "/var/git") */
    public static String pathGITApache;

    /** Ruta relativa web para los repositorios GIT.
     * (por defecto: "/git") */
    public static String pathGITWeb;

    /** Ruta relativa web para los repositorios públicos.
     * (por defecto: "/svn") */
    public static String pathReposPublicosWeb;

    /** Ruta absoluta donde se encuentran las carpetas de proyectos de la Forja dentro
     * del servidor Apache. (por defecto: "/var/files") */
    public static String pathCarpetas;

    /** Ruta absoluta donde alojamos la carpeta privada del proyecto dentro del servidor Apache.
     * (por defecto: "/var/files/private") */
    public static String pathCarpetaPrivadaApache;

    /** Ruta relativa web para las carpetas privadas de los proyectos
     * (por defecto: "/private") */
    public static String pathCarpetaPrivadaWeb;

    /** Ruta absoluta donde alojamos la carpeta pública del proyecto dentro del servidor Apache.
     * (por defecto: "/var/files/public") */
    public static String pathCarpetaPublicaApache;

    /** Ruta relativa web para las carpetas públicas de los proyectos
     * (por defecto: "/public") */
    public static String pathCarpetaPublicaWeb;

    /** Ruta absoluta donde se encuentran los ficheros sites-available de Apache.
     * (por defecto: "/etc/apache2/sites-available") */
    public static String pathsitesAvailableApache;

    /** Nombre del fichero de configuración Apache de los proyectos (repositorios y
     * carpetas para conexión HTTPS (por defecto: "dev.misidelab.es-ssl-projects") */
    public static String ficheroProyectosSSL;

    /** Nombre del fichero de configuración Apache de los proyectos (repositorios y
     * carpetas para conexión HTTP (por defecto: "dev.misidelab.es-projects") */
    public static String ficheroProyectos;

    /** Ruta + fichero de configuración para enjaular a los usuarios de la forja
     * mediante SSH. (por defecto: "/etc/ssh/sshd_config") */
    public static String ficheroConfigSSH;

    /** String que marca a partir de dónde se añaden los usuarios del fichero de
     * configuración SSH. (por defecto: "##### No borrar esta linea: jaula de usuarios #####") */
    public static String marcadorJaulaSSH;



    /******************** PARÁMETROS DEL GESTOR REDMINE ***********************/

    /** Host completo donde se sirve Redmine para poder conectar con su API.
     * (por defecto: "http://dev.misidelab.es") */
    public static String hostRedmine;

    /** Key - Clave para acceder a la API de Redmine con privilegios de
     * administrador (por defecto: "58ed87a309faa61f30746e28a275baa7f77b0e49") */
    public static String keyRedmineAPI;

    /** Host del servidor mysql que utiliza Redmine. (por defecto: "127.0.0.1") */
    public static String hostMysql;

    /** Puerto del servidor mysql que utiliza Redmine. (por defecto: "3306") */
    public static String puertoMysql;

    /** Nombre de usuario del Mysql que utiliza Redmine. (por defecto: "root") */
    public static String usernameMysql;

    /** Contraseña del usuario de Mysql que utiliza Redmine (por defecto: "admin") */
    public static String passMysql;

    /** Nombre schema de Redmine (por defecto: "redmine") */
    public static String schemaRedmine;



    

    /**
     * <p>Carga todos los parámetros de configuración de la Forja en memoria para
     * su ejecución a partir de un fichero de texto con cada uno de los parámetros
     * de configuración de la Forja.</p>
     * <p>En caso de que no se pase como parámetro la ruta del fichero cargará
     * los datos por defecto.</p>
     * <p>Si el fichero especificado no existe o es nulo, se cargará la configuración
     * por defecto y se regenerará un fichero de configuración con dichos parámetros.</p>
     * @param ficheroConfiguracion Ruta y nombre del fichero con los parámetros
     * de configuración de la Forja.
     */
    public ConfiguracionForja(String ficheroConfiguracion) {
        Properties props = new Properties();
        try {            
            if (!ficheroConfiguracion.equals("") && ficheroConfiguracion != null)
                props.load(new FileInputStream(ficheroConfiguracion));
            this.cargarConfiguracion(props);
        } catch (IOException e) {
            System.err.println("Se ha producido un error durante la apertura del fichero: " + ficheroConfiguracion +
                    " - Asegúrese que está en la ruta especificada");
            System.err.println("Se procede a cargar la configuración por defecto de la Forja y a regenerar el fichero " + ficheroConfiguracion);
            this.cargarConfiguracion(props);
            this.regenerarFicheroConfigDefault(ficheroConfiguracion, this.guardarConfiguracion());
        }
    }




    /**
     * <p>Método que inicializa las variables estáticas de la Forja a partir de
     * las properties cargadas desde el fichero de configuración.</p>
     * <p>En caso de que falte alguna property, se carga el valor por defecto.</p>
     * @param props Objeto con las properties cargadas desde el fichero de
     * configuración
     */
    private void cargarConfiguracion(Properties props) {
        ConfiguracionForja.hostLDAP = props.getProperty("hostLDAP", "localhost");
        ConfiguracionForja.puertoLDAP = Integer.valueOf(props.getProperty("puertoLDAP", "636"));
        ConfiguracionForja.bindDN = props.getProperty("bindDN", "cn=admin,dc=sidelab,dc=es");
        ConfiguracionForja.passBindDN = props.getProperty("passBindDN", "sidelab2010");
        ConfiguracionForja.baseDN = props.getProperty("baseDN", "dc=sidelab,dc=es");
        ConfiguracionForja.ouUsuarios = props.getProperty("ouUsuarios", "people");
        ConfiguracionForja.ouProyectos = props.getProperty("ouProyectos", "group");
        ConfiguracionForja.ouConfiguracion = props.getProperty("ouConfiguracion", "configuracion");
        ConfiguracionForja.groupSuperadmin = props.getProperty("groupSuperadmin", "superadmins");
        ConfiguracionForja.pathSVNApache = props.getProperty("pathSVNApache", "/var/svn");
        ConfiguracionForja.pathSVNWeb = props.getProperty("pathSVNWeb", "/svn");
        ConfiguracionForja.pathGITApache = props.getProperty("pathGITApache", "/var/git");
        ConfiguracionForja.pathGITWeb = props.getProperty("pathGITWeb", "/git");
        ConfiguracionForja.pathReposPublicosWeb = props.getProperty("pathReposPublicosWeb", "/svn");
        ConfiguracionForja.pathCarpetas = props.getProperty("pathCarpetas", "/var/files");
        ConfiguracionForja.pathCarpetaPrivadaApache = props.getProperty("pathCarpetaPrivadaApache", "/var/files/private");
        ConfiguracionForja.pathCarpetaPrivadaWeb = props.getProperty("pathCarpetaPrivadaWeb", "/private");
        ConfiguracionForja.pathCarpetaPublicaApache = props.getProperty("pathCarpetaPublicaApache", "/var/files/public");
        ConfiguracionForja.pathCarpetaPublicaWeb = props.getProperty("pathCarpetaPublicaWeb", "/public");
        ConfiguracionForja.pathsitesAvailableApache = props.getProperty("pathsitesAvailableApache", "/etc/apache2/sites-available");
        ConfiguracionForja.ficheroProyectosSSL = props.getProperty("ficheroProyectosSSL", "dev.misidelab.es-ssl-projects");
        ConfiguracionForja.ficheroProyectos = props.getProperty("ficheroProyectos", "dev.misidelab.es-projects");
        ConfiguracionForja.ficheroConfigSSH = props.getProperty("ficheroConfigSSH", "/etc/ssh/sshd_config");
        ConfiguracionForja.marcadorJaulaSSH = props.getProperty("marcadorJaulaSSH", "##### No borrar esta linea: jaula de usuarios #####");
        ConfiguracionForja.hostRedmine = props.getProperty("hostRedmine", "https://dev.misidelab.es");
        ConfiguracionForja.keyRedmineAPI = props.getProperty("keyRedmineAPI", "8214d778a97312de0879f213ca1ae805c6e502d2");
        ConfiguracionForja.hostMysql = props.getProperty("hostMysql", "127.0.0.1");
        ConfiguracionForja.puertoMysql = props.getProperty("puertoMysql", "3306");
        ConfiguracionForja.usernameMysql = props.getProperty("usernameMysql", "root");
        ConfiguracionForja.passMysql = props.getProperty("passMysql", "admin");
        ConfiguracionForja.schemaRedmine = props.getProperty("schemaRedmine", "redmine");
    }



    /**
     * <p>Genera un objeto properties a partir de los valores que tienen las
     * variables estáticas de la Forja.</p>
     * @return Objeto properties con los valores de las variables estáticas
     */
    private Properties guardarConfiguracion() {
        Properties props = new Properties();

        props.setProperty("hostLDAP", ConfiguracionForja.hostLDAP);
        props.setProperty("puertoLDAP", Integer.toString(ConfiguracionForja.puertoLDAP));
        props.setProperty("bindDN", ConfiguracionForja.bindDN);
        props.setProperty("passBindDN", ConfiguracionForja.passBindDN);
        props.setProperty("baseDN", ConfiguracionForja.baseDN);
        props.setProperty("ouUsuarios", ConfiguracionForja.ouUsuarios);
        props.setProperty("ouProyectos", ConfiguracionForja.ouProyectos);
        props.setProperty("ouConfiguracion", ConfiguracionForja.ouConfiguracion);
        props.setProperty("groupSuperadmin", ConfiguracionForja.groupSuperadmin);
        props.setProperty("pathSVNApache", ConfiguracionForja.pathSVNApache);
        props.setProperty("pathSVNWeb", ConfiguracionForja.pathSVNWeb);
        props.setProperty("pathGITApache", ConfiguracionForja.pathGITApache);
        props.setProperty("pathGITWeb", ConfiguracionForja.pathGITWeb);
        props.setProperty("pathReposPublicosWeb", ConfiguracionForja.pathReposPublicosWeb);
        props.setProperty("pathCarpetas", ConfiguracionForja.pathCarpetas);
        props.setProperty("pathCarpetaPrivadaApache", ConfiguracionForja.pathCarpetaPrivadaApache);
        props.setProperty("pathCarpetaPrivadaWeb", ConfiguracionForja.pathCarpetaPrivadaWeb);
        props.setProperty("pathCarpetaPublicaApache", ConfiguracionForja.pathCarpetaPublicaApache);
        props.setProperty("pathCarpetaPublicaWeb", ConfiguracionForja.pathCarpetaPublicaWeb);
        props.setProperty("pathsitesAvailableApache", ConfiguracionForja.pathsitesAvailableApache);
        props.setProperty("ficheroProyectosSSL", ConfiguracionForja.ficheroProyectosSSL);
        props.setProperty("ficheroProyectos", ConfiguracionForja.ficheroProyectos);
        props.setProperty("ficheroConfigSSH", ConfiguracionForja.ficheroConfigSSH);
        props.setProperty("marcadorJaulaSSH", ConfiguracionForja.marcadorJaulaSSH);
        props.setProperty("hostRedmine", ConfiguracionForja.hostRedmine);
        props.setProperty("keyRedmineAPI", ConfiguracionForja.keyRedmineAPI);
        props.setProperty("hostMysql", ConfiguracionForja.hostMysql);
        props.setProperty("puertoMysql", ConfiguracionForja.puertoMysql);
        props.setProperty("usernameMysql", ConfiguracionForja.usernameMysql);
        props.setProperty("passMysql", ConfiguracionForja.passMysql);
        props.setProperty("schemaRedmine", ConfiguracionForja.schemaRedmine);
        props.setProperty("hostRest", "localhost");
        props.setProperty("puertoRest", "5555");
        props.setProperty("protocoloRest", "HTTPS");
        props.setProperty("carpetaHtml", "SidelabCode_WebInterface");
        props.setProperty("rootServicio", "");

        return props;
    }


    /**
     * <p>Genera un fichero de configuración a partir de unas properties indicadas
     * que se le pasan como parámetro.</p>
     * @param ruta Ruta absoluta y nombre del fichero de configuración a guardar
     * @param props Objeto con las properties a guardar en el fichero
     */
    public void regenerarFicheroConfigDefault(String ruta, Properties props) {
        try {
            props.store(new FileOutputStream(ruta), null);
        } catch (IOException ex) {
        }
    }

    
}
