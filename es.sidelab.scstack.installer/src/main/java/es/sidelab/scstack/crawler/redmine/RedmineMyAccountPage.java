package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Class following the PageObject pattern, used to represent
 * the My Account page from Redmine.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineMyAccountPage {
	static final Logger LOG = Logger.getLogger(RedmineMyAccountPage.class.getName());
	
	@FindBy(id = "api-access-key")
	private WebElement apiKey; 
	
	/**
	 * Gets the API key (it's display style is none).
	 * @return the key if found, null in case of error
	 */
	public String getAPIKey() {
		try {
			return apiKey.getText();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the API key element (NoSuchElementException for id = 'api-access-key').");
		}
		return null;
	}
}
