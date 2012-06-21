package es.sidelab.scstack.crawler.redmine;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.sidelab.scstack.crawler.Crawler;
import es.sidelab.scstack.crawler.CrawlerException;
import es.sidelab.scstack.crawler.CrawlerInfo;
import es.sidelab.scstack.crawler.RedmineCrawler;
import es.sidelab.scstack.installer.Instalacion;
import es.sidelab.scstack.installer.LDAPConnection;

public class RedmineCrawlerTest {
	private Crawler rcJSEnabled;
	private LDAPConnection conn;
	private Properties config;
	
	@Before
	public void setUp() throws Exception {
		config = Instalacion.cargarConfiguracion("src/main/resources");
		rcJSEnabled = new RedmineCrawler(true, new CrawlerInfo("redmine", "http://localhost/login"));
		conn = new LDAPConnection.Builder("textconn")
					.host(config.getProperty("hostLDAP"))
					.port(config.getProperty("puertoLDAP"))
					.account(config.getProperty("bindDN"))
					.accountPassword(config.getProperty("passBindDN"))
					.baseDN(config.getProperty("baseDN"))
					.build();
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testGetAPIKey() {
		try {
			String api = rcJSEnabled.getAPIKey("admin", "admin", conn);
			assertTrue(null != api && !api.isEmpty());
		} catch (Exception e) {
			assertEquals(CrawlerException.class, e.getClass());
			e.printStackTrace();
		}
	}

	@Test
	public void testRedmineCrawler() {
		fail("Not yet implemented");
	}

	@Test
	public void testCloseDriver() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetName() {
		fail("Not yet implemented");
	}

}
