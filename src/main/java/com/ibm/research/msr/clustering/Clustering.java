/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	static Map<ClusterDetails, Double> consolidatedClusters = new HashMap<ClusterDetails, Double>();
	// this is static as it consolidates outputs across Multiple algorithms if used 
	protected List<Document> listOfDocuments = new ArrayList<Document>();
	protected List<ClusterDetails> clusters = new ArrayList<>();

	public Clustering(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
		consolidatedClusters = new HashMap<ClusterDetails, Double>();
		//Reset  this is static as it consolidates outputs across Multiple algorithms 

	}

	public void CLeanClusters() {
		for (ClusterDetails cls : clusters) {
			cls.removeDuplicates();

		}
	}



	public List<ClusterDetails> getConsolidatedClusters() {
		List<ClusterDetails> newclusters = new ArrayList<>();

		for (ClusterDetails cls :consolidatedClusters.keySet()) {
			ClusterDetails temp = new ClusterDetails(cls.getListOfDocuments(),
					consolidatedClusters.get(cls));
			temp.removeDuplicates();
			newclusters.add(temp);
		}

		return newclusters;
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

	public void extendClusters(List<ClusterDetails> clusterslist) {
		
		Set<ClusterDetails> s = new HashSet<ClusterDetails>();
		s.addAll(this.clusters);
		List<ClusterDetails> listofclusters = new ArrayList<ClusterDetails>();
		listofclusters.addAll(s);
		for (ClusterDetails cls : clusterslist) {
//			 cls.showDetails();
			consolidatedClusters.put(cls, cls.getScore());
		}
//		 this.clusters=listofclusters;
//		 Clustering.consolidatedClusters.put(key, value)
	}

	public void setClusters(List<ClusterDetails> clusters) {

		this.clusters = clusters;
	}

	public List<ClusterDetails> getNonScoreClusters() {
		return this.clusters.stream().filter(cls -> cls.getScore() == 1).collect(Collectors.toList());
	}

	public List<ClusterDetails> combineNoneClusters() {
		List<ClusterDetails> noneCLusters = getNonScoreClusters();
		List<ClusterDetails> newList = null;
		for (ClusterDetails cls : this.clusters) {
			if (noneCLusters.contains(cls)) {

			}
		}
		return newList;

	}

	public List<ClusterDetails> getNoneClusters() {
		return this.clusters.stream().filter(cls -> cls.getClusterName() == "None").collect(Collectors.toList());
	}

	protected Instances filterData(Instances data) throws Exception {
//		System.out.println("Filtering data ...");
		Remove remove = new Remove();
		String[] options = new String[2];
		options[0] = "-R";
		options[1] = "1-2";
		remove.setOptions(options);
		remove.setInputFormat(data);
		return Filter.useFilter(data, remove);
	}
	public int getUniquedocs( List<ClusterDetails>  list) {
		HashSet<Document> doclist = new HashSet<>();
		for(ClusterDetails cls:list) {
			for(Document doc: cls.getListOfDocuments()) {
				doclist.add(doc);
			}
		}
		return doclist.size();
		
	}

	/**
	 * 
	 * Overriding function
	 */
	public abstract void runClustering();

	public void setCusterListNames() {
		for (ClusterDetails cls : this.clusters) {
			cls.getClusterApiList();
			cls.setClusterName();
		}
	}

	/**
	 * Cluster save as Circle-pack D3 viz JSON
	 * @return 
	 * 
	 * 
	 */

	@SuppressWarnings("unchecked")
	public String saveClusterAsCirclePackJSON(String writepath) {

		JSONObject rootObject = new JSONObject();
		
		Iterator<ClusterDetails> itr = clusters.iterator();

		rootObject.put("name", "clusters");

		JSONArray rootChildrenArray = new JSONArray();

		while (itr.hasNext()) {
			ClusterDetails c = itr.next();
			JSONObject clusterobj = new JSONObject();
			clusterobj.put("name", c.getClusterName());

			List<String> childList = c.getListOfShortDocumentsNames();
			if (!childList.isEmpty()) {
				JSONArray childrenArray = new JSONArray();

				for (String child : childList) {
					JSONObject childObj = new JSONObject();
					childObj.put("name", child);
					// TODO: size has to be determined properly, right now using the name of the
					// class
					childObj.put("size", child.length());

					childrenArray.add(childObj);
				}

				clusterobj.put("children", childrenArray);
			}

			rootChildrenArray.add(clusterobj);

		}

		rootObject.put("children", rootChildrenArray);
		
		if (rootObject != null) {
			try {
				Files.write(Paths.get(writepath), rootObject.toString().getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				System.out.println("Error while writing file to  " + writepath);
			}
		} else {
			System.out.println("Error while writing file to  " + writepath);
		}

		System.out.println("File written at " + writepath);
		return rootObject.toString();
	}

	public void removeDuplicate() {
		Set<ClusterDetails> s = new HashSet<ClusterDetails>();
		s.addAll(this.clusters);
		List<ClusterDetails> listofclusters = new ArrayList<ClusterDetails>();
		listofclusters.addAll(s);
		this.clusters = listofclusters;
	}

	public List<ClusterDetails> sortClusterOnScore(List<ClusterDetails> clusters) {
		Comparator<ClusterDetails> comparator = new Comparator<ClusterDetails>() {

			public int compare(ClusterDetails tupleA, ClusterDetails tupleB) {
				int sComp = Double.compare(tupleA.getScore(), tupleB.getScore());

				if (sComp != 0) {
					return sComp;
				}
				Integer x1 = tupleA.getListOfDocuments().size();
				Integer x2 = tupleB.getListOfDocuments().size();
				return x2 - x1; // reverse order
			}

		};
		Collections.sort(clusters, Collections.reverseOrder(comparator));
		return clusters;
	}

}
