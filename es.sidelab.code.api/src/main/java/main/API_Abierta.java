/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: API_Abierta.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package main;


import apoyo.Utilidades;
import config.ConfiguracionForja;
import configuradorApache.ConfiguradorApache;
import configuradorApache.GeneradorFicherosApache;
import es.sidelab.commons.commandline.CommandLine;
import es.sidelab.commons.commandline.ExecutionCommandException;
import excepciones.ExcepcionForja;
import excepciones.configuradorApache.ExcepcionGeneradorFicherosApache;
import excepciones.configuradorApache.ExcepcionConsola;
import excepciones.gestorLdap.ExcepcionGestorLDAP;
import excepciones.gestorLdap.ExcepcionLDAPAdministradorUnico;
import excepciones.gestorLdap.ExcepcionLDAPNoExisteRegistro;
import excepciones.gestorRedmine.ExcepcionGestorRedmine;
import excepciones.modeloDatos.ExcepcionProyecto;
import excepciones.modeloDatos.ExcepcionRepositorio;
import excepciones.modeloDatos.ExcepcionUsuario;
import gestorRedmine.GestorRedmine;
import gestorldap.GestorLDAP;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.exolab.castor.util.CommandLineOptions;

import modeloDatos.*;
import modeloDatos.repositorios.FactoriaRepositorios;
import modeloDatos.repositorios.FactoriaRepositorios.TipoRepositorio;
import modeloDatos.repositorios.Repositorio;

/**
 * <p>Esta clase supone la capa superior de la API, aquella que deberá de utilizar
 * el usuario de la API.</p>
 * @author Arek Klauza
 */
public class API_Abierta {
    private GestorLDAP gestorLdap;
    private ConfiguradorApache configApache;
    private GestorRedmine gestorRedmine;
    private ConfiguracionForja config;


    /**
     * <p>Crea la API de la Forja e inicializa todos los componentes necesarios 
     * para su correcto funcionamiento.</p>
     * @param ficheroConfiguracion Ruta y nombre del fichero que contiene los 
     * parámetros de configuración de la Forja
     * @throws ExcepcionForja Cuando se produce algún error durante la configuración
     * de la API de la Forja.
     */
    public API_Abierta(String ficheroConfiguracion) throws ExcepcionForja {
        try {
            this.config = new ConfiguracionForja(ficheroConfiguracion);
            this.gestorLdap = new GestorLDAP();
            this.configApache = new ConfiguradorApache();
            this.gestorRedmine = new GestorRedmine();
        } catch (ExcepcionGestorLDAP ex) {
            throw new ExcepcionForja("Error durante la creación y configuración de la API de la Forja: " + ex.getMessage());
        }
    }



    public void addProyecto(String cn, String description, String primerAdmin, TipoRepositorio tipoRepositorio, boolean esRepoPublico, String rutaRepo)
            throws ExcepcionProyecto, ExcepcionGestorLDAP, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionRepositorio, ExcepcionGestorRedmine {

        Repositorio repo = FactoriaRepositorios.crearRepositorio(tipoRepositorio, esRepoPublico, rutaRepo);
        Proyecto proyecto = new Proyecto(cn, description, primerAdmin, repo);
        this.gestorLdap.addProyecto(proyecto);
        this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), proyecto, gestorLdap.getListaUIdsUsuario());
        this.gestorRedmine.crearProyecto(proyecto);
        this.gestorRedmine.desactivarPublicidadProyectos(cn);
    }


    public void addUsuario(String uid, String nombre, String apellidos, String email, String pass) throws ExcepcionUsuario, ExcepcionGestorLDAP, NoSuchAlgorithmException, ExcepcionGestorRedmine {
        // Hay que codificar la contraseña a MD5 de LDAP
        String passMD5 = Utilidades.toMD5(pass);

        // Los emails son clave en Redmine, hay que comprobar que no haya 2 iguales
        try {
            ArrayList<String> listaEmails = this.gestorLdap.getListaEmailsUsuarios();
            if (listaEmails != null && listaEmails.contains(email))
                throw new ExcepcionUsuario("El email del usuario ya existe, no puede haber duplicados. Introduzca otro email por favor");
        } catch (ExcepcionGestorLDAP ex) {
            throw new ExcepcionUsuario("No se ha podido comprobar que el email de usuario es único: " + ex.getMessage());
        }
        Usuario user = new Usuario(uid, nombre, apellidos, email, passMD5);
        this.gestorLdap.addUsuario(user);
        this.gestorRedmine.crearUsuario(user, pass);
        
        GeneradorFicherosApache apache = new GeneradorFicherosApache();
        try {
			apache.generarFicheroJaulaUsuariosSSH(gestorLdap.getListaUIdsUsuario().toArray(new String[]{}));
		} catch (ExcepcionGeneradorFicherosApache e) {
			throw new RuntimeException(e);
		}
        
        CommandLine commandLine = new CommandLine();
        try {        	
			commandLine.syncExec("/etc/init.d/ssh restart");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ExecutionCommandException e) {
			throw new RuntimeException(e);
		}         
    }


    public void addUsuarioAProyecto(String uid, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionGestorRedmine {
        this.gestorLdap.addUsuarioAProyecto(cnProyecto, uid);
        this.gestorRedmine.addMiembroAProyecto(uid, cnProyecto);
    }


    public void addAdministradorAProyecto(String uidAdmin, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionGestorRedmine {
        this.gestorLdap.addAdminAProyecto(cnProyecto, uidAdmin);
        this.gestorRedmine.addAdministradorAProyecto(uidAdmin, cnProyecto);
    }


    public void addRepositorioAProyecto(TipoRepositorio tipoRepo, boolean esPublico, String rutaRepo, String cnProyecto) throws ExcepcionRepositorio, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionGeneradorFicherosApache, ExcepcionConsola {
        Repositorio repo = FactoriaRepositorios.crearRepositorio(tipoRepo, esPublico, rutaRepo);
        this.gestorLdap.addRepositorioAProyecto(repo, cnProyecto);
        // Cuando se añaden repositorios hay que regenerar los ficheros Apache
        this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto), gestorLdap.getListaUIdsUsuario());
    }


    public Usuario getUsuario(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        return this.gestorLdap.getUsuario(uid);
    }


    public ArrayList<String> getListaProyectosAdministrados(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        return this.gestorLdap.getListaProyectosAdministradosXUid(uid);
    }

    
    public ArrayList<String> getListaUsuariosAdministrados(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        return this.gestorLdap.getListaUsuariosAdministradosXUid(uid);
    }


    public ArrayList<String> getListaProyectosParticipados(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        return this.gestorLdap.getListaProyectosMiembroXUid(uid);
    }


    public ArrayList<String> getListaUsuariosPorProyecto(String cnProyecto) throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaUsuariosXProyecto(cnProyecto);
    }


    public ArrayList<String> getListaAdministradoresPorProyecto(String cnProyecto) throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaAdministradoresXProyecto(cnProyecto);
    }


    public Proyecto getProyecto(String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        return this.gestorLdap.getProyecto(cnProyecto);
    }


    public ArrayList<String> getListaUidsUsuarios() throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaUIdsUsuario();
    }


    public ArrayList<String> getListaNombresUsuarios() throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaNombresCompletosUsuarios();
    }


    public ArrayList<String> getListaEmailsUsuarios() throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaEmailsUsuarios();
    }


    public ArrayList<String> getListaCnProyectos() throws ExcepcionGestorLDAP {
        return this.gestorLdap.getListaNombresProyectos();
    }


    public void editUsuario(Usuario user) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorRedmine, ExcepcionGestorLDAP, ExcepcionUsuario {
        // Los emails son clave en Redmine, hay que comprobar que no haya 2 iguales
        try {
            ArrayList<String> listaEmails = this.gestorLdap.getListaEmailsUsuarios();
            if (listaEmails.contains(user.getEmail()) && !gestorLdap.getUsuario(user.getUid()).getEmail().equals(user.getEmail()))
                throw new ExcepcionUsuario("El email del usuario está siendo utilizado por otro usuario, no puede haber duplicados. Introduzca otro email por favor");
        } catch (ExcepcionGestorLDAP ex) {
            throw new ExcepcionUsuario("Fallo LDAP: No se ha podido comprobar que el email de usuario es único: " + ex.getMessage());
        }
        this.gestorLdap.editUsuario(user);
        this.gestorRedmine.editarUsuario(user);
    }


    public void editProyecto(Proyecto proyecto) throws ExcepcionGestorRedmine, ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        this.gestorLdap.editProyecto(proyecto);
        this.gestorRedmine.editarProyecto(proyecto);
    }


    public void bloquearUsuario(String uid) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP {
        this.gestorLdap.bloquearUsuario(uid);
    }


    public void desbloquearUsuario(String uid, String nuevaPass) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, NoSuchAlgorithmException {
        // Hay que codificar la contraseña a MD5 de LDAP
        String nuevaPassMD5 = Utilidades.toMD5(nuevaPass);
        this.gestorLdap.desbloquearUsuario(uid, nuevaPassMD5);
    }


    public void deleteUsuarioDeProyecto(String uid, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionLDAPAdministradorUnico, ExcepcionGestorLDAP, ExcepcionGestorRedmine {
        this.gestorLdap.deleteUsuarioDeProyecto(uid, cnProyecto);
        // Cuando borramos a un usuario que también es administrador hay que regenerar los ficheros Apache
        this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto), gestorLdap.getListaUIdsUsuario());
        this.gestorRedmine.deleteMiembroDeProyecto(uid, cnProyecto);
    }


    public void deleteAdministradorDeProyecto(String uid, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionGestorLDAP, ExcepcionGestorRedmine {
        this.gestorLdap.deleteAdministradorDeProyecto(uid, cnProyecto);
        // Cuando borramos a un administrador hay que regenerar los ficheros Apache
        this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto), gestorLdap.getListaUIdsUsuario());
        this.gestorRedmine.deleteAdministradorDeProyecto(uid, cnProyecto);
    }

    public void deleteRepositorioDeProyecto(String tipoRepo, String cnProyecto) throws ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionGestorLDAP, ExcepcionConsola, ExcepcionGeneradorFicherosApache {
        // Borramos la carpeta del repositorio
        Proyecto proyecto = gestorLdap.getProyecto(cnProyecto);
        ArrayList<Repositorio> repos = proyecto.getRepositorios();
        for (Repositorio repo : repos)
            if (repo.getTipo().equalsIgnoreCase(tipoRepo))
                if (Utilidades.existeCarpeta(repo.getRuta(), proyecto.getCn()))
                    repo.borrarRepositorio(proyecto.getCn());
        // Borramos del directorio LDAP
        this.gestorLdap.deleteRepositorioDeProyecto(tipoRepo, cnProyecto);
        // Cuando borramos un repositorio hay que regenerar los ficheros Apache
        this.configApache.configurarProyecto(gestorLdap.getListaProyectos(), gestorLdap.getProyecto(cnProyecto), gestorLdap.getListaUIdsUsuario());
    }


    public void deleteProyecto(String cnProyecto) 
            throws ExcepcionLDAPNoExisteRegistro, ExcepcionGestorLDAP, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionGestorRedmine {
        Proyecto proyecto = this.gestorLdap.getProyecto(cnProyecto);
        this.gestorLdap.deleteProyecto(cnProyecto);
        this.configApache.borrarProyecto(gestorLdap.getListaProyectos(), proyecto);
        this.gestorRedmine.borrarProyecto(proyecto);
    }


    public void deleteUsuario(String uid) 
            throws ExcepcionLDAPNoExisteRegistro, ExcepcionLDAPAdministradorUnico, ExcepcionConsola, ExcepcionGeneradorFicherosApache, ExcepcionGestorLDAP, ExcepcionGestorRedmine {
        // Borra al usuario de todos los proyectos que administra y en los que participa
        for (String proyecto : this.gestorLdap.getListaProyectosMiembroXUid(uid)) {
            this.deleteUsuarioDeProyecto(uid, proyecto);
        }
        this.gestorLdap.deleteUsuario(uid);
        this.gestorRedmine.deleteUsuario(uid);
    }



    /**
     * <p>Este método se debe ejecutar para crear el grupo de superadministradores
     * y el Primer administrador de la Forja.</p>
     * @param groupSuperadmins
     * @param uidSuperadmin
     * @param passSuperadmin
     */
    public void inicializaForja(String uidSuperadmin, String passSuperadmin) 
            throws ExcepcionUsuario, NoSuchAlgorithmException, ExcepcionGestorRedmine, ExcepcionProyecto, ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionRepositorio, ExcepcionGestorLDAP {
        this.addUsuario(uidSuperadmin, "Usuario Superadministrador", "Permanente", "info@info.com", passSuperadmin);
        this.addProyecto(ConfiguracionForja.groupSuperadmin, "Proyecto que agrupa a los superadministradores de la Forja", uidSuperadmin, null, false, null);
    }
    
}