package com.ibm.research.msr.driver;

import java.io.File;
import java.io.IOException;

import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.clustering.DBSCAN;
import com.ibm.research.msr.clustering.KMeans;
import com.ibm.research.msr.clustering.Naive;
import com.ibm.research.msr.clustering.NaiveTFIDF;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.ibm.research.msr.utils.ReadJarMap;


/**
 * 
 *
 */
public class MSRdriver {

	public static void main(String[] args) throws IOException, Exception {
		try {
			JarApiList.createJARFile(args[0]);
			ReadJarMap.createJARCategoryMap();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error while creating jar map"+e.toString());
		}

//		String appPath="/Users/shreya/git/digdeep";
		
		String appPath = args[0];
		String algorithm = args[1];// "KMeans";
		DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(args[2]));	  // TODO: PASS AS ARGUMENT true/false
		
		// Create the output folder
		String outputDir = "src/main/output";
		new File(outputDir).mkdir();

		AnalyzeApp analyzer = new AnalyzeApp(appPath);

//		analyzer.computeMeasure();
//		analyzer.saveMeasure(null);

		Clustering oc = null;
		System.out.println(algorithm);
		String combineStrategy="";
		switch (algorithm) {
		case "kMeans": {
			int k = Integer.parseInt(args[3]);
			oc = new KMeans(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), k);
			break;
		}
		case "DBSCAN": {
			double epsilon = Double.parseDouble(args[3]);// 0.0003 ;
			int neighbours = Integer.parseInt(args[4]);// args[3];
			oc = new DBSCAN(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), epsilon, neighbours);
			break;
		}
		case "NAIVETFIDF": {
			String meaureType = args[3]; // "cosine";//args[2];
			combineStrategy=args[4]; // "onlyMerge"
			oc = new NaiveTFIDF(analyzer.getListOfDocuments(), combineStrategy,meaureType);
//				 oc = new NaiveTFIDF(analyzer.getListOfDocuments(),"cosine");
			algorithm = algorithm + meaureType;
			break;
		}
		case "NAIVE": {
			combineStrategy=args[3]; //"onlyMerge"
			oc = new Naive(analyzer.getListOfDocuments(),combineStrategy);
			break;
		}
		default: {
			System.out.println("No algorithm, exiting");
			System.exit(0);
		}

		}

		oc.runClustering();
		oc.getClusters();

		String d3filename = "src/main/output/cluster.html"; // TODO : Make argument
		d3filename = d3filename.replaceAll(".html", algorithm+combineStrategy+args[2]+".html");
		oc.savecLusterJSON(d3filename);

	}
}
