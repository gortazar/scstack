package es.sidelab.scstack.crawler;

/**
 * Exception class used whenever the needs arise.
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class CrawlerException extends Exception {

	/**
	 * Unique UID.
	 */
	private static final long serialVersionUID = -7712211217416021960L;

	public CrawlerException(String msg) {
		super(msg);
	}
	
	public CrawlerException(String msg, Throwable e) {
		super(msg, e);
	}

	/**
	 * Custom toString() method.
	 * @see java.lang.Exception#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (null == this.getMessage())
			sb.append("CrawlerException");
		if (null != this.getMessage() && ! this.getLocalizedMessage().trim().equalsIgnoreCase(""))
			sb.append("CrawlerException: " + this.getLocalizedMessage().trim());
		return sb.toString();
	}
}
