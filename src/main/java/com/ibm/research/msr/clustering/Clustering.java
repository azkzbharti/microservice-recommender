/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.research.msr.extraction.Document;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author ShreyaKhare
 *
 */
public abstract class Clustering {

	protected List<Document> listOfDocuments = new ArrayList<Document>();
	protected List<ClusterDetails> clusters = new ArrayList<>();

	public Clustering(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}

	public List<Document> getListOfDocuments() {
		return listOfDocuments;
	}

	public void setListOfDocuments(ArrayList<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}

	public List<ClusterDetails> getClusters() {
		return clusters;
	}

	public void setClusters(ArrayList<ClusterDetails> clusters) {
		this.clusters = clusters;
	}
	
	protected Instances filterData(Instances data) throws Exception {
		System.out.println("Filtering data ...");
		Remove remove = new Remove();
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "1-2";
		remove.setOptions(options);
		remove.setInputFormat(data);
		return Filter.useFilter(data, remove);
	}
	
	/**
	 * 
	 * Overriding function 
	 */
	public abstract void runClustering();
	
	

	/**
	 * 
	 * Gets JSON for each cluster
	 * @throws IOException 
	 */
	public void savecLusterJSON(String writepath) throws IOException { 
		JSONObject clusterobj = new JSONObject();
		clusterobj.put("name", "root");
		clusterobj.put("parent", null);
		JSONArray clusterArray = new JSONArray();
		int count = 0;
		for (ClusterDetails cls : clusters) {
			if (cls.getListOfDocuments().size() > 0) {
				clusterArray.add(cls.getClusterJson(count));
				count += 1;
			}
		}
		clusterobj.put("children", clusterArray);
		System.out.println(clusterobj);
		String fileString = new String(Files.readAllBytes(Paths.get("src/main/resources/d3.html")), StandardCharsets.UTF_8);
		fileString= fileString.replaceAll("%%JSONCONTENT%%", clusterobj.toString());
	    Files.write(Paths.get(writepath), fileString.getBytes());
	    System.out.println("File written at "+ writepath);
	}
	


}
