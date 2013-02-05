package es.sidelab.scstack.installer.internal;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.commons.Utilidades;
import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.dataModel.Proyecto;
import es.sidelab.scstack.lib.dataModel.Usuario;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios.TipoRepositorio;
import es.sidelab.scstack.lib.exceptions.ExcepcionForja;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPYaExisteEntrada;

public class LDAPTest {

	private static final String CONFIG_FILE = "scstack.conf";
	private String PEOPLE_BASE;
	private ConfiguracionForja config;

	@Before
	public void setUp() {
		config = new ConfiguracionForja(CONFIG_FILE);
		PEOPLE_BASE = "," + config.ouUsuarios + "," + config.baseDN;
	}

	@Test
	public void testUser() throws ExcepcionForja, NoSuchAlgorithmException {

		API_Abierta api = new API_Abierta(CONFIG_FILE);
		api.addUsuario("alex", "Alex", "Xela", "xela@alex.com", "kakatua");
		
		Usuario alex = api.getUsuario("alex");
		assertNotNull(alex);
		assertEquals(alex.getUid(), "alex");
		assertEquals(alex.getEmail(), "xela@alex.com");

	}

	@Test
	public void testProject() throws ExcepcionForja, NoSuchAlgorithmException {

		API_Abierta api = new API_Abierta(CONFIG_FILE);
		api.addUsuario("alex", "Alex", "Xela", "xela@alex.com", "kakatua");
		
		Usuario alex = api.getUsuario("alex");
		assertNotNull(alex);
		assertEquals(alex.getUid(), "alex");
		assertEquals(alex.getEmail(), "xela@alex.com");

		api.addProyecto("project1", "A description", "alex", null, false, null);
		Proyecto proyecto = api.getProyecto("project1");
		assertNotNull(proyecto);
		assertEquals(proyecto.getPrimerAdmin(), "alex");
		
		
	}

}
