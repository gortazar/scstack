package es.sidelab.scstack.crawler.redmine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;
/**
 * Class following the PageObject pattern, used to represent
 * the Authentication tab of the Settings page from Redmine.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineAuthenticationSettingsPage {
	static final Logger LOG = Logger.getLogger(RedmineAuthenticationSettingsPage.class.getName());
	
	private final static String DISABLE_SELF_REGISTRATION_TEXT = "disabled";
	
	/**
	 * There are 2 elements with the same ID.
	 * One is hidden and the other is the checkbox that we want.
	 */
	@FindBy(css = "#settings_login_required")
	private List<WebElement> authRequiredElements; 
	
	@FindBy(css = "#settings_self_registration")
	private WebElement selfRegSelect; 
	
	/**
	 * There are 2 elements with the same ID.
	 * One is hidden and the other is the checkbox that we want.
	 */
	@FindBy(css = "#settings_rest_api_enabled")
	private List<WebElement> restWSElements;
	
	@FindBy(linkText = "LDAP authentication")
	private WebElement linkLDAP;
	
	/**
	 * Configures the Authentication settings:
	 * <ul><li>Check Authentication required</li>
	 * <li>Disable self-registration</li>
	 * <li>Enable REST web service</li>
	 * </ul> 
	 */
	public void configureAuthentication() {
		try {
			for (WebElement auth : authRequiredElements)
				if (auth.getAttribute("type").equalsIgnoreCase("checkbox") && ! auth.isSelected())
					auth.click();
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the auth required checkbox " +
					"(NoSuchElementException for css = '#settings_login_required').");
		}
		try {
			new Select(selfRegSelect).selectByVisibleText(DISABLE_SELF_REGISTRATION_TEXT);
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the self registration Select tag " +
					"(NoSuchElementException for css = '#settings_self_registration').");
		}
		try {
			for (WebElement rest : restWSElements)
				if (rest.getAttribute("type").equalsIgnoreCase("checkbox") && ! rest.isSelected())
					rest.click();
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the Enable REST WS checkbox " +
					"(NoSuchElementException for css = '#settings_rest_api_enabled').");
		}
	}
	
	/**
	 * Goes to the LDAP Authentication page.
	 * @return true if successfully clicked the required link, false otherwise.
	 */
	public boolean goToLDAPAuthPage() {
		try {
			linkLDAP.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the LDAP authentication link (NoSuchElementException).");
			return false;
		}
		return true;
	}
}
