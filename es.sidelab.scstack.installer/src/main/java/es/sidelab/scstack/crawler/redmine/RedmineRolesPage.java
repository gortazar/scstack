package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Class following the PageObject pattern, used to represent
 * the Roles page from Redmine.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineRolesPage {
	static final Logger LOG = Logger.getLogger(RedmineRolesPage.class.getName());
	
	@FindBy(linkText = "Manager")
	private WebElement manager; 
	
	/**
	 * Goes to the Manager's permissions page.
	 * @return true if successfully clicked the required link, false otherwise.
	 */
	public boolean openManagerPage() {
		try {
			manager.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the Manager link (NoSuchElementException).");
			return false;
		}
		return true;
	}
}
