package edu.carleton.comp4601.assignment2.graphing;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import org.jgrapht.graph.*;

public class Grapher implements Serializable {
	
	private static final long serialVersionUID = -6017417770252581831L;
	private Multigraph<PageVertex, DefaultEdge> graph;
	private ConcurrentHashMap<Integer, PageVertex> vertices;
	private String name;
	public int idCounter;
	
	//Initializes graph object
	public Grapher(String name) {
		this.name = name;
		this.graph = new Multigraph<PageVertex, DefaultEdge> (DefaultEdge.class);
		this.vertices = new ConcurrentHashMap<Integer, PageVertex>();
		this.idCounter = 0;
	}
	
	//Adds a vertex to the graph
	public synchronized boolean addVertex(PageVertex vertex) {
		this.vertices.put(vertex.getId(), vertex);
		if(this.graph.addVertex(vertex)) {
			return true;
		}
		return false;
	}
	
	//Removes a vertex from the graph
	public synchronized boolean removeVertex(PageVertex vertex) {
		this.vertices.remove(vertex.getId());
		if(this.graph.removeVertex(vertex)) {
			idCounter--;
			return true;
		}
		return false;
	}
	
	//Adds an edge to the graph
	public synchronized void addEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.addEdge(vertex1, vertex2);
	}
	
	//Removes an edge from the graph
	public synchronized void removeEdge(PageVertex vertex1, PageVertex vertex2) {
		 this.graph.removeEdge(vertex1, vertex2);
	}
	
	//Finds a vertex in the graph by url
	public synchronized PageVertex findVertex(String url) {
		for (PageVertex vertex : getVertices().values()) {
		    if(vertex.getUrl().equals(url)) {
		    	return vertex;
		    }
		}
		return null;
	}
	
	//Gets a HashMap of all vertices in the graph
	public synchronized ConcurrentHashMap<Integer, PageVertex> getVertices() {
		return this.vertices;
	}
	
	//Gets the name of the graph
	public synchronized String getName() {
		return this.name;
	}
	
	//Gets the graph object
	public Multigraph<PageVertex, DefaultEdge>  getGraph() {
		return this.graph;
	}
}
