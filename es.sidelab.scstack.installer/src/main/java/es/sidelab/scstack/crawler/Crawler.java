package es.sidelab.scstack.crawler;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import es.sidelab.scstack.installer.LDAPConnection;

/**
 * Crawler superclass that should be subtyped whenever we want to create a new Crawler.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public abstract class Crawler {
	/* Logger instance, should be passed from the subclass */
	private Logger LOG;
	/* Disabling verbose warnings from the Selenium libraries*/
	static {
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");
	}
	/** Wait timeout for the WebDriver in seconds.
	 * <br>It dictates the time it waits for the page to load an element 
	 * before trying to select it as an WebElement.
	 * <br>Used to poll the DOM for a certain element(s) only for these
	 * many seconds.
	 * <br>Default value will be <strong>0</strong>.
	 * @see {@link Timeouts#implicitlyWait(long, TimeUnit)}
	 */
	int implicitlyWaitTimeout = 0;
	/** Asynchronous script timeout for the WebDriver in seconds.
	 * <br>It dictates the time it waits for a script to finish before
	 * throwing an error.
	 * <br>Default value will be <strong>0</strong>.
	 * @see {@link Timeouts#setScriptTimeout(long, TimeUnit)}
	 */
	int scriptTimeout = 0;
	/**
	 * The info (name, url, etc.) on this crawler. Should be passed in the constructor.
	 */
	CrawlerInfo crawlerInfo;
	/** 
	 * The {@link WebDriver}. 
	 */
	WebDriver driver;

	/**
	 * Constructor for the Crawler class.
	 * @param d a {@link WebDrivers} value to resolve which type of {@link WebDriver}
	 * this crawler will use.
	 * @param enableJavaScript a flag to enable JavaScript.
	 * @param info the name of the instantiated crawler, from {@link CrawlerInfo}.
	 * @param log an instantiated {@link Logger} of the subclass. 
	 * @throws CrawlerException if the {@link WebDriver} could not be created.
	 */
	public Crawler(boolean enableJavaScript, CrawlerInfo info, Logger log) throws CrawlerException {
		this.LOG = log;
		try {
			this.driver = new HtmlUnitDriver(enableJavaScript);
			this.crawlerInfo = info;
			setImplicitlyWaitTimeout();
			setScriptTimeout();
			setTimeouts();
			LOG.log(Level.INFO, "New <{0}> crawler instance with HtmlUnitDriver (JS - {1}).",
					new Object[]{
					this.crawlerInfo.getName(), enableJavaScript});
		} catch (Exception e) {
			this.closeDriver();
			throw new CrawlerException("Exception creating HtmlUnitDriver:", e);
		}
	}

	public abstract String getAPIKey(String user, String pass, LDAPConnection conn) throws CrawlerException;
	/**
	 * Change the default {@link Crawler#scriptTimeout} if required.
	 */
	protected abstract void setScriptTimeout();
	/**
	 * Change the {@link Crawler#implicitlyWaitTimeout} if required.
	 */
	protected abstract void setImplicitlyWaitTimeout();
	
	/**
	 * Closes the web driver if not null.
	 */
	protected void closeDriver() {
		if (null != this.driver)
			this.driver.quit();
	}
	/**
	 * The name of the crawler.
	 * @return
	 */
	public String getName() {
		return this.crawlerInfo.getName();
	}
	/**
	 * Set the WebDriver timeouts.
	 */
	private void setTimeouts() {
		this.driver.manage().timeouts().implicitlyWait(implicitlyWaitTimeout, TimeUnit.SECONDS);
		this.driver.manage().timeouts().setScriptTimeout(scriptTimeout, TimeUnit.SECONDS);
	}
}