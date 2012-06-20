package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Class following the PageObject pattern, used to represent
 * the login page from the Redmine page.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineLoginPage {
	
	static final Logger LOG = Logger.getLogger(RedmineLoginPage.class.getName());
	
	@FindBy(css = "input#user")
	private WebElement inputUser;

	@FindBy(css = "input#password")
	private WebElement inputPassword;

	@FindBy(css = "input.LoginButton")
	private WebElement loginButton;
	
	public boolean performLogin(String user, String pass) {
		try {
			inputUser.sendKeys(user);
		} catch (NoSuchElementException e) {
			LOG.info("Not able to login, inputUser (input#user) not present (NoSuchElementException).");
			return false;
		} 
		try {
			inputPassword.sendKeys(pass);
		} catch (NoSuchElementException e) {
			LOG.info("Not able to login, inputPassword (input#password) not present (NoSuchElementException).");
			return false;
		} 
		try {
			loginButton.click();		
		} catch (NoSuchElementException e) {
			LOG.info("Not able to login, loginButton (input.LoginButton) not present (NoSuchElementException).");
			return false;
		} 
		LOG.finer("Logged in successfully.");
		return true;
	}

}
