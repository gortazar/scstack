package main;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: API_SeguraTest.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


import java.util.ArrayList;
import modeloDatos.Proyecto;
import modeloDatos.Usuario;
import modeloDatos.repositorios.FactoriaRepositorios.TipoRepositorio;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import apoyo.Utilidades;
import config.ConfiguracionForja;
import excepciones.api.ExcepcionLogin;
import excepciones.gestorLdap.ExcepcionLDAPAdministradorUnico;
import excepciones.gestorLdap.ExcepcionLDAPNoExisteRegistro;
import excepciones.gestorLdap.ExcepcionLDAPYaExisteEntrada;
import excepciones.modeloDatos.ExcepcionProyecto;
import excepciones.modeloDatos.ExcepcionRepositorio;
import excepciones.modeloDatos.ExcepcionUsuario;
import java.util.Collections;
import modeloDatos.repositorios.FactoriaRepositorios;
import modeloDatos.repositorios.Repositorio;


/**
 * Esta clase de pruebas es la encargada de probar el correcto funcionamiento de
 * los distintos métodos ofrecidos por la API de la Forja.
 * @author Arek Klauza
 */
public class API_SeguraTest {
    private API_Segura api;
    private Usuario miUser;
    private Proyecto miProyecto;


    /*    PARÁMETROS DE CONFIGURACIÓN    */
    private String uidSuperAdmin = "super";
    private String passSuperAdmin = "super";

    /* Pequeña guía de uso de las pruebas:
     *  - Primera vez que lancemos: construirEscenario = true, resto false
     *  - Cuando se pervierta el escenario y necesitemos borrarlo: borrarEscenario = true
     *    resto a false.
     *  - Cuando queramos que cada prueba se regenere el escenario:
     *    autoRegenerarEscenario = true, resto false
     *  - Cuando realicemos muchas pruebas seguidas: todo a false
     */
    private boolean construirEscenario = false;
    private boolean borrarEscenario = false;
    private boolean autoRegenerarEscenario = false;






    @Before
    public void setUp() throws Exception {
        api = new API_Segura();
        this.miUser = new Usuario("testuser", "Test", "User", "test@user.com", Utilidades.toMD5("testPass"));
        this.miProyecto = new Proyecto("proyectoTest", "Un proyecto de prueba", uidSuperAdmin, FactoriaRepositorios.crearRepositorio(TipoRepositorio.SVN, true, null));
    }



    /**
     * Automáticamente borra y vuelve a generar el escenario de pruebas
     */
     @Test
     public void autoRegenerarEscenario() throws Exception {
         if (autoRegenerarEscenario) {
             this.construirEscenario = true;
             this.borrarEscenario = true;

             this.limpiarEscenarioTrasTest();
             this.levantarEscenarioPruebas();

             this.construirEscenario = false;
             this.borrarEscenario = false;
         }
     }



    /**
     * Levanta todo el escenario de pruebas preestablecido
     */
    @Test
    public void levantarEscenarioPruebas() throws Exception {
        if (this.construirEscenario) {
            System.out.println("# Levantando Escenario");
            String uid = uidSuperAdmin;
            String pass = passSuperAdmin;
            // Creación Usuarios
            api.crearUsuario("arek", "Arek", "Klauza", "arek.klauza@gmail.com", "arek", uid, pass);
            api.crearUsuario("patxi", "Francisco", "Gortázar", "patxi@gmail.com", "patxi", uid, pass);
            api.crearUsuario("mica", "Micael", "Gallego", "mica@gmail.com", "mica", uid, pass);
            api.crearUsuario("adolfo", "Adolfo", "Panizo", "adolfo@gmail.com", "adolfo", uid, pass);
            api.crearUsuario("alvaro", "Álvaro", "Martínez", "amartinez@gmail.com", "alvaro", uid, pass);
            api.crearUsuario("helena", "Helena", "Cebrián Benito", "helenacb@gmail.com", "helena", uid, pass);
            api.crearUsuario("visitante", "Visitante", "Sin privilegios", "visitante@gmail.com", "visitante", uid, pass);
            api.crearUsuario("alumnos", "Alumno", "Genérico", "alumnos@urjc.es", "alumno", uid, pass);
            System.out.println("Usuarios creados");

            // Creación y configuración de Proyectos
            api.crearProyecto("PFCArek", "PFC de Arek", "arek", TipoRepositorio.SVN, false, null, uid, pass);
            api.addAdminAProyecto("patxi", "PFCArek", uid, pass);
            api.addAdminAProyecto("mica", "PFCArek", uid, pass);
            api.addRepositorioAProyecto("GIT", false, null, "PFCArek", uid, pass);

            api.crearProyecto("practicaISI", "Práctica de Ingeniería Software", "arek", TipoRepositorio.SVN, true, null, uid, pass);
            api.addUsuarioAProyecto("alvaro", "practicaISI", uid, pass);
            api.addUsuarioAProyecto("adolfo", "practicaISI", uid, pass);
            api.addUsuarioAProyecto("helena", "practicaISI", uid, pass);

            api.crearProyecto("practicaAS", "Práctica de AS", "adolfo", TipoRepositorio.GIT, true, null, uid, pass);
            api.addUsuarioAProyecto("arek", "practicaAS", uid, pass);

            api.crearProyecto("practicaASAmbu", "Práctica de AS de Ambulancias", "alvaro", TipoRepositorio.GIT, false, null, uid, pass);
            api.addAdminAProyecto("helena", "practicaASAmbu", uid, pass);

            api.crearProyecto("asignatura", "Asignatura sin repositorios", "patxi", null, false, null, uid, pass);
            api.addUsuarioAProyecto("alumnos", "asignatura", uid, pass);

            api.crearProyecto("solitario", "Proyecto solitario", "arek", null, true, null, uid, pass);
            System.out.println("Proyectos creados");
        }
    }








    /***************************************************************************
     *                          TEST CON USUARIOS
     **************************************************************************/

    /**
     * Crea un nuevo usuario válido, consulta con el recuperado que coincide y
     * posteriormente lo elimina.
     */
    @Test
    public void testCrear_Eliminar_Usuario() throws Exception {
        System.out.println("# Crear_Eliminar_Usuario");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        api.crearUsuario(miUser.getUid(), miUser.getNombre(), miUser.getApellidos(), miUser.getEmail(), "testPass", uid, pass);
        Usuario expResult = this.miUser;
        Usuario result = api.getDatosUsuario(miUser.getUid(), uid, pass);
        assertEquals(expResult, result);
        // Elimina
        api.eliminarUsuario(miUser.getUid(), uid, pass);
        try {
            api.getDatosUsuario(miUser.getUid(), uid, pass);
            fail("El usuario no ha sido borrado correctamente");
        } catch (ExcepcionLDAPNoExisteRegistro e) {
            // Aquí debe llegar para indicar que ha sido eliminado
        }
    }


    /**
     * Intenta crear un usuario con sintaxis de UID inválida
     */
    @Test
    public void testCrearUsuarioUIDInvalido() throws Exception {
        System.out.println("# CrearUsuarioConUidInválido");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.crearUsuario("uidInválidoConEÑES", miUser.getNombre(), miUser.getApellidos(), miUser.getEmail(), "testPass", uid, pass);
            fail("El usuario ha podido ser creado");
        } catch (ExcepcionUsuario e) {
            // Aquí debe llegar para indicar que no se pudo crear el usuario
        }
    }


    /**
     * Intenta crear dos veces al mismo usuario finalmente elimina el que sí pudo
     * crearse.
     */
    @Test
    public void testCrearUsuarioYaExistente() throws Exception {
        System.out.println("# CrearUsuarioYaExistente");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Crea al original
        api.crearUsuario(miUser.getUid(), miUser.getNombre(), miUser.getApellidos(), miUser.getEmail(), "testPass", uid, pass);
        try {
            // Intenta crear un clon
            api.crearUsuario(miUser.getUid(), miUser.getNombre(), miUser.getApellidos(), "hello@kitty.com", "testPass", uid, pass);
            fail("El clon ha podido ser creado");
        } catch (ExcepcionLDAPYaExisteEntrada e) {
            try {
                // Aquí debe llegar para indicar que no se pudo crear el clon
                // Reestablece el estado
                api.eliminarUsuario(miUser.getUid(), uid, pass);
                api.getDatosUsuario(miUser.getUid(), uid, pass);
                fail("El usuario no ha podido ser eliminado");
            } catch (ExcepcionLDAPNoExisteRegistro ex) {
                // Aquí debe llegar para indicar que ha sido eliminado
            }
        }
    }


    /**
     * Edita uno de los usuarios existentes, comprueba que lo hace bien y
     * finalmente reestablece su estado original.
     */
    @Test
    public void testEditarUsuario() throws Exception {
        System.out.println("# EditarUsuario");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;
        String uidConsulta = "alvaro";

        // Recupera a uno para editarlo
        Usuario user = api.getDatosUsuario(uidConsulta, uid, pass);
        // Almacena la info antes de modificarla
        String nombre = user.getNombre();
        String apellidos = user.getApellidos();
        // Edita
        user.setNombre("UsuarioModificado");
        user.setApellidos("Modificado");
        api.editarDatosUsuario(user, uid, pass);
        // Recupera el modificado
        Usuario result = api.getDatosUsuario(user.getUid(), uid, pass);
        assertEquals(user, result);
        // Reestablece los datos del usuario de prueba
        user.setNombre(nombre);
        user.setApellidos(apellidos);
        api.editarDatosUsuario(user, uid, pass);
        result = api.getDatosUsuario(user.getUid(), uid, pass);
        assertEquals(user, result);
    }


    /**
     * Comprueba que el listado de miembros, la adición y eliminado de miembros
     * de un proyecto funciona correctamente.
     */
    @Test
    public void testListado_Adicion_Eliminado_MiembrosProyecto() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_MiembrosProyecto");
        String uidConsulta = "arek";
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que el usuario aparece en los proyectos que debe
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        expResult.add("PFCArek");
        expResult.add("practicaISI");
        expResult.add("practicaAS");
        expResult.add("solitario");
        ArrayList result = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result);
        assertEquals(expResult, result);

        // Ahora le borra de un proyecto y vuelve a consultar la lista
        api.eliminarUsuarioDeProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.remove(cnProyecto);
        ArrayList result2 = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Finalmente reestablece el escenario: le vuelve a añadir y consulta
        api.addAdminAProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.add(cnProyecto);
        ArrayList result3 = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result3);
        assertEquals(expResult, result3);
    }


    /**
     * A partir de un miembro que no pertenece a ningún proyecto, primero lista
     * sus proyectos participados (vacío) y luego le agrega y quita de un proyecto.
     */
    @Test
    public void testListado_Adicion_Eliminado_MiembroDeNingunProyecto() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_MiembrosProyectoVacio");
        String uidConsulta = "visitante";
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que el usuario aparece en los proyectos que debe
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        ArrayList result = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        assertEquals(expResult, result);

        // Ahora le añade a un proyecto y vuelve a consultar la lista
        api.addUsuarioAProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.add(cnProyecto);
        ArrayList result2 = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Finalmente reestablece el escenario: le borra y consulta
        api.eliminarUsuarioDeProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.remove(cnProyecto);
        ArrayList result3 = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        assertEquals(expResult, result3);
    }


    /**
     * Trata borrar a un miembro único de un proyecto, cosa que está prohibida
     * y debe saltar excepción, porque este miembro único es a la vez administrador
     * único del proyecto
     */
    @Test
    public void testBorradoMiembroUnico() throws Exception {
        System.out.println("# BorradoMiembroUnico");
        String uidConsulta = "arek";
        String cnProyecto = "solitario";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.eliminarUsuarioDeProyecto(uidConsulta, cnProyecto, uid, pass);
            fail("El miembro único se ha podido borrar");
        } catch (ExcepcionLDAPAdministradorUnico e) {
            // Si llega aquí indica que no ha dejado borrarlo, por tanto OK
        }
    }


    /**
     * Comprueba que el listado, la adición y eliminado de administradores
     * de un proyecto funciona correctamente.
     */
    @Test
    public void testListado_Adicion_Eliminado_AdminsProyecto() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_AdminsProyecto");
        String uidConsulta = "patxi";
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que el administrador aparece en los proyectos que debe
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        expResult.add("PFCArek");
        expResult.add("asignatura");
        ArrayList result = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result);
        assertEquals(expResult, result);

        // Ahora le borra de un proyecto y vuelve a consultar la lista
        api.eliminarAdminDeProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.remove(cnProyecto);
        ArrayList result2 = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Finalmente reestablece el escenario: le vuelve a añadir y consulta
        api.addAdminAProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.add(cnProyecto);
        ArrayList result3 = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result3);
        assertEquals(expResult, result3);
    }



    /**
     * A partir de un miembro que no pertenece a ningún proyecto, primero lista
     * sus proyectos administrados (vacío) y luego le agrega y quita de un proyecto.
     */
    @Test
    public void testListado_Adicion_Eliminado_AdminsDeNingunProyecto() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_AdminsProyectoVacio");
        String uidConsulta = "alumnos";
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que el usuario aparece en los proyectos que debe
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        ArrayList result = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        assertEquals(expResult, result);

        // Ahora le añade a un proyecto y vuelve a consultar la lista
        api.addAdminAProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.add(cnProyecto);
        ArrayList result2 = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Finalmente reestablece el escenario: le borra como admin y consulta
        api.eliminarAdminDeProyecto(uidConsulta, cnProyecto, uid, pass);
        expResult.remove(cnProyecto);
        ArrayList result3 = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        assertEquals(expResult, result3);
    }


    /**
     * Trata borrar a un administrador único de un proyecto, cosa que está prohibida
     * y debe saltar excepción.
     */
    @Test
    public void testBorradoAdministradorUnico() throws Exception {
        System.out.println("# BorradoAdministradorUnico");
        String uidConsulta = "adolfo";
        String cnProyecto = "practicaAS";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.eliminarAdminDeProyecto(uidConsulta, cnProyecto, uid, pass);
            fail("El administrador único se ha podido borrar");
        } catch (ExcepcionLDAPAdministradorUnico e) {
            // Si llega aquí indica que no ha dejado borrarlo, por tanto OK
        }
    }


    /**
     * Trata borrar a un administrador que no existe en el proyecto
     */
    @Test
    public void testBorradoAdministradorInexistente() throws Exception {
        System.out.println("# BorradoAdministradorInexistente");
        String uidConsulta = "arek";
        String cnProyecto = "practicaAS";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.eliminarAdminDeProyecto(uidConsulta, cnProyecto, uid, pass);
            fail("El administrador único se ha podido borrar");
        } catch (ExcepcionLDAPNoExisteRegistro e) {
            // Si llega aquí indica que no ha dejado borrarlo, por tanto OK
        }
    }


    /**
     * Trata de hacer un borrado de miembro de un usuario que también es admin
     * único de un proyecto, lo cual está prohibido.
     */
    @Test
    public void testBorrarMiembroAdministradorUnico() throws Exception {
        System.out.println("# BorrarMiembroAdministradorUnico");
        String uidConsulta = "patxi";
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.eliminarUsuarioDeProyecto(uidConsulta, cnProyecto, uid, pass);
            fail("El miembro y administrador único se ha podido borrar");
        } catch (ExcepcionLDAPAdministradorUnico e) {
            // Si llega aquí indica que no ha dejado borrarlo, por tanto OK
        }
    }


    /**
     * Comprueba si al borrar a un usuario que también es admin de un proyecto
     * borra también al miembro de dicho proyecto. También comprueba que al
     * añadirlo de nuevo como admin se añade a su vez como miembro.
     */
    @Test
    public void testBorrar_Add_UsuarioAdminProyecto() throws Exception {
        System.out.println("# Borrar_Add_UsuarioAdmin_Proyecto");
        String uidConsulta = "alvaro";
        String cnProyecto = "practicaASAmbu";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        api.eliminarUsuarioDeProyecto(uidConsulta, cnProyecto, uid, pass);
        ArrayList<String> administradosHay = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        ArrayList<String> participadosHay = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        ArrayList<String> participadosDebe = new ArrayList<String>();
        participadosDebe.add("practicaISI");
        ArrayList<String> administradosDebe = new ArrayList<String>();
        assertEquals(administradosHay, administradosDebe);
        assertEquals(participadosHay, participadosDebe);

        // Reestablecer
        api.addAdminAProyecto(uidConsulta, cnProyecto, uid, pass);
        administradosDebe.add(cnProyecto);
        participadosDebe.add(cnProyecto);
        administradosHay = api.getListadoProyectosAdministrados(uidConsulta, uid, pass);
        participadosHay = api.getListadoProyectosParticipados(uidConsulta, uid, pass);
        assertEquals(administradosHay, administradosDebe);
        assertEquals(participadosHay, participadosDebe);
    }


    /**
     * Recupera el listado de todos los UID de usuario de la Forja y lo contrasta con
     * lo que debería haber según el escenario.
     * Luego crea un nuevo usuario y vuelve a consultar
     * Finalmente lo borra para reestablecer el escenario.
     */
    @Test
    public void testGetListadoUidsUsuarios() throws Exception {
        System.out.println("# GetListadoUidsUsuarios");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        ArrayList<String> usuariosHay = api.getListadoUidsUsuarios(uid, pass);
        ArrayList<String> usuariosDebeHaber = new ArrayList<String>();
        usuariosDebeHaber.add(uidSuperAdmin);
        usuariosDebeHaber.add("arek");
        usuariosDebeHaber.add("patxi");
        usuariosDebeHaber.add("mica");
        usuariosDebeHaber.add("alvaro");
        usuariosDebeHaber.add("adolfo");
        usuariosDebeHaber.add("helena");
        usuariosDebeHaber.add("visitante");
        usuariosDebeHaber.add("alumnos");
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);

        // Añade uno nuevo
        api.crearUsuario(miUser.getUid(), miUser.getNombre(), miUser.getApellidos(), miUser.getEmail(), "testPass", uid, pass);
        usuariosHay = api.getListadoUidsUsuarios(uid, pass);
        usuariosDebeHaber.add(miUser.getUid());
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);

        // Reestablece el escenario
        api.eliminarUsuario(miUser.getUid(), uid, pass);
        usuariosHay = api.getListadoUidsUsuarios(uid, pass);
        usuariosDebeHaber.remove(miUser.getUid());
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);
    }


    /**
     * Recupera el listado de todos los nombres y apellidos de usuarios de la
     * Forja y lo contrasta con lo que debería haber según el escenario.
     * Luego crea un nuevo usuario y vuelve a consultar
     * Finalmente lo borra para reestablecer el escenario.
     */
    @Test
    public void testGetListadoNombresCompletosUsuarios() throws Exception {
        System.out.println("# GetListadoNombresCompletosUsuarios");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        ArrayList<String> usuariosHay = api.getListadoNombresUsuarios(uid, pass);
        ArrayList<String> usuariosDebeHaber = new ArrayList<String>();
        usuariosDebeHaber.add("Permanente, Usuario Superadministrador");
        usuariosDebeHaber.add("Klauza, Arek");
        usuariosDebeHaber.add("Gortázar, Francisco");
        usuariosDebeHaber.add("Gallego, Micael");
        usuariosDebeHaber.add("Martínez, Álvaro");
        usuariosDebeHaber.add("Panizo, Adolfo");
        usuariosDebeHaber.add("Cebrián Benito, Helena");
        usuariosDebeHaber.add("Sin privilegios, Visitante");
        usuariosDebeHaber.add("Genérico, Alumno");
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);

        // Añade uno nuevo
        api.crearUsuario(miUser.getUid(), miUser.getNombre(), miUser.getApellidos(), miUser.getEmail(), "testPass", uid, pass);
        usuariosHay = api.getListadoNombresUsuarios(uid, pass);
        usuariosDebeHaber.add(miUser.getApellidos() + ", " + miUser.getNombre());
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);

        // Reestablece el escenario
        api.eliminarUsuario(miUser.getUid(), uid, pass);
        usuariosHay = api.getListadoNombresUsuarios(uid, pass);
        usuariosDebeHaber.remove(miUser.getApellidos() + ", " + miUser.getNombre());
        Collections.sort(usuariosHay);
        Collections.sort(usuariosDebeHaber);
        assertEquals(usuariosHay, usuariosDebeHaber);
    }


    /**
     * Prueba a bloquear un usuario determinado (le borra su contraseña),
     * consulta si se ha hecho correctamente y reestablece la contraseña
     */
    @Test
    public void testBloquear_DesbloquearUsuario() throws Exception {
        System.out.println("# Bloquear_DesbloquearUsuario");
        String uidUsuario = "arek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero prueba a bloquear al usuario (le deja sin campo contraseña)
        String passMD5 = api.getDatosUsuario(uidUsuario, uid, pass).getPassMD5();
        api.bloquearUsuario(uidUsuario, uid, pass);
        String passRecup = api.getDatosUsuario(uidUsuario, uid, pass).getPassMD5();
        assertNull(passRecup);

        // Ahora reestablecemos la contraseña del usuario
        api.desbloquearUsuario(uidUsuario, "arek", uid, pass);
        passRecup = api.getDatosUsuario(uidUsuario, uid, pass).getPassMD5();
        assertEquals(passRecup, passMD5);
    }













    /***************************************************************************
     *                          TEST DE PROYECTOS
     **************************************************************************/

    /**
     * Crea un nuevo proyecto en la Forja, comprueba que se han creado
     * correctamente en el directorio LDAP, que se han creado las carpetas
     * pública y privada y el repositorio correspondiente.
     * Luego lo elimina y vuelve a realizar todas las comprobaciones para ver si
     * se ha hecho correctamente.
     */
    @Test
    public void testCrear_EliminarProyecto() throws Exception {
        System.out.println("# Crear_EliminarProyecto");
        String cn = miProyecto.getCn();
        String description = miProyecto.getDescription();
        String primerAdmin = miProyecto.getPrimerAdmin();
        String passAdmin = passSuperAdmin;
        TipoRepositorio tipoRepositorio = TipoRepositorio.SVN;
        boolean esRepoPublico = miProyecto.getPrimerRepositorio().esPublico();
        String rutaRepo = miProyecto.getPrimerRepositorio().getRuta();
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Crea un nuevo proyecto de test
        api.crearProyecto(cn, description, primerAdmin, tipoRepositorio, esRepoPublico, rutaRepo, uid, pass);
        // Comprueba que se ha creado todo correctamente
        Proyecto proyecto = api.getDatosProyecto(miProyecto.getCn(), uid, pass);
        assertEquals(miProyecto, proyecto);
        assertTrue(Utilidades.existeCarpeta(ConfiguracionForja.pathCarpetaPrivadaApache, cn));
        assertTrue(Utilidades.existeCarpeta(ConfiguracionForja.pathCarpetaPublicaApache, cn));
        assertTrue(Utilidades.existeCarpeta(miProyecto.getPrimerRepositorio().getRuta(), cn));

        // Elimina el proyecto
        api.eliminarProyecto(miProyecto.getCn(), uid, pass);
        // Comprueba que se ha eliminado todo correctamente
        assertFalse(Utilidades.existeCarpeta(ConfiguracionForja.pathCarpetaPrivadaApache, cn));
        assertFalse(Utilidades.existeCarpeta(ConfiguracionForja.pathCarpetaPublicaApache, cn));
        assertFalse(Utilidades.existeCarpeta(miProyecto.getPrimerRepositorio().getRuta(), cn));
        try {
            api.getDatosProyecto(miProyecto.getCn(), uid, pass);
            fail("El proyecto no ha sido borrado correctamente");
        } catch (ExcepcionLDAPNoExisteRegistro e) {
            // Aquí debe llegar para indicar que ha sido eliminado
        }
    }


    /**
     * Intenta crear un proyecto ya existente
     */
    @Test
    public void testCrearProyectoYaExistente() throws Exception {
        System.out.println("# CrearProyectoYaExistente");
        String cn = miProyecto.getCn();
        String description = miProyecto.getDescription();
        String primerAdmin = miProyecto.getPrimerAdmin();
        String passAdmin = passSuperAdmin;
        TipoRepositorio tipoRepositorio = TipoRepositorio.SVN;
        boolean esRepoPublico = miProyecto.getPrimerRepositorio().esPublico();
        String rutaRepo = miProyecto.getPrimerRepositorio().getRuta();
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Crea un nuevo proyecto de test
        api.crearProyecto(cn, description, primerAdmin, tipoRepositorio, esRepoPublico, rutaRepo, uid, pass);

        // Lo intenta volver a crear
        try {
            api.crearProyecto(cn, description, primerAdmin, tipoRepositorio, esRepoPublico, rutaRepo, uid, pass);
            fail("Se ha podido crear dos veces el mismo proyecto");
        } catch (ExcepcionLDAPYaExisteEntrada e) {
            // Si ha llegado aquí es que no se ha creado el duplicado, por tanto ahora hay que borrar el que sí se creó
            api.eliminarProyecto(miProyecto.getCn(), uid, pass);
            try {
                api.getDatosProyecto(miProyecto.getCn(), uid, pass);
                fail("El proyecto no ha podido borrarse correctamente");
            } catch (ExcepcionLDAPNoExisteRegistro ex) {
                // Aquí debe llegar para indicar que ha sido eliminado
            }
        }
    }


    /**
     * Intenta crear un proyecto con una sintaxis incorrecta
     */
    @Test
    public void testCrearProyectoSintaxisIncorrecta() throws Exception {
        System.out.println("# CrearProyectoSintaxisIncorrecta");
        String cn = "tíldes_eñes";
        String description = miProyecto.getDescription();
        String primerAdmin = miProyecto.getPrimerAdmin();
        String passAdmin = passSuperAdmin;
        TipoRepositorio tipoRepositorio = TipoRepositorio.SVN;
        boolean esRepoPublico = miProyecto.getPrimerRepositorio().esPublico();
        String rutaRepo = miProyecto.getPrimerRepositorio().getRuta();
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Crea un nuevo proyecto de test
        try {
            api.crearProyecto(cn, description, primerAdmin, tipoRepositorio, esRepoPublico, rutaRepo, uid, pass);
            fail("El proyecto ha podido ser creado con sintaxis incorrecta");
        } catch (ExcepcionProyecto e) {
            // Si llega aquí indica que no se ha podido crear
        }

    }


    /**
     * Prueba que el listado de usuarios en un proyecto se hace correctamente
     * listando el inicial, añadiendo y quitando usuarios y volviendo a listarles
     */
    @Test
    public void testGetListadoUsuariosPorProyecto() throws Exception {
        System.out.println("# ListadoUsuariosPorProyecto");
        String cnProyecto = "practicaISI";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Probamos la lista original
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add("arek");
        expResult.add("alvaro");
        expResult.add("adolfo");
        expResult.add("helena");
        ArrayList<String> result = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result);
        Collections.sort(expResult);
        assertEquals(expResult, result);

        // Añadimos un nuevo usuario
        expResult.add("patxi");
        api.addUsuarioAProyecto("patxi", cnProyecto, uid, pass);
        ArrayList<String> result2 = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result2);
        Collections.sort(expResult);
        assertEquals(expResult, result2);

        // Reestablecemos el escenario, borrando al recién añadido
        expResult.remove("patxi");
        api.eliminarUsuarioDeProyecto("patxi", cnProyecto, uid, pass);
        ArrayList<String> result3 = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result3);
        Collections.sort(expResult);
        assertEquals(expResult, result3);
    }


    /**
     * Prueba que el listado de administradores en un proyecto se hace correctamente
     * listando el inicial, añadiendo y quitando admins y volviendo a listarles
     */
    @Test
    public void testGetListadoAdminsPorProyecto() throws Exception {
        System.out.println("# ListadoAdminsPorProyecto");
        String cnProyecto = "practicaASAmbu";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Probamos la lista original
        ArrayList<String> expResult = new ArrayList<String>();
        expResult.add("alvaro");
        expResult.add("helena");
        ArrayList<String> result = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result);
        Collections.sort(expResult);
        assertEquals(expResult, result);

        // Añadimos un nuevo usuario
        expResult.add("patxi");
        api.addAdminAProyecto("patxi", cnProyecto, uid, pass);
        ArrayList<String> result2 = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result2);
        Collections.sort(expResult);
        assertEquals(expResult, result2);

        // Reestablecemos el escenario, borrando al recién añadido
        expResult.remove("patxi");
        api.eliminarAdminDeProyecto("patxi", cnProyecto, uid, pass);
        ArrayList<String> result3 = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(result3);
        Collections.sort(expResult);
        assertEquals(expResult, result3);
    }


    /**
     * Prueba a recuperar los datos de un proyecto, los edita, compara si lo ha
     * hecho bien, luego revierte dichas modificaciones y vuelve a comparar
     */
    @Test
    public void testEditarDatosProyecto() throws Exception {
        System.out.println("# EditarDatosProyecto");
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Editamos el proyecto
        Proyecto proyecto = api.getDatosProyecto(cnProyecto, uid, pass);
        String originalDesc = proyecto.getDescription();
        String originalRepo = proyecto.getDefaultRepositorio();
        proyecto.setDefaultRepositorio("GIT");
        proyecto.setDescription("Otra descripción");
        api.editarDatosProyecto(proyecto, uid, pass);
        Proyecto proyectoRecup = api.getDatosProyecto(cnProyecto, uid, pass);
        assertEquals(proyectoRecup,proyecto);

        // Reestablecemos los datos del proyecto
        proyecto.setDefaultRepositorio(originalRepo);
        proyecto.setDescription(originalDesc);
        api.editarDatosProyecto(proyecto, uid, pass);
        proyectoRecup = api.getDatosProyecto(cnProyecto, uid, pass);
        assertEquals(proyectoRecup,proyecto);
    }


    /**
     * Prueba a editar el defaultRepositorio de un proyecto que no tiene
     * repositorios.
     */
    @Test
    public void testEditarDatosProyectoSinRepositorio() throws Exception {
        System.out.println("# EditarDatosProyectoSinRepositorio");
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Editamos el proyecto
        Proyecto proyecto = api.getDatosProyecto(cnProyecto, uid, pass);
        String originalDesc = proyecto.getDescription();
        String originalRepo = proyecto.getDefaultRepositorio();
        proyecto.setDefaultRepositorio("GIT");
        proyecto.setDescription("Otra descripción");
        api.editarDatosProyecto(proyecto, uid, pass);
        Proyecto proyectoRecup = api.getDatosProyecto(cnProyecto, uid, pass);
        // A pesar de haber mandado un proyecto con defaultRepositorio = GIT, no
        // debe cambiarlo, porque es un proyecto sin repositorios, debe seguir a null
        proyecto.setDefaultRepositorio(null);
        assertEquals(proyectoRecup,proyecto);

        // Reestablecemos los datos del proyecto
        proyecto.setDescription(originalDesc);
        api.editarDatosProyecto(proyecto, uid, pass);
        proyectoRecup = api.getDatosProyecto(cnProyecto, uid, pass);
        assertEquals(proyectoRecup,proyecto);
    }


    /**
     * Comprueba que la lista de proyecto que debe haber en la Forja según el
     * escenario coincide con lo que realmente hay
     */
    @Test
    public void testGetListadoProyectos() throws Exception {
        System.out.println("# GetListadoProyectos");
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Comprueba la lista original
        ArrayList<String> proyectosHay = api.getListadoProyectos(uid, pass);
        ArrayList<String> proyectosDebeHaber = new ArrayList<String>();
        proyectosDebeHaber.add("superadmins");
        proyectosDebeHaber.add("PFCArek");
        proyectosDebeHaber.add("practicaISI");
        proyectosDebeHaber.add("practicaAS");
        proyectosDebeHaber.add("practicaASAmbu");
        proyectosDebeHaber.add("asignatura");
        proyectosDebeHaber.add("solitario");
        Collections.sort(proyectosDebeHaber);
        Collections.sort(proyectosHay);
        assertEquals(proyectosDebeHaber, proyectosHay);

        // Añade uno más y vuelve a comprobar
        proyectosDebeHaber.add(miProyecto.getCn());
        api.crearProyecto(miProyecto.getCn(), miProyecto.getDescription(), miProyecto.getPrimerAdmin(), null, true, null, uid, pass);
        proyectosHay = api.getListadoProyectos(uid, pass);
        Collections.sort(proyectosDebeHaber);
        Collections.sort(proyectosHay);
        assertEquals(proyectosDebeHaber, proyectosHay);

        // Reestablece el escenario
        proyectosDebeHaber.remove(miProyecto.getCn());
        api.eliminarProyecto(miProyecto.getCn(), uid, pass);
        proyectosHay = api.getListadoProyectos(uid, pass);
        Collections.sort(proyectosDebeHaber);
        Collections.sort(proyectosHay);
        assertEquals(proyectosDebeHaber, proyectosHay);
    }


    /**
     * Comprueba que el listado, la adición y eliminado de administradores
     * de un proyecto funciona correctamente. A diferencia del otro método similar,
     * éste utiliza listado por proyecto, no por uid.
     */
    @Test
    public void testListado_Adicion_Eliminado_AdminsProyectoPorCN() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_AdminsProyectoPorCN");
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que los administradores del proyecto son los que deben
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        expResult.add("arek");
        expResult.add("patxi");
        expResult.add("mica");
        ArrayList result = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result);
        assertEquals(expResult, result);

        // Ahora añade un nuevo usuario y vuelve a consultar la lista
        api.addAdminAProyecto("visitante", cnProyecto, uid, pass);
        expResult.add("visitante");
        ArrayList result2 = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Finalmente reestablece el escenario: le borra y consulta de nuevo
        api.eliminarAdminDeProyecto("visitante", cnProyecto, uid, pass);
        api.eliminarUsuarioDeProyecto("visitante", cnProyecto, uid, pass);
        expResult.remove("visitante");
        ArrayList result3 = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result3);
        assertEquals(expResult, result3);
    }


    /**
     * Prueba a añadir un administrador que ya existe a un proyecto determinado.
     */
    @Test
    public void testAddAdminYaExistenteAProyecto() throws Exception {
        System.out.println("# AddAdminYaExistenteAProyecto");
        String uidAdmin = "arek";
        String cnProyecto = "practicaISI";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Según el escenario sólo está en la lista uno
        ArrayList<String> recup = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        ArrayList<String> debe = new ArrayList<String>();
        debe.add(uidAdmin);
        assertEquals(recup,debe);

        // Añadimos al que ya es admin
        api.addAdminAProyecto(uidAdmin, cnProyecto, uid, pass);
        recup = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        assertEquals(recup,debe);
    }




    /**
     * Comprueba que el listado, la adición y eliminado de miembros
     * de un proyecto funciona correctamente. A diferencia del otro método similar,
     * éste utiliza listado por proyecto, no por uid.
     */
    @Test
    public void testListado_Adicion_Eliminado_MiembrosProyectoPorCN() throws Exception {
        System.out.println("# Listado_Adicion_Eliminado_MiembrosProyectoPorCN");
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Primero comprueba que los miembros del proyecto son los que deben
        // según el escenario
        ArrayList expResult = new ArrayList<String>();
        expResult.add("arek");
        expResult.add("patxi");
        expResult.add("mica");
        ArrayList result = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result);
        assertEquals(expResult, result);

        // Ahora añade un nuevo usuario y vuelve a consultar la lista
        api.addUsuarioAProyecto("visitante", cnProyecto, uid, pass);
        expResult.add("visitante");
        ArrayList result2 = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result2);
        assertEquals(expResult, result2);

        // Comprueba también que la lista de admins no ha variado
        ArrayList<String> listaAdminsRecup = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        expResult.remove("visitante");
        Collections.sort(expResult);
        Collections.sort(listaAdminsRecup);
        assertEquals(expResult, listaAdminsRecup);

        // Finalmente reestablece el escenario: borra al usuario y consulta de nuevo
        api.eliminarUsuarioDeProyecto("visitante", cnProyecto, uid, pass);
        ArrayList result3 = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(result3);
        assertEquals(expResult, result3);

        // También comprueba que los admins siguen siendo igual
        listaAdminsRecup = api.getListadoAdministradoresPorProyecto(cnProyecto, uid, pass);
        Collections.sort(expResult);
        Collections.sort(listaAdminsRecup);
        assertEquals(expResult, listaAdminsRecup);
    }


    /**
     * Prueba a añadir un miembro que ya existe a un proyecto determinado.
     */
    @Test
    public void testAddMiembroYaExistenteAProyecto() throws Exception {
        System.out.println("# AddMiembroYaExistenteAProyecto");
        String uidMiembro = "alumnos";
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Según el escenario sólo está en la lista uno
        ArrayList<String> recup = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        ArrayList<String> debe = new ArrayList<String>();
        debe.add(uidMiembro);
        debe.add("patxi");
        Collections.sort(recup);
        Collections.sort(debe);
        assertEquals(recup,debe);

        // Añadimos al que ya es admin
        api.addUsuarioAProyecto(uidMiembro, cnProyecto, uid, pass);
        recup = api.getListadoUsuariosPorProyecto(cnProyecto, uid, pass);
        Collections.sort(recup);
        assertEquals(recup,debe);
    }


    /**
     * Prueba a añadir un nuevo repositorio a un proyecto que ya tiene, lo fija
     * como default y luego lo borra.
     */
    @Test
    public void testAdd_DeleteRepositorioAProyecto() throws Exception {
        System.out.println("# Add_DeleteRepositorioAProyecto");
        String tipoRepo = "GIT";
        boolean esPublico = false;
        String rutaRepo = null;
        String cnProyecto = "practicaISI";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Añade el repositorio
        api.addRepositorioAProyecto(tipoRepo, esPublico, rutaRepo, cnProyecto, uid, pass);
        ArrayList<Repositorio> listaRepos = api.getDatosProyecto(cnProyecto, uid, pass).getRepositorios();
        ArrayList<Repositorio> esperados = new ArrayList<Repositorio>();
        esperados.add(FactoriaRepositorios.crearRepositorio(TipoRepositorio.SVN, esPublico, rutaRepo));
        esperados.add(FactoriaRepositorios.crearRepositorio(TipoRepositorio.GIT, esPublico, rutaRepo));
        assertEquals(listaRepos,esperados);

        // Reestablece el escenario: elimina el repositorio
        api.eliminarRepositorioDeProyecto(tipoRepo, cnProyecto, uid, pass);
        listaRepos = api.getDatosProyecto(cnProyecto, uid, pass).getRepositorios();
        esperados.remove(1);    // El segundo en ser creado
        assertEquals(listaRepos,esperados);
    }


    /**
     * Intenta añadir un repositorio SVN a un proyecto que ya tiene un repositorio
     * SVN, lo cual está prohibido.
     */
    @Test
    public void testAddRepositorioYaExistenteAProyecto() throws Exception {
        System.out.println("# AddRepositorioYaExistenteAProyecto");
        String tipoRepo = "SVN";
        boolean esPublico = false;
        String rutaRepo = null;
        String cnProyecto = "PFCArek";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        try {
            api.addRepositorioAProyecto(tipoRepo, esPublico, rutaRepo, cnProyecto, uid, pass);
            fail("El repositorio del mismo tipo ha podido crearse");
        } catch (ExcepcionRepositorio e) {
            // Si llega aquí es que no se ha podido crear el repositorio, OK
        }
    }


    /**
     * Prueba a añadir un nuevo repositorio a un proyecto que no los tiene previamente,
     * lo fija como default y luego lo borra. Como el proyecto se creó sin repos,
     * se debe añadir el defaultRepositorio posteriormente a la creación del repo.
     */
    @Test
    public void testAdd_DeleteRepositorioAProyectoQueNoTeniaRepos() throws Exception {
        System.out.println("# Add_DeleteRepositorioAProyectoQueNoTeniaRepos");
        String tipoRepo = "SVN";
        boolean esPublico = true;
        String rutaRepo = null;
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        // Añade el repositorio
        api.addRepositorioAProyecto(tipoRepo, esPublico, rutaRepo, cnProyecto, uid, pass);
        ArrayList<Repositorio> listaRepos = api.getDatosProyecto(cnProyecto, uid, pass).getRepositorios();
        ArrayList<Repositorio> esperados = new ArrayList<Repositorio>();
        esperados.add(FactoriaRepositorios.crearRepositorio(TipoRepositorio.SVN, esPublico, rutaRepo));
        assertEquals(listaRepos,esperados);

        // Reestablece el escenario: elimina el repositorio
        api.eliminarRepositorioDeProyecto(tipoRepo, cnProyecto, uid, pass);
        listaRepos = api.getDatosProyecto(cnProyecto, uid, pass).getRepositorios();
        esperados.remove(0);    // El único que hay
        assertEquals(listaRepos,esperados);
    }


    /**
     * Intenta eliminar un repositorio de un proyecto que no tiene repos
     */
    @Test
    public void testEliminarRepositorioDeProyectoSinRepos() throws Exception {
        System.out.println("# EliminarRepositorioDeProyectoSinRepos");
        String tipoRepo = "SVN";
        String cnProyecto = "asignatura";
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;

        api.eliminarRepositorioDeProyecto(tipoRepo, cnProyecto, uid, pass);
    }








    /***************************************************************************
     *                          TEST DE PERMISOS
     **************************************************************************/

    /**
     * Prueba de que un usuario sólo puede hacer aquello para lo cual se le han
     * definido permisos.
     */
    @Test
    public void testPermisosConsultaUsuario() throws Exception {
        System.out.println("# PermisosConsultaUsuario");
        String uid = "helena";
        String pass = "helena";
        String otroUid = "arek";

        // Lo que sí puede hacer -> Si no dan ExcepcionLogin entonces OK
        api.getDatosUsuario(uid, uid, pass);
        api.getListadoProyectosParticipados(uid, uid, pass);
        api.getListadoProyectosAdministrados(uid, uid, pass);
        api.getListadoUsuariosPorProyecto("practicaISI", uid, pass);
        api.getDatosProyecto("practicaASAmbu", uid, pass);
        api.getListadoAdministradoresPorProyecto("practicaASAmbu", uid, pass);

        // Lo que no puede hacer -> Deben dar todos ExcepcionLogin
        try {
            api.getDatosUsuario(otroUid, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoProyectosParticipados(otroUid, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoProyectosAdministrados(otroUid, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoUsuariosPorProyecto("PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getDatosProyecto("PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoAdministradoresPorProyecto("PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }


    /**
     * Prueba los permisos de edición de un usuario (solo puede editarse a sí mismo)
     */
    @Test
    public void testPermisosEdicionUsuario() throws Exception {
        System.out.println("# PermisosEdicionUsuario");
        String uid = miUser.getUid();
        String pass = "testPass";

        // Primero añade al usuario de prueba
        api.crearUsuario(uid, "testPass", miUser.getApellidos(), miUser.getEmail(), pass, uidSuperAdmin, passSuperAdmin);

        // Prueba a editarse a sí mismo
        miUser.setApellidos("modificación");
        api.editarDatosUsuario(miUser, uid, pass);
        Usuario recup = api.getDatosUsuario(uid, uid, pass);
        assertEquals(recup, miUser);

        // Eliminamos al usuario de prueba
        api.eliminarUsuario(uid, uidSuperAdmin, passSuperAdmin);
    }


    /**
     * Comprueba que un usuario no puede hacer lo que corresponde a un administrador
     */
    @Test
    public void testPermisosAdministracionUsuarios() throws Exception {
        System.out.println("# PermisosAdministracionUsuarios");
        String uid = "alumnos";
        String pass = "alumnos";

        // Un usuario no tiene permisos de administración de ningún tipo -> Todos ExcepcionLogin
        try {
            api.getListadoUidsUsuarios(uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoNombresUsuarios(uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addUsuarioAProyecto("visitante", "asignatura", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addAdminAProyecto("visitante", "PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addRepositorioAProyecto("SVN", true, null, "asignatura", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoProyectos(uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.editarDatosProyecto(miProyecto, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarAdminDeProyecto("patxi", "PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarAdminDeProyecto("mica", "PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarRepositorioDeProyecto("SVN", "asignatura", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }


    /**
     * Comprueba que un usuario no puede hacer lo que corresponde a un Superadmin
     */
    @Test
    public void testPermisosSuperadministracionUsuarios() throws Exception {
        System.out.println("# PermisosAdministracionUsuarios");
        String uid = "helena";
        String pass = "helena";

        // Un usuario no tiene permisos de superadministración de ningún tipo -> Todos ExcepcionLogin
        try {
            api.crearUsuario("sancho", "Sancho", "Panza", "mail@com.com", "unaPass", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.bloquearUsuario("alvaro", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.desbloquearUsuario("alvaro", "alvaro", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarUsuario("helena", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.crearProyecto(miProyecto.getCn(), miProyecto.getDescription(), miProyecto.getPrimerAdmin(),
                    null, false, null, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarProyecto("practicaASAmbu", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }


    /**
     * Prueba las consultas que puede hacer un administrador (todas las de sus
     * proyectos) y las que no puede hacer (proyectos ajenos).
     */
    @Test
    public void testPermisosConsultaAdmins() throws Exception {
        System.out.println("# PermisosConsultaAdmins");
        String uid = "arek";
        String pass = "arek";

        // Lo que sí puede hacer -> Si no dan ExcepcionLogin entonces OK
        api.getDatosUsuario("adolfo", uid, pass);
        api.getListadoProyectosParticipados("alvaro", uid, pass);
        api.getListadoProyectosAdministrados("adolfo", uid, pass);
        api.getListadoUsuariosPorProyecto("practicaISI", uid, pass);
        api.getDatosProyecto("PFCArek", uid, pass);
        api.getListadoAdministradoresPorProyecto("solitario", uid, pass);

        api.getListadoUidsUsuarios(uid, pass);
        api.getListadoNombresUsuarios(uid, pass);
        api.getListadoProyectos(uid, pass);

        // Lo que no puede hacer -> Deben dar todos ExcepcionLogin
        try {
            api.getDatosUsuario("alumnos", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoProyectosParticipados("alumnos", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoProyectosAdministrados("visitante", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoUsuariosPorProyecto("practicaASAmbu", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getListadoAdministradoresPorProyecto("asignatura", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.getDatosProyecto("asignatura", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }



    /**
     * Comprueba las tareas de administración que sí puede hacer un administrador
     * y las que no (proyectos ajenos).
     */
    @Test
    public void testPermisosAdministracionAdmins() throws Exception {
        System.out.println("# PermisosAdministracionAdmins");
        String uid = "patxi";
        String pass = "patxi";
        String nuevoAdmin = "mica";
        String nuevoUser = "helena";
        String proyecto = "asignatura";

        // Puede editar a los que participan en sus proyectos administrados
        Usuario user = api.getDatosUsuario("alumnos", uid, pass);
        String apellidoOriginal = user.getApellidos();
        user.setApellidos("Otros apellidos");
        api.editarDatosUsuario(user, uid, pass);
        Usuario recup = api.getDatosUsuario(user.getUid(), uid, pass);
        assertEquals(user, recup);
        user.setApellidos(apellidoOriginal);
        api.editarDatosUsuario(user, uid, pass);
        recup = api.getDatosUsuario(user.getUid(), uid, pass);
        assertEquals(user, recup);

        // Puede editar aquellos proyectos que administra
        Proyecto proy = api.getDatosProyecto(proyecto, uid, pass);
        String descOriginal = proy.getDescription();
        proy.setDescription("otra descripción en pruebas");
        api.editarDatosProyecto(proy, uid, pass);
        Proyecto recupe = api.getDatosProyecto(proy.getCn(), uid, pass);
        assertEquals(proy,recupe);
        proy.setDescription(descOriginal);
        api.editarDatosProyecto(proy, uid, pass);
        recupe = api.getDatosProyecto(proy.getCn(), uid, pass);
        assertEquals(proy,recupe);

        // Puede administrar los proyectos en los que es administrador
        api.addAdminAProyecto(nuevoAdmin, proyecto, uid, pass);
        api.addUsuarioAProyecto(nuevoUser, proyecto, uid, pass);
        api.addRepositorioAProyecto("SVN", true, null, proyecto, uid, pass);

        api.eliminarUsuarioDeProyecto(nuevoAdmin, proyecto, uid, pass);
        api.eliminarUsuarioDeProyecto(nuevoUser, proyecto, uid, pass);
        api.eliminarRepositorioDeProyecto("SVN", proyecto, uid, pass);

        // No puede administrar aquellos proyectos que no son suyos
        try {
            Usuario prueba = api.getDatosUsuario("helena", uidSuperAdmin, passSuperAdmin);
            api.editarDatosUsuario(prueba, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            Proyecto prueba = api.getDatosProyecto("practicaAS", uidSuperAdmin, passSuperAdmin);
            api.editarDatosProyecto(prueba, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addAdminAProyecto("mica", "practicaAS", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addUsuarioAProyecto("adolfo", "solitario", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.addRepositorioAProyecto("repoGiot", true, null, "practicaISI", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarAdminDeProyecto("alvaro", "practicaASAmbu", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarUsuarioDeProyecto("arek", "practicaAS", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarRepositorioDeProyecto("GIT", "practicaASAmbu", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }




    /**
     * Comprueba que un administrador no puede hacer lo que corresponde a un Superadmin
     */
    @Test
    public void testPermisosSuperadministracionAdmins() throws Exception {
        System.out.println("# PermisosSuperadministracionAdmins");
        String uid = "arek";
        String pass = "arek";

        // Un usuario no tiene permisos de superadministración de ningún tipo -> Todos ExcepcionLogin
        try {
            api.crearUsuario("sancho", "Sancho", "Panza", "mail@com.com", "unaPass", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.bloquearUsuario("alvaro", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.desbloquearUsuario("alvaro", "alvaro", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarUsuario("helena", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.crearProyecto(miProyecto.getCn(), miProyecto.getDescription(), miProyecto.getPrimerAdmin(),
                    null, false, null, uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
        try {
            api.eliminarProyecto("PFCArek", uid, pass);
            fail("Fallo de permisos");
        } catch (ExcepcionLogin e) {}
    }
    

    /**
     * Elimina todo lo que ha sido creado durante las pruebas para garantizar
     * que su ejecución no ha afectado al estado de la Forja. O en caso de no
     * estar marcado el borrado, checkea que el escenario no haya variado
     */
    @Test
    public void limpiarEscenarioTrasTest() throws Exception {
        String uid = uidSuperAdmin;
        String pass = passSuperAdmin;
        
        if (this.borrarEscenario) {
            System.out.println("# Limpiando escenario");
            
            // Borrado de proyectos
            api.eliminarProyecto("PFCArek", uid, pass);
            api.eliminarProyecto("practicaISI", uid, pass);
            api.eliminarProyecto("practicaAS", uid, pass);
            api.eliminarProyecto("practicaASAmbu", uid, pass);
            api.eliminarProyecto("asignatura", uid, pass);
            api.eliminarProyecto("solitario", uid, pass);

            // Borrado de usuarios
            api.eliminarUsuario("arek", uid, pass);
            api.eliminarUsuario("patxi", uid, pass);
            api.eliminarUsuario("mica", uid, pass);
            api.eliminarUsuario("adolfo", uid, pass);
            api.eliminarUsuario("alvaro", uid, pass);
            api.eliminarUsuario("helena", uid, pass);
            api.eliminarUsuario("visitante", uid, pass);
            api.eliminarUsuario("alumnos", uid, pass);

        } else {
            // Comprobación de los usuarios
            ArrayList<String> usuariosHay = api.getListadoUidsUsuarios(uid, pass);
            ArrayList<String> usuariosDebeHaber = new ArrayList<String>();
            usuariosDebeHaber.add(uidSuperAdmin);
            usuariosDebeHaber.add("arek");
            usuariosDebeHaber.add("patxi");
            usuariosDebeHaber.add("mica");
            usuariosDebeHaber.add("alvaro");
            usuariosDebeHaber.add("adolfo");
            usuariosDebeHaber.add("helena");
            usuariosDebeHaber.add("visitante");
            usuariosDebeHaber.add("alumnos");
            Collections.sort(usuariosHay);
            Collections.sort(usuariosDebeHaber);
            assertEquals(usuariosHay, usuariosDebeHaber);

            // Comprobación de proyectos
            ArrayList<String> proyectosHay = api.getListadoProyectos(uid, pass);
            ArrayList<String> proyectosDebeHaber = new ArrayList<String>();
            proyectosDebeHaber.add("superadmins");
            proyectosDebeHaber.add("PFCArek");
            proyectosDebeHaber.add("practicaISI");
            proyectosDebeHaber.add("practicaAS");
            proyectosDebeHaber.add("practicaASAmbu");
            proyectosDebeHaber.add("asignatura");
            proyectosDebeHaber.add("solitario");
            Collections.sort(proyectosDebeHaber);
            Collections.sort(proyectosHay);
            assertEquals(proyectosDebeHaber, proyectosHay);

            // La asignación de cada usuario y proyecto no se comprueba
        }
    }
}