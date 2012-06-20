package es.sidelab.scstack.crawler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.support.PageFactory;

import es.sidelab.scstack.crawler.redmine.RedmineAdminPage;
import es.sidelab.scstack.crawler.redmine.RedmineHomePage;
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

	/**
	 * Returns the REST API key from the Redmine site.
	 * @param user
	 * @param pass
	 * @return
	 * @throws CrawlerException
	 */
	@Override
	public String getAPIKey(String user, String pass) throws CrawlerException {
		try {
			RedmineLoginPage rlp = this.toLoginPage(this.crawlerInfo.getUrl());
			if (!rlp.performLogin(user, pass)) {
				System.out.println("Couldn't log in as " + user + " with pass " + pass);
				return null;
			}
			RedmineHomePage rhp = this.toHomePage();
			String whois = rhp.getLoggedAs();
			if (null == whois || whois.isEmpty() || !whois.contains(user))
				return null;
			LOG.log(Level.INFO, "Logged as message: {0}", whois);
			if (! rhp.goToAdminPage()) {
				System.out.println("Couldn't go to the admin page!");
				return null;
			}
			RedmineAdminPage rap = this.toAdminPage();
			if (! rap.loadDefaultConfiguration()) {
				System.out.println("Wasn't able to load the default config!");
				return null;
			}
			return whois;
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Driver was at {0} when:\nException related to the driver: {1}", 
					new Object[]{this.driver.getCurrentUrl(), e.getLocalizedMessage()});
			throw new CrawlerException("Exception from selenium driver", e);
		} finally {
			this.closeDriver();
		}
	}
	/**
	 * Creates a new RedmineLoginPage based on the specified location. 
	 * @param path
	 * @return
	 */
	private RedmineLoginPage toLoginPage(String path) throws CrawlerException {
		try {
			this.driver.get(path);
		} catch (org.openqa.selenium.WebDriverException e) {
			if (e.getCause() instanceof com.gargoylesoftware.htmlunit.ScriptException)
				LOG.log(Level.INFO, "Script Exception from WebDriver caught, will ignore it and continue.");
			else {
				throw new CrawlerException("Unknown exception from WebDriver", e);
			}
		}
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineLoginPage.class);
	}

	/**
	 * Creates a new RedmineHomePage based on the current location. 
	 * @return
	 */
	private RedmineHomePage toHomePage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineHomePage.class);
	}
	
	/**
	 * Creates a new RedmineAdminPage based on the current location. 
	 * @return
	 */
	private RedmineAdminPage toAdminPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineAdminPage.class);
	}
}
