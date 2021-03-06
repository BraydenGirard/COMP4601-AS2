package edu.carleton.comp4601.assignment2.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.carleton.comp4601.assignment2.database.DatabaseManager;
import edu.carleton.comp4601.assignment2.index.CrawlIndexer;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	final static Logger logger = LoggerFactory.getLogger(Controller.class);
	
	// Options
	final static String homePath = System.getProperty("user.home");
	final static String crawlStorageFolder = "/data/crawl/root";
	final static String luceneIndexFolder = "/data/lucene/";
	final static int numberOfThreads = 1;
	final static String[] crawlDomains = new String[] { "http://sikaman.dyndns.org:8888/courses/4601/resources/" };
	//final static String[] crawlDomains = new String[] { "http://sikaman.dyndns.org:8888/courses/4601/resources/", "http://www.carleton.ca", "http://daydreamdev.com" };
	
	/**
	 * Initializes a crawl controller and adds all the seed domains
	 * 
	 * @param config A Crawler4j configuation object 
	 * @param domains A String array of all the domains to crawl
	 * @return controller A controller
	 * @throws Exception A general exception
	 */
	public static CrawlController initController(CrawlConfig config, String[] domains) throws Exception {

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		 for (String domain : domains) {
		      controller.addSeed(domain);
		}

		return controller;
	}

	/**
	 * The Main crawler control point. Drops the database, cleans the lucene directory, calls the controller
	 * and crawler configuration and calls the lucene indexing
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		logger.info("Web Crawler Controller Started");
		
		DatabaseManager.getInstance().getDatabase().dropDatabase();	
		
		try {
			FileUtils.cleanDirectory(new File(homePath + luceneIndexFolder + "index-directory"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(homePath + crawlStorageFolder);	
		config.setPolitenessDelay(300);
		config.setIncludeBinaryContentInCrawling(true);
		
		config.setMaxPagesToFetch(100); // TODO: Remove limit for submission

		Crawler.configure(crawlDomains);
		CrawlController controller = initController(config, crawlDomains);
		controller.start(Crawler.class, numberOfThreads);
	
		logger.info("Done crawling");
		CrawlIndexer indexer = new CrawlIndexer(homePath + luceneIndexFolder, controller.getCrawlersLocalData());
		
		try {
			indexer.rebuildIndexes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("All Done Goodbye!");
		System.exit(0);
	}
}