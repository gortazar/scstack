package es.sidelab.scstack.lib.api;

import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionGeneradorFicherosApache;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionLogin;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionProyecto;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionUsuario;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args)
            throws ExcepcionGeneradorFicherosApache, ExcepcionConsola, ExcepcionProyecto,
            ExcepcionGestorLDAP, ExcepcionUsuario, ExcepcionRepositorio, ExcepcionGestorRedmine,
            SCStackException, ExcepcionLogin, ExcepcionLDAPNoExisteRegistro, NoSuchAlgorithmException {

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
