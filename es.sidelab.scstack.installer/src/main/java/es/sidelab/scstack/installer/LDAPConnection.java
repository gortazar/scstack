package es.sidelab.scstack.installer;

/**
 * This stores the details for a LDAP Connection mode from Redmine.
 * The values should be extracted from a configuration file.
 * It uses the Builder pattern.
 * The values for {@code login}, {@code firstName}, {@code lastName} and {@code email} 
 * are already defined ({@code uid, cn, sn, mail}). Two boolean atributes, 
 * {@code on-the-fly user creation} and {@code TLS security}, are also set to {@true}.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class LDAPConnection {

	private String name;
	private String host;
	private String port;
	private boolean tlsActivated;
	private String account;
	private String accountPassword;
	private String baseDN;
	private boolean onTheFlyCreation;
	private String login;
	private String firstName;
	private String lastName;
	private String email;
	
	/**
	 * Builder for the {@link LDAPConnection} class.
	 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
	 */
	public static class Builder {
		private String name;
		private String host;
		private String port;
		private boolean tlsActivated = true;
		private String account;
		private String accountPassword;
		private String baseDN;
		private boolean onTheFlyCreation = true;
		private String login = "uid";
		private String firstName = "cn";
		private String lastName = "sn";
		private String email = "mail";
		
		/**
		 * Creates the Builder object.
		 * @param name the LDAP connection name
		 */
		public Builder(String name) {
			this.name = name;
		}
		
		/**
		 * Adds the host name of the LDAP server.
		 * @param host
		 * @return the Builder object
		 */
		public Builder host(String host) {
			this.host = host;
			return this;
		}
		/**
		 * Adds the port number of the LDAP server.
		 * @param port
		 * @return the Builder object
		 */
		public Builder port(String port) {
			this.port = port;
			return this;
		}
		/**
		 * If TLS mode is activated.
		 * @param tlsActivated
		 * @return the Builder object
		 */
		public Builder activateTLS(boolean tlsActivated) {
			this.tlsActivated = tlsActivated;
			return this;
		}
		/**
		 * Adds the account name. 
		 * @param account
		 * @return the Builder object
		 */
		public Builder account(String account) {
			this.account = account;
			return this;
		}
		/**
		 * Adds the account's password.
		 * @param accountPassword
		 * @return the Builder object
		 */
		public Builder accountPassword(String accountPassword) {
			this.accountPassword = accountPassword;
			return this;
		}
		/**
		 * Adds the base domain name.
		 * @param baseDN
		 * @return the Builder object
		 */
		public Builder baseDN(String baseDN) {
			this.baseDN = baseDN;
			return this;
		}
		/**
		 * If users should be created on-the-fly.
		 * @param onTheFlyCreation
		 * @return the Builder object
		 */
		public Builder createUserOnTheFly(boolean onTheFlyCreation) {
			this.onTheFlyCreation = onTheFlyCreation;
			return this;
		}
		/**
		 * Adds the login name.
		 * @param login - it's usually {@code uid}
		 * @return the Builder object
		 */
		public Builder login(String login) {
			this.login = login;
			return this;
		}
		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		public Builder email(String email) {
			this.email = email;
			return this;
		}
		public LDAPConnection build() {
			return new LDAPConnection(this);
		}
	}
	
	private LDAPConnection(Builder builder) {
		this.name = builder.name;
		this.host = builder.host;
		this.port = builder.port;
		this.tlsActivated = builder.tlsActivated;
		this.account = builder.account;
		this.accountPassword = builder.accountPassword;
		this.baseDN = builder.baseDN;
		this.onTheFlyCreation = builder.onTheFlyCreation;
		this.login = builder.login;
		this.firstName = builder.firstName;
		this.lastName = builder.lastName;
		this.email = builder.email;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @return the tlsActivated
	 */
	public boolean isTlsActivated() {
		return tlsActivated;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * @return the accountPassword
	 */
	public String getAccountPassword() {
		return accountPassword;
	}

	/**
	 * @return the baseDN
	 */
	public String getBaseDN() {
		return baseDN;
	}

	/**
	 * @return the onTheFlyCreation
	 */
	public boolean isOnTheFlyCreation() {
		return onTheFlyCreation;
	}

	/**
	 * @return the login
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @param tlsActivated the tlsActivated to set
	 */
	public void setTlsActivated(boolean tlsActivated) {
		this.tlsActivated = tlsActivated;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @param accountPassword the accountPassword to set
	 */
	public void setAccountPassword(String accountPassword) {
		this.accountPassword = accountPassword;
	}

	/**
	 * @param baseDN the baseDN to set
	 */
	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}

	/**
	 * @param onTheFlyCreation the onTheFlyCreation to set
	 */
	public void setOnTheFlyCreation(boolean onTheFlyCreation) {
		this.onTheFlyCreation = onTheFlyCreation;
	}

	/**
	 * @param login the login to set
	 */
	public void setLogin(String login) {
		this.login = login;
	}

	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
}
