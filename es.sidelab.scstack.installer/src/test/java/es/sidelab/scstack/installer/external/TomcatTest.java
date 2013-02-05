package es.sidelab.scstack.installer.external;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import org.junit.Test;

public class TomcatTest {

	private static final String DOMAIN = "dominio";
	private static final String SCSTACK_CONF = "scstack.conf";
	private Properties conf;
	
	public void setUp() throws FileNotFoundException, IOException {
		conf = new Properties();
		conf.load(new FileReader(SCSTACK_CONF));
	}
	
	@Test
	public void jenkinsTest() {
		
		testConnection(getJenkinsURL());
		
	}

	@Test
	public void archivaTest() {
		
		testConnection(getArchivaURL());
		
	}	
	
	@Test
	public void tomcatManagerTest() {
		
		testConnection(getTomcatManagerURL());
		
	}

	@Test
	public void tomcatHostManagerTest() {
		
		testConnection(getTomcatHostManagerURL());
		
	}
	
	@Test
	public void tomcatDirectAccessTest() {
		
		URL url = null;
		try {
			url = new URL(getTomcatURI());
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		
		try {
			URLConnection conn = url.openConnection();
			fail("Connection to " + url.toString() + " accepted");
		} catch (IOException e) {
			// Ok
		}
		
	}
	
	private String getTomcatURI() {
		return new StringBuilder().append("http://").append(conf.get(DOMAIN)).append(":8080").toString();
	}

	private void testConnection(String path) {
		URL url = null;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			fail(e.getMessage());
		}
		
		try {
			URLConnection conn = url.openConnection();
			process(conn.getInputStream());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	private String getArchivaURL() {
		return new StringBuilder().append("https://").append(conf.get(DOMAIN)).append("/archiva").toString();
	}

	private String getJenkinsURL() {
		return new StringBuilder().append("https://").append(conf.get(DOMAIN)).append("/jenkins").toString();
	}

	private String getTomcatManagerURL() {
		return new StringBuilder().append("https://").append(conf.get(DOMAIN)).append("/manager").toString();
	}

	private String getTomcatHostManagerURL() {
		return new StringBuilder().append("https://").append(conf.get(DOMAIN)).append("/host-manager").toString();
	}

	private void process(InputStream inputStream) throws IOException {
		
		byte[] bytes = new byte[2048];
		int totalBytesRead = 0;
		int bytesRead;
		while((bytesRead = inputStream.read(bytes)) != -1) {
			totalBytesRead += bytesRead;
		}
		
		assertTrue(totalBytesRead > 0);
	}

}
