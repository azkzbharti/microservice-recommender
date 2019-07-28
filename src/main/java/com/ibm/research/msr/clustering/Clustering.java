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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.research.msr.extraction.Document;
import com.ibm.research.msr.utils.ReadJarMap;

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
	 * @param clusters 
	 * @return the consolidatedClusters
	 */
	public List<ClusterDetails> scorePartialClusters(List<ClusterDetails> clusters){
		
		List<ClusterDetails> newclusters =  new ArrayList<>();
		
		for (int i=0;i<clusters.size();i++) {
			ClusterDetails cls_i =clusters.get(i);	
//			if(cls_i.getScore()==0) {
//				cls_i.setScore(score);
//			}
//			cls_i.showDetails();
//			System.out.println("Setting score"+cls_i.getScore());
////			if(cls_i.getScore()==0) {
//			for(int j=0;j<clusters.size();j++) {
//				if(i!=j) {
//
//
//				ClusterDetails cls_j = clusters.get(j);
//				cls_j.showDetails();
//				System.out.println("j score"+cls_j.getScore());
//				int inter= cls_i.getNoIntersection(cls_j);
//				System.out.println("Inter"+inter);
//				double term1 = inter/((cls_i.getListOfDocuments().size()+cls_j.getListOfDocuments().size())-inter);
//				double score=term1+cls_j.getScore()+cls_i.getScore();
//				if(cls_i.getScore()==0) {
//				cls_i.setScore(cls_i.getScore()+score);
//				}
//				}
//				}
////			}
			
			System.out.println("Setting score"+cls_i.getScore());
			newclusters.add(cls_i);	
		}
		this.clusters=newclusters;
		return newclusters;


	}
	public List<ClusterDetails> getConsolidatedClusters() {
		List<ClusterDetails> newclusters =  new ArrayList<>();
		List<ClusterDetails> newclusters1 =  new ArrayList<>();

		
		for (ClusterDetails cls:Clustering.consolidatedClusters.keySet()) {
//			System.out.println(Clustering.consolidatedClusters.get(cls));
			ClusterDetails temp = new ClusterDetails(cls.getListOfDocuments(),Clustering.consolidatedClusters.get(cls));
			newclusters.add(temp);	
//			temp.showDetails();
		}
		System.out.println("here"+newclusters.size());

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
				cls.setClusterName();
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
			Integer cvalue=0;
			if(consolidatedClusters.containsKey(cls)) {
				cvalue = consolidatedClusters.get(cls);
				cvalue = cvalue+1;
				ClusterDetails tcls = consolidatedClusters.entrySet()
					      .stream()
					      .filter(entry -> consolidatedClusters.containsKey(cls))
					      .map(Map.Entry::getKey).findFirst().get();
				if(cls.getClusterName()!="None" && tcls.getClusterName()=="None" ) {
			     cls.setClusterName(tcls.getClusterName());
			     consolidatedClusters.replace(cls, cvalue, cvalue);
				}
				else {
					consolidatedClusters.put(cls, cvalue);
				}

			}
			else {
				consolidatedClusters.put(cls, cvalue);
				// compute score based on intersection if not present as is 
				// S(C1)= Sum_i=1 to n ((C1 inter Ci)*V(ci)/(#C1+#Ci-(c1 inter ci))*V(C1)
				
			}			
		}
//	 List<Entry<ClusterDetails, Integer>> listOfEntry = consolidatedClusters.entrySet().stream().sorted().collect(Collectors.toList());
				
			
		
				
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
				int sComp = Double.compare(tupleA.getScore(),tupleB.getScore());

	            if (sComp != 0) {
	               return sComp;
	            } 
	            Integer x1 = tupleA.getListOfDocuments().size();
	            Integer x2 = tupleB.getListOfDocuments().size();
	            return x2-x1; // reverse order
//				return Double.compare(tupleA.getScore(), tupleB.getScore());
//                return (tupleA.getSize()-(tupleB.getSize()));
			}
			
	
		};
		Collections.sort(clusters, Collections.reverseOrder(comparator));
		return clusters;
	}
	
}
