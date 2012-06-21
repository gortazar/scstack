package es.sidelab.scstack.crawler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.support.PageFactory;

import es.sidelab.scstack.crawler.redmine.RedmineAdminPage;
import es.sidelab.scstack.crawler.redmine.RedmineAuthenticationSettingsPage;
import es.sidelab.scstack.crawler.redmine.RedmineGeneralSettingsPage;
import es.sidelab.scstack.crawler.redmine.RedmineHomePage;
import es.sidelab.scstack.crawler.redmine.RedmineLDAPAuthenticationPage;
import es.sidelab.scstack.crawler.redmine.RedmineLoginPage;
import es.sidelab.scstack.crawler.redmine.RedmineManagerPage;
import es.sidelab.scstack.crawler.redmine.RedmineMyAccountPage;
import es.sidelab.scstack.crawler.redmine.RedmineRolesPage;
import es.sidelab.scstack.installer.LDAPConnection;
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
	 * @param conn
	 * @return
	 * @throws CrawlerException
	 */
	@Override
	public String getAPIKey(String user, String pass, LDAPConnection conn) throws CrawlerException {
		String api = null;
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
				//return null;
			}
			if (! rap.goToRoles()) {
				System.out.println("Unable to get to the Roles and permissions page!");
				return null;
			}
			RedmineRolesPage rrp = this.toRolesPage();
			if (! rrp.openManagerPage()) {
				System.out.println("Unable to get to the Manager tab!");
				return null;
			}
			RedmineManagerPage rmp = this.toManagerPage();
			if (! rmp.uncheckPermissions()) {
				System.out.println("Unable to remove certain permissions from the Manager role!");
				return null;
			}
			if (! rmp.goToSettingsPage()) {
				System.out.println("Unable to get to the Settings page!");
				return null;
			}
			RedmineGeneralSettingsPage rgsp = this.toSettingsPage();
			if (! rgsp.goToAuthTab()) {
				System.out.println("Unable to get to the Authentication tab in the Settings page!");
				return null;
			}
			RedmineAuthenticationSettingsPage rasp = this.toAuthSettingsPage();
			rasp.configureAuthentication();
			if (! rasp.goToLDAPAuthPage()) {
				System.out.println("Unable to get to the LDAP Authentication page!");
				return null;
			}
			RedmineLDAPAuthenticationPage rlap = this.toLDAPAuthPage();
			if (! rlap.createNewAuth()) {
				System.out.println("Unable to create a new LDAP Authentication mode!");
				return null;
			}
			rlap = this.toLDAPAuthPage();
			rlap.configureNewLDAPConn(conn);
			rlap = this.toLDAPAuthPage();
			if (! rlap.isCreationSuccessful()) {
				System.out.println("Creation of the new LDAP connection was not successful!");
				return null;
			}
			if (! rlap.clickTestConnection()) {
				System.out.println("Unable to click Test for the newly created LDAP connection!");
				return null;
			}
			rlap = this.toLDAPAuthPage();
			if (! rlap.isTestingSuccessful()) {
				System.out.println("The new LDAP connection testing was not successful!");
				return null;
			}
			if (! rlap.clickMyAccount()) {
				System.out.println("Unable to get to the My Account page!");
				return null;
			}
			RedmineMyAccountPage rmap = this.toMyAccountPage();
			api = rmap.getAPIKey();
			if (api != null) {
				System.out.println("Recovered API Key: " + api);
			} else {
				System.out.println("API key is null, so it couldn't be retrieved!!");
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Driver was at {0} when:\nException related to the driver: {1}", 
					new Object[]{this.driver.getCurrentUrl(), e.getLocalizedMessage()});
			throw new CrawlerException("Exception from selenium driver", e);
		} finally {
			this.closeDriver();
		}
		return api;
	}
	/**
	 * Creates a new RedmineLoginPage object based on the specified location. 
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
	 * Creates a new RedmineHomePage object based on the current location. 
	 * @return
	 */
	private RedmineHomePage toHomePage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineHomePage.class);
	}
	/**
	 * Creates a new RedmineAdminPage object based on the current location. 
	 * @return
	 */
	private RedmineAdminPage toAdminPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineAdminPage.class);
	}
	/**
	 * Creates a new RedmineRolesPage object based on the current location. 
	 * @return
	 */
	private RedmineRolesPage toRolesPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineRolesPage.class);
	}
	/**
	 * Creates a new RedmineManagerPage object based on the current location. 
	 * @return
	 */
	private RedmineManagerPage toManagerPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineManagerPage.class);
	}
	/**
	 * Creates a new RedmineGeneralSettingsPage object based on the current location. 
	 * @return
	 */
	private RedmineGeneralSettingsPage toSettingsPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineGeneralSettingsPage.class);
	}
	/**
	 * Creates a new RedmineAuthenticationSettingsPage object based on the current location. 
	 * @return
	 */
	private RedmineAuthenticationSettingsPage toAuthSettingsPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineAuthenticationSettingsPage.class);
	}
	/**
	 * Creates a new RedmineLDAPAuthenticationPage object based on the current location. 
	 * @return
	 */
	private RedmineLDAPAuthenticationPage toLDAPAuthPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineLDAPAuthenticationPage.class);
	}
	/**
	 * Creates a new RedmineMyAccountPage object based on the current location. 
	 * @return
	 */
	private RedmineMyAccountPage toMyAccountPage() {
		LOG.log(Level.INFO, "The driver is at: {0}", this.driver.getCurrentUrl());
		return PageFactory.initElements(this.driver, RedmineMyAccountPage.class);
	}
	
}
