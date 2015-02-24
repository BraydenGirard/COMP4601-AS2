package edu.carleton.comp4601.assignment2.graphing;

import java.io.Serializable;

public class PageVertex implements Serializable, Cloneable {

	private static final long serialVersionUID = -1214346455090744350L;
	private Integer id;
	private String url;
	private long duration;
	
	private int row;
	private int col;
	
	//Initializes a vertex that represents a page that was crawled
	public PageVertex(Integer id, String url, long duration) {
		setId(id);
		setUrl(url);
		setDuration(duration);
	}
	
	//Gets the vertex url
	public synchronized String getUrl() {
		return url;
	}

	//Sets the vertex url
	public synchronized void setUrl(String url) {
		this.url = url;
	}
	
	//Gets the id of the vertex
	public synchronized Integer getId() {
		return id;
	}

	//Sets the id of the vertex
	public synchronized void setId(int id) {
		this.id = id;
	}
	
	//Gets the duration of the page crawl
	public synchronized long getDuration() {
		return duration;
	}
	
	//Sets the duration of the page crawl
	public synchronized void setDuration(long duration) {
		this.duration = duration;
	}
	
	//Gets the row of the matrix the vertex belong to
	//for page rank
	public int getRow() {
		return row;
	}
	
	//Sets the row of the matrix the vertex belong to
	//for page rank
	public void setRow(int row) {
		this.row = row;
	}
	
	//Gets the column of the matrix the vertex belong to
	//for page rank
	public int getCol() {
		return col;
	}
	
	//Sets the column of the matrix the vertex belong to
	//for page rank
	public void setCol(int col) {
		this.col = col;
	}
}
