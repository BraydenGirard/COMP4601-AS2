package edu.carleton.comp4601.assignment2.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchEngine {
	
	private IndexSearcher searcher = null;
	private QueryParser parser = null;

	//Creates a new instance of search engine
	public SearchEngine(String dirPath) throws IOException {
		searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(new File(dirPath + "index-directory"))));
		parser = new QueryParser("content", new StandardAnalyzer());
	}
	
	//Searches the index based on query and number of results
	public TopDocs performSearch(String queryString, int n)
			throws IOException, ParseException {
		Query query = parser.parse(queryString);        
		return searcher.search(query, n);
	}
	
	//Gets a lucene document back by document id
	public Document getDocument(int docId)
			throws IOException {
		return searcher.doc(docId);
	}
}
