package es.sidelab.scstack.service;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import es.sidelab.commons.commandline.ExecutionCommandException;
import es.sidelab.commons.commandline.console.Console;
import es.sidelab.commons.commandline.console.LocalConsole;
import es.sidelab.commons.commandline.console.SSHConsole;
import es.sidelab.scstack.service.data.Usuarios;
import flexjson.JSONDeserializer;

/**
 * Tests a running forge.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RemoteIT {
	private static final String SSHD_CONFIG = "/etc/ssh/sshd_config";
	
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
	}

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
	 * Checks the user' list. The super user should be included.
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ExecutionCommandException 
	 */
	@Test
	public void testUIDs() throws NoSuchAlgorithmException, 
									KeyManagementException, 
									MalformedURLException, 
									IOException, 
									ExecutionCommandException {
		String response = getResponseJSON("usuarios", "GET", HttpURLConnection.HTTP_OK);
		assertNotNull(response);
		Usuarios uidUsers = new JSONDeserializer<Usuarios>().deserialize(response);
		boolean superUserIsConfirmed = false;
		for (String u : uidUsers.listaUsuarios) {
			System.out.println("UID#" + uidUsers.listaUsuarios.indexOf(u) + ": " + u);
			if (u.contentEquals(superUser))
				superUserIsConfirmed = true;
		}
		assertTrue(superUserIsConfirmed);
		try {
			System.out.println("Entry for the Superuser in the passwd file:");
			sshCons.exec("getent passwd | grep " + superUser);
		} catch (ExecutionCommandException e) {
			System.err.println("Failed looking for the superuser in the passwd file!");
			System.err.println("The whole list is:\n-----------------------------");
			sshCons.exec("getent passwd");
			fail(e.getMessage() + "Std: " + e.getStandardOutput() + "Err:" + e.getErrorOutput());
		}
		String sshdConfigContents = sshCons.getFile(SSHD_CONFIG);
		assertNotNull(sshdConfigContents);
		assertFalse(sshdConfigContents.isEmpty());
		String[] parts = sshdConfigContents.split(sshJailMarker, 2);
		if (parts.length < 2)
			fail("Unable to find the text " + sshJailMarker + " in the remote file " + SSHD_CONFIG);
		System.out.println(parts[1]);
		String superuserEntry = getUserEntry(superUser, parts[1]); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		//TODO
		//localCons.exec("lftp -u " + superUser + "," + superUserPass + " sftp://" + stackIP + ";ls;exit");
		
	}

	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes 2 correct entries.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_TwoEntries() {
		String text = "Match User superforja\n" + 
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
		String superuserEntry = getUserEntry(superUser, text); 
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
		String text = "Match User superforja\n" + 
					"	ChrootDirectory /var/files\n" +
					"	AllowTCPForwarding no\n" +
					"	X11Forwarding no\n" +
					"	ForceCommand internal-sftp\n" +
					"\n";
		String superuserEntry = getUserEntry(superUser, text); 
		assertNotNull(superuserEntry);
		assertFalse(superuserEntry.isEmpty());
		System.out.println("Received matched text for 1 entry:\n------------\n" + 
							superuserEntry + "\n-------------\n");
	}
	
	/**
	 * Tests the local method that retrieves an entry from the file SSHD_CONFIG,
	 * by sending it a mocked version of the file. This one includes several correct entries.
	 */
	@Test
	public void testSSHD_CONFIG_Pattern_VariousEntries() {
		String text = "Match User oneuser\n" + 
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
		String superuserEntry = getUserEntry(superUser, text); 
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
		String text = "Match User oneuser\n" + 
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
		String superuserEntry = getUserEntry(superUser, text); 
		assertNull(superuserEntry);
	}
	
	/**
	 * This method interacts to the REST service by using the superuser's credentials 
	 * ({@code uid:pass} in Base64 encoding). The protocol is HTTPS and
	 * this method will trust any certificate that the other side is using.
	 * It employs the connection mechanism exported by the standard Java https implementation
	 * (the classes {@link URL} and {@link HttpsURLConnection}.
	 * A description of the steps involved:
	 * <ul><li>constructs the URI by appending the relativeURI to the service's {@link #baseURL}</li> 
	 * <li>sends a request with the specified method type</li>
	 * <li>checks the expected response code with the one that receives</li>
	 * <li>returns the response message body (usually a JSONized String)</li></ul>
	 * @param relativeURI it will be appended to the {@link #baseURL}
	 * @param method the request's method type (GET, POST, etc.)
	 * @param expectedCode an integer, should have one of the constant values 
	 * from {@link HttpURLConnection}:<br/>{@link HttpURLConnection#HTTP_OK}, 
	 * {@link HttpURLConnection#HTTP_BAD_REQUEST}, etc.
	 * @return a String with the response message body
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	private static String getResponseJSON(String relativeURI, String method, int expectedCode) throws IOException, NoSuchAlgorithmException, KeyManagementException {
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
		conn.setRequestProperty("Authorization", "Basic " + getAuthString());
		conn.connect();
		if (conn.getResponseCode() != expectedCode) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode() + " instead of expected: " + expectedCode);
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
	 * superuser's credentials ({@code uid:pass}) that will be used to construct the REST requests. 
	 * @return the credentials encoded in a Base64 format
	 */
	private static String getAuthString() {
		String credentials = superUser + ":" + superUserPass;
		String enc = com.unboundid.util.Base64.encode(credentials.getBytes());
		System.out.println("Base64Encoded for '" + credentials + "' is: '" + enc + "'");
		return enc;
	}
	
	private static String getUserEntry(String uid, String text) {
		String[] lines = text.split("\\r\\n|\\r|\\n");
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
}
