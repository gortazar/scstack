package es.sidelab.scstack.installer.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.dataModel.repos.FactoriaRepositorios.TipoRepositorio;
import es.sidelab.scstack.lib.exceptions.ExcepcionForja;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionGeneradorFicherosApache;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;

public class SFTPTest {

	private static final String PROJECT2 = "project2";
	private static final String PROJECT1 = "project1";
	private static final String PASSWORD = "kakatua";
	private static final String USERNAME1 = "alex";
	private static final String USERNAME2 = "john";
	private static final String CONFIG_FILE = "scstack.conf";
	API_Abierta api;
	
	@Before
	public void setUp() throws ExcepcionForja, NoSuchAlgorithmException {
		api = new API_Abierta(CONFIG_FILE);
		api.addUsuario(USERNAME1, "Alex", "Xela", "xela@alex.com", PASSWORD);
		api.addUsuario(USERNAME2, "John", "McDonald", "john@example.com", PASSWORD);
		api.addProyecto(PROJECT1, "A description", "alex", TipoRepositorio.SVN, true, null);
		api.addProyecto(PROJECT2, "A description", "john", TipoRepositorio.SVN, true, null);
	}
	
	@After
	public void tearDown() throws ExcepcionLDAPNoExisteRegistro, ExcepcionConsola, ExcepcionGestorLDAP, ExcepcionGeneradorFicherosApache, ExcepcionGestorRedmine {
		api.deleteProyecto(PROJECT1);
		api.deleteProyecto(PROJECT2);
		api.deleteUsuario(USERNAME1);
		api.deleteUsuario(USERNAME2);
	}

	@Test
	public void testConnection() {
		
		ConnectionOptions options = new ConnectionOptions();
		options.set(ConnectionOptions.ADDRESS, "127.0.0.1");
		options.set(ConnectionOptions.USERNAME, USERNAME1);
		options.set(ConnectionOptions.PASSWORD, PASSWORD);
		options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
		options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);
		OverthereConnection conn = Overthere.getConnection("ssh", options);
		
	}
	
	@Test
	public void sshTest() {
		
		ConnectionOptions options = new ConnectionOptions();
		options.set(ConnectionOptions.ADDRESS, "127.0.0.1");
		options.set(ConnectionOptions.USERNAME, USERNAME1);
		options.set(ConnectionOptions.PASSWORD, PASSWORD);
		options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
		options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);
		OverthereConnection conn = Overthere.getConnection("ssh", options);
		
		OverthereFile rootTest = conn.getFile("test");
		OutputStream os = rootTest.getOutputStream();
		try {
			os.write("Hi".getBytes());
			fail("Should not create file with current user");
			os.close();
		} catch (IOException e) {
			// Ok
		}
		
		OverthereFile publicFolder = conn.getFile("public");
		assertTrue(publicFolder.exists());
		assertTrue(publicFolder.isDirectory());
		
		OverthereFile privateFolder = conn.getFile("private");
		assertTrue(privateFolder.exists());
		assertTrue(privateFolder.isDirectory());
		
		assertFalse(publicFolder.canWrite());
		assertFalse(privateFolder.canWrite());
		
		List<OverthereFile> projects = publicFolder.listFiles();
		assertEquals(2, projects.size());
		
		for(OverthereFile project : projects) {
			if(project.getName().equals(PROJECT1)) {
				assertTrue(project.canWrite());
				OverthereFile testFile = project.getFile("test");
			} else {
				assertFalse(project.canWrite());
				assertTrue(project.canRead());
			}
		}
		
		List<OverthereFile> privateProjects = privateFolder.listFiles();
		assertEquals(2, privateProjects.size());
		
		for(OverthereFile project : privateProjects) {
			if(project.getName().equals(PROJECT1)) {
				assertTrue(project.canWrite());
			} else {
				assertFalse(project.canWrite());
				assertFalse(project.canRead());
			}
		}
		
	}

}
