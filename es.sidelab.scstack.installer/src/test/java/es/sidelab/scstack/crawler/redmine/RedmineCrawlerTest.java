package es.sidelab.scstack.crawler.redmine;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.sidelab.scstack.crawler.CrawlerException;
import es.sidelab.scstack.crawler.CrawlerInfo;
import es.sidelab.scstack.crawler.RedmineCrawler;

public class RedmineCrawlerTest {
	private RedmineCrawler rcJSEnabled;
	
	@Before
	public void setUp() throws CrawlerException {
		rcJSEnabled = new RedmineCrawler(true, new CrawlerInfo("redmine", "http://stack.sidelab.es"));
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testGetAPIKey() {
		try {
			String api = rcJSEnabled.getAPIKey();
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
