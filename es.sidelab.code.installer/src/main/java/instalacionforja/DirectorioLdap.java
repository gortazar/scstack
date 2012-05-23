/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: DirectorioLdap.java
 * Autor: Arek Klauza
 * Fecha: Mayo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package instalacionforja;

import apoyo.Utilidades;
import es.sidelab.tools.commandline.ExecutionCommandException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Clase encargada de instalar el servicio de directorios de LDAP con los
 * parámetros del fichero de configuración.</p>
 * @author Arek Klauza
 */
public class DirectorioLdap {

    public void instalar() throws ExecutionCommandException, IOException, NoSuchAlgorithmException {
        System.out.println("\n*** INSTALACIÓN DIRECTORIO LDAP ***\n");
        Instalacion.ejecutar("apt-get -y install slapd ldap-utils");
        Instalacion.ejecutar("cp ficherosInstalacion/directorioLdap/proyectosForjaSidelab.ldif /etc/ldap/schema");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/schema/cosine.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/schema/inetorgperson.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/schema/nis.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/schema/proyectosForjaSidelab.ldif");
        crearFicheroDB_LDIF("/etc/ldap/db.ldif");
        crearFicheroBASE_LDIF("/etc/ldap/base.ldif");
        crearFicheroCONFIG_LDIF("/etc/ldap/config.ldif");
        crearFicheroCONSTRUIR_LDIF("/etc/ldap/construir.ldif");
        crearFicheroACL_LDIF("/etc/ldap/acl.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/db.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/base.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/config.ldif");
        Instalacion.ejecutar("ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/ldap/construir.ldif");
        Instalacion.ejecutar("ldapmodify -x -D cn=admin,cn=config -w " + Instalacion.config.getProperty("passBindDN") + " -f /etc/ldap/acl.ldif");
        Instalacion.ejecutar("sh ficherosInstalacion/directorioLdap/restartLdap.sh");
        Instalacion.ejecutar("/etc/init.d/slapd start");
        crearFicheroCONFIG_SSL("/etc/ldap/keys.ssl");
        Instalacion.ejecutar("openssl req -new -x509 -nodes -config /etc/ldap/keys.ssl -out /etc/ldap/slapdcert.pem -keyout /etc/ldap/slapdkey.pem -days 3650");
        Instalacion.ejecutar("chown openldap:openldap /etc/ldap/slapdcert.pem");
        Instalacion.ejecutar("chown openldap:openldap /etc/ldap/slapdkey.pem");
        crearFicheroTLSCONFIG_LDIF("/etc/ldap/tls-config.ldif");
        Instalacion.ejecutar("sh ficherosInstalacion/directorioLdap/restartLdap.sh");
        Instalacion.ejecutar("/etc/init.d/slapd start");
        Instalacion.ejecutar("ldapmodify -x -D cn=admin,cn=config -w " + Instalacion.config.getProperty("passBindDN") + " -f /etc/ldap/tls-config.ldif");
        crearFicheroSLDAP("/etc/default/slapd");
        Instalacion.ejecutar("sh ficherosInstalacion/directorioLdap/restartLdap.sh");
        Instalacion.ejecutar("/etc/init.d/slapd start");
        System.out.println("**************************************************\n");
    }
    
    
    





    
    private void crearFicheroDB_LDIF(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("# Load dynamic backend modules");
        pw.println("dn: cn=module{0},cn=config");
        pw.println("objectClass: olcModuleList");
        pw.println("cn: module");
        pw.println("olcModulepath: /usr/lib/ldap");
        pw.println("olcModuleload: {0}back_hdb");
        pw.println("");
        pw.println("# Create the database");
        pw.println("dn: olcDatabase={1}hdb,cn=config");
        pw.println("objectClass: olcDatabaseConfig");
        pw.println("objectClass: olcHdbConfig");
        pw.println("olcDatabase: {1}hdb");
        pw.println("olcDbDirectory: /var/lib/ldap");
        pw.println("olcSuffix: " + Instalacion.config.getProperty("baseDN"));
        pw.println("olcRootDN: " + Instalacion.config.getProperty("bindDN"));
        pw.println("olcRootPW: " + Instalacion.config.getProperty("passBindDN"));
        pw.println("olcDbConfig: {0}set_cachesize 0 2097152 0");
        pw.println("olcDbConfig: {1}set_lk_max_objects 1500");
        pw.println("olcDbConfig: {2}set_lk_max_locks 1500");
        pw.println("olcDbConfig: {3}set_lk_max_lockers 1500");
        pw.println("olcLastMod: TRUE");
        pw.println("olcDbCheckpoint: 512 30");
        pw.println("olcDbIndex: uid pres,eq");
        pw.println("olcDbIndex: cn,sn,mail pres,eq,approx,sub");
        pw.println("olcDbIndex: objectClass eq");
        
        pw.close();
        file.close();
    }

    private void crearFicheroBASE_LDIF(String ruta) throws IOException, NoSuchAlgorithmException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("dn: " + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: dcObject");
        pw.println("objectclass: organization");
        pw.println("o: " + Instalacion.config.getProperty("dominio"));
        pw.println("dc: " + Instalacion.config.getProperty("dominioSinExt"));
        pw.println("description: " + Instalacion.config.getProperty("dominio"));
        pw.println("");
        pw.println("dn: cn=admin," + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: simpleSecurityObject");
        pw.println("objectClass: organizationalRole");
        pw.println("cn: admin");
        pw.println("userPassword: " + Utilidades.toMD5(Instalacion.config.getProperty("passBindDN")));
        pw.println("description: LDAP administrator");
        
        pw.close();
        file.close();
    }

    private void crearFicheroCONFIG_LDIF(String ruta) throws IOException, NoSuchAlgorithmException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("dn: olcDatabase={0}config,cn=config");
        pw.println("changetype: modify");
        pw.println("add: olcRootDN");
        pw.println("olcRootDN: cn=admin,cn=config");
        pw.println("");
        pw.println("dn: olcDatabase={0}config,cn=config");
        pw.println("changetype: modify");
        pw.println("add: olcRootPW");
        pw.println("olcRootPW: " + Utilidades.toMD5(Instalacion.config.getProperty("passBindDN")));
        pw.println("");
        pw.println("dn: olcDatabase={0}config,cn=config");
        pw.println("changetype: modify");
        pw.println("delete: olcAccess");

        pw.close();
        file.close();
    }

    private void crearFicheroCONSTRUIR_LDIF(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("# Construye los distintos OU");
        pw.println("dn: ou=" + Instalacion.config.getProperty("ouProyectos") + "," + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: organizationalUnit");
        pw.println("objectClass: top");
        pw.println("ou: " + Instalacion.config.getProperty("ouProyectos"));
        pw.println("");
        pw.println("dn: ou=" + Instalacion.config.getProperty("ouUsuarios") + "," + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: organizationalUnit");
        pw.println("objectClass: top");
        pw.println("ou: " + Instalacion.config.getProperty("ouUsuarios"));
        pw.println("");
        pw.println("dn: ou=" + Instalacion.config.getProperty("ouConfiguracion") + "," + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: organizationalUnit");
        pw.println("objectClass: top");
        pw.println("ou: " + Instalacion.config.getProperty("ouConfiguracion"));
        pw.println("");
        pw.println("# Crea la entrada de configuración para el marcador de UID y GID Number");
        pw.println("dn: uid=marcadorIDNumber,ou=" + Instalacion.config.getProperty("ouConfiguracion") + "," + Instalacion.config.getProperty("baseDN"));
        pw.println("objectClass: inetOrgPerson");
        pw.println("objectClass: organizationalPerson");
        pw.println("objectClass: person");
        pw.println("objectClass: posixAccount");
        pw.println("objectClass: top");
        pw.println("uid: marcadorIDNumber");
        pw.println("cn: Marcador que lleva la cuenta del GID y UID number actuales");
        pw.println("homeDirectory: .");
        pw.println("sn: Contador del gid y uid Number para la siguiente entrada a crear");
        pw.println("gidNumber: 5000");
        pw.println("uidNumber: 5000");

        pw.close();
        file.close();
    }

    private void crearFicheroACL_LDIF(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("dn: olcDatabase={1}hdb,cn=config");
        pw.println("add: olcAccess");
        pw.println("olcAccess: to attrs=userPassword,shadowLastChange by dn=\"cn=admin," + Instalacion.config.getProperty("baseDN") + "\" write by anonymous auth by self write by * none");
        pw.println("olcAccess: to * by dn=\"cn=admin," + Instalacion.config.getProperty("baseDN") + "\" write by users read");
        
        pw.close();
        file.close();
    }

    private void crearFicheroCONFIG_SSL(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("[ req ]");
        pw.println("default_bits           = 1024");
        pw.println("prompt                 = no");
        pw.println("distinguished_name     = req_distinguished_name");
        pw.println("");
        pw.println("[ req_distinguished_name ]");
        pw.println("C                      = ES");
        pw.println("ST                     = Madrid");
        pw.println("L                      = Madrid");
        pw.println("O                      = " + Instalacion.config.getProperty("dominio"));
        pw.println("OU                     = " + Instalacion.config.getProperty("dominio"));
        pw.println("CN                     = " + Instalacion.config.getProperty("dominio"));
        pw.println("emailAddress           = info@" + Instalacion.config.getProperty("dominio"));

        pw.close();
        file.close();
    }

    private void crearFicheroTLSCONFIG_LDIF(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("dn: cn=config");
        pw.println("add: olcTLSCertificateFile");
        pw.println("olcTLSCertificateFile: /etc/ldap/slapdcert.pem");
        pw.println("");
        pw.println("dn: cn=config");
        pw.println("add: olcTLSCertificateKeyFile");
        pw.println("olcTLSCertificateKeyFile: /etc/ldap/slapdkey.pem");
        
        pw.close();
        file.close();
    }

    private void crearFicheroSLDAP(String ruta) throws IOException {
        FileWriter file = null;
        file = new FileWriter(ruta);
        PrintWriter pw = new PrintWriter(file);

        pw.println("SLAPD_CONF=");
        pw.println("");
        pw.println("SLAPD_USER=\"openldap\"");
        pw.println("");
        pw.println("SLAPD_GROUP=\"openldap\"");
        pw.println("");
        pw.println("SLAPD_PIDFILE=");
        pw.println("");
        pw.println("SLAPD_SERVICES=\"ldap:/// ldaps:/// ldapi:///\"");
        pw.println("");
        pw.println("SLAPD_SENTINEL_FILE=/etc/ldap/noslapd");
        pw.println("");
        pw.println("SLAPD_OPTIONS=\"\"");

        pw.close();
        file.close();
    }


    
}
