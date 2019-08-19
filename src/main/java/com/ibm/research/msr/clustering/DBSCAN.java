/**
 * 
 */
package com.ibm.research.msr.clustering;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.ibm.research.msr.extraction.Document;

import weka.clusterers.DBScan;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

/**
 * @author ShreyaKhare
 *
 */
public class DBSCAN extends Clustering{
	DBScan dbScan = new DBScan();
	String  tfpath;
	
//	public void estimateEpsilion(Instances data) {
//		EuclideanDistance Dist = 
//
//		
//	}
	
	public DBSCAN(List<Document> list, String filepath, double eps, int neigh) throws IOException {
		super(list);
		this.tfpath = filepath;
	    dbScan.setEpsilon(eps);
	    dbScan.setMinPoints(neigh);
//		

	}
	 public int clusterInstance(Instance instance) throws Exception {
		          int retval = 0;
		       try {
		         retval = dbScan.clusterInstance(instance);
		         } catch (Exception e) {
		             retval = -1;
		       }
		        return retval;
  }
	@Override
	public void runClustering() {
		// TODO Auto-generated method stub
		File inFile = new File(tfpath);
		CSVLoader loader = new CSVLoader();
		Map<Integer, ClusterDetails> clus = new TreeMap<>();

		try {
			loader.setSource(inFile);
			Instances data = loader.getDataSet();
			// filter data
			data = filterData(data);
			dbScan.buildClusterer(data);
			int i=0;
			for (Instance instance: data) {
				
				int label=clusterInstance(instance);
				if(clus.containsKey(label)) {
					clus.get(label).addDocumentToCluster(listOfDocuments.get(i));
				} else {
					ClusterDetails cd = new ClusterDetails(listOfDocuments.get(i));
					clus.put(label, cd);
				}		
				i=i+1;
			}
			clusters =clus.values().stream().collect(Collectors.toList());
//			ClusterEvaluation evaluation = new ClusterEvaluation();
//		    evaluation.setClusterer(dbScan);
//		    evaluation.evaluateClusterer(data);
//		    System.out.println(evaluation.clusterResultsToString());
////		    System.out.println(dbScan.numberOfClusters());


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
}
