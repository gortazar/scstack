package es.sidelab.scstack.crawler.redmine;

import java.util.logging.Logger;

import es.sidelab.scstack.crawler.Crawler;
import es.sidelab.scstack.crawler.CrawlerException;
import es.sidelab.scstack.crawler.CrawlerInfo;

public class RedmineCrawler extends Crawler {

	public RedmineCrawler(boolean enableJavaScript, CrawlerInfo info, Logger log)
			throws CrawlerException {
		super(enableJavaScript, info, log);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void setScriptTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void setImplicitlyWaitTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAPIKey() throws CrawlerException {
		// TODO Auto-generated method stub
		return null;
	}

}
