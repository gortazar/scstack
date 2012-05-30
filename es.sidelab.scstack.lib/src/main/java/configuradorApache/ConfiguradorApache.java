/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ConfiguradorApache.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package configuradorApache;

import apoyo.Utilidades;
import config.ConfiguracionForja;
import modeloDatos.Proyecto;
import excepciones.configuradorApache.ExcepcionGeneradorFicherosApache;
import excepciones.configuradorApache.ExcepcionConsola;
import java.util.ArrayList;
import modeloDatos.repositorios.Repositorio;


/**
 * <p>Clase encargada de ofrecernos la gestión de los repositorios, carpetas y
 * configuración de Apache para la forja Sidelab.</p>
 * @author Arek Klauza
 */
public  class ConfiguradorApache {
    /** Ruta absoluta donde alojamos la carpeta privada del proyecto dentro del servidor Apache. */
    private String pathCarpetaPrivadaApache;
    /** Ruta relativa web para las carpetas privadas de los proyectos */
    private String pathCarpetaPrivadaWeb;
    /** Ruta absoluta donde alojamos la carpeta pública del proyecto dentro del servidor Apache. */
    private String pathCarpetaPublicaApache;
    /** Ruta relativa web para las carpetas públicas de los proyectos */
    private String pathCarpetaPublicaWeb;
    /** Ruta absoluta donde se encuentran los ficheros sites-available de Apache. */
    private String pathSitesAvailableApache;
    /** Nombre del fichero de configuración Apache de los proyectos (repositorios y carpetas). */
    private String ficheroProyectos;
    /** Runtime de Java que utilizaremos para escribir en la consola de Linux */
    private Runtime consola;




    /**
     * <p>Consructor de la clase que coge todos los parámetros desde la clase
     * de configuración de la Forja.</p>
     */
    public ConfiguradorApache() {
        this.pathSitesAvailableApache = ConfiguracionForja.pathsitesAvailableApache;
        this.ficheroProyectos = ConfiguracionForja.ficheroProyectosSSL;
        this.pathCarpetaPrivadaWeb = ConfiguracionForja.pathCarpetaPrivadaWeb;
        this.pathCarpetaPrivadaApache = ConfiguracionForja.pathCarpetaPrivadaApache;
        this.pathCarpetaPublicaWeb = ConfiguracionForja.pathCarpetaPublicaWeb;
        this.pathCarpetaPublicaApache = ConfiguracionForja.pathCarpetaPublicaApache;
        this.consola = Runtime.getRuntime();
    }




    /**
     * <p>Método encargado de crear todas la carpetas necesarias y configurar
     * Apache para añadir un nuevo proyecto a la Forja.</p>
     * <p>Se puede utilizar tanto para añadir nuevos proyectos como para editar
     * sus propiedades/repositorios.</p>
     * @param listaProyectos Lista de los proyectos que hay en la forja. Se debe
     * obtener directamente desde el gestorLDAP.
     * @param proyecto Proyecto que queremos configurar
     * @param listaUidsUsuarios Lista con los Uids de todos los usuarios de la
     * Forja para poder enjaularlos
     * @throws ExcepcionGeneradorFicherosApache Se lanza cuando se ha producido
     * algún tipo de error durante la generación del fichero de configuración de
     * Apache.
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    public void configurarProyecto(Proyecto[] listaProyectos, Proyecto proyecto, ArrayList<String> listaUidsUsuarios)
            throws ExcepcionGeneradorFicherosApache, ExcepcionConsola {
        this.generarFicheroProyectosApache(listaProyectos);
        this.crearRepositorios(proyecto);
        this.crearCarpetasProyecto(proyecto.getCn(), proyecto.getPrimerAdmin());
        this.reiniciarApache();
    }



    /**
     * <p>Método encargado de eliminar los repositorios, ficheros de configuración
     * y carpetas asociadas a un proyecto determinado en Apache.</p>
     * @param listaProyectos Lista de los proyectos que hay en la forja. Se debe
     * obtener directamente desde el gestorLDAP.
     * @param proyecto Proyecto que queremos eliminar
     * @throws ExcepcionGeneradorFicherosApache Se lanza cuando se ha producido
     * algún tipo de error durante la regeneración del fichero de configuración de
     * Apache.
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    public void borrarProyecto(Proyecto[] listaProyectos, Proyecto proyecto)
            throws ExcepcionConsola, ExcepcionGeneradorFicherosApache {
        this.generarFicheroProyectosApache(listaProyectos);
        this.borrarRepositorios(proyecto);
        this.borrarCarpetasProyecto(proyecto.getCn());
        this.reiniciarApache();
    }




    /**
     * <p>A partir de la lista de proyectos que hay en la forja que nos facilita
     * el gestorLDPA, genera el fichero de configuración de Apache con los repositorios
     * y carpetas de los proyectos de la Forja.</p>
     * @param listaProyectos Lista de Proyectos de la forja (debe ser facilitado
     * por el gestorLDAP).
     * @throws ExcepcionGeneradorFicherosApache Se lanza cuando se ha producido
     * algún error a la hora de generar el fichero de configuración de Apache.
     */
    private void generarFicheroProyectosApache(Proyecto[] listaProyectos) throws ExcepcionGeneradorFicherosApache {
        new GeneradorFicherosApache().generarFicherosProyectos(listaProyectos);
    }



    /**
     * <p>Crea y configura los repositorios que tenga el proyecto. En caso de que
     * ya se haya creado uno se omite su creación y pasa al siguiente.</p>
     * @param proyecto Proyecto cuyos repositorios queremos crear
     * @throws ExcepcionConsola Cuando se produjo algún error durante la creación
     * del repositorio por error de la consola linux
     */
    private void crearRepositorios(Proyecto proyecto) throws ExcepcionConsola {
        if (proyecto.tieneRepositorio())
            for (Repositorio repo : proyecto.getRepositorios()) {
                if (!Utilidades.existeCarpeta(repo.getRuta(), proyecto.getCn()))
                    repo.crearRepositorio(proyecto.getCn(), proyecto.getPrimerAdmin());
            }
    }


    

    /**
     * <p>Elimina todos los repositorios asociados a un proyecto.</p>
     * @param proyecto Objeto Proyecto cuyos repositorios se va a borrar
     * @throws ExcepcionConsola Cuando se produce algún error durante el borrado
     * de los repositorios por error en la consola de Linux
     */
    private void borrarRepositorios(Proyecto proyecto) throws ExcepcionConsola {
        if (proyecto.tieneRepositorio())
            for (Repositorio repo : proyecto.getRepositorios()) {
                if (Utilidades.existeCarpeta(repo.getRuta(), proyecto.getCn()))
                    repo.borrarRepositorio(proyecto.getCn());
            }
    }



    /**
     * <p>Método encargado de crear las carpetas pública y privada accesibles a
     * través del navegador web en el servidor Apache. Si estas ya existen no se
     * hace nada en este método.</p>
     * <p>También se encarga de enjaular a los usuarios en la carpeta privada y
     * pública para que no puedan salir de ahí al Apache.</p>
     * @param cnProyecto Nombre del proyecto
     * @param uidAdminProyecto UID del usuario Administrador del proyecto
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    private void crearCarpetasProyecto(String cnProyecto, String uidAdminProyecto) throws ExcepcionConsola, ExcepcionGeneradorFicherosApache {
        if (Utilidades.existeCarpeta(this.pathCarpetaPrivadaApache, cnProyecto) || 
                Utilidades.existeCarpeta(this.pathCarpetaPublicaApache, cnProyecto))
            return;

        try {
            consola.exec("mkdir " + this.pathCarpetaPublicaApache + "/" + cnProyecto).waitFor();
            consola.exec("mkdir " + this.pathCarpetaPrivadaApache + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló la creación de las carpetas (pública y privada) "
                    + "del proyecto - " + e.getMessage());
        }
        try {
            // Permisos para la carpeta pública
            consola.exec("chown " + uidAdminProyecto + ":" + cnProyecto + " " + this.pathCarpetaPublicaApache + "/" + cnProyecto).waitFor();
            consola.exec("chmod g+w " + this.pathCarpetaPublicaApache + "/" + cnProyecto).waitFor();

            // Permisos para la carpeta privada
            consola.exec("chown " + uidAdminProyecto + ":" + cnProyecto + " " + this.pathCarpetaPrivadaApache + "/" + cnProyecto).waitFor();
            consola.exec("chmod 770 " + this.pathCarpetaPrivadaApache + "/" + cnProyecto).waitFor();            
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló la asignación de permisos de las carpetas "
                    + "(pública y privada) del proyecto - " + e.getMessage());
        }
    }


    /**
     * <p>Método encargado del borrado de la carpeta pública y privada asociadas
     * a un proyecto determinado de la Forja.</p>
     * @param cnProyecto Nombre del proyecto cuyas carpetas se quiere borrar
     * @throws ExcepcionConsola Se produce cuando sucede algún error al ejecutar
     * las sentencias de borrado en la consola de Linux
     */
    private void borrarCarpetasProyecto(String cnProyecto) throws ExcepcionConsola {
        if (!Utilidades.existeCarpeta(this.pathCarpetaPrivadaApache, cnProyecto) &&
                !Utilidades.existeCarpeta(this.pathCarpetaPublicaApache, cnProyecto))
            return;
        try {
            consola.exec("rm -rf " + this.pathCarpetaPublicaApache + "/" + cnProyecto).waitFor();
            consola.exec("rm -rf " + this.pathCarpetaPrivadaApache + "/" + cnProyecto).waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló el borrado de las carpetas (pública y privada) "
                    + "del proyecto - " + e.getMessage());
        }
    }

    
    /**
     * <p>Método que tendrá que ejecutarse después de realizar las configuraciones
     * oportunas sobre el servidor Apache para que este se reinicie y se apliquen
     * los cambios realizados.</p>
     * @throws ExcepcionConsola Cuando se produce algún error durante el acceso
     * del método a la consola Linux del servidor.
     */
    private void reiniciarApache() throws ExcepcionConsola {
        try {
            consola.exec("apache2ctl graceful").waitFor();
        } catch (Exception e) {
            throw new ExcepcionConsola("Error del terminal Linux: falló reseteo de Apache - " + e.getMessage());
        }
    }

}
