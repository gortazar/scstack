package es.sidelab.scstack.service;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.CharacterSet;
import org.restlet.ext.json.JsonRepresentation;

import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.commons.commandline.console.Console;
import es.sidelab.commons.commandline.console.LocalConsole;
import es.sidelab.commons.commandline.console.SSHConsole;
import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.service.data.Proyecto;
import es.sidelab.scstack.service.data.ProyectoNuevo;
import es.sidelab.scstack.service.data.Proyectos;
import es.sidelab.scstack.service.data.Usuario;
import es.sidelab.scstack.service.data.Usuarios;
import flexjson.JSONDeserializer;

/**
 * Tests a running forge.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RemoteIT {
	public static final String SSHD_CONFIG = "/etc/ssh/sshd_config";
	public static final String RECONNECT_INTERVAL = "1";
	public static final String DEFAULT_CHARSET = "UTF-8";

	private static String stackURI;
	private static String stackIP;
	private static String stackSSHUser;
	private static URL baseURL;
	private static Properties props;
	private static String superUser;
	private static String superUserPass;
	private static String sshJailMarker;

	private static Console sshCons;
	private static Console localCons;

	private static String reconnInterval;
	
	private static Usuario testUser1 = new Usuario("testuser1", 
			"Test", "User1", "u@test.com", "testuserpass");
	// its name can be retrieved by getCn() (not getName())
	private static ProyectoNuevo testProj1 = new ProyectoNuevo(
			"testproj1", "A new test project, no repo.", testUser1.getUid(),
			//no repository
			null, null, null);
	
	/**
	 * Loads configuration to read the superuser's uid and password.
	 * Must receive the stack's URL as a Java or system property.
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stackURI = System.getProperty("STACK_URL");
		if (null == stackURI || stackURI.isEmpty())
			fail("Stack's URL must be specified as a system (or Java) property!");
		System.out.println("Forge is working on " + stackURI);
		baseURL = new URL(stackURI);
		props = ServicioREST.loadPropertiesFromFile(ServicioREST.CONFIG_FILE);
		superUser = props.getProperty("sadminUID");
		superUserPass = props.getProperty("sadminPass");
		if (superUser == null || superUser.isEmpty())
			fail("The superuser's uid was not found!");
		if (superUserPass == null || superUserPass.isEmpty())
			fail("The superuser's password was not found!");

		sshJailMarker = props.getProperty("marcadorJaulaSSH");
		if (sshJailMarker == null || sshJailMarker.isEmpty())
			fail("The SSH jail placeholder (or marker) was not found!");

		stackIP = System.getProperty("STACK_IP");
		stackSSHUser = System.getProperty("STACK_USER");
		sshCons = new SSHConsole(stackIP, stackSSHUser);
		System.out.println("Attempting to test the SSH connection...");
		try {
			sshCons.exec("echo 2>&1");
		} catch (ExecutionCommandException e) {
			System.err.println("Failed executing command in remote machine!");
			fail(e.getMessage() + "Std: " + e.getStandardOutput() + "Err:" + e.getErrorOutput());
		}
		System.out.println("OK.");
		localCons = new LocalConsole();
		String rint = System.getProperty("RECONNECT_INTERVAL");
		reconnInterval = (rint == null || rint.isEmpty()) ? RECONNECT_INTERVAL : rint;
		System.out.println("Reconnection interval for lftp: " + reconnInterval + " sec");

		createTestUser(testUser1);
		createTestProject(testProj1);
	}

	///////////// API for the accessing the REST service of the stack //////////////////
	// Here there are only 2 methods, but it can be further extended by adding 
	//  methods for the rest of the operations (add user to project, remove user, etc.)
	/**
	 * Creates a new {@link Proyecto} by receiving a 
	 * {@link ProyectoNuevo} object (a new project has to have assigned an admin and
	 * the Proyecto class doesn't include this information).
	 * @param proj a {@link ProyectoNuevo} object, the repository information can be set to null
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws SCStackException when the response code is not the expected one; this
	 * means that the project was not created or an internal error has happen
	 */
	public static void createTestProject(ProyectoNuevo proj) throws KeyManagementException, NoSuchAlgorithmException, IOException, SCStackException {
		JsonRepresentation projJson = proj.serializarJson();
		projJson.setCharacterSet(CharacterSet.valueOf(DEFAULT_CHARSET));
		String response = getResponseJSON(
				"proyectos", projJson.getText(), "POST", HttpURLConnection.HTTP_CREATED, superUser, superUserPass);
		assertNotNull(response);
	}

	/**
	 * Creates a new {@link Usuario} using its JSON representation
	 * by sending an HTTP query to the stack's REST service.
	 * @param user the {@link Usuario} to be created
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws SCStackException when the response code is not the expected one; this
	 * means that the user was not created
	 */
	public static void createTestUser(es.sidelab.scstack.service.data.Usuario user) 
			throws KeyManagementException, NoSuchAlgorithmException, IOException, SCStackException { 
		JsonRepresentation userJson = user.serializarJson();
		userJson.setCharacterSet(CharacterSet.valueOf(DEFAULT_CHARSET));
		String response = getResponseJSON(
				"usuarios", userJson.getText(), "POST", HttpURLConnection.HTTP_CREATED, superUser, superUserPass);
		assertNotNull(response);
	}
	///////////// End of API for the accessing the REST service of the stack //////////////////
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Checks the user' list for the 2 first users (super user and the Test User).
	 * Connects by SSH to the machine where the forge is installed and runs the
	 * <strong>getent passwd</strong> command to see if the users are included in the
	 * list maintained by LDAP. 
	 * Then, it retrieves the {@link RemoteIT#SSHD_CONFIG} file to check
	 * the user entries coincide with the existing users.
	 * It also checks the list of existing projects (there should be 2, superadmins and testproj1).
	 * Finally, it connects via SFTP (with each user) and runs a series of commands. 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ExecutionCommandException 
	 * @throws SCStackException 
	 */
	@Test
	public void testInitialConfiguration() throws 
							NoSuchAlgorithmException, KeyManagementException, 
							MalformedURLException, IOException,	ExecutionCommandException, SCStackException {
		// connects to https://REST-stack-url:REST-port/usuarios and receives the list of users
		String response = getResponseJSON(
				"usuarios", null, "GET", HttpURLConnection.HTTP_OK, superUser, superUserPass);
		assertNotNull(response);
		Usuarios uidUsers = new JSONDeserializer<Usuarios>().deserialize(response);
		boolean superUserIsConfirmed = false;
		boolean testUser1IsConfirmed = false;
		for (String u : uidUsers.listaUsuarios) {
			System.out.println("UID#" + uidUsers.listaUsuarios.indexOf(u) + ": " + u);
			if (u.contentEquals(superUser))
				superUserIsConfirmed = true;
			if (u.contentEquals(testUser1.getUid()))
				testUser1IsConfirmed = true;
		}
		assertTrue(superUserIsConfirmed);
		assertTrue(testUser1IsConfirmed);
		
		// connect by ssh and see the contents of passwd
		try {
			System.out.println("Entry for the Superuser in the passwd file:");
			sshCons.exec("getent passwd | grep " + superUser);
		} catch (ExecutionCommandException e) {
			System.err.println("Failed looking for the superuser in the passwd file!");
			System.err.println("The whole list is:\n-----------------------------");
			sshCons.exec("getent passwd");
			fail(e.getMessage() + "Std: " + e.getStandardOutput() + "Err:" + e.getErrorOutput());
		}
		try {
			System.out.println("Entry for the Test User in the passwd file:");
			sshCons.exec("getent passwd | grep " + testUser1.getUid());
		} catch (ExecutionCommandException e) {
			System.err.println("Failed looking for the Test User in the passwd file!");
			System.err.println("The whole list is:\n-----------------------------");
			sshCons.exec("getent passwd");
			fail(e.getMessage() + "Std: " + e.getStandardOutput() + "Err:" + e.getErrorOutput());
		}
		
		// retrieve the SSHD_CONFIG file
		String sshdConfigContents = sshCons.getFile(SSHD_CONFIG);
		assertNotNull(sshdConfigContents);
		assertFalse(sshdConfigContents.isEmpty());
		System.out.println("Contents of SSHD_CONFIG:\n------------\n" + 
							sshdConfigContents + 
							"\n------------\n");
		String superuserEntry = getUserEntryInSSHD_CONFIG(superUser, sshdConfigContents); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		String testUser1Entry = getUserEntryInSSHD_CONFIG(testUser1.getUid(), sshdConfigContents); 
		assertNotNull(testUser1Entry);
		assertFalse(testUser1Entry.isEmpty());
		
		// connects to https://REST-stack-url:REST-port/proyectos and receives the list of existing projects
		response = getResponseJSON(
				"proyectos", null, "GET", HttpURLConnection.HTTP_OK, superUser, superUserPass);
		assertNotNull(response);
		Proyectos projects = new JSONDeserializer<Proyectos>().deserialize(response);
		boolean superadminsIsConfirmed = false;
		boolean testproj1IsConfirmed = false;
		for (String p : projects.listaProyectos) {
			System.out.println("UID#" + projects.listaProyectos.indexOf(p) + ": " + p);
			if (p.contentEquals("superadmins"))
				superadminsIsConfirmed = true;
			if (p.contentEquals(testProj1.getCn()))
				testproj1IsConfirmed = true;
		}
		assertTrue(superadminsIsConfirmed);
		assertTrue(testproj1IsConfirmed);
				
		//connect as superuser by SFTP
		String tmp1Path = createLFTPScriptTempFile(superUser, superUserPass, 
				new String[]{
				"echo Local dir:", "", "lpwd", 
				"echo Remote dir:", "pwd", "ls",
				"ls public",
				"ls private", 
				"cd private", "ls superadmins", "cd superadmins", "cd ..",
				"cd ../public",
				"ls superadmins", "cd superadmins"});
		String lftpCmd = "lftp -f " + tmp1Path;
		localCons.exec("echo Executing lftp: " + lftpCmd);
		localCons.exec(lftpCmd);
		
		//connect as Test User by SFTP
		String tmp2Path = createLFTPScriptTempFile(testUser1.getUid(), testUser1.getPass(), 
				new String[]{
				"echo Local dir:", "", "lpwd", 
				"echo Remote dir:", "pwd", "ls",
				"ls public",
				"ls private", 
				"cd private", "ls " + testProj1.getCn(), "cd " + testProj1.getCn(), "cd ..",
				"cd ../public",
				"ls " + testProj1.getCn(), "cd " + testProj1.getCn()});
		lftpCmd = "lftp -f " + tmp2Path;
		localCons.exec("echo Executing lftp: " + lftpCmd);
		localCons.exec(lftpCmd);
	}

	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes 2 correct entries.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_TwoEntries() {
		String text = sshJailMarker + "\nMatch User superforja\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n" +
				"Match User anotheruser\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n";
		String superuserEntry = getUserEntryInSSHD_CONFIG(superUser, text); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		System.out.println("Received matched text for 2 entries:\n------------\n" + 
				superuserEntry + "\n-------------\n");
	}

	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes one correct entry.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_OneEntry() {
		String text = sshJailMarker + "\nMatch User superforja\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n";
		String superuserEntry = getUserEntryInSSHD_CONFIG(superUser, text); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		System.out.println("Received matched text for 1 entry:\n------------\n" + 
				superuserEntry + "\n-------------\n");
	}

	/**
	 * Tests the failure of the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes one correct entry
	 * but NOT the Jail Marker (or placeholder).
	 */
	@Test
	public void testFailureSSHD_CONFIG_Pattern_OneEntry() {
		String text = "Match User superforja\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n";
		try {
			getUserEntryInSSHD_CONFIG(superUser, text);
		} catch (Error e) {
			assertTrue(e instanceof AssertionError);
			assertTrue(e.getClass().getName().equalsIgnoreCase(AssertionError.class.getName()));
			assertEquals("Unable to find the text " + sshJailMarker + " in the provided contents.", 
					e.getMessage());
		}
	}

	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes several correct entries.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_VariousEntries() {
		String text = sshJailMarker + "\nMatch User oneuser\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n" +
				"Match User superforja\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n" +
				"Match User anotheruser\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n";
		String superuserEntry = getUserEntryInSSHD_CONFIG(superUser, text); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		System.out.println("Received matched text for several entries:\n------------\n" + 
				superuserEntry + "\n-------------\n");
	}

	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes several bad entries.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_VariousEntries_BadText() {
		String text = sshJailMarker + "\nMatch User oneuser\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding Match User no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n" +
				"Match User superforja Match User\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n" +
				"Match User anotheruser\n" + 
				"	ChrootDirectory /var/files\n" +
				"	AllowTCPForwarding no\n" +
				"	X11Forwarding no\n" +
				"	ForceCommand internal-sftp\n" +
				"\n";
		String superuserEntry = getUserEntryInSSHD_CONFIG(superUser, text); 
		assertNull(superuserEntry);
	}

	/**
	 * Creates a temporary file in the system's tmp directory that
	 * will be used as a script with commands for the 
	 * <strong><a href="http://lftp.yar.ru/lftp-man.html">lftp</a></strong> system tool.
	 * The first command will be the open connection to the SFTP service offered
	 * by the installed stack running in the virtual machine. 
	 * @param user the user ID
	 * @param pass the password
	 * @param cmds a list with <strong>lftp</strong> commands
	 * @return the absolute path of the temporary file
	 * @throws IOException when the file could not be created
	 */
	private static String createLFTPScriptTempFile(String user, String pass, String[] cmds) throws IOException {
		File tmpLFTPScriptFile = File.createTempFile("scstackLFTP", "tmp");
		PrintWriter out = new PrintWriter(new FileWriter(tmpLFTPScriptFile));
		out.println("set cmd:fail-exit true");
		out.println("set net:max-retries 3");
		out.println("set net:reconnect-interval-base " + reconnInterval);
		out.println("open -u " + user + "," + pass + " sftp://" + stackIP);
		for (String cmd : cmds)
			out.println(cmd); 
		out.println("");
		out.close();
		tmpLFTPScriptFile.deleteOnExit();
		return tmpLFTPScriptFile.getAbsolutePath();
	}

	/**
	 * Parses the contents of the remote file SSHD_CONFIG
	 * and extracts the entry relative to the specified user.
	 * An important requirement of this file is that it contains
	 * the Jail Marker (or placeholder) added by the forge.
	 * What it comes after this placeholder will be analyzed to
	 * see if it includes the following line<br/>
	 * &nbsp;&nbsp;&nbsp;{@code Math User UID}<br/>
	 * If the placeholder is not found, this method fails 
	 * ({@link org.junit.Assert#fail}) with a message explaining the error.
	 * @param uid the user's ID
	 * @param sshd_configContents the contents of the file
	 * @return a String with the lines relative to the specified UID (separated by {@code \n}) 
	 */
	private static String getUserEntryInSSHD_CONFIG(String uid, String sshd_configContents) {
		String[] parts = sshd_configContents.split(sshJailMarker, 2);
		if (parts.length < 2)
			fail("Unable to find the text " + sshJailMarker + " in the provided contents.");
		String[] lines = parts[1].split("\\r\\n|\\r|\\n");
		int counter = 0;
		StringBuilder result = null;
		for (String l : lines) {
			String line = l.trim();
			if (line.isEmpty())
				continue;
			if (counter == 0 && line.compareToIgnoreCase("match user " + uid) == 0) {
				counter++;
				if (null == result)
					result = new StringBuilder();
				result.append(line);
			} else if (counter > 0) {
				if (line.toLowerCase().contains("match user")) {
					break;
				} else {	
					result.append("\n" + line.trim());
					counter++;
				}
			}
		}
		return (null == result) ? null : result.toString();
	}

	/**
	 * This method interacts to the REST service by using an user's credentials 
	 * ({@code uid:pass} in Base64 encoding).<br/>
	 * The protocol is HTTPS and this method will trust any certificate that 
	 * the other side (the REST service running remotely) is using.
	 * It employs the connection mechanism exported by the standard Java 
	 * <em>https</em> implementation (the classes {@link URL} and {@link HttpsURLConnection}.
	 * <br/>A description of the steps involved:
	 * <ul><li>constructs the URI by appending the relativeURI to the service's {@link #baseURL}</li> 
	 * <li>sends a request with the specified method type</li>
	 * <li>checks the expected response code with the one that receives</li>
	 * <li>returns the response message body (usually a JSONized String)</li></ul>
	 * @param relativeURI it will be appended to the {@link #baseURL}
	 * @param body optional body message, can be null
	 * @param method the request's method type (GET, POST, etc.)
	 * @param expectedCode an integer, should have one of the constant values 
	 * from {@link HttpURLConnection}:<br/>{@link HttpURLConnection#HTTP_OK}, 
	 * {@link HttpURLConnection#HTTP_BAD_REQUEST}, etc.
	 * @param user the user's ID
	 * @param pass the user's pass
	 * @return a String with the response message body
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws SCStackException 
	 */
	private static String getResponseJSON(String relativeURI, String body, String method, int expectedCode, 
			String user, String pass) throws IOException, NoSuchAlgorithmException, KeyManagementException, SCStackException {
		X509TrustManager tm = new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			@Override
			public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
			}
			@Override
			public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
			}
		};
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, new TrustManager[] { tm }, null);
		SSLContext.setDefault(ctx); 
		HttpsURLConnection conn = (HttpsURLConnection) new URL(baseURL, relativeURI).openConnection();
		conn.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		conn.setRequestMethod(method);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Accept-Charset", DEFAULT_CHARSET);
		conn.setRequestProperty("Authorization", "Basic " + getAuthString(user, pass));
		if (method.equalsIgnoreCase("POST") && body != null && !body.isEmpty()) {
			conn.setDoOutput(true); // Triggers POST.
			conn.setRequestProperty("Content-Type", "application/json;charset=" + DEFAULT_CHARSET);
			OutputStream output = null;
			try {
				output = conn.getOutputStream();
				output.write(body.getBytes(DEFAULT_CHARSET));
			} finally {
				if (output != null) 
					try { 
						output.close();
					} catch (IOException logOrIgnore) {}
			}
		}
		conn.connect();
		if (conn.getResponseCode() != expectedCode) {
			throw new SCStackException("Failed : HTTP error code : " + 
					conn.getResponseCode() + " instead of expected: " + expectedCode);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));
		StringBuffer response = new StringBuffer();
		int c;
		while ((c = br.read()) != -1) {
			response.append((char)c);
		}
		conn.disconnect();
		return response.toString();
	}

	/**
	 * Uses an external library ({@link com.unboundid.util.Base64}) to encode the
	 * user's credentials ({@code uid:pass}) that will be used to construct the REST requests.
	 * @param user the user's ID
	 * @param pass the password
	 * @return the credentials encoded in a Base64 format
	 */
	private static String getAuthString(String user, String pass) {
		String credentials = user + ":" + pass;
		String enc = com.unboundid.util.Base64.encode(credentials.getBytes());
		System.out.println("Base64Encoded for '" + credentials + "' is: '" + enc + "'");
		return enc;
	}
}
