package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Class following the PageObject pattern, used to represent
 * the home page from the Redmine page (after the user has been logged in).
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineHomePage {
	static final Logger LOG = Logger.getLogger(RedmineHomePage.class.getName());
	
	@FindBy(css = "div#loggedas")
	private WebElement loggedAs;
	
	@FindBy(linkText = "Administration")
	private WebElement adminLink;
	
	public String getLoggedAs() {
		try {
			return loggedAs.getText();
		} catch (NoSuchElementException e) {
			LOG.info("Not able to find who's logged in (NoSuchElementException for id = 'loggedas').");
			return null;
		}
	}
	
	public boolean goToAdminPage() {
		try {
			adminLink.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the admin link (NoSuchElementException for linkText = 'Administration').");
			return false;
		}
		return true;
	}
}
