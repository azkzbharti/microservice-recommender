package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.ibm.research.msr.extraction.Document;

public class PostProcessClusters {
	
	
	List<ClusterDetails> listofclusters ;
	static Map<ClusterDetails, Integer> consolidatedClusters = new TreeMap<ClusterDetails, Integer>();

	
	
	

	/**
	 * @return the consolidatedClusters
	 */
	public static Map getConsolidatedClusters() {
		return consolidatedClusters;
	}

	/**
	 * @param consolidatedClusters the consolidatedClusters to set
	 */
	public static void setConsolidatedClusters(Map consolidatedClusters) {
		PostProcessClusters.consolidatedClusters = consolidatedClusters;
	}


	public List<ClusterDetails> getListofclusters() {
		return listofclusters;
	}

	public void setListofclusters(List<ClusterDetails> listofclusters) {
		this.listofclusters = listofclusters;
	}
	
	public void removeDuplicate() {
		// will remove the same clusters
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(listofclusters);
		 this.listofclusters = new ArrayList<ClusterDetails>();
		 this.listofclusters.addAll(s); 
	}
	
	public void CombineClusters(List<ClusterDetails> listOfClusters) {
		for(ClusterDetails cls :listOfClusters) {
			Integer cvalue=0;
			if(consolidatedClusters.containsKey(cls)) {
				cvalue=(Integer) consolidatedClusters.get(cls);
			}
			consolidatedClusters.put(cls, cvalue);
		}
				
	}
	
 
	
	
}
