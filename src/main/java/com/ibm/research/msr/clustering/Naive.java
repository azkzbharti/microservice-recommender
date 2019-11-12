/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.ibm.research.msr.extraction.Document;

/**
 * @author ShreyaKhare
 *
 */
public class Naive extends Clustering {
	
	private String combineStrategy ="split";

	public Naive(List<Document> documents,String combineStrategy ) {
		super(documents);
		this.combineStrategy=combineStrategy;

	}

	protected ArrayList<ClusterDetails> calculate_initial_clusters(List<Document> pool_of_Documents) {
		// Implementing Clustering++ for initial seeding
		ArrayList<ClusterDetails> initial_centroids = new ArrayList<>();
		int total_words = pool_of_Documents.get(0).getDocVector().size();
		for (int i = 0; i < total_words; i++) {
			ArrayList<Document> docsList = new ArrayList<>();
			for (int j = 0; j < pool_of_Documents.size(); j++) {
				double tf = pool_of_Documents.get(j).getDocVector().get(i);
//				if(pool_of_Documents.get(j).getFile().getAbsolutePath().contains("GitLabUrlFormatter.java"))
//					System.out.println("here");
				if (tf > 0) {
					docsList.add(pool_of_Documents.get(j));
				}
			}
			initial_centroids.add(i, new ClusterDetails(docsList));
		}
//		
		 Set<ClusterDetails> s= new HashSet<ClusterDetails>();
		 s.addAll(initial_centroids);
		 initial_centroids = new ArrayList<ClusterDetails>();
		 initial_centroids.addAll(s); 
		
//		for (int j = 0; j < initial_centroids.size(); j++) {
//		System.out.println("cluster " + j );
//		System.out.println(initial_centroids.get(j).getListOfDocumentsNames());
//	}
		return initial_centroids;
		
	}

	public void runClustering() {
		ArrayList<ClusterDetails> clusters = calculate_initial_clusters(listOfDocuments);
		System.out.println("Initial unique Docs: "+getUniquedocs(clusters));
		Quartet quartet;
		Quartet selquartet;
		Comparator<Quartet> comparator = new Comparator<Quartet>() {

			public int compare(Quartet tupleA, Quartet tupleB) {
				return Double.compare(tupleA.getSize(), tupleB.getSize());
//                return (tupleA.getSize()-(tupleB.getSize()));
			}

		};
//    	System.out.println(clusters.size());
		for (int i = 0; i < clusters.size(); i++) {
//			for (int j = 0; j < clusters.size(); j++) {
////				System.out.println("cluster " + j + "before" + i + "iterations");
//				System.out.println(clusters.get(j).getListOfDocumentsNames());
//			}
			ClusterDetails firstcls = clusters.get(i);
    		System.out.println("cluster at "+i+" with size "+ firstcls.listOfDocuments.size());
			firstcls.showDetails();
			ArrayList<Quartet> inter_size = new ArrayList<Quartet>();
			for (int j = 0; j < clusters.size(); j++) {
				if (i != j) {
					ClusterDetails secondcls = clusters.get(j);
					List<ClusterDetails> newcls ;
					newcls = splitCluster(firstcls, secondcls);
					int sz = newcls.get(0).getListOfDocuments().size();
					quartet = new Quartet(sz, i, j, newcls);
					inter_size.add(quartet);
				}
			}
			Collections.sort(inter_size, Collections.reverseOrder(comparator));
//    	    for(Quartet s:inter_size) {
//    	    	System.out.println(s.getSize());
//    	    }
			selquartet = inter_size.get(0);

			if (selquartet.getSize() > 0) {
//				System.out.println("seletcted size");
//				System.out.println(selquartet.getSize());
//				System.out.println("selected i");
//				System.out.println(selquartet.getI());
//				System.out.println("selected j");
//				System.out.println(selquartet.getJ());

				if (selquartet.getJ() < selquartet.getI()) // order of deletion is important
				{
//					 System.out.println("cluster removed index " + selquartet.getJ() + "--" + selquartet.getI());
//					System.out.println(clusters.get(selquartet.getJ()).listOfDocuments.size());
					clusters.remove(selquartet.getJ());// cause i is already removed
//					System.out.println(clusters.get(selquartet.getI() - 1).listOfDocuments.size());
					clusters.remove(selquartet.getI() - 1);
				} else {
//					System.out.println("cluster removed index " + selquartet.getJ() + "--" + selquartet.getI());
//					System.out.println(clusters.get(selquartet.getI()).listOfDocuments.size());
					clusters.remove(selquartet.getI());// cause i is already removed
//					System.out.println(clusters.get(selquartet.getJ() - 1).listOfDocuments.size());
					clusters.remove(selquartet.getJ() - 1);

				}
//    			System.out.println("Intsection size"+selquartet.getCd().get(0).getListOfDocuments().size());
//    			System.out.println("Difference size"+selquartet.getCd().get(1).getListOfDocuments().size());
				for (ClusterDetails cd : selquartet.getCd()) {
					if (cd.getListOfDocuments().size() > 0) // adding only non empty cluster to avoid null pointer error
					{
						if(!clusters.contains(cd))
//						System.out.println("new cluster adding " + cd.getListOfDocuments().size());
						clusters.add(cd);

					}
				}

			}

//        		for (int j=0;j<clusters.size();j++) {
//        		System.out.println("cluster "+j+"after"+i+"iterations");
//        		System.out.println(clusters.get(j).getListOfDocuments().size());
//        		} 
		}
//		System.out.println("----");
//		System.out.println("finalscls");
//		for (int i = 0; i < clusters.size(); i++) {
//			System.out.println("cluster " + i);
//			System.out.println(clusters.get(i).getListOfDocumentsNames());
//			System.out.println("+++++++");
//
//		}
		this.clusters = clusters;
		System.out.println("At End unique Docs: "+getUniquedocs(clusters));

		int count = 0;
		JSONObject clusterobj = new JSONObject();
		clusterobj.put("name", "root");
		clusterobj.put("parent", null);
		JSONArray clusterArray = new JSONArray();
		for (ClusterDetails cls : clusters) {
			if (cls.getListOfDocuments().size() > 0) {
				clusterArray.add(cls.getClusterJson(count));
				count += 1;
			}
		}
		clusterobj.put("children", clusterArray);

		System.out.println(clusterobj);
	}

	public ArrayList<Document> getIntersection(ClusterDetails c1, ClusterDetails c2) {
		ArrayList<Document> Intersection = new ArrayList<>();
		for (Document doc : c1.getListOfDocuments()) {
			if (c2.getListOfDocuments().contains(doc)) {

				Intersection.add(doc);
			}

		}
		return Intersection;

	}

		
	public List<ClusterDetails> splitCluster(ClusterDetails c1, ClusterDetails c2) {
		List<ClusterDetails> clusters = new ArrayList<>();
		
		if(this.combineStrategy=="onlyMerge") {
			List<Document> combineddocs = new ArrayList<>();
			combineddocs.addAll(c1.getListOfDocuments().stream().collect(Collectors.toList()));
			combineddocs.addAll(c2.getListOfDocuments().stream().collect(Collectors.toList()));
			Set<Document> combineddocsSet = new HashSet<Document>(combineddocs); 
			clusters.add(new ClusterDetails(combineddocs));
			return clusters;
		}
		
		List<Document> intersection;
		List<Document> difference;
		 
		Set<String> docsNames1 = c1.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> docsNames2 = c2.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> intersectionNamesSet = Sets.intersection(docsNames1, docsNames2);
//		if(docsNames1.contains("GitLabUrlFormatter.java"))
//			System.out.println("debug here");
//		if(docsNames2.contains("GitLabUrlFormatter.java"))
//			System.out.println("debug here");
		intersection = c1.getListOfDocuments().stream().filter(d -> intersectionNamesSet.contains(d.getName()))
				.collect(Collectors.toList());
		clusters.add(new ClusterDetails(intersection));
		Set<String> differenceNamesSet = Sets.difference(docsNames1, docsNames2);	

		difference = c1.getListOfDocuments().stream().filter(d -> differenceNamesSet.contains(d.getName()))
				.collect(Collectors.toList());
		clusters.add(new ClusterDetails(difference));
		Set<String> differenceNamesSet2 = Sets.difference(docsNames2, docsNames1);	
		difference = c2.getListOfDocuments().stream().filter(d -> differenceNamesSet2.contains(d.getName()))
				.collect(Collectors.toList());
		clusters.add(new ClusterDetails(difference));
//		difference.addAll(c2.getListOfDocuments().stream().filter(d -> differenceNamesSet.contains(d.getName()))
//				.collect(Collectors.toList()));


		return clusters;
	}

}
