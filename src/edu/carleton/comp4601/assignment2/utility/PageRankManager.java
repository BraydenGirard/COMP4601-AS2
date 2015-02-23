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
	
	public Tuple<ArrayList<String>, ArrayList<Float>> getPageRank() {

		if(rankComplete) {
			ArrayList<Document> allDocuments = DatabaseManager.getInstance().getDocuments();
			System.out.println("There are: " + allDocuments.size() + " documents");
			//System.out.println("There are: " + currentVertices.size() + " vertices");
			ArrayList<String> documentTitle = new ArrayList<String>();
			ArrayList<Float> documentRank = new ArrayList<Float>();

			Multigraph<PageVertex, DefaultEdge> mutliGraph = this.graph.getGraph();
			
			/*
			for(int i=0; i<allDocuments.size(); i++) {
				for(int j=0; j < currentVertices.size(); j++) {
					Document doc = allDocuments.get(i);
					int docId = doc.getId();
					int vertexId = v.getId();
				
					if(docId == vertexId) {
						float score = (float) pageRankMatrix.get(i, 0);
						documentTitle.add(doc.getName());
						documentRank.add(score);
					}
				}
				for (DefaultEdge edge : mutliGraph.edgeSet()) {
					PageVertex v1 = mutliGraph.getEdgeSource(edge);
					PageVertex v2 = mutliGraph.getEdgeTarget(edge);
					
					int v1Id = v1.getId();
					int v2Id = v2.getId();
					
					if(v1Id == docId) {
						float score = (float) pageRankMatrix.get(v1Id, v2Id);
						documentTitle.add(doc.getName());
						documentRank.add(score);
					}
				}
				
			}*/
			
			/*
			for(int i=0; i<allDocuments.size(); i++) {
				for(PageVertex v: currentVertices) {
					int vertexId = v.getId();
					int documentId = allDocuments.get(i).getId();
					if(vertexId == documentId) {
						documentTitle.add(allDocuments.get(i).getName());
						documentRank.add((float) pageRanks.get(v.getRow()));
					}
				}
			}*/
			return new Tuple<ArrayList<String>, ArrayList<Float>>(documentTitle, documentRank);
		}

		return null;    
	}
	
	/**
	 * TODO: This wont work
	 * @param docId
	 * @return
	 */
	public float getDocumentPageRank(int docId) {

		float documentRank = 0;

		for(PageVertex v: currentVertices) {
			if(v.getId() == docId) {
				documentRank = (float) pageRanks.get(v.getRow());
			}

		}

		rankComplete = true;

		return documentRank;
	}

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

	private Matrix normMatrix(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = 0;
		int trackOne = 0;

		if (row > 0) {
			col = matrix.getColumnDimension();
		}

		for (int i = 0; i < row; i++) {
			trackOne = 0;

			for (int j = 0; j < col; j++) {
				if (matrix.get(i, j) == 1) {
					trackOne++;
				}	
			}

			if (trackOne > 0) {
				for (int k = 0; k < col; k++) {
					if (matrix.get(i, k) == 1) {
						matrix.set(i, k, 1.0 / (double) trackOne);
					}
				}
			}
		}

		return matrix;
	}

	private Matrix multipleMatrixByAlpha(Matrix matrix) {

		matrix = matrix.times((1.0 - a));
		return matrix;

	}

	private Matrix addMatrices(Matrix matrix) {
		int row = matrix.getRowDimension();
		int col = matrix.getColumnDimension();

		Matrix additionMa = new Matrix(row, col, (a / (double) col));
		Matrix result = matrix.plus(additionMa);

		return result;
	}

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

	public boolean isRankComplete() {
		return rankComplete;
	}
}
