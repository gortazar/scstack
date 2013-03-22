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

public class RedmineTest {

	private static final String DOMAIN = "dominio";
	private static final String SCSTACK_CONF = "scstack.conf";
	private Properties conf;
	
	public void setUp() throws FileNotFoundException, IOException {
		conf = new Properties();
		conf.load(new FileReader(SCSTACK_CONF));
	}
	
	@Test
	public void test() {
		
		URL url = null;
		try {
			url = new URL(getRedmineURL());
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

	private void process(InputStream inputStream) throws IOException {
		
		byte[] bytes = new byte[2048];
		int totalBytesRead = 0;
		int bytesRead;
		while((bytesRead = inputStream.read(bytes)) != -1) {
			totalBytesRead += bytesRead;
		}
		
		assertTrue(totalBytesRead > 0);
	}

	private String getRedmineURL() {
		return new StringBuilder().append("http://").append(conf.get(DOMAIN)).toString();
	}

}
