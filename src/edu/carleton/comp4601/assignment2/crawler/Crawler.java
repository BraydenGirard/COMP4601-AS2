package edu.carleton.comp4601.assignment2.crawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tika.metadata.Metadata;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.carleton.comp4601.assignment2.graphing.*;
import edu.carleton.comp4601.assignment2.database.DatabaseManager;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	// Utility
	final static Logger logger = LoggerFactory.getLogger(Crawler.class);
	final String homePath = System.getProperty("user.home");
	final String luceneIndexFolder = "/data/lucene/";

	// Crawler requirements
	private static String[] domains;
	private long sleepTime;

	// Data collection objects
	private Grapher crawlGraph;
	private CrawlData data;
	
	// Static doc id for keeping track of vertices and pages
    public static int docId = 0;

	@SuppressWarnings("unused")
	private static final Pattern allowedPatterns = Pattern.compile(".*(\\.(pdf|png|gif|jpe?g|xls|xlsx|ppt|pptx|doc|docx?))$");

	// Extensions to avoid
	private static final Pattern BINARY_FILES_EXTENSIONS =
			Pattern.compile(".*\\.(ico|xaml|pict|rif|ps|css|js" +
					"|mid|mp2|mp3|mp4|wav|wma|au|aiff|flac|ogg|3gp|aac|amr|au|vox" +
					"|avi|mov|mpe?g|ra?m|m4v|smil|wm?v|swf|aaf|asf|flv|mkv" +
					"|zip|rar|gz|7z|aac|ace|alz|apk|arc|arj|dmg|jar|lzip|lha|bmp|rm)" +
					"(\\?.*)?$");

	public Crawler() {
		this.data = new CrawlData();
		this.crawlGraph = new Grapher("graph");
		this.sleepTime = 0;
	}

	/**
	 * Sets the domains from the controller for shouldVisit logic
	 * 
	 * @param domain A string array of domains to visit
	 */
	public static void configure(String[] domain) {
		domains = domain;
	}
	/*
	@Override
	public void onStart() {};*/

	/**
	 * Called before the crawler thread exits. Serializes the graph and writes to the database.
	 */
	@Override
	public void onBeforeExit() {
		try {
			String name = this.crawlGraph.getName();
			byte[] bytes = Marshaller.serializeObject(crawlGraph);
			DatabaseManager.getInstance().addNewGraph(name, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}	

		logger.info("Crawl Thread Complete");
	}	

	/**
	 * This function is called by controller to get the local data of this crawler when job is finished
	 */
	@Override
	public Object getMyLocalData() {
		return data;
	}

	/**
	 * You should implement this function to specify whether
	 * the given url should be crawled or not (based on your
	 * crawling logic).
	 */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		for (String domain : domains) {
			if (href.startsWith(domain) && !BINARY_FILES_EXTENSIONS.matcher(href).matches()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This function is called when a page is fetched and ready 
	 * to be processed by your program.
	 */
	@Override
	public void visit(Page page) {    

		long startTime = System.nanoTime();
		
		// Adaptive sleep
		/*
		try {
			Thread.sleep(this.sleepTime);

		} catch (InterruptedException e) {
			System.err.println("-- Sleep Interrupted");

		}*/

		Date date = new Date();
		long currentTime = date.getTime();
		WebURL weburl = page.getWebURL();

		// Content Type
		if (page.getParseData() instanceof HtmlParseData) {
			parseHTMLToDocument(page, currentTime);
			data.addVisitedUrl(weburl.getDocid(), weburl.getURL());
			docId++;

		} else if(page.getParseData() instanceof BinaryParseData) {
			parseBinaryToDocument(page, currentTime);
			data.addVisitedUrl(weburl.getDocid(), weburl.getURL());
			docId++;
		}
		
		// Adaptive calculation 
		/*
		long endTime = System.nanoTime();
		long duration = (endTime - startTime);
		this.sleepTime = duration * 2;*/
	}

	/**
	 * Parses any binary data on a page
	 * @param page A page that was crawled by the crawler
	 * @param currentTime The time the page was crawled
	 */
	private void parseBinaryToDocument(Page page, long currentTime) {

		try {
			
			// Send stream to tika to parse it
			InputStream inputStream = new ByteArrayInputStream(page.getContentData());
			Metadata metadata = TikaParsingManager.getInstance().parseUsingAutoDetect(inputStream);
			String url = page.getWebURL().getURL();

			if(metadata != null) {
				edu.carleton.comp4601.assignment2.dao.Document doc = new edu.carleton.comp4601.assignment2.dao.Document();

				// Get the name from the URL in case metadata lacks a name
				String name = url.substring(url.lastIndexOf('/') + 1, url.length());
				
				doc.setId(docId);
				
				if(name != null) {
					doc.setName(name);
				}

				// Add document and metadata
				data.addVisitedDocument(doc.getId(), doc);
				data.addVisitedMetadata(doc.getId(), metadata);

				// Graph the page
				buildVertexForPage(page, currentTime);

				// Add document to DB
				DatabaseManager.getInstance().addNewDocument(doc);

			} else {
				logger.warn("Could not parse metadata using auto detect");

			}

		} catch (Exception e) {
			logger.error("Exception while parsing nonHTML: " + e.getMessage());

		}
	}

	/**
	 * Parses any html on a page
	 * @param page A page that was crawled by the crawler
	 * @param currentTime The time the page was crawled
	 */
	private void parseHTMLToDocument(Page page, long currentTime) {

		try {
			// Get basic page properties 
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String html = htmlParseData.getHtml();
			Document doc = Jsoup.parse(html);
			Elements allImages = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
			Elements allText = doc.select("p,h1,h2,h3,h4,h5");

			// Store document basic values
			edu.carleton.comp4601.assignment2.dao.Document myDoc = new edu.carleton.comp4601.assignment2.dao.Document(docId);
			if(doc.title() != null) {
				myDoc.setName(doc.title());
				myDoc.addTag(doc.title());
			}			

			// Store all document text
			String rawText = "";
			for(Element elem : allText) {
				rawText += (" " + elem.text());

			}
			
			if(rawText != null) {
				myDoc.setText(rawText);
			}

			// Store all image src and alt
			ArrayList<String> imagealts = new ArrayList<String>();
			for(Element elem : allImages) {
				String imageAlt = elem.attr("alt");

				if(!imageAlt.isEmpty())
					imagealts.add(imageAlt);

			}
			data.addVisitedImageAltList(myDoc.getId(), imagealts);

			// Add current page vertex
			PageVertex current = buildVertexForPage(page, currentTime);

			// Add links to document and add vertices and edges for links
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			for(WebURL link : links) {
				if(link != null) {
					myDoc.addLink(link.getURL());
				}
				addOutGoingLinkToGraph(link.getURL(), current);

			}

			// Add document to visited and add to DB
			data.addVisitedDocument(myDoc.getId(), myDoc);
			DatabaseManager.getInstance().addNewDocument(myDoc);
		
		} catch (Exception e) {
			logger.warn("Exception parsing HTML: " + e.getMessage());
			logger.info(page.getWebURL().getURL());
			
		}
	}

	/**
	 * Builds and adds the graph vertex for a given page and returns it.
	 * 
	 * @param page The current page crawled
	 * @param time The current time in long
	 * @return existingPage The current page vertex
	 */
	private PageVertex buildVertexForPage(Page page, long time) {
		String parentUrl = page.getWebURL().getParentUrl();
		String url = page.getWebURL().getURL();
		
		PageVertex existingPage = this.crawlGraph.findVertex(url);
		
		if(existingPage == null) {
			existingPage = new PageVertex(docId, url, time);
			this.crawlGraph.addVertex(existingPage);
		} 

		if(parentUrl != null) {
			PageVertex parentPage = this.crawlGraph.findVertex(parentUrl);
			if(parentPage !=null) {
				this.crawlGraph.addEdge(parentPage, existingPage);
			}

		}

		return existingPage;
	}

	/**
	 * Maps edges to the vertex and the current pages vertex.
	 * 
	 * @param url A URL to search for vertices
	 * @param currentPage The current page vertex
	 */
	private void addOutGoingLinkToGraph(String url, PageVertex currentPage) {
		PageVertex vertex = this.crawlGraph.findVertex(url);

		if(vertex != null) {
			if(!this.crawlGraph.getGraph().containsEdge(currentPage, vertex)) {
				this.crawlGraph.addEdge(currentPage, vertex);
			}	
		}
	}
}
