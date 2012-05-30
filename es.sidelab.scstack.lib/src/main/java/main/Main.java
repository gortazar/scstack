package main;

import excepciones.ExcepcionForja;
import excepciones.api.ExcepcionLogin;
import excepciones.configuradorApache.ExcepcionGeneradorFicherosApache;
import excepciones.configuradorApache.ExcepcionConsola;
import excepciones.gestorLdap.ExcepcionGestorLDAP;
import excepciones.gestorLdap.ExcepcionLDAPNoExisteRegistro;
import excepciones.gestorRedmine.ExcepcionGestorRedmine;
import excepciones.modeloDatos.ExcepcionProyecto;
import excepciones.modeloDatos.ExcepcionRepositorio;
import excepciones.modeloDatos.ExcepcionUsuario;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args)
            throws ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionProyecto,
            ExcepcionGestorLDAP, ExcepcionUsuario, ExcepcionRepositorio, ExcepcionGestorRedmine,
            ExcepcionForja, ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, NoSuchAlgorithmException {

        // Lo primerísimo de todo es crear la API de la Forja
        API_Segura api = new API_Segura();
//        api.crearUsuario("pepe", "Pedro", "Lopez", "arek.pedro@hotmail.com", "pepe", "super", "super");
//        api.addAdminAProyecto("pepe", "PFCArek", "super", "super");
//        api.eliminarUsuarioDeProyecto("pepe", "PFCArek", "super", "super");
        api.eliminarUsuario("testuser", "super", "super");

        
        String uid = "super";
        String pass = "super";
    }

    private static String printLista(String[] lista) {
        String cadena = "[";
        for (String uid : lista) {
            cadena += uid + ",\n";
        }
        return cadena + "]";
    }
}
