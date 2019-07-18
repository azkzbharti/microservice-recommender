package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ibm.research.msr.extraction.Document;

public class PostProcessClusters {
	
	List<ClusterDetails> listofclusters ;

	public PostProcessClusters(List<ClusterDetails> listofclusters) {
		super();
		this.listofclusters = listofclusters;
	}

	public List<ClusterDetails> getListofclusters() {
		return listofclusters;
	}

	public void setListofclusters(List<ClusterDetails> listofclusters) {
		this.listofclusters = listofclusters;
	}
	
	public void removeDuplicate() {
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(listofclusters);
		 this.listofclusters = new ArrayList<ClusterDetails>();
		 this.listofclusters.addAll(s); 
	}
//	
//	public void CombineClusters(List<ClusterDetails> clusterList1,List<ClusterDetails> clusterList2) {
//		Set<ClusterDetails> c1 = new HashSet<>(clusterList1);
//		Set<ClusterDetails> c2 = new HashSet<>(clusterList2);
//		
//		
//		
//				
//	}
	
 
	
	
}
