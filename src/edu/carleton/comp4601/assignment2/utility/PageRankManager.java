package edu.carleton.comp4601.assignment2.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;

import edu.carleton.comp4601.assignment2.dao.Document;
import edu.carleton.comp4601.assignment2.database.DatabaseManager;
import edu.carleton.comp4601.assignment2.graphing.Grapher;
import edu.carleton.comp4601.assignment2.graphing.PageVertex;
import Jama.Matrix;

public class PageRankManager {

	Grapher graph;
	Matrix pageRankMatrix;
	double a = 0.1;
	HashMap<Integer, Float> pageRanks = new HashMap<Integer, Float>();
	private boolean rankComplete = false;
	private static PageRankManager instance;
	Set<PageVertex> currentVertices;

	// Singleton setter
	public static void setInstance(PageRankManager instance) {
		PageRankManager.instance = instance;
	}


	// Gets this singleton instance
	public static PageRankManager getInstance() {

		if (instance == null)
			instance = new PageRankManager();
		return instance;

	}

	// Constructor (only called once)
	public PageRankManager() {
		try {
			this.graph = (Grapher) Marshaller.deserializeObject(DatabaseManager.getInstance().getGraphData("graph"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Finds the result of all the page rank computations for all the documents and returns them
	 * in a tuple. Returns null if rank computation is not finished.
	 * 
	 * @return tuple or null Tuple if complete null if not
	 */
	public Tuple<ArrayList<String>, ArrayList<Float>> getPageRank() {

		if(rankComplete) {
			
			ArrayList<Document> allDocuments = DatabaseManager.getInstance().getDocuments();
			System.out.println("There are: " + allDocuments.size() + " documents");
			
			ArrayList<String> documentTitle = new ArrayList<String>();
			ArrayList<Float> documentRank = new ArrayList<Float>();
			
			for(int i=0; i < allDocuments.size(); i++) {
				Document doc = allDocuments.get(i);
				
				documentTitle.add(doc.getName());
				documentRank.add((float) pageRankMatrix.get(0, i));
			}
			
			return new Tuple<ArrayList<String>, ArrayList<Float>>(documentTitle, documentRank);
		}

		return null;    
	}
	
	/**
	 * Returns a given docIds page rank score
	 * 
	 * @param docId A doc Id to find the rank for
	 * @return documentRank a float rank score
	 */
	public float getDocumentPageRank(int docId) {

		float documentRank = (float) pageRankMatrix.get(0, docId);

		rankComplete = true;

		return documentRank;
	}

	/**
	 * Runs the page rank algorithm on a graph and computes the steady state
	 * probabilities. Sets the rankComplete flag to true when its done. 
	 */
	public void computePageRank() {
		this.currentVertices = this.graph.getGraph().vertexSet();
		
		int size = this.currentVertices.size();

		Matrix matrix = new Matrix(1, size);
		matrix.set(0, 0, 1.0);

		Matrix temp = setupMatrix(size);
		Matrix temp2 = normPageNumberLinks(temp);
		Matrix temp3 = normMatrix(temp2);
		Matrix temp4 = multipleMatrixByAlpha(temp3);

		Matrix addMatrix = addMatrices(temp4);

		double max = 99999;
		double min = 0.00000001;

		while (max >= min) {
			Matrix copy = matrix;

			matrix = matrix.times(addMatrix);
			matrix = matrix.times(1 / matrix.normInf());

			max = copy.minus(matrix).normF();
		}
		
		pageRankMatrix = matrix;

		rankComplete = true;
	}

	/**
	 * Normalizes the matrix as part of remaking the adjacency matrix 
	 * 
	 * @param matrix The current matrix state
	 * @return matrix The new matrix state
	 */
	private Matrix normPageNumberLinks(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = 0;
		int zero = 0;

		if (row > 0) {
			col = matrix.getColumnDimension();
		}

		for (int i = 0; i < row; i++) {
			zero = 0;
			for (int k = 0; k < col; k++) {
				if (matrix.get(i, k) == 0) {
					zero++;
				}
				if (zero == col) {
					for (int j = 0; j < col; j++) {
						matrix.set(i, k, 1.0 / (double) col);
					}
				}
			}
		}

		return matrix;
	}

	/**
	 * Normalizes the matrix as part of remaking the adjacency matrix 
	 * 
	 * @param matrix The current matrix state
	 * @return matrix The new matrix state
	 */
	private Matrix normMatrix(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = 0;
		int one = 0;

		if (row > 0) {
			col = matrix.getColumnDimension();
		}

		for (int i = 0; i < row; i++) {
			one = 0;

			for (int j = 0; j < col; j++) {
				if (matrix.get(i, j) == 1) {
					one++;
				}	
			}

			if (one > 0) {
				for (int k = 0; k < col; k++) {
					if (matrix.get(i, k) == 1) {
						matrix.set(i, k, 1.0 / (double) one);
					}
				}
			}
		}

		return matrix;
	}

	/**
	 * Multiplies a given matrix by the 1 - the alpha value
	 * 
	 * @param matrix The current matrix state
	 * @return matrix The new matrix state
	 */
	private Matrix multipleMatrixByAlpha(Matrix matrix) {

		matrix = matrix.times((1.0 - a));
		return matrix;

	}

	/**
	 * Add α/N to every entry of the resulting matrix, to obtain our probability matrix.
	 * 
	 * @param matrix The current matrix state
	 * @return result the probability matrix
	 */
	private Matrix addMatrices(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();

		Matrix additionMa = new Matrix(row, col, (a / (double) col));
		Matrix result = matrix.plus(additionMa);

		return result;
	}

	/**
	 * Looks through the edge set and sets all vertices with edges to 1 in a new
	 * matrix of a given size
	 * 
	 * @param size The number of rows and columns in the matrix
	 * @return ma The initial matrix
	 */
	private Matrix setupMatrix(int size) {		
		Multigraph<PageVertex, DefaultEdge> mutliGraph = this.graph.getGraph();

		Set<DefaultEdge> edges = mutliGraph.edgeSet();

		Matrix ma = new Matrix(size, size);

		for (DefaultEdge edge : edges) {
			PageVertex v1 = mutliGraph.getEdgeSource(edge);
			PageVertex v2 = mutliGraph.getEdgeTarget(edge);
			ma.set(v1.getId(), v2.getId(), 1.0);
		}

		return ma;
	}

	/**
	 * Returns if the page rank process is complete or not
	 * 
	 * @return rankComplete true if complete false if not
	 */
	public boolean isRankComplete() {
		return rankComplete;
	}
}
