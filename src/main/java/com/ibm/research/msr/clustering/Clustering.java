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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.ibm.research.msr.extraction.Document;
import com.ibm.research.msr.utils.ReadJarMap;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
/**
 * @author ShreyaKhare
 *
 */
public abstract class Clustering {

	static Map<ClusterDetails, Double> consolidatedClusters = new HashMap<ClusterDetails, Double>();

	protected List<Document> listOfDocuments = new ArrayList<Document>();
	protected List<ClusterDetails> clusters = new ArrayList<>();

	public Clustering(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}

	public void CLeanClusters()	{
		for(ClusterDetails cls:clusters) {
			cls.showDetails();
			cls.removeDuplicates();
			
		}
	}
	
	public double getScoreForList(List<ClusterDetails> clusterList) {
		double score=0;
		List<Set<String>> docList;
		boolean first = true;
		Set<String> docsNames1= new HashSet();
		Set<String> docsNames2= new HashSet();
		Set<String> inter= new HashSet();
		Set<String> alldocs = new HashSet();
		for(ClusterDetails cls: clusterList) {
			if(first) {
				docsNames1=cls.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
				first=false;
			}
			else {
				docsNames2=cls.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
				inter=Sets.intersection(docsNames1, docsNames2);
//				if(inter.size()>0)
//					System.out.println(inter);
				docsNames1=inter;
			}
			alldocs.addAll(cls.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet()));
		}
//		Set<String> = Sets.cartesianProduct(docList.subList(0, docList.size()));
		score=inter.size()/alldocs.size();
//		System.out.println(score);
		return score;
		
	}
	
//	private static List<ImmutableList<ClusterDetails>> makeListofImmutable(List<ClusterDetails> values) {
//		  List<ImmutableList<ClusterDetails>> converted = new LinkedList<>();
//		  values.forEach(array -> {
//		    converted.add(ImmutableList.copyOf(array));
//		  });
//		  return converted;
//		}
//	
//	private void computeCartesianProduct(List<List<ClusterDetails>> algoClusterList) {
//		
//		// create object for each algorithm
//		List<AlgorithmResult> algoClusters = new LinkedList<>();
//		for(List<ClusterDetails> clusters : algoClusterList) {
//			algoClusters.add(new AlgorithmResult(clusters));
//		}
//		
//		List<AlgorithmResult> result = new LinkedList<>();
//				
//		// populate first list as is
//		AlgorithmResult firstList = algoClusters.get(0);
//		for(ClusterDetails cd : firstList.clusters) {
//			result.add(new AlgorithmResult(List.of(cd)));	
//		}
//		
//		
//		// now multiply the existing result with the next list in the iterator
//		for(int i = 1 ; i < algoClusters.size(); i++) {
//			result = multiply(result, algoClusters.get(i));
//		}
//		
//		System.out.println(result.size());
//	}
//	
//	private List<AlgorithmResult> multiply(List<AlgorithmResult> current, AlgorithmResult listToMultiply) {
//		System.out.println(current.size() + " " + listToMultiply.clusters.size());
//		List<AlgorithmResult> result = new LinkedList<>();
//		for(AlgorithmResult algorithmResult : current) {
//			for(ClusterDetails cd : listToMultiply.clusters) { 
////				int inter=cd.getNoIntersection(algorithmResult.clusters)
////				if(inter==0) {
////					
////				}
//				//List<ClusterDetails> clusters = new ArrayList<>();
//				//clusters.add(cd);
//				//clusters.addAll(algorithmResult.clusters);
//				//result.add(new AlgorithmResult(clusters));
//			}
//		}
//		
//		System.out.println("Result size :- " + result.size());
//		return result;
//		
//	}
//	
//	private static class AlgorithmResult{
//		private List<ClusterDetails> clusters = new LinkedList<>();
//
//		public AlgorithmResult(List<ClusterDetails> clusters) {
//			super();
//			this.clusters.addAll(clusters);
//		}
//	}
//	
//	private static class Result{
//		
//	}
//	
//	
//	private void computeCartesianProductR(List<List<ClusterDetails>> algoClusterList) {
//		
//		// create object for each algorithm
//		List<AlgorithmResult> algoClusters = new LinkedList<>();
//		for(List<ClusterDetails> clusters : algoClusterList) {
//			algoClusters.add(new AlgorithmResult(clusters));
//		}
//		
//		List<AlgorithmResult> result = new LinkedList<>();
//				
//		// populate first list as is
//		AlgorithmResult firstList = algoClusters.get(0);
//		for(ClusterDetails cd : firstList.clusters) {
//			result.add(new AlgorithmResult(List.of(cd)));	
//		}
//		
//		
//		// now multiply the existing result with the next list in the iterator
//		for(int i = 1 ; i < algoClusters.size(); i++) {
//			result = multiply(result, algoClusters.get(i));
//		}
//		
//		System.out.println(result.size());
//	}
//	
	
	public List<ClusterDetails> mergeRemainingClusters(List<List<ClusterDetails>> algoCLusterList1) {
		
		
//		List<Set<ClusterDetails>> algoCLusterList = algoCLusterList1.stream().filter(cls->cls.size()!=0).collect(Collectors.toList());
		List<List<ClusterDetails>> algoCLusterList = algoCLusterList1.stream().filter(cls->cls.size()!=0).collect(Collectors.toList());
		long l=1;
		for(List<ClusterDetails> cls:algoCLusterList) {
			l=l*cls.size();
			System.out.println(cls.size());
		}
		System.out.println(l);
		List<ClusterDetails> mergerClusters = new ArrayList<>();
		
//		Set<List<ClusterDetails>> resultClusters =Sets.cartesianProduct(algoCLusterList.subList(0, algoCLusterList.size()));
//		computeCartesianProduct(algoCLusterList);

		List<List<ClusterDetails>> resultClusters =Lists.cartesianProduct(algoCLusterList);

		Map<List<ClusterDetails>,Double> scoreMap=new HashMap<List<ClusterDetails>,Double>();          
		Map<List<ClusterDetails>,Double> sortedscoreMap;
		
//		List<Double> allscores= new ArrayList<>();
//		for(List<ClusterDetails> cls:resultClusters) {
//			allscores.add(getScoreForList(cls));
//			scoreMap.put(cls,getScoreForList(cls));
//		}
//		Collections.sort(allscores);
//		
//		sortedscoreMap = scoreMap
//			        .entrySet()
//			        .stream()
//			        .limit(10)
//			        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//			        .collect(
//			            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
//			                LinkedHashMap::new));
//		boolean first=true;
//		double maxvalue=0.0,currentvalue;
//		for(List<ClusterDetails> clsList:sortedscoreMap.keySet()) {
//			if(first) {
//				currentvalue=sortedscoreMap.get(clsList);
//				maxvalue=currentvalue;
//				first=false;
//			}
//			else {			
//				currentvalue=sortedscoreMap.get(clsList);
//			}
//				if(currentvalue >= maxvalue) { // TODO: put a threshold on the difference
//					Set<Document> doclist = new HashSet<>();
//
//					for(ClusterDetails cls:clsList) {
////						cls.showDetails();
//						doclist.addAll(cls.getListOfDocuments());
//					}
//					if(doclist.size()>0) {
//						System.out.println("currentvalue"+currentvalue);
//						ClusterDetails cls = new ClusterDetails(doclist.stream().collect(Collectors.toList()));
//						cls.setScore(currentvalue);
//						mergerClusters.add(cls);
//						System.out.println(cls.getScore());
//					}
//				
//			}
////			List<List> doclist=clsList.stream().map(ClusterDetails::getListOfDocuments).collect(Collectors.toList());
//			
//		}
		return mergerClusters;
		
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
			temp.removeDuplicates();
			newclusters.add(temp);	
//			if(temp.getClusterName().equals("commons-math3.3.0")) {
//				temp.showDetails();
//			}
//			temp.showDetails();
		}

		return newclusters;
//		return consolidatedClusters;
	}



	/**
	 * @param consolidatedClusters the consolidatedClusters to set
	 */
	public static void setConsolidatedClusters(Map<ClusterDetails, Double> consolidatedClusters) {
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
	
	public void extendClusters(List<ClusterDetails> clusterslist) {
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(this.clusters);
		 List<ClusterDetails> listofclusters = new ArrayList<ClusterDetails>();
		 listofclusters.addAll(s); 
		 for (ClusterDetails cls:clusterslist) {
//			 cls.showDetails();
			 Clustering.consolidatedClusters.put(cls,cls.getScore());
		 }
//		 this.clusters=listofclusters;
//		 Clustering.consolidatedClusters.put(key, value)
	}


	public void setClusters(List<ClusterDetails> clusters) {
		
		this.clusters = clusters;
	}
	
	public List<ClusterDetails> getNonScoreClusters(){
		return this.clusters.stream().filter(cls->cls.getScore()==0).collect(Collectors.toList());
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
	
	public void setCusterListNames() {
		for (ClusterDetails cls : this.clusters) {
			cls.setClusterName();
		}
	}
	

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
				if(cls.getClusterName().equals("Non"))
					cls.showDetails();
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
	
	public void savecLusterJSONALL(String writepath, List<String> htmlpaths) throws IOException {  
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
//				System.out.println(cls.getClusterName());
//				if(!cls.getClusterName().equals("Unused"))
//					cls.setClusterName();
				System.out.println(cls.getClusterName());
				clusterArray.add(cls.getClusterJson(count));
				count += 1;
			}
		}
		clusterobj.put("children", clusterArray);
		System.out.println(clusterobj);
		String fileString = new String(Files.readAllBytes(Paths.get("src/main/resources/clusterall.html")), StandardCharsets.UTF_8);
		fileString= fileString.replaceAll("%%JSONCONTENT%%", clusterobj.toString());
	    fileString=fileString.replaceAll("%%FILEPATHS%%", htmlpaths.toString());
		Files.write(Paths.get(writepath), fileString.getBytes());
	    
	    System.out.println("File written at "+ writepath);
	}
	public void CombineClusters() {

		for(ClusterDetails cls :this.clusters) {
			Double cvalue=0.0;
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
		 CLeanClusters();
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
