package com.ibm.research.msr.expandcluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.research.appmod.pa.expandcluster.ClassPair;
import com.ibm.research.appmod.pa.expandcluster.InterClassUsageFinder;
import com.ibm.research.msr.clustering.ClusterDetails;
import com.ibm.research.msr.extraction.Document;

public class MetricComputation {
	List<ClusterDetails> listofclusters=new ArrayList<>() ;
	List<ClusterDetails> newlistofclusters=new ArrayList<>() ;
	
	static InterClassUsageFinder i = new InterClassUsageFinder();
	
	
	public void getMetrics() {
		for(ClusterDetails cls:listofclusters) {
			int cohesion=0;
			cls.setScore(cohesion);
			//computeMetrics();
		}
		for(ClusterDetails cls:listofclusters) {		
		
		// example of getting docUsageMap from Interclass 
		Map<String,Integer> docUsageMap = new HashMap<>();
		
		for (Document doc :cls.getListOfDocuments()) {
	
		List<com.ibm.research.appmod.pa.expandcluster.ClassPair> cp=i.getAssociatedClassPairForClass(doc.getUniqueName());
		
		for (ClassPair pair : cp) {
			if(!pair.getThisClass().equals(doc.getUniqueName())) {
				docUsageMap.put(pair.getThisClass(), i.interClassUsageMatrix.get(pair));
			}
			if(!pair.getUsedClass().equals(doc.getUniqueName())) {
				docUsageMap.put(pair.getUsedClass(), i.interClassUsageMatrix.get(pair));
			}

		}
		
		}
		}
		
	}

}
