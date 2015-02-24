package edu.carleton.comp4601.assignment2.graphing;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgraph.graph.Edge;
import org.jgrapht.graph.*;

import edu.carleton.comp4601.assignment2.crawler.Crawler;


public class Grapher implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6017417770252581831L;
	private Multigraph<PageVertex, DefaultEdge> graph;
	private ConcurrentHashMap<Integer, PageVertex> vertices;
	private String name;
	public int idCounter;
	
	public Grapher(String name) {
		this.name = name;
		this.graph = new Multigraph<PageVertex, DefaultEdge> (DefaultEdge.class);
		this.vertices = new ConcurrentHashMap<Integer, PageVertex>();
		this.idCounter = 0;
	}
	
	public synchronized boolean addVertex(PageVertex vertex) {
		this.vertices.put(vertex.getId(), vertex);
		if(this.graph.addVertex(vertex)) {
			return true;
		}
		return false;
	}

	public synchronized boolean removeVertex(PageVertex vertex) {
		this.vertices.remove(vertex.getId());
		if(this.graph.removeVertex(vertex)) {
			idCounter--;
			return true;
		}
		return false;
	}
	
	public synchronized void addEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.addEdge(vertex1, vertex2);
	}
	
	public synchronized void removeEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.removeEdge(vertex1, vertex2);
	}
	
	public synchronized PageVertex findVertex(String url) {
		for (PageVertex vertex : getVertices().values()) {
		    if(vertex.getUrl().equals(url)) {
		    	return vertex;
		    }
		}
		return null;
	}
	
	public synchronized boolean updateVertex(PageVertex oldVertex, int newId) {
		
		PageVertex updatedVertex = oldVertex;
		updatedVertex.setId(newId);
		this.graph.addVertex(updatedVertex);
		System.out.println("Hello world");
		for (PageVertex vertex : getVertices().values()) {
		    if(this.graph.containsEdge(oldVertex, vertex)) {
		    	this.graph.removeEdge(oldVertex, vertex);
		    	this.graph.addEdge(updatedVertex, vertex);
		    }
		}
		
		this.graph.removeVertex(oldVertex);
		
		return true;
		
	}
	
	public synchronized ConcurrentHashMap<Integer, PageVertex> getVertices() {
		return this.vertices;
	}
	
	public synchronized String getName() {
		return this.name;
	}

	public int getIdCounter() {
		return idCounter;
	}
	
	public Multigraph<PageVertex, DefaultEdge>  getGraph() {
		return this.graph;
	}
}
