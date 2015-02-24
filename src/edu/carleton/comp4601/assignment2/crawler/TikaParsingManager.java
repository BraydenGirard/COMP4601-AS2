package edu.carleton.comp4601.assignment2.crawler;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.image.ImageParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

public class TikaParsingManager {

	private static TikaParsingManager instance;
	
	public static void setInstance(TikaParsingManager instance) {
		TikaParsingManager.instance = instance;
	}
	
	/**
	 * Tika singleton access
	 * 
	 * @return instance The singleton
	 */
	public static TikaParsingManager getInstance() {

		if (instance == null)
			instance = new TikaParsingManager();
		return instance;

	}
	
	/**
	 * Parses the metadata from a inputstream using the auto parser
	 * 
	 * @param is an inputstream of data
	 * @return metadata the Metadata object
	 * @throws Exception a Tika exception
	 */
	public Metadata parseUsingAutoDetect(InputStream is) throws Exception {
		Parser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		ParseContext context = new ParseContext();
		
		try {
			parser.parse(is, handler, metadata, context);
		} finally {
			is.close();
		}
		
		return metadata;
	}
	
	/**
	 * Parses the metadata from a inputstream using the png parser
	 * 
	 * @param is an inputstream of data
	 * @return metadata the Metadata object
	 * @throws Exception a Tika exception
	 */
	public Metadata parseMetadataForPNG(InputStream is) throws Exception {
		Metadata metadata = new Metadata();
		metadata.set(Metadata.CONTENT_TYPE, "image/png");
		
		try {
			new ImageParser().parse(is, new DefaultHandler(), metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}
	
	/**
	 * Parses the metadata from a inputstream using the pdf parser
	 * 
	 * @param is an inputstream of data
	 * @return metadata the Metadata object
	 * @throws Exception a Tika exception
	 */
	public Metadata parseMetadataForPDF(InputStream is) throws Exception {
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		
		try {
			new PDFParser().parse(is, handler, metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}
	
	/**
	 * Parses the metadata from a inputstream using the office parser
	 * 
	 * @param is an inputstream of data
	 * @return metadata the Metadata object
	 * @throws Exception a Tika exception
	 */
	public Metadata parseMetadataForDOC(InputStream is) throws Exception {
		Metadata metadata = new Metadata();
		ContentHandler handler = new BodyContentHandler();
		
		try {
			new OfficeParser().parse(is, handler, metadata, new ParseContext());
		} finally {
			is.close();
		}
		
		return metadata;
	}

}
