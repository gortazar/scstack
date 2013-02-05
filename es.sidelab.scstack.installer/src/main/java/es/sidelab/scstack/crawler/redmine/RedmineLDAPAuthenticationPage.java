package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import es.sidelab.scstack.installer.LDAPConnection;
/**
 * Class following the PageObject pattern, used to represent
 * the LDAP Authentication page from Redmine.
 * The strategy for this page should be:<ol>
 * <li>Click New auth mode.</li>
 * <li>{@code <Load a new object of this type>}</li>
 * <li>Fill in the details and click the Create button.</li>
 * <li>{@code <Load a new object of this type>}</li>
 * <li>Check the creation message (if successful or not).</li>
 * <li>Click the Test connection link.</li>
 * <li>{@code <Load a new object of this type>}</li>
 * <li>Check the testing message.</li>
 * <li>Click the My account link.</li></ol>
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class RedmineLDAPAuthenticationPage {
	static final Logger LOG = Logger.getLogger(RedmineLDAPAuthenticationPage.class.getName());
	
	@FindBy(linkText = "New authentication mode")
	private WebElement newAuthMode;
	
	@FindBy(css = "#auth_source_name")
	private WebElement name;
	
	@FindBy(css = "#auth_source_host")
	private WebElement host;
	
	@FindBy(css = "#auth_source_port")
	private WebElement port;
	
	@FindBy(css = "#auth_source_tls")
	private WebElement tlsCheckbox;
	
	@FindBy(css = "#auth_source_account")
	private WebElement account;
	
	@FindBy(css = "#auth_source_account_password")
	private WebElement accountPassword;
	
	@FindBy(css = "#auth_source_base_dn")
	private WebElement baseDN;
	
	@FindBy(css = "#auth_source_onthefly_register")
	private WebElement onTheFlyCheckbox;
	
	@FindBy(css = "#auth_source_attr_login")
	private WebElement login;
	
	@FindBy(css = "#auth_source_attr_firstname")
	private WebElement firstName;
	
	@FindBy(css = "#auth_source_attr_lastname")
	private WebElement lastName;
	
	@FindBy(css = "#auth_source_attr_mail")
	private WebElement email;
	
	@FindBy(name = "commit")
	private WebElement createButton;
	
	@FindBy(css = ".flash.notice")
	private WebElement notice;
	
	@FindBy(linkText = "Test")
	private WebElement testConn;
	
	@FindBy(linkText = "My account")
	private WebElement myAccountLink;
	
	/**
	 * Clicks the new auth mode link. You should reload this page object after this action.
	 * @return true if successfully clicked the required link, false otherwise.
	 */
	public boolean createNewAuth() {
		try {
			newAuthMode.click();
		} catch (NoSuchElementException e) {
			LOG.info("Can't find the new authentication mode link (NoSuchElementException).");
			return false;
		}
		return true;
	}
	
	/**
	 * Configures a new LDAP connection using the values from the specified object.
	 * Clicks the Create button in the end, so you should reload the page 
	 * (re-apply the pattern).
	 * @param conn LDAPConnection object holding the values for a new connection
	 */
	public void configureNewLDAPConn(LDAPConnection conn) {
		try {
			this.name.sendKeys(conn.getName());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the name text field " +
					"(NoSuchElementException for css = '#auth_source_name').");
		}
		try {
			this.host.sendKeys(conn.getHost());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the host text field " +
					"(NoSuchElementException for css = '#auth_source_host').");
		}
		try {
			this.port.sendKeys(conn.getPort());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the port text field " +
					"(NoSuchElementException for css = '#auth_source_port').");
		}
		try { 
			// click the checkbox when its value (isSelected) 
			//  it's not the same as the connection's tlsActivated
			if (this.tlsCheckbox.isSelected() != conn.isTlsActivated())
				this.tlsCheckbox.click();
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the LADPS checkbox " +
					"(NoSuchElementException for css = '#auth_source_tls').");
		}
		try {
			this.account.sendKeys(conn.getAccount());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the account text field " +
					"(NoSuchElementException for css = '#auth_source_account').");
		}
		try {
			this.accountPassword.sendKeys(conn.getAccountPassword());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the account password field " +
					"(NoSuchElementException for css = '#auth_source_account_password').");
		}
		try {
			this.baseDN.sendKeys(conn.getBaseDN());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the base DN text field " +
					"(NoSuchElementException for css = '#auth_source_base_dn').");
		}
		try { 
			// click the checkbox when its value (isSelected) 
			//  it's not the same as the connection's on-the-fly user creation
			if (this.onTheFlyCheckbox.isSelected() != conn.isOnTheFlyCreation())
				this.onTheFlyCheckbox.click();
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the on-the-fly user creation checkbox " +
					"(NoSuchElementException for css = '#auth_source_onthefly_register').");
		}
		try {
			this.login.sendKeys(conn.getLogin());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the login text field " +
					"(NoSuchElementException for css = '#auth_source_attr_login').");
		}
		try {
			this.firstName.sendKeys(conn.getFirstName());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the first name text field " +
					"(NoSuchElementException for css = '#auth_source_attr_firstname').");
		}
		try {
			this.lastName.sendKeys(conn.getLastName());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the last name text field " +
					"(NoSuchElementException for css = '#auth_source_attr_lastname').");
		}
		try {
			this.email.sendKeys(conn.getEmail());
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the email text field " +
					"(NoSuchElementException for css = '#auth_source_attr_mail').");
		}
		try {
			this.createButton.click();
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the Create button " +
					"(NoSuchElementException for name = 'commit').");
		}
	}
	
	/**
	 * Tests the message that appears after a successful creation of a new connection mode.
	 * @return true if found and the message checks, false otherwise
	 */
	public boolean isCreationSuccessful() {
		try {
			String flashText = notice.getText();
			if (flashText.equalsIgnoreCase("successful creation.") ||
					flashText.toLowerCase().contains("successful creation"))
				return true;
			else
				LOG.log(Level.INFO, "Creation of new LDAP conn was not successful, message is: " 
							+ flashText);
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the flash notice message " +
					"(NoSuchElementException for css = '.flash.notice').");
		}
		return false;
	}
	
	/**
	 * Clicks the Test link (the first one encountered, 
	 * if there are more than one.. well, 
	 * it's your problem, there shouldn't be more than 1 connection created).
	 * <br/> Re-apply the pattern to check the resulting message.  
	 * @return true if clicked OK, false otherwise
	 */
	public boolean clickTestConnection() {
		try {
			testConn.click();
			return true;
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the Test connection link " +
					"(NoSuchElementException for linkText = 'Test').");
		}
		return false;
	}
	
	/**
	 * Tests the message that appears after clicking the Test link.
	 * @return true if found and the message checks, false otherwise
	 */
	public boolean isTestingSuccessful() {
		try {
			String flashText = notice.getText();
			if (flashText.equalsIgnoreCase("successful connection.") ||
					flashText.toLowerCase().contains("successful connection"))
				return true;
			else
				LOG.log(Level.INFO, "Testing LDAP conn not successful, message is: " + flashText);
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the flash notice message " +
					"(NoSuchElementException for css = '.flash.notice').");
		}
		return false;
	}
	
	/**
	 * Clicks the My Account link
	 * @return true if clicked OK, false otherwise
	 */
	public boolean clickMyAccount() {
		try {
			myAccountLink.click();
			return true;
		} catch (NoSuchElementException e) {
			LOG.log(Level.INFO, "Unable to find the My Account link " +
					"(NoSuchElementException for linkText = 'My account').");
		}
		return false;
	}
}
