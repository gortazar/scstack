/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GestorLDAP.java
 * Autor: Arek Klauza
 * Fecha: Diciembre 2010
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.ldap;


import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios;
import es.sidelab.scstack.lib.dataModel.repos.Repositorio;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPAdministradorUnico;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPManejoIDNumber;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPYaExisteEntrada;
import java.util.ArrayList;
import java.util.Arrays;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>Clase encargada de interactuar y ofrecer la gestión del directorio LDAP.</p>
 * @author Arek Klauza
 */
public class GestorLDAP {
    /** DN base sobre el que buscar (Ejemplo: "dc=sidelab,dc=es") */
    private String baseDN;
    /** Nombre del OU (Organizational Unit) de usuarios (Ejemplo: "people") */
    private String ouUsuarios;
    /** Nombre del OU (Organizational Unit) de proyectos (Ejemplo: "group") */
    private String ouProyectos;
    /** Nombre del OU (Organizational Unit) de configuración (Ejemplo: "configuracion") */
    private String ouConfiguracion;
    /** Proxy de la API para hablar directamente con LDAP */
    private LDAPConnection conexion;

    


    /**
     * <p>Crea un Gestor del servidor LDAP y conecta con dicho servidor automáticamente
     * con los datos recogidos en la clase es.sidelab.scstack.lib.config.ConfiguracionForja.</p>
     * <p>A través de dicho servidor podremos hablar directamente con el directorio
     * LDAP a través de conexión Segura SSL.</p>
     * @throws ExcepcionGestorLDAP Indica que ha habido algún error durante la
     * conexión con el servidor de LDAP.
     */
    public GestorLDAP() throws ExcepcionGestorLDAP {
        this.baseDN = ConfiguracionForja.baseDN;
        this.ouUsuarios = ConfiguracionForja.ouUsuarios;
        this.ouProyectos = ConfiguracionForja.ouProyectos;
        this.ouConfiguracion = ConfiguracionForja.ouConfiguracion;

        try {
            SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());
            SSLSocketFactory socketFactory = sslUtil.createSSLSocketFactory();
            conexion = new LDAPConnection(socketFactory, ConfiguracionForja.hostLDAP, ConfiguracionForja.puertoLDAP);
            conexion.bind(ConfiguracionForja.bindDN, ConfiguracionForja.passBindDN);
        } catch (Exception e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        this.cerrarConexion();
        super.finalize();
    }



    /**
     * <p>Añade una persona al directorio LDAP al OU=people. En caso de ya existir
     * alguien con dicho UID se lanza una excepción. El UIDNumber y GIDNumber son
     * extraidos automáticamente de un marcador en el propio directorio que lleva
     * la cuenta del último número disponible, y además lo actualiza.</p>
     * @param usuario Objeto Usuario que queremos añadir al directorio LDAP
     * @throws ExcepcionGestorLDAP Cuando se produce algún error generalista del
     * GestorLDAP, o cuando el UID contiene tildes o ñ.
     * @throws ExcepcionLDAPYaExisteEntrada Cuando ya existe una entrada con el uid
     * que se está pasando como parámetro.
     */
    public void addUsuario(Usuario usuario) throws ExcepcionGestorLDAP, ExcepcionLDAPYaExisteEntrada {
        if (usuario == null)
            throw new ExcepcionGestorLDAP("No se pudo añadir el usuario al directorio LDAP porque el Usuario facilitado era nulo");
        int uidNumber = this.getSiguienteUIDNumber();
        int gidNumber = this.getSiguienteGIDNumber();
        Attribute[] atributos =
            {
              new Attribute("objectClass", "top", "person", "organizationalPerson", "inetOrgPerson", "posixAccount"),
              new Attribute("uid", usuario.getUid()),
              new Attribute("cn", usuario.getNombre()),
              new Attribute("gidNumber", Integer.toString(gidNumber)),
              new Attribute("uidNumber", Integer.toString(uidNumber)),
              new Attribute("homeDirectory", "/"),
              new Attribute("sn", usuario.getApellidos()),
              new Attribute("mail", usuario.getEmail()),
              new Attribute("userPassword", usuario.getPassMD5())
            };
        try {
            // Si la entrada ya existe, lanza una excepción
            conexion.add("uid=" + usuario.getUid() + ",ou=" + this.ouUsuarios + "," + this.baseDN, atributos);
        } catch (LDAPException e) {
            if (e.getResultCode() == ResultCode.ENTRY_ALREADY_EXISTS)
                throw new ExcepcionLDAPYaExisteEntrada(e);
            else
                throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Añade un proyecto al directorio, en el OU=group. En caso de existir ya
     * un proyecto con dicho CN se lanza una excepción. El GIDNumber es extraído
     * automáticamente de un marcador en el propio directorio que lleva la
     * cuenta del último número disponible, y además lo actualiza.
     * @param proyecto Objeto Proyecto no nulo que contiene los datos a almacenar
     * @throws ExcepcionGestorLDAP Cuando se produce algún error generalista del
     * GestorLDAP, o cuando el CN contiene tildes o ñ.
     * @throws ExcepcionLDAPYaExisteEntrada Cuando ya existe una entrada con el CN
     * que se está pasando como parámetro o cuando el param description es inválido.
     */
    public void addProyecto(Proyecto proyecto) throws ExcepcionGestorLDAP, ExcepcionLDAPYaExisteEntrada {
        if (proyecto == null)
            throw new ExcepcionGestorLDAP("No se pudo añadir el proyecto al directorio LDAP porque el Proyecto facilitado era nulo");
        if (!this.existeUsuario(proyecto.getPrimerAdmin()))
            throw new ExcepcionGestorLDAP("El UID = " + proyecto.getPrimerAdmin() + " de administrador no existe en el directorio. "
                    + "Antes de crearse el proyecto se debe crear al usuario administrador.");
        
        int gidNumber = this.getSiguienteGIDNumber();

        // Añadimos el proyecto con Repositorio
        if (proyecto.tieneRepositorio()) {
            Attribute[] atributos =
                {
                  new Attribute("objectClass", "top", "posixGroup", "proyectoForja"),
                  new Attribute("cn", proyecto.getCn()),
                  new Attribute("gidNumber", Integer.toString(gidNumber)),
                  new Attribute("description", proyecto.getDescription()),
                  new Attribute("adminUid", proyecto.getPrimerAdmin()),
                  new Attribute("memberUid", proyecto.getPrimerAdmin()),
                  new Attribute("repositorio", proyecto.getPrimerRepositorio().aplanar()),
                  new Attribute("defaultRepositorio", proyecto.getDefaultRepositorio()),
                };
            try {
                // Si la entrada ya existe, lanza una excepción
                conexion.add("cn=" + proyecto.getCn() + ",ou=" + this.ouProyectos + "," + this.baseDN, atributos);
            } catch (LDAPException e) {
                if (e.getResultCode() == ResultCode.ENTRY_ALREADY_EXISTS)
                    throw new ExcepcionLDAPYaExisteEntrada(e);
                else
                    throw new ExcepcionGestorLDAP(e);
            }

        // Añadimos el proyecto sin repositorio
        } else {
            Attribute[] atributos =
                {
                  new Attribute("objectClass", "top", "posixGroup", "proyectoForja"),
                  new Attribute("cn", proyecto.getCn()),
                  new Attribute("gidNumber", Integer.toString(gidNumber)),
                  new Attribute("description", proyecto.getDescription()),
                  new Attribute("adminUid", proyecto.getPrimerAdmin()),
                  new Attribute("memberUid", proyecto.getPrimerAdmin())
                };
            try {
                // Si la entrada ya existe, lanza una excepción
                conexion.add("cn=" + proyecto.getCn() + ",ou=" + this.ouProyectos + "," + this.baseDN, atributos);
            } catch (LDAPException e) {
                if (e.getResultCode() == ResultCode.ENTRY_ALREADY_EXISTS)
                    throw new ExcepcionLDAPYaExisteEntrada(e);
                else
                    throw new ExcepcionGestorLDAP(e);
            }
        }
        
        
    }


    /**
     * <p>Método que añade a un usuario ya existente a un proyecto determinado.</p>
     * <p>Si el usuario que se le pasa no existe en el directorio, no se añade y se
     * devuelve una excepción indicándolo. Es decir, sólo añade usuarios preexistentes.</p>
     * <p>Cuando el usuario que queremos añadir ya es miembro, no pasa nada, ni siquiera
     * lo notifica.</p>
     * @param cnProyecto Nombre (CN) del proyecto al que queremos añadir al usuario
     * @param uid UID del usuario que queremos añadir al proyecto
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe el UID pasado como parámetro.
     * @throws ExcepcionGestorLDAP Se lanzará en cualquier otro tipo de operación
     * de acceso al servidor LDAP que haya fallado.
     */
    public void addUsuarioAProyecto(String cnProyecto, String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos añadir y proyecto existen
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("No existe el usuario " + uid);
        if (this.getAtributoProyecto(cnProyecto, "cn") == null)
            throw new ExcepcionLDAPNoExisteRegistro("No existe el proyecto " + cnProyecto);

        // Luego procedemos a añadir al usuario al proyecto
        ArrayList<String> miembrosProyecto = this.getListaUsuariosXProyecto(cnProyecto);
        if (miembrosProyecto == null)
            this.editAtributo(this.ouProyectos, cnProyecto, "memberUid", uid);
        else {
            if (!miembrosProyecto.contains(uid)) {
                String[] lista = new String[miembrosProyecto.size()+1];
                miembrosProyecto.toArray(lista);
                lista[lista.length-1] = uid;
                this.editAtributo(this.ouProyectos, cnProyecto, "memberUid", lista);
            }
        }
    }



    /**
     * <p>Método que añade a un administrador ya existente a un proyecto determinado.</p>
     * <p>Si el usuario que se le pasa no existe en el directorio, no se añade y se
     * devuelve una excepción indicándolo. Es decir, sólo añade usuarios preexistentes.</p>
     * <p>Cuando el uid que queremos añadir ya es admin, no pasa nada, ni siquiera
     * lo notifica.</p>
     * <p>Aparte de añadir al administrador a la lista de admins del proyecto,
     * también le añade como usuario del mismo.</p>
     * @param cnProyecto Nombre (CN) del proyecto al que queremos añadir al usuario
     * @param uid UID del usuario que queremos añadir al proyecto
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe el UID pasado como parámetro.
     * @throws ExcepcionGestorLDAP Se lanzará en cualquier otro tipo de operación
     * de acceso al servidor LDAP que haya fallado.
     */
    public void addAdminAProyecto(String cnProyecto, String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos añadir y proyecto existen
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("No existe el usuario " + uid);
        if (this.getAtributoProyecto(cnProyecto, "cn") == null)
            throw new ExcepcionLDAPNoExisteRegistro("No existe el proyecto " + cnProyecto);

        // Luego procedemos a añadir al admin al proyecto
        ArrayList<String> adminsProyecto = this.getListaAdministradoresXProyecto(cnProyecto);
        if (adminsProyecto == null)
            this.editAtributo(this.ouProyectos, cnProyecto, "adminUid", uid);
        else {
            if (!adminsProyecto.contains(uid)) {
                String[] lista = new String[adminsProyecto.size()+1];
                adminsProyecto.toArray(lista);
                lista[lista.length-1] = uid;
                this.editAtributo(this.ouProyectos, cnProyecto, "adminUid", lista);
            }
        }
        // Como todo admin es a su vez usuario, hay que añadirlo tb a la lista de usuarios
        this.addUsuarioAProyecto(cnProyecto, uid);
    }




    /**
     * <p>Método que añade un repositorio nuevo a un proyecto existente.</p>
     * <p>Si el proyecto que estamos pasando como parámetros no existe en el
     * directorio LDAP, o el repositorio pasado es nulo, lanza excepcion.</p>
     * @param repo Objeto Repositorio a añadir al proyecto
     * @param cnProyecto CN del proyecto donde añadir el repositorio
     * @throws ExcepcionLDAPNoExisteRegistro Cuando el Repositorio pasado es nulo 
     * o cuando no existe el proyecto pasado en el directorio LDAP
     * @throws ExcepcionRepositorio Cuando el intentamos añadir un tipo de repositorio
     * que ya existe en el proyecto. (Solo puede haber un SVN, un GIT, a la vez.)
     * @throws ExcepcionGestorLDAP Se lanzará en cualquier otro tipo de operación
     * de acceso al servidor LDAP que haya fallado.
     */
    public void addRepositorioAProyecto(Repositorio repo, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionRepositorio {
        // Primero comprobamos que el proyecto existe y que el Repo no es nulo
        if (repo == null)
            throw new ExcepcionLDAPNoExisteRegistro("El repositorio que se está intentado añadir es nulo");
        if (this.getAtributoProyecto(cnProyecto, "cn") == null)
            throw new ExcepcionLDAPNoExisteRegistro("No existe el proyecto " + cnProyecto);

        
        ArrayList listaRepos = this.getListaRepositoriosXProyecto(cnProyecto);
        // Luego procedemos a añadir el repositorio al proyecto
        if (listaRepos == null || listaRepos.isEmpty()) {
            this.editAtributo(this.ouProyectos, cnProyecto, "repositorio", repo.aplanar());
            this.editAtributo(this.ouProyectos, cnProyecto, "defaultRepositorio", repo.getTipo());
        } else {
            // Comprobamos que el repositorio que intenamos añadir no existe ya, puesto que sólo
            // puede haber un repo SVN, uno GIT, etc... No puede haber 2 del mismo tipo.
            if (listaRepos.contains(repo))
                throw new ExcepcionRepositorio("Ya existe un repositorio del tipo " + repo.getTipo() + " en el proyecto "
                        + cnProyecto + ". Sólo puede haber un repositorio del mismo tipo por proyecto.");
            String[] lista = new String[listaRepos.size()+1];
            for (int i = 0; i < listaRepos.size(); i++)
                lista[i] = ((Repositorio)listaRepos.get(i)).aplanar();
            lista[lista.length-1] = repo.aplanar();
            this.editAtributo(this.ouProyectos, cnProyecto, "repositorio", lista);
        }
    }



    /**
     * <p>Devuelve un objeto Usuario construido a partir de los datos almacenados
     * en el directorio LDAP.</p>
     * @param uid UID del usuario a buscar
     * @return Objeto Usuario del UID indicado como parámetro
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no se ha encontrado el UID
     * buscado en el directorio LDAP
     * @throws ExcepcionGestorLDAP Se lanza cuando sucede algún error durante el
     * acceso al directorio LDAP.
     */
    public Usuario getUsuario(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP  {
        try {
            SearchResult searchResults = conexion.search("ou=" + ouUsuarios + "," + baseDN, SearchScope.SUB, "(uid=" + uid + ")");
            if (searchResults.getEntryCount() > 0) {
                return new Usuario(searchResults.getSearchEntries().get(0).getAttributeValue("uid"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("cn"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("sn"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("mail"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("userPassword"));
            } else
                throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");
        } catch (ExcepcionUsuario e) {
            throw new ExcepcionGestorLDAP(e);
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Devuelve un objeto Proyecto construido a partir de los datos almacenados
     * en el directorio LDAP.</p>
     * @param cn CN del proyecto
     * @return Objeto Proyecto del cn indicado como parámetro del método
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no se ha encontrado el CN
     * buscado en el directorio LDAP
     * @throws ExcepcionGestorLDAP Se lanza cuando sucede algún error durante el
     * acceso al directorio LDAP.
     */
    public Proyecto getProyecto(String cn) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        try {
            SearchResult searchResults = conexion.search("ou=" + ouProyectos + "," + baseDN, SearchScope.SUB, "(cn=" + cn + ")");
            if (searchResults.getEntryCount() > 0) {
                return new Proyecto(searchResults.getSearchEntries().get(0).getAttributeValue("cn"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("description"),
                        searchResults.getSearchEntries().get(0).getAttributeValue("gidNumber"),
                        this.getListaAdministradoresXProyecto(cn),
                        this.getListaUsuariosXProyecto(cn),
                        this.getListaRepositoriosXProyecto(cn),
                        searchResults.getSearchEntries().get(0).getAttributeValue("defaultRepositorio"));
            } else
                throw new ExcepcionLDAPNoExisteRegistro("El proyecto " + cn + " no existe en el directorio LDAP");
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }




    /**
     * <p>Devuelve la lista de UIDS de usuarios que hay en el directorio LDAP.</p>
     * @return Lista de uids de usuarios del directorio
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaUIdsUsuario() throws ExcepcionGestorLDAP {
        return this.getListaXAtributo(this.ouUsuarios, "uid");
    }


    /**
     * <p>Devuelve la lista de apellido y nombre de todos los usuarios que hay
     * en el directorio LDAP, ordenados por apellido.</p>
     * @return Lista de nombres completos de usuarios del directorio
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaNombresCompletosUsuarios() throws ExcepcionGestorLDAP {
        ArrayList<String> nombres = this.getListaXAtributo(this.ouUsuarios, "cn");
        ArrayList<String> apellidos = this.getListaXAtributo(this.ouUsuarios, "sn");
        ArrayList<String> merge = new ArrayList<String>();
        for (int i = 0; i < nombres.size(); i++)
            merge.add(apellidos.get(i) + ", " + nombres.get(i));
        //Collections.sort(merge);
        return merge;
    }


    /**
     * <p>Devuelve una lista con las direcciones de email de los distintos usuarios
     * de la Forja.</p>
     * @return Lista de direcciones de correo de todos los usuarios de la Forja
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaEmailsUsuarios() throws ExcepcionGestorLDAP {
        return this.getListaXAtributo(this.ouUsuarios, "mail");
    }


    /**
     * <p>Devuelve la lista de nombres de proyectos (CN's) que hay en el directorio LDAP.</p>
     * @return Lista de nombres de los proyectos del directorio
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaNombresProyectos() throws ExcepcionGestorLDAP {
        return this.getListaXAtributo(this.ouProyectos, "cn");
    }


    /**
     * <p>Devuelve una lista de objetos de tipo Proyecto de todas las entradas de
     * proyectos que hay en el directorio LDAP.</p>
     * @return Lista de objetos Proyecto que hay en el servidor LDAP
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public Proyecto[] getListaProyectos() throws ExcepcionGestorLDAP {
        try {
            SearchResult searchResults = conexion.search("ou=" + ouProyectos + "," + baseDN, SearchScope.SUB, "(cn=*)");
         
            if (searchResults.getEntryCount() > 0) {
                Proyecto[] lista = new Proyecto[searchResults.getEntryCount()];
                for (int i = 0; i < searchResults.getEntryCount(); i++) {
                    String cn = searchResults.getSearchEntries().get(i).getAttributeValue("cn");
                    lista[i] = new Proyecto(cn,
                            searchResults.getSearchEntries().get(i).getAttributeValue("description"),
                            searchResults.getSearchEntries().get(i).getAttributeValue("gidNumber"),
                            this.getListaAdministradoresXProyecto(cn),
                            this.getListaUsuariosXProyecto(cn),
                            this.getListaRepositoriosXProyecto(cn),
                            searchResults.getSearchEntries().get(0).getAttributeValue("defaultRepositorio"));
                }
                return lista;
            } else {
                return null;
            }
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }



    /**
     * <p>Devuelve una lista de todos los nombres de proyectos en los que participa
     * como miembro el usuario indicado.</p>
     * @param uid UID del usuario cuyos proyectos buscamos.
     * @return Arraylist de los nombres de los proyectos en los que participa el
     * usuario.
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando pasamos como parámetro
     * un UID de un usario que no existe.
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaProyectosMiembroXUid(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        ArrayList<String> lista = new ArrayList<String>();
        Proyecto[] listaProyectos = this.getListaProyectos();

        for (Proyecto p : listaProyectos) {
            if (p.getUsuarios().contains(uid))
                lista.add(p.getCn());
        }
        return lista;
    }



    /**
     * <p>Devuelve una lista de todos los nombres de proyectos en los que el usuario
     * indicado es administrador.</p>
     * @param uid UID del usuario cuyos proyectos buscamos.
     * @return Arraylist de los nombres de los proyectos que administra el usuario.
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando pasamos como parámetro
     * un UID de un usario que no existe.
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaProyectosAdministradosXUid(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        ArrayList<String> lista = new ArrayList<String>();
        Proyecto[] listaProyectos = this.getListaProyectos();

        for (Proyecto p : listaProyectos) {
            if (p.getAdministradores().contains(uid))
                lista.add(p.getCn());
        }
        return lista;
    }


    /**
     * <p>Método que recupera una lista de todos los usuarios que pertenecen a
     * proyectos administrados por el usuario con UID indicado.</p>
     * @param uid UID del usuario cuya lista de usuarios se quiere recuperar
     * @return Lista de UIDs de los usuarios administrados
     * @throws ExcepcionLDAPNoExisteRegistro Cuando el UID pasado como parámetro
     * no pertenece a ningún usuario del directorio LDAP
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaUsuariosAdministradosXUid(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        ArrayList<String> lista = new ArrayList<String>();
        for (String p : this.getListaProyectosAdministradosXUid(uid))
            for (String uid2 : this.getListaUsuariosXProyecto(p))
                if (!lista.contains(uid2))
                    lista.add(uid2);
        return lista;
    }


    /**
     * <p>Devuelve una lista de los UIDS de usuarios que son miembros de un proyecto
     * determinado cuyo nombre pasamos como parámetro.</p>
     * @param cn Common Name del proyecto
     * @return Lista de los uid de usuarios miembros del proyecto
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaUsuariosXProyecto(String cn) throws ExcepcionGestorLDAP {
        return this.getAtributoMultiple(this.ouProyectos, cn, "memberUid");
    }



    /**
     * <p>Devuelve una lista de los UIDS de los usuarios que son administradores de un proyecto
     * determinado cuyo nombre pasamos como parámetro.</p>
     * @param cn Common Name del proyecto
     * @return Lista de los uid de usuarios miembros del proyecto
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<String> getListaAdministradoresXProyecto(String cn) throws ExcepcionGestorLDAP {
        return this.getAtributoMultiple(this.ouProyectos, cn, "adminUid");
    }


    /**
     * <p>Devuelve una lista de objetos Repositorio a partir de la información
     * contenida en el directorio LDAP.</p>
     * @param cn Common Name del proyecto
     * @return ArrayList de objetos Repositorio
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * al directorio LDAP.
     */
    public ArrayList<Repositorio> getListaRepositoriosXProyecto(String cn) throws ExcepcionGestorLDAP {
        ArrayList<String> listaAplanada = this.getAtributoMultiple(this.ouProyectos, cn, "repositorio");

        if (listaAplanada == null)
            return null;

        ArrayList lista = new ArrayList<Repositorio>();
        for (String cadena : listaAplanada) {
            Repositorio repo = FactoriaRepositorios.crearRepositorio(cadena);
            lista.add(repo);
        }
        return lista;
    }



    /**
     * <p>Este método permite editar los atributos de un usuario determinado.</p>
     * <p>Si el usuario tiene un UID que no existe en el directorio, se lanza una 
     * excepción.</p>
     * <p>El usuario pasado deberá tener todos sus parámetros rellenos, ya que si
     * no se almacenarán en blanco en el directorio LDAP.</p>
     * @param user Objeto usuario con los valores actualizados (con todos los
     * atributos rellenados).
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe el UID de usuario que
     * hemos indicado como parámetro
     * @throws ExcepcionGestorLDAP Cuando sucede algún error durante la modificación
     * de los atributos de usuario.
     */
    public void editUsuario(Usuario user) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos modificar existe
        if (this.getAtributoUsuario(user.getUid(), "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + user.getUid() + " no existe en el directorio LDAP");

        // Editamos todos los atributos del usuario
        this.editAtributo(this.ouUsuarios, user.getUid(), "cn", user.getNombre());
        this.editAtributo(this.ouUsuarios, user.getUid(), "sn", user.getApellidos());
        this.editAtributo(this.ouUsuarios, user.getUid(), "mail", user.getEmail());

        // Solo cambia la contraseña si no es nula ni vacía
        if (user.getPassMD5() != null && !user.getPassMD5().isEmpty())
            this.editAtributo(this.ouUsuarios, user.getUid(), "userPassword", user.getPassMD5());
    }


    /**
     * <p>Este método permite editar los atributos de un proyecto determinado.</p>
     * <p>Si el proyecto tiene un CN que no existe en el directorio, se lanza una
     * excepción.</p>
     * <p>Independientemente de los datos que contenga el proyecto, sólo se editan
     * los campos description y defaultRepositorio.</p>
     * @param user Objeto proyecto con los valores actualizados (sólo importan los
     * atributos "description" y "defaultRepositorio").
     * @throws ExcepcionLDAPNoExisteRegistro Cuando no existe el Cn del proyecto que
     * hemos indicado como parámetro
     * @throws ExcepcionGestorLDAP Cuando sucede algún error durante la modificación
     * de los atributos de usuario.
     */
    public void editProyecto(Proyecto proyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el proyecto que queremos modificar existe
        if (this.getAtributoProyecto(proyecto.getCn(), "cn") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El proyecto " + proyecto.getCn() + " no existe en el directorio LDAP");

        // Editamos todos los atributos editables del proyecto
        this.editAtributo(this.ouProyectos, proyecto.getCn(), "description", proyecto.getDescription());
        if (this.getAtributoProyecto(proyecto.getCn(), "defaultRepositorio") != null && proyecto.getDefaultRepositorio() != null)
            this.editAtributo(this.ouProyectos, proyecto.getCn(), "defaultRepositorio", proyecto.getDefaultRepositorio());
    }



    /**
     * <p>Bloquea a un usuario determinado para que no pueda acceder temporalmente 
     * a las funcionalidades de la Forja.</p>
     * @param uid UID del usuario a bloquear
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando no existe el UID facilitado
     * @throws ExcepcionGestorLDAP Cuando sucede algún error durante la modificación
     * de los atributos de usuario.
     */
    public void bloquearUsuario(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos bloquear existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        this.deleteAtributo(this.ouUsuarios, uid, "userPassword");
    }


    /**
     * <p>Reestablece la contraseña de un usuario previamente bloqueado para que
     * vuelva a poder acceder a la Forja.</p>
     * @param uid UID del usuario a bloquear
     * @param pass Nueva contraseña del usuario
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando no existe el UID facilitado
     * @throws ExcepcionGestorLDAP Cuando sucede algún error durante la modificación
     * de los atributos de usuario.
     */
    public void desbloquearUsuario(String uid, String pass) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos bloquear existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        this.editAtributo(this.ouUsuarios, uid, "userPassword", pass);
    }



    /**
     * <p>Este método se encarga de borrar a un usuario determinado del proyecto
     * indicado, dejando al usuario intacto en el directorio de usuarios.</p>
     * <p>Si el usuario indicado era administrador, también le borra de la lista
     * de administradores. A menos que sea administrador único, entonces se
     * interrumpe el método.</p>
     * @param uid UID del usuario que queremos borrar
     * @param cnProyecto CN del proyecto donde está el usuario a borrar
     * @throws ExcepcionLDAPAdministradorUnico Se lanza cuando estamos intenando
     * borrar a un usuario que también es administrador y además único.
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando el usuario no existe
     * en el proyecto indicado.
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * o modificación sobre el servidor LDAP.
     */
    public void deleteUsuarioDeProyecto(String uid, String cnProyecto) throws ExcepcionLDAPAdministradorUnico,
            ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos borrar pertenece al proyecto
        ArrayList<String> miembrosProyecto = this.getListaUsuariosXProyecto(cnProyecto);
        if (!miembrosProyecto.contains(uid))
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " y/o el proyecto " + cnProyecto + " no existen.");
        else {
            // Primero le borramos como administrador en caso de que también lo fuera
            try {
                this.deleteAdministradorDeProyecto(uid, cnProyecto);
            } catch (ExcepcionLDAPNoExisteRegistro e) {
                // Do nothing
            }
            String[] lista = new String[miembrosProyecto.size()-1];
            int contMiembros = 0;
            // Excluimos de la nueva lista al usuario a borrar
            for (int i = 0; i < lista.length; i++) {
                if (!miembrosProyecto.get(contMiembros).equalsIgnoreCase(uid)) {
                    lista[i] = miembrosProyecto.get(contMiembros);
                    contMiembros++;
                } else {
                    contMiembros++;
                    i--;
                }
            }
            this.editAtributo(this.ouProyectos, cnProyecto, "memberUid", lista);
        }
    }



    /**
     * <p>Este método se encarga de borrar a un administrador determinado del proyecto
     * indicado, dejando al memberUid en el proyecto y al usuario intacto en el
     * directorio de usuarios.</p>
     * <p>Cuando el administrador que queremos borrar es único, no deja borrarlo
     * porque sino el proyecto se quedaría sin administrador. En este caso se lanza
     * excepción y se interrumpe el método.</p>
     * @param uid UID del usuario que queremos borrar
     * @param cnProyecto CN del proyecto donde está el usuario a borrar
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando el usuario no existe
     * en el proyecto indicado.
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * o modificación sobre el servidor LDAP.
     */
    public void deleteAdministradorDeProyecto(String uid, String cnProyecto) 
            throws ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos borrar pertenece al proyecto
        ArrayList<String> adminsProyecto = this.getListaAdministradoresXProyecto(cnProyecto);
        if (!adminsProyecto.contains(uid))
            throw new ExcepcionLDAPNoExisteRegistro("El administador " + uid + " y/o el proyecto " + cnProyecto + " no existen.");
        // Segundo comprobamos que no es administrador único, pq si lo es prohibido borrarle
        else if (adminsProyecto.size() == 1) {
            throw new ExcepcionLDAPAdministradorUnico("Imposible borrar al administrador " + uid + " ya que es el único administrador del proyecto "
                    + cnProyecto + ". Añada primero a otro administrador y luego borre a " + uid);
        } else {
            String[] lista = new String[adminsProyecto.size()-1];
            int contMiembros = 0;
            // Excluimos de la nueva lista al usuario a borrar
            for (int i = 0; i < lista.length; i++) {
                if (!adminsProyecto.get(contMiembros).equalsIgnoreCase(uid)) {
                    lista[i] = adminsProyecto.get(contMiembros);
                    contMiembros++;
                } else {
                    contMiembros++;
                    i--;
                }
            }
            this.editAtributo(this.ouProyectos, cnProyecto, "adminUid", lista);
        }
    }



    /**
     * <p>Borra un repositorio determinado del proyecto y actualiza el marcador
     * "defaultRepositorio" para que apunte al otro repositorio del proyecto.</p>
     * @param tipoRepositorio Nombre del tipo de repositorio que queremos borrar
     * @param cnProyecto Nombre del proyecto donde está el repositorio
     * @throws ExcepcionLDAPNoExisteRegistro Se lanza cuando el repositorio que
     * queremos borrar no existe en el proyecto.
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error de acceso
     * o modificación sobre el servidor LDAP.
     */
    public void deleteRepositorioDeProyecto(String tipoRepositorio, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        ArrayList<Repositorio> listaRepos = this.getListaRepositoriosXProyecto(cnProyecto);
        
        // Si no hay repositorios
        if (listaRepos == null || listaRepos.isEmpty())
            return;

        // Si tenemos un único repositorio y lo borramos, la lista queda vacía
        if (listaRepos.size() == 1) {
            for (Repositorio repo : listaRepos) {
                if (!repo.getTipo().equals(tipoRepositorio))
                    throw new ExcepcionLDAPNoExisteRegistro("El repositorio " + tipoRepositorio + " no existe en el proyecto " + cnProyecto);
            }
            this.deleteAtributo(this.ouProyectos, cnProyecto, "defaultRepositorio");
            this.deleteAtributo(this.ouProyectos, cnProyecto, "repositorio");

        // Si la el proyecto tenía más de un repositorio
        } else {
            String[] nuevaLista = new String[listaRepos.size()-1];
            int i = 0;
            String nuevoDefault = "";
            for (Repositorio repo : listaRepos) {
                if (!repo.getTipo().equals(tipoRepositorio)) {
                    nuevoDefault = repo.getTipo();
                    nuevaLista[i] = repo.aplanar();
                    i++;
                }
            }
            this.editAtributo(this.ouProyectos, cnProyecto, "defaultRepositorio", nuevoDefault);
            this.editAtributo(this.ouProyectos, cnProyecto, "repositorio", nuevaLista);
        }
    }



    /**
     * <p>Elimina al usuario indicado del directorio LDAP.</p>
     * <p>Eliminar un usuario implica también eliminarlo de los proyectos en los
     * que estaba dado de alta.</p>
     * @param uid UID del usuario que queremos borrar
     * @throws ExcepcionGestorLDAP Se lanza cuando no existe el usuario con el
     * UID especificado.
     */
    public void deleteUsuario(String uid) throws ExcepcionGestorLDAP {
        // Primero comprobamos que el usuario que queremos eliminar existe
        if (this.getAtributoUsuario(uid, "uid") == null)
            throw new ExcepcionLDAPNoExisteRegistro("El usuario " + uid + " no existe en el directorio LDAP");

        // Eliminamos al usuario de todos los proyectos en los que estuviera
        ArrayList<String> listaProyectos = this.getListaNombresProyectos();
        for (int i = 0; i < listaProyectos.size(); i++) {
            try {
                this.deleteUsuarioDeProyecto(uid, listaProyectos.get(i));
                this.deleteAdministradorDeProyecto(uid, listaProyectos.get(i));
            } catch (ExcepcionLDAPNoExisteRegistro e) {
                continue;
            }
        }

        // Eliminamos finalmente al usuario del servidor LDAP
        this.deleteRegistro(this.ouUsuarios, uid);
    }



    /**
     * <p>Elimina el proyecto indicado del directorio LDAP.</p>
     * @param cn CN del proyecto que queremos borrar
     * @throws ExcepcionGestorLDAP Se lanza cuando no existe el proyecto con el
     * CN especificado.
     */
    public void deleteProyecto(String cn) throws ExcepcionGestorLDAP {
        this.deleteRegistro(this.ouProyectos, cn);
    }


    
    /**
     * <p>Cierra la conexión con el servidor LDAP. Deberá llamarse a este método
     * siempre que dejemos de usar el GestorLDAP.</p>
     */
    public void cerrarConexion() {
        conexion.close();
    }




    /**
     * <p>Realiza una búsqueda del valor de un atributo de un usuario concreto
     * dentro del directorio LDAP y devuelve dicho valor.</p>
     * @param uid UID del usuario cuyo atributo queremos recuperar
     * @param nombreAtributo Nombre del atributo a recuperar
     * @return Valor que tiene el atributo que estamos buscando del registro,
     * null en caso de que no exista el ID o el atributo
     * @throws ExcepcionGestorLDAP Puede ser lanzado cuando se produce algún error
     * de acceso al directorio LDAP.
     */
    private String getAtributoUsuario(String uid, String nombreAtributo) throws ExcepcionGestorLDAP  {
        return this.getAtributo(this.ouUsuarios, uid, nombreAtributo);
    }


    /**
     * <p>Realiza una búsqueda del valor de un atributo de un proyecto concreto
     * dentro del directorio LDAP y devuelve dicho valor.</p>
     * @param cn CN del proyecto cuyo atributo queremos recuperar
     * @param nombreAtributo Nombre del atributo a recuperar
     * @return Valor que tiene el atributo que estamos buscando del registro,
     * null en caso de que no exista el ID o el atributo
     * @throws ExcepcionGestorLDAP Puede ser lanzado cuando se produce algún error
     * de acceso al directorio LDAP.
     */
    private String getAtributoProyecto(String cn, String nombreAtributo) throws ExcepcionGestorLDAP {
        return this.getAtributo(this.ouProyectos, cn, nombreAtributo);
    }



    /**
     * <p>Devuelve el número que deberemos asignar a toda nueva persona que vaya 
     * a ser creado en el directorio. Además, automáticamente incrementa el
     * marcador que tenemos en el directorio LDAP y que lleva la cuenta de por
     * dónde va el UIDNumber.</p>
     * @return UIDNumber que debemos asignar a toda nueva persona en el directorio LDAP.
     * @throws ExcepcionManejoUIDNumber Se lanzará cuando haya sucedido algún
     * problema durante la modificación del marcador en el LDAP.
     * @throws ExcepcionGestorLDAP Se lanzará cuando haya sucedido algún problema
     * durante la lectura de la entrada del marcadorUIDNumber actual.
     */
    private synchronized int getSiguienteUIDNumber() throws ExcepcionLDAPManejoIDNumber, ExcepcionGestorLDAP {
        /* UID del people que hayamos creado como marcador para llevar los IdNumber */
        final String uidMarcador = "marcadorIDNumber";

        int current = Integer.valueOf(this.getAtributo(this.ouConfiguracion, uidMarcador, "uidNumber"));
        try {
            // Incrementamos el UIDNumber del marcador
            this.editAtributo(this.ouConfiguracion, uidMarcador, "uidNumber", Integer.toString(current+1));
        } catch (Exception e) {
            throw new ExcepcionLDAPManejoIDNumber(e);
        }
        return current;
    }



    /**
     * <p>Devuelve el número que deberemos asignar a todo nuevo grupo que vaya
     * a ser creado en el directorio. Además, automáticamente incrementa el
     * marcador que tenemos en el directorio LDAP y que lleva la cuenta de por
     * dónde va el GIDNumber.</p>
     * @return GIDNumber que debemos asignar a todo nuevo grupo en el directorio LDAP.
     * @throws ExcepcionLDAPManejoIDNumber Se lanzará cuando haya sucedido algún
     * problema durante la modificación del marcador en el LDAP.
     * @throws ExcepcionGestorLDAP Se lanzará cuando haya sucedido algún problema
     * durante la lectura de la entrada del marcadorUIDNumber actual.
     */
    private synchronized int getSiguienteGIDNumber() throws ExcepcionLDAPManejoIDNumber, ExcepcionGestorLDAP {
        /* UID del people que hayamos creado como marcador para llevar los IdNumber */
        final String uidMarcador = "marcadorIDNumber";

        int current = Integer.valueOf(this.getAtributo(this.ouConfiguracion, uidMarcador, "gidNumber"));
        try {
            // Incrementamos el GIDNumber del marcador
            this.editAtributo(this.ouConfiguracion, uidMarcador, "gidNumber", Integer.toString(current+1));
        } catch (Exception e) {
            throw new ExcepcionLDAPManejoIDNumber(e);
        }
        return current;
    }




    /**
     * <p>Recibe un OU del directorio donde buscar, un identificador y un nombre
     * de atributo y devuelve su valor.</p>
     * @param ou OU del directorio donde se encuentre el registro a buscar
     * @param id Identificador del registro a buscar
     * @param nombreAtributo Nombre del atributo cuyo valor buscamos
     * @return Valor del atributo del registro que estamos buscando, o null en
     * caso de no existir dicho registro o atributo
     * @throws ExcepcionGestorLDAP Se lanzará cuando se produzca algún error de
     * acceso al directorio o cuando no se argumente un OU válido.
     */
    private String getAtributo(String ou, String id, String nombreAtributo) throws ExcepcionGestorLDAP{
        try {
            SearchResult searchResults = null;
            if (ou.equals(this.ouUsuarios))
                searchResults = conexion.search("ou=" + ouUsuarios + "," + baseDN, SearchScope.SUB, "(uid=" + id + ")", nombreAtributo);
            else if (ou.equals(this.ouProyectos))
                searchResults = conexion.search("ou=" + ouProyectos + "," + baseDN, SearchScope.SUB, "(cn=" + id + ")", nombreAtributo);
            else if (ou.equals(this.ouConfiguracion))
                searchResults = conexion.search("ou=" + ouConfiguracion + "," + baseDN, SearchScope.SUB, "(uid=" + id + ")", nombreAtributo);
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");

            if (searchResults.getEntryCount() == 1) {
                return searchResults.getSearchEntries().get(0).getAttributeValue(nombreAtributo);
            } else {
                return null;
            }
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }




    /**
     * <p>Recibe un OU del directorio donde buscar, un identificador y un nombre
     * de atributo y devuelve una lista de los valores del atributo</p>
     * @param ou OU del directorio donde se encuentre el registro a buscar
     * @param id Identificador del registro a buscar
     * @param nombreAtributo Nombre del atributo cuyo valor buscamos
     * @return Lista de valores del atributo del registro que estamos buscando, o null en
     * caso de no existir dicho registro o atributo
     * @throws ExcepcionGestorLDAP Se lanzará cuando se produzca algún error de
     * acceso al directorio o cuando no se argumente un OU válido.
     */
    private ArrayList<String> getAtributoMultiple(String ou, String id, String nombreAtributo) throws ExcepcionGestorLDAP{
        try {
            SearchResult searchResults = null;
            if (ou.equals(this.ouUsuarios))
                searchResults = conexion.search("ou=" + ouUsuarios + "," + baseDN, SearchScope.SUB, "(uid=" + id + ")");
            else if (ou.equals(this.ouProyectos))
                searchResults = conexion.search("ou=" + ouProyectos + "," + baseDN, SearchScope.SUB, "(cn=" + id + ")");
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");

            if (searchResults.getEntryCount() == 1) {
                ArrayList<String> lista = new ArrayList<String>();
                String[] aux = searchResults.getSearchEntries().get(0).getAttributeValues(nombreAtributo);
                if (aux != null)
                    lista.addAll(Arrays.asList(aux));
                return lista;
            } else {
                return null;
            }
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }





    /**
     * <p>Devuelve una lista de Strings del atributo especificado de todos los
     * registros del directorio LDAP.</p>
     * @param ou OU del directorio donde se encuentre el registro a buscar
     * @param nombreAtributo Nombre del atributo que queremos meter en la lista
     * @return Lista de Strings con el atributo de todos los registros del OU
     * del directorio LDAP. Devuelve null si el OU está vacío.
     * @throws ExcepcionGestorLDAP Cuando se ha producido excepción durante la
     * búsqueda en el directorio LDAP.
     */
    private ArrayList<String> getListaXAtributo(String ou, String nombreAtributo) throws ExcepcionGestorLDAP {
        try {
            SearchResult searchResults = null;
            if (ou.equals(this.ouUsuarios))
                searchResults = conexion.search("ou=" + ouUsuarios + "," + baseDN, SearchScope.SUB, "(uid=*)", nombreAtributo);
            else if (ou.equals(this.ouProyectos))
                searchResults = conexion.search("ou=" + ouProyectos + "," + baseDN, SearchScope.SUB, "(cn=*)", nombreAtributo);
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");
            
            if (searchResults.getEntryCount() > 0) {
                ArrayList<String> lista = new ArrayList<String>();
                for (int i = 0; i < searchResults.getEntryCount(); i++) {
                    lista.add(searchResults.getSearchEntries().get(i).getAttributeValue(nombreAtributo));
                }
                return lista;
            } else {
                return null;
            }
        } catch (LDAPSearchException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Método que recibe como parámetros un OU e ID de un registro del directorio,
     * un atributo y su valor y lo reemplaza en el directorio LDAP.</p>
     * @param ou OU del directorio donde se encuentre el registro a editar
     * @param id ID del registro que queremos modificar (GID para grupos, UID para
     * personas)
     * @param nombreAtributo Nombre que tiene el atributo a modificar en el directorio
     * @param valor Nuevo valor que queremos asignarle a dicho atributo
     * @throws ExcepcionGestorLDAP Se lanza cuando se produjo un error al reeemplazar
     * el atributo o porque no existe el registro especificado por el parámetro ID.
     */
    private void editAtributo(String ou, String id, String nombreAtributo, String valor) throws ExcepcionGestorLDAP {
        try {
            Modification mod = new Modification(ModificationType.REPLACE, nombreAtributo, valor);
            if (ou.equals(this.ouUsuarios))
                conexion.modify("uid=" + id + ",ou=" + this.ouUsuarios + "," + this.baseDN, mod);
            else if (ou.equals(this.ouProyectos))
                conexion.modify("cn=" + id + ",ou=" + this.ouProyectos + "," + this.baseDN, mod);
            else if (ou.equals(this.ouConfiguracion))
                conexion.modify("uid=" + id + ",ou=" + this.ouConfiguracion + "," + this.baseDN, mod);
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");
            
        } catch (LDAPException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }

    /**
     * <p>Método que recibe como parámetros un OU e ID de un registro del directorio,
     * un atributo y su lista de valores y lo reemplaza en el directorio LDAP. Se
     * utiliza para atributos con múltiples valores (listas).</p>
     * @param ou OU del directorio donde se encuentre el registro a editar
     * @param id ID del registro que queremos modificar (GID para grupos, UID para
     * personas)
     * @param nombreAtributo Nombre que tiene el atributo a modificar en el directorio
     * @param valores Lista de nuevos valores que queremos asignarle a dicho atributo
     * @throws ExcepcionGestorLDAP Se lanza cuando se produjo un error al reeemplazar
     * el atributo o porque no existe el registro especificado por el parámetro ID.
     */
    private void editAtributo(String ou, String id, String nombreAtributo, String[] valores) throws ExcepcionGestorLDAP {
        try {
            Modification mod = new Modification(ModificationType.REPLACE, nombreAtributo, valores);

            if (ou.equals(this.ouUsuarios))
                conexion.modify("uid=" + id + ",ou=" + this.ouUsuarios + "," + this.baseDN, mod);
            else if (ou.equals(this.ouProyectos))
                conexion.modify("cn=" + id + ",ou=" + this.ouProyectos + "," + this.baseDN, mod);
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");

        } catch (LDAPException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Método que recibe como parámetros un OU, el ID de un registro del directorio
     * y un atributo y lo elimina del directorio LDAP.</p>
     * @param ou OU del directorio donde se encuentre el registro a editar
     * @param id ID del registro que queremos modificar (GID para grupos, UID para
     * personas)
     * @param nombreAtributo Nombre que tiene el atributo a borrar en el directorio
     * @throws ExcepcionGestorLDAP Se lanza cuando se produjo un error al reeemplazar
     * el atributo o porque no existe el registro especificado por el parámetro ID.
     */
    private void deleteAtributo(String ou, String id, String nombreAtributo) throws ExcepcionGestorLDAP {
        try {
            Modification mod = new Modification(ModificationType.DELETE, nombreAtributo);

            if (ou.equals(this.ouUsuarios))
                conexion.modify("uid=" + id + ",ou=" + this.ouUsuarios + "," + this.baseDN, mod);
            else if (ou.equals(this.ouProyectos))
                conexion.modify("cn=" + id + ",ou=" + this.ouProyectos + "," + this.baseDN, mod);
            else if (ou.equals(this.ouConfiguracion))
                conexion.modify("uid=" + id + ",ou=" + this.ouConfiguracion + "," + this.baseDN, mod);
            else
                throw new ExcepcionGestorLDAP("Atributo OU incorrecto");

        } catch (LDAPException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Método que elimina un registro concreto del directorio LDAP.</p>
     * @param ou OU del directorio donde se encuentre el registro a editar
     * @param id ID del registro que queremos modificar (GID para grupos, UID para
     * personas)
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error durante
     * el borrado. Por ejemplo, cuando no existe el registro especificado.
     */
    private void deleteRegistro(String ou, String id) throws ExcepcionGestorLDAP {
        try {
            if (ou.equals(this.ouUsuarios))
                conexion.delete("uid=" + id + ",ou=" + this.ouUsuarios + "," + this.baseDN);
            else if (ou.equals(this.ouProyectos))
                conexion.delete("cn=" + id + ",ou=" + this.ouProyectos + "," + this.baseDN);
        } catch(LDAPException e) {
            throw new ExcepcionGestorLDAP(e);
        }
    }


    /**
     * <p>Comprueba que un usuario concreto existe en el directorio LDAP. Este
     * método es especialmente útil para comprobar la existencia de un usuario
     * en el directorio antes de darle de alta como administrador de proyecto.</p>
     * @param uid UID del usuario a buscar
     * @return true si el usuario existe en el directorio, false si no está
     * @throws ExcepcionGestorLDAP Se lanza cuando se produce algún error durante
     * el acceso al directorio LDAP
     */
    private boolean existeUsuario(String uid) throws ExcepcionGestorLDAP {
        return this.getListaUIdsUsuario().contains(uid);
    }
}
