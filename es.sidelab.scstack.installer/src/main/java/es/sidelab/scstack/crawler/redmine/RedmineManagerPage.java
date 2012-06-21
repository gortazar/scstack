package es.sidelab.scstack.crawler.redmine;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
/**
 * Class following the PageObject pattern, used to represent
 * the Manager role page from Redmine.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineManagerPage {
	static final Logger LOG = Logger.getLogger(RedmineManagerPage.class.getName());
	
	/**	There are 4 permissions that need to be checked. */
	private final static int PERMISSIONS_TO_UNCHECK = 4;
	
	@FindBy(css = "#role_permissions_")
	private List<WebElement> permissionsCheckboxes; 
	
	@FindBy(linkText = "Settings")
	private WebElement settings; 
	
	//TODO @FindBy commit...
	
	/**
	 * Unchecks the following permissions from the role Manager:
	 * <ul><li>Create project</li>
	 * <li>Edit project</li>
	 * <li>Manage members</li>
	 * <li>Create subprojects</li></ul> 
	 * @return true if successful, false otherwise
	 */
	public boolean uncheckPermissions() {
		int unchecked = 0;
		try {
			for(WebElement p : permissionsCheckboxes) {
				if (p.getAttribute("value").equalsIgnoreCase("add_project")) {
					if (p.isSelected()) {
						p.click();
					}
					unchecked++;
				}
				if (p.getAttribute("value").equalsIgnoreCase("edit_project")) {
					if (p.isSelected()) {
						p.click();
					}
					unchecked++;
				}
				if (p.getAttribute("value").equalsIgnoreCase("manage_members")) {
					if (p.isSelected()) {
						p.click();
					}
					unchecked++;
				}
				if (p.getAttribute("value").equalsIgnoreCase("add_subprojects")) {
					if (p.isSelected()) {
						p.click();
					}
					unchecked++;
				}
			}
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the manager's permissions checkboxes " +
					"(NoSuchElementException for css = '#role_permissions_').");
		}
		if (unchecked != PERMISSIONS_TO_UNCHECK) {
			LOG.log(Level.INFO, "Unable to find all {0} required checkboxes!", PERMISSIONS_TO_UNCHECK);
			return false;
		}
		return true;
	}
	
	/**
	 * Goes to the Settings page.
	 * @return true if successfully clicked the required link, false otherwise.
	 */
	public boolean goToSettingsPage() {
		try {
			settings.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the Settings link (NoSuchElementException).");
			return false;
		}
		return true;
	}
}
