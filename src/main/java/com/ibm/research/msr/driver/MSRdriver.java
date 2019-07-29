package com.ibm.research.msr.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.clustering.DBSCAN;
import com.ibm.research.msr.clustering.KMeans;
import com.ibm.research.msr.clustering.Naive;
import com.ibm.research.msr.clustering.NaiveTFIDF;
import com.ibm.research.msr.clustering.PostProcessClusters;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.ibm.research.msr.utils.ReadJarMap;


/**
 * 
 *
 */
public class MSRdriver {
	
	public static Clustering runSingleAlgorithm(AnalyzeApp analyzer,List<String> args) throws IOException {
		
		String algorithm = args.get(1);// "KMeans";

		Clustering oc = null;
//		System.out.println(algorithm);
		System.out.println(args.toString());

		String combineStrategy="";
		//TODO: check on the number of arguments provided by the user
		switch (algorithm) {
		case "kMeans": {
			int k = Integer.parseInt(args.get(3));
			oc = new KMeans(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), k);
			break;
		}
		case "DBSCAN": {
			double epsilon = Double.parseDouble(args.get(3));// 0.0003 ;
			int neighbours = Integer.parseInt(args.get(4));// args[3];
			oc = new DBSCAN(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), epsilon, neighbours);
			break;
		}
		case "NAIVETFIDF": {
			String meaureType =args.get(3); // "cosine";//args[2];
			combineStrategy=args.get(4); // "onlyMerge"
			oc = new NaiveTFIDF(analyzer.getListOfDocuments(), combineStrategy,meaureType);
//				 oc = new NaiveTFIDF(analyzer.getListOfDocuments(),"cosine");
			algorithm = algorithm + meaureType;
			break;
		}
		case "NAIVE": {
			combineStrategy=args.get(3);//"onlyMerge"
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
		oc.removeDuplicate();
		System.out.println(oc.getClusters().size());

		
		
		String d3filename = "src/main/output/cluster.html"; // TODO : Make argument 
		d3filename = d3filename.replaceAll(".html", algorithm+combineStrategy+args.get(2)+".html");
		oc.savecLusterJSON(d3filename);
		oc.CombineClusters();
		

		return oc;
		
		
	}
	
	public static void runAllAlgorithms(AnalyzeApp analyzer,List<String> args) throws IOException {

	// total 8 outputs
		Clustering oc =null;
		
		args.set(1, "NAIVE"); // has 2 variations as below 
		args.add(3, "onlyMerge");

		oc=runSingleAlgorithm(analyzer,args);
		oc.setClusters(oc.getConsolidatedClusters());
		
//		
//		
		args.set(3, "split");
//		oc=runSingleAlgorithm(analyzer,args);
//		oc.setClusters(oc.getConsolidatedClusters());
//		
//		
//
		args.set(1, "kMeans");
		args.add(3, "2");
		oc=runSingleAlgorithm(analyzer,args);
		oc.setClusters(oc.getConsolidatedClusters());
//		
//	
//		
		args.set(1, "DBSCAN");
		args.set(3, "0.0003");
		args.add(4,  "1");
		
//		oc=runSingleAlgorithm(analyzer,args);
//		oc.setClusters(oc.getConsolidatedClusters());
//		
		 args.set(1, "NAIVETFIDF"); // has 4 variations as below 
		
			args.set(3, "cosine");
			args.set(4,  "onlyMerge");
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			
			args.set(3, "euclidiean");
			args.set(4,  "onlyMerge");
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			
			args.set(3, "cosine");
			args.set(4,  "split");
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			
			args.set(3, "euclidiean");
			args.set(4,  "split");
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
//			
		 
//		oc.scorePartialClusters(oc.getClusters());	
		String d3filename = "src/main/output/clusterall.html"; // TODO : Make argument 
		oc.savecLusterJSON(d3filename);
		
	}

	public static void main(String[] args) throws IOException, Exception {
		
		
		try {
			JarApiList.createJARFile(args[0]);
			ReadJarMap.createJARCategoryMap();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error while creating jar map"+e.toString());
		}
		
		String appPath = args[0]; // "/Users/shreya/git/digdeep";
		
		// Create the output folder 
		String outputDir = "src/main/output2"; //TODO take in as argument
		if(new File(outputDir).mkdir()){
		}
		else {
			System.out.println("Output directory exists");

		}
		AnalyzeApp analyzer ;
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));;
		List<String> argsList2 = new ArrayList<String>(Arrays.asList(args));;
		
		if(args[1].equals("all")){
			argsList.add("true"); //TODO: remove this
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(2)));
			analyzer = new AnalyzeApp(appPath);
			System.out.println(DocumentParserUtil.getIgnoreNone());

			System.out.println(argsList.size());
			runAllAlgorithms(analyzer, argsList);
			
			argsList2.add("false"); //TODO: remove this
			
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList2.get(2)));
			
			System.out.println("her"+DocumentParserUtil.getIgnoreNone());
			analyzer = new AnalyzeApp(appPath);
			runAllAlgorithms(analyzer, argsList2);
		}
		else {
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(2)));
			analyzer = new AnalyzeApp(appPath);
			runSingleAlgorithm(analyzer,argsList); // TODO: remove setIgnoreNone from user input always generate both
		}

//		analyzer.computeMeasure();
//		analyzer.saveMeasure(null);

		
	}
}
