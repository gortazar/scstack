package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Class following the PageObject pattern, used to represent
 * the General tab of the Settings page from Redmine.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineGeneralSettingsPage {
	static final Logger LOG = Logger.getLogger(RedmineGeneralSettingsPage.class.getName());
	
	@FindBy(css = "#tab-authentication")
	private WebElement authTab; 
	
	/**
	 * Changes the Settings page by selecting the Authentication tab.
	 * @return true if successfully clicked the required link, false otherwise.
	 */
	public boolean goToAuthTab() {
		try {
			authTab.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the authentication tab (NoSuchElementException).");
			return false;
		}
		return true;
	}
}
