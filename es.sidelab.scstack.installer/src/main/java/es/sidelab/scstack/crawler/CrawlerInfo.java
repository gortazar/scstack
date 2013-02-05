package es.sidelab.scstack.crawler;

/** 
 * Class that holds information about an implemented Crawler:
 * <li>crawler's name</li>
 * <li>base url</li>
 * @author <a href="mailto:radutom.vlad@gmail.com">Radu Tom Vlad</a>
 */
public class CrawlerInfo {
	
	private String name;
	private String url;
	
	/**
	 * Constructor that also sets the name and url.
	 * @param name
	 * @param url
	 */
	public CrawlerInfo(String name, String url) {
		this.name = name;
		this.url = url;
	}
	/**
	 * Get the URL.
	 * @return
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * Set the URL.
	 * @param url value to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 * Get the name of the crawler.
	 * @return
	 */
	public String getName() {
		return name;
	}
	/**
	 * Set the crawler's name.
	 * @param name value to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
