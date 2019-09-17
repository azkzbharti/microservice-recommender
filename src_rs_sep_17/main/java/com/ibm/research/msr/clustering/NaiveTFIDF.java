package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.research.msr.extraction.Document;

public class NaiveTFIDF extends Naive {
	String distancetype;
	 
    
	public NaiveTFIDF(List<Document> documents,String combineStrategy,String meausretype) {
		super(documents,combineStrategy);
		this.distancetype=meausretype;
	}

	private double editDistance(List<Double> list, List<Double> list2) {
		double distance = 0;
		double d;
		for (int component = 0; component < list2.size(); component++) {
			d = list.get(component) - list2.get(component);
			distance += d * d;
		}

		return Math.sqrt(distance);
	}
    
	private double cosinedistance(List<Double> list1, List<Double> list2){
		if (list1 == null || list2== null || list1.size() == 0 || list2.size() == 0 || list1.size() != list2.size()) {
    		return 2;
    	}

	   	double sumProduct = 0;
    	double sumASq = 0;
    	double sumBSq = 0;
    	for (int i = 0; i < list1.size(); i++) {
    		sumProduct +=  list1.get(i) * list2.get(i);
    		sumASq += list1.get(i) * list1.get(i);
    		sumBSq +=  list2.get(i) * list2.get(i);
    	}
    	if (sumASq == 0 && sumBSq == 0) {
    		return 2.0;
    	}
		
		      	return sumProduct / (Math.sqrt(sumASq) * Math.sqrt(sumBSq));

		
	}
	public double compareClusters(ClusterDetails c1, ClusterDetails c2) {
		double tmin_distance = 0;
		for (Document doc : c1.getListOfDocuments()) {
			for (Document doc2 : c1.getListOfDocuments()) {
				double min_distance =0;
				if(distancetype=="cosine")
				 min_distance = cosinedistance(doc.getDocVector(), doc2.getDocVector());
				else
				 min_distance = editDistance(doc.getDocVector(), doc2.getDocVector());
				if (tmin_distance < min_distance) {
					tmin_distance = min_distance;
				}
			}
		}
		return tmin_distance;
	}

	public void runClustering() {
		ArrayList<ClusterDetails> clusters = calculate_initial_clusters(listOfDocuments);   
		Quartet quartet;
		Quartet selquartet;
		Comparator<Quartet> comparator = new Comparator<Quartet>() {

			public int compare(Quartet tupleA, Quartet tupleB) {
				return Double.compare(tupleA.getSize(), tupleB.getSize());
			}

		};
//		System.out.println(clusters.size());
		for (int i = 0; i < clusters.size(); i++) {
//			for (int j = 0; j < clusters.size(); j++) {
//				System.out.println("cluster " + j + "before" + i + "iterations");
//				System.out.println(clusters.get(j).getListOfDocuments().size());
//			}
			ClusterDetails firstcls = clusters.get(i);
//			System.out.println("cluster at " + i);
//			firstcls.showDetails();
			ArrayList<Quartet> inter_size = new ArrayList<Quartet>();
			for (int j = 0; j < clusters.size(); j++) {
				if (i != j) {

					ClusterDetails secondcls = clusters.get(j);
//					System.out.println("cluster at j " + j);
//					secondcls.showDetails();
					double sz = compareClusters(firstcls, secondcls);
					List<ClusterDetails> newcls = splitCluster(firstcls, secondcls);
//    	    		System.out.println(newcls.get(0).getListOfDocuments());
//					System.out.println("intesection dist" + sz);
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
					clusters.remove(selquartet.getJ());// cause i is already removed
					clusters.remove(selquartet.getI() - 1);
				} else {

					clusters.remove(selquartet.getI());// cause i is already removed
					clusters.remove(selquartet.getJ() - 1);
				}
//				System.out.println("Intsection size" + selquartet.getCd().get(0).getListOfDocuments().size());
//				System.out.println("Difference size" + selquartet.getCd().get(1).getListOfDocuments().size());
				for (ClusterDetails cd : selquartet.getCd()) {
					if (cd.getListOfDocuments().size() > 0) // adding only non empty cluster to avoid null pointer error
						clusters.add(cd);
				}

			}
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
	}
	

}
