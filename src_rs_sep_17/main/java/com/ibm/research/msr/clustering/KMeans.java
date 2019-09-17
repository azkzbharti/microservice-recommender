/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.ibm.research.msr.extraction.Document;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.CSVLoader;

/**
 * @author ShreyaKhare
 *
 */
public class KMeans extends Clustering {

	SimpleKMeans kmeans = new SimpleKMeans();
	private static ArrayList<Document> pool_of_Documents;

	String tfpath;

	public KMeans(List<Document> list, String filepath, int K) throws IOException {
		super(list);
		this.tfpath = filepath;
		kmeans.setInitializationMethod(new SelectedTag(SimpleKMeans.KMEANS_PLUS_PLUS, SimpleKMeans.TAGS_SELECTION));
		kmeans.setSeed(10);
		kmeans.setPreserveInstancesOrder(true);
		try {
			kmeans.setNumClusters(K);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void runClustering() {
		File inFile = new File(tfpath);
		CSVLoader loader = new CSVLoader();
		Map<Integer, ClusterDetails> clus = new TreeMap<>();
		try {
			loader.setSource(inFile);
			Instances data = loader.getDataSet();
			data = filterData(data);
			kmeans.buildClusterer(data);

			int[] assignments = kmeans.getAssignments();

			for (int i = 0; i < assignments.length; i++) {
				
				if(clus.containsKey(assignments[i])) {
					clus.get(assignments[i]).addDocumentToCluster(listOfDocuments.get(i));
				} else {
					ClusterDetails cd = new ClusterDetails(listOfDocuments.get(i));
					clus.put(assignments[i], cd);
				}			       			          
			}
			clusters =clus.values().stream().collect(Collectors.toList());
			
		}
		catch(IllegalArgumentException e) {
			System.out.println("Argument Erorr : reduce k for KMeans");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
