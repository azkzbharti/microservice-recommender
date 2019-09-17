package com.ibm.research.msr.expandcluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
		List<ClassPair> cp=i.getAssociatedClassPairForClass(doc.getUniqueName());
		
		for (ClassPair pair : cp) {
			if(!pair.thisClass.equals(doc.getUniqueName())) {
				docUsageMap.put(pair.thisClass, i.interClassUsageMatrix.get(pair));
			}
			if(!pair.usedClass.equals(doc.getUniqueName())) {
				docUsageMap.put(pair.usedClass, i.interClassUsageMatrix.get(pair));
			}

		}
		
		}
		}
		
	}

}
