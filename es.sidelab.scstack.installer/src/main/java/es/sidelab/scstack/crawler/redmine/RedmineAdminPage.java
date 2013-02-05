package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Class following the PageObject pattern, used to represent
 * the Administration page from Redmine (after the user has been logged in and clicked the Admin link).
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineAdminPage {
	static final Logger LOG = Logger.getLogger(RedmineAdminPage.class.getName());
	
	@FindBy(name = "commit")
	private WebElement loadDefaultConfig;
	
	@FindBy(linkText = "Roles and permissions")
	private WebElement roles; 
	
	public boolean loadDefaultConfiguration() {
		try {
			loadDefaultConfig.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the load default config button (NoSuchElementException for name = 'commit').");
			return false;
		}
		return true;
	}
	
	public boolean goToRoles() {
		try {
			roles.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the Roles and permissions link (NoSuchElementException).");
			return false;
		}
		return true;
	}
}
