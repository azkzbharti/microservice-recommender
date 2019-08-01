package com.ibm.research.msr.expandcluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.ibm.research.msr.clustering.ClusterDetails;
import com.ibm.research.msr.extraction.Document;

public class ExpandClusters {

	List<ClusterDetails> listofclusters=new ArrayList<>() ;
	List<ClusterDetails> nonelistofclusters=new ArrayList<>(); 
	List<ClusterDetails> nonelistofclusters1=new ArrayList<>(); 

	static InterClassUsageFinder i = new InterClassUsageFinder();

	public ExpandClusters(List<ClusterDetails> listofclusters,String srcFilesRoot) {
		// TODO Auto-generated constructor stub
		Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		s.addAll(listofclusters);
		listofclusters = new ArrayList<ClusterDetails>();
		listofclusters.addAll(s); 
		for(ClusterDetails cls:listofclusters) {
			if(cls.getClusterName().trim().contains("Non") || cls.getClusterName().trim().equals("None"))
				this.nonelistofclusters.add(cls);
			else 
				this.listofclusters.add(cls);
				
		}
		
//		this.nonelistofclusters=listofclusters.stream().filter(cls->cls.getClusterName().trim().equals("None")).collect(Collectors.toList());
		System.out.println("None clusters "+nonelistofclusters.size()); 

//		this.listofclusters=listofclusters.stream().filter(cls->!cls.getClusterName().trim().equals("None")).collect(Collectors.toList());
		System.out.println("All clusters "+listofclusters.size()); 

		i.find(srcFilesRoot);
		
	}
	
	public void getUsage() {

		List<List<Document>> listofDocs= this.listofclusters
				.stream()
				.map(ClusterDetails::getListOfDocuments)
				.collect(Collectors.toList());
		
		List<Document> listofAllDocs=listofDocs.stream().flatMap(List::stream)
		        .collect(Collectors.toList());
		
		List<List<Document>> listofDocsinNone= this.nonelistofclusters
				.stream()
				.map(ClusterDetails::getListOfDocuments)
				.collect(Collectors.toList());
		List<Document> listofAllDocsinNone=listofDocsinNone.stream().flatMap(List::stream)
		        .collect(Collectors.toList());
		List<Document> notusedDocs = new ArrayList<Document>();
		
		for (Document doc:listofAllDocsinNone) {
			
			if(doc.getUniqueName().equals("com.ibm.research.json.JSONArray"))
				System.out.println("here");
			Map<ClusterDetails,Integer> docCLusterUsageMap = new HashMap<>();
			Map<ClusterDetails,Integer> sorteddocCLusterUsageMap = new HashMap<>();

			Map<String,Integer> docUsageMap = new HashMap<>();
			
			List<ClassPair> cp=i.getAssociatedClassPairForClass(doc.getUniqueName());
//			System.out.println(cp.size());
//			System.out.println(docname);

			if(cp.size()==0) {
				System.out.println("Document not used "+doc.getUniqueName());
				notusedDocs.add(doc);
				continue;
			}

			
			for(ClassPair pair:cp) {
				System.out.println(pair.usedClass);
				System.out.println(doc.getUniqueName());
				if(!pair.usedClass.equals(doc.getUniqueName()))
				docUsageMap.put(pair.usedClass, i.interClassUsageMatrix.get(pair));
//				else {
//					notusedDocs.add(doc);
//				}
			}

			for(ClusterDetails cls:this.listofclusters) {
					for (String docInCluster:cls.getListOfDocumentsNames()) {	
						if( docUsageMap.keySet().contains(docInCluster)) {
							if(docCLusterUsageMap.containsKey(cls))
								docCLusterUsageMap.put(cls,  docCLusterUsageMap.get(cls)+docUsageMap.get(docInCluster));
							else
								docCLusterUsageMap.put(cls,  docUsageMap.get(docInCluster));

						}

					}
			}
			if(docCLusterUsageMap.size()==0) {
				System.out.println("doc is not used in any cluser classes");
				notusedDocs.add(doc);
				continue;
			}
//			System.out.println(docCLusterUsageMap.toString());
			sorteddocCLusterUsageMap = docCLusterUsageMap
	        .entrySet()
	        .stream()
	        .limit(1)
	        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	        .collect(
	            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
	                LinkedHashMap::new));
			System.out.println(sorteddocCLusterUsageMap.toString());

			ClusterDetails maxUsageCluster = sorteddocCLusterUsageMap.entrySet().stream().findFirst().get().getKey();
			if(!maxUsageCluster.getListOfDocumentsNames().contains(doc)) {
				// add to this and remove from others
				for(ClusterDetails cls:docCLusterUsageMap.keySet()) {
					if(!cls.equals(maxUsageCluster)) {
							cls.removeDoc(doc);
					}
					else {
//						if(cls.getListOfDocuments())
						cls.addDocumentToCluster(doc);
					}
				}

			}
			
		}
//		List<ClusterDetails> nonelistofclusters1=this.listofclusters.stream().filter(cls->cls.getClusterName().trim().equals("None")).collect(Collectors.toList());
//		System.out.println("NONE CLUSTER"+nonelistofclusters1.size());
		if(notusedDocs.size()>0) {
			ClusterDetails cls = new ClusterDetails(notusedDocs);
			cls.setClusterName("Unused");			
			this.listofclusters.add(cls);

		}
		System.out.println("size: "+this.listofclusters.size());

		
	}

	/**
	 * @return the listofclusters
	 */
	public List<ClusterDetails> getListofclusters() {
		return listofclusters;
	}

	/**
	 * @param listofclusters the listofclusters to set
	 */
	public void setListofclusters(List<ClusterDetails> listofclusters) {
		this.listofclusters = listofclusters;
	}

}
