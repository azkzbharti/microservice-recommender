/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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

	static Map<ClusterDetails, Integer> consolidatedClusters = new HashMap<ClusterDetails, Integer>();

	protected List<Document> listOfDocuments = new ArrayList<Document>();
	protected List<ClusterDetails> clusters = new ArrayList<>();

	public Clustering(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}

	

	/**
	 * @return the consolidatedClusters
	 */
	public List<ClusterDetails> getConsolidatedClusters() {
		List<ClusterDetails> newclusters =  new ArrayList<>();
		
		for (ClusterDetails cls:Clustering.consolidatedClusters.keySet()) {
//			System.out.println(Clustering.consolidatedClusters.get(cls));
			ClusterDetails temp = new ClusterDetails(cls.getListOfDocuments(),Clustering.consolidatedClusters.get(cls));
			newclusters.add(temp);	
//			temp.showDetails();
		}
		return newclusters;
//		return consolidatedClusters;
	}



	/**
	 * @param consolidatedClusters the consolidatedClusters to set
	 */
	public static void setConsolidatedClusters(Map<ClusterDetails, Integer> consolidatedClusters) {
		Clustering.consolidatedClusters = consolidatedClusters;
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

	public void setClusters(List<ClusterDetails> clusters) {
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
//		System.out.println(clusters.size());
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(clusters); 

		 clusters = new ArrayList<ClusterDetails>();
		 clusters.addAll(s);  
		 clusters=sortClusterOnScore(clusters);

//			System.out.println(clusters.size());

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
	
	public void CombineClusters() {
		
		for(ClusterDetails cls :this.clusters) {
			Integer cvalue;
			if(consolidatedClusters.containsKey(cls)) {
				cvalue = consolidatedClusters.get(cls);
				cvalue = cvalue+1;
//				System.out.println(cls.showDetails());
				cls.showDetails();
			}
			else {
				cvalue=0;

			}
			System.out.println(consolidatedClusters.size()+"----"+cvalue);
			consolidatedClusters.put(cls, cvalue);
		}
				
	}
	public void removeDuplicate() {
		// will remove the same clusters
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(this.clusters);
		 List<ClusterDetails> listofclusters = new ArrayList<ClusterDetails>();
		 listofclusters.addAll(s); 
		 this.clusters=listofclusters;
		 }

	public  List<ClusterDetails> sortClusterOnScore(List<ClusterDetails> clusters) {
		Comparator<ClusterDetails> comparator = new Comparator<ClusterDetails>() {

			public int compare(ClusterDetails tupleA, ClusterDetails tupleB) {
				return Double.compare(tupleA.getScore(), tupleB.getScore());
//                return (tupleA.getSize()-(tupleB.getSize()));
			}
			
	
		};
		Collections.sort(clusters, Collections.reverseOrder(comparator));
		return clusters;
	}
	
}
