package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
	public void CombineClusters(List<ClusterDetails> c1,List<ClusterDetails> c2) {
		
	}
	
 
	
	
}
