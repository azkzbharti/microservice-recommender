/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	public Naive(List<Document> documents) {
		super(documents);
	}

	protected ArrayList<ClusterDetails> calculate_initial_clusters(List<Document> pool_of_Documents) {
		// Implementing Clustering++ for initial seeding
		ArrayList<ClusterDetails> initial_centroids = new ArrayList<>();
		int total_words = pool_of_Documents.get(0).getDocVector().size();
		for (int i = 0; i < total_words; i++) {
			ArrayList<Document> docsList = new ArrayList<>();
			for (int j = 0; j < pool_of_Documents.size(); j++) {
				double tf = pool_of_Documents.get(j).getDocVector().get(i);
				if (tf > 0) {
					docsList.add(pool_of_Documents.get(j));
				}
			}
			initial_centroids.add(i, new ClusterDetails(docsList));
		}

		return initial_centroids;
	}

	public void runClustering() {
		ArrayList<ClusterDetails> clusters = calculate_initial_clusters(listOfDocuments);
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
			for (int j = 0; j < clusters.size(); j++) {
				System.out.println("cluster " + j + "before" + i + "iterations");
				System.out.println(clusters.get(j).getListOfDocuments().size());
			}
			ClusterDetails firstcls = clusters.get(i);
//    		System.out.println("cluster at "+i+" with size "+ firstcls.listOfDocuments.size());
//			firstcls.showDetails();
			ArrayList<Quartet> inter_size = new ArrayList<Quartet>();
			for (int j = 0; j < clusters.size(); j++) {
				if (i != j) {
					ClusterDetails secondcls = clusters.get(j);
//    				System.out.println("cluster at j "+j);
//    	    		System.out.println("cluster at j "+j+" with size "+ secondcls.listOfDocuments.size());

//    				secondcls.show_details();
					List<ClusterDetails> newcls = splitCluster(firstcls, secondcls);
//    	    		System.out.println(newcls.get(0).getListOfDocuments());
					int sz = newcls.get(0).getListOfDocuments().size();
//    	    		System.out.println("intesection size"+sz+"diff size : "+newcls.get(1).getListOfDocuments().size());
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
				System.out.println("seletcted size");
				System.out.println(selquartet.getSize());
				System.out.println("selected i");
				System.out.println(selquartet.getI());
				System.out.println("selected j");
				System.out.println(selquartet.getJ());

				if (selquartet.getJ() < selquartet.getI()) // order of deletion is important
				{
					System.out.println("cluster removed index " + selquartet.getJ() + "--" + selquartet.getI());
					System.out.println(clusters.get(selquartet.getJ()).listOfDocuments.size());
					clusters.remove(selquartet.getJ());// cause i is already removed
					System.out.println(clusters.get(selquartet.getI() - 1).listOfDocuments.size());
					clusters.remove(selquartet.getI() - 1);
				} else {
					System.out.println("cluster removed index " + selquartet.getJ() + "--" + selquartet.getI());
					System.out.println(clusters.get(selquartet.getI()).listOfDocuments.size());
					clusters.remove(selquartet.getI());// cause i is already removed
					System.out.println(clusters.get(selquartet.getJ() - 1).listOfDocuments.size());
					clusters.remove(selquartet.getJ() - 1);

				}
//    			System.out.println("Intsection size"+selquartet.getCd().get(0).getListOfDocuments().size());
//    			System.out.println("Difference size"+selquartet.getCd().get(1).getListOfDocuments().size());
				for (ClusterDetails cd : selquartet.getCd()) {
					if (cd.getListOfDocuments().size() > 0) // adding only non empty cluster to avoid null pointer error
					{
						System.out.println("new cluster adding " + cd.getListOfDocuments().size());
						clusters.add(cd);

					}
				}

			}

//        		for (int j=0;j<clusters.size();j++) {
//        		System.out.println("cluster "+j+"after"+i+"iterations");
//        		System.out.println(clusters.get(j).getListOfDocuments().size());
//        		} 
		}
		System.out.println("----");
		System.out.println("finalscls");
		for (int i = 0; i < clusters.size(); i++) {
			System.out.println("cluster " + i);
			System.out.println(clusters.get(i).listOfDocuments.size());
			System.out.println("+++++++");

		}
		this.clusters = clusters;
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
		List<Document> intersection;
		List<Document> difference;
		Set<String> docsNames1 = c1.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> docsNames2 = c2.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> intersectionNamesSet = Sets.intersection(docsNames1, docsNames2);
		Set<String> differenceNamesSet = Sets.symmetricDifference(docsNames1, docsNames2);

		intersection = c1.getListOfDocuments().stream().filter(d -> intersectionNamesSet.contains(d.getName()))
				.collect(Collectors.toList());
		difference = c1.getListOfDocuments().stream().filter(d -> differenceNamesSet.contains(d.getName()))
				.collect(Collectors.toList());
		difference.addAll(c2.getListOfDocuments().stream().filter(d -> differenceNamesSet.contains(d.getName()))
				.collect(Collectors.toList()));

		clusters.add(new ClusterDetails(intersection));
		clusters.add(new ClusterDetails(difference));
		return clusters;
	}

}
