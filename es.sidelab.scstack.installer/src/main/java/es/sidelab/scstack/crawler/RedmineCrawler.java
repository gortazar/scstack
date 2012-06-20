package es.sidelab.scstack.crawler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.support.PageFactory;

import es.sidelab.scstack.crawler.redmine.RedmineLoginPage;
/**
 * A crawler used for setting up the Redmine page before starting the REST service. 
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineCrawler extends Crawler {

	static final Logger LOG = Logger.getLogger(RedmineCrawler.class.getName());
	
	public RedmineCrawler(boolean enableJavaScript, CrawlerInfo info)
			throws CrawlerException {
		super(enableJavaScript, info, Logger.getLogger(RedmineCrawler.class.getName()));
	}

	@Override
	protected void setScriptTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setImplicitlyWaitTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAPIKey() throws CrawlerException {
		RedmineLoginPage rlp = this.toLoginPage(this.crawlerInfo.getUrl());
		rlp.performLogin("admin", "admin");
		return null;
	}

	private RedmineLoginPage toLoginPage(String path) throws CrawlerException {
		try {
			this.driver.get(path);
		} catch (org.openqa.selenium.WebDriverException e) {
			if (e.getCause() instanceof com.gargoylesoftware.htmlunit.ScriptException)
				LOG.log(Level.FINE, "Script Exception from WebDriver caught, will ignore it and continue.");
			else {
				throw new CrawlerException("Unknown exception from WebDriver", e);
			}
		}
		LOG.log(Level.FINER, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineLoginPage.class);
	}
}
