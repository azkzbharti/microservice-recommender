package com.ibm.research.msr.driver;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.ibm.research.msr.clustering.ClusterDetails;
import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.clustering.DBSCAN;
import com.ibm.research.msr.clustering.KMeans;
import com.ibm.research.msr.clustering.Naive;
import com.ibm.research.msr.clustering.NaiveTFIDF;
import com.ibm.research.msr.expandcluster.ExpandClusters;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.ibm.research.msr.utils.ReadJarMap;


/**
 * 
 *
 */
public class MSRdriver {
	
	
	
	public static Clustering runSingleAlgorithm(AnalyzeApp analyzer,List<String> args) throws IOException {
		
		String algorithm = args.get(2).trim().toLowerCase();// "KMeans";

		Clustering oc = null;
		System.out.println(args.toString());

		String combineStrategy="";
		//TODO: check on the number of arguments provided by the user
		switch (algorithm) {
		case Constants.KMEANS: {
			int k = Integer.parseInt(args.get(4));
			oc = new KMeans(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), k);
			break;
		}
		case Constants.DBSCAN: {
			double epsilon = Double.parseDouble(args.get(4));// 0.0003 ;
			int neighbours = Integer.parseInt(args.get(5));// args[3];
			oc = new DBSCAN(analyzer.getListOfDocuments(), analyzer.getMeasurePath(), epsilon, neighbours);
			break;
		}
		case Constants.NAIVE_TFIDF: {
			String meaureType =args.get(4); // "cosine";//args[2];
			combineStrategy=args.get(5); // "onlyMerge"
			oc = new NaiveTFIDF(analyzer.getListOfDocuments(), combineStrategy,meaureType);
//				 oc = new NaiveTFIDF(analyzer.getListOfDocuments(),"cosine");
			algorithm = algorithm + meaureType;
			break;
		}
		case Constants.NAIVE: {
			combineStrategy=args.get(4);//"onlyMerge"
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
		
//		
//		
//		String d3filename = "src/main/output/cluster.html"; // TODO : Make argument 
		String d3filename =args.get(1)+"/cluster.html";
		d3filename = d3filename.replaceAll(".html", algorithm+combineStrategy+args.get(3)+".html");
		oc.savecLusterJSON(d3filename);
		oc.CombineClusters();
		
		

		return oc;
		
		
	}
	public static Clustering runNaiveUtility(AnalyzeApp analyzer,List<String> args) throws IOException {
		Clustering oc =null;
		
		List<List<ClusterDetails>>  allAlgoClusterList = new ArrayList<>();

		args.set(2, Constants.NAIVE); // has 2 variations as below 
		args.add(4, Constants.ONLY_MERGE);
//
		oc=runSingleAlgorithm(analyzer,args);
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());
		System.out.println("Currently total consolidated clusters: "+oc.getConsolidatedClusters().size());

		oc.setClusters(oc.getConsolidatedClusters());
		System.out.println(oc.getClusters().size());
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

	
		args.set(4, Constants.SPLIT);
		oc=runSingleAlgorithm(analyzer,args);
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());
		System.out.println("Currently total consolidated clusters: "+oc.getConsolidatedClusters().size());

		oc.setClusters(oc.getConsolidatedClusters());
		System.out.println(oc.getClusters().size());		
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));
		return oc;
		
	}
	
	public static void runNaive(String appPath,String outputPath) throws IOException, Exception {
		
		try {
			JarApiList.createJARFile(appPath); // write to CSV
			ReadJarMap.createJARCategoryMap(); // read CSV to map

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error while creating jar map"+e.toString());
		}
		
		if(new File(outputPath).mkdir()){
			System.out.println("Result directory created at "+outputPath);
		}
		else {
			System.out.println("Output directory exists");

		}
		AnalyzeApp analyzer ;
		Clustering oc = null;
		List<String> argsList = new ArrayList<String>();
		argsList.add(appPath);  // workaround:: argList created just to keep code changes minimal
		argsList.add(outputPath);
		argsList.add(Constants.ALL); // TODO: will be removed minimial code changes 
		List<String> argsList2=new ArrayList<String>(argsList);
		argsList.add("true"); //"will ignore none category
		DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(3)));
		analyzer = new AnalyzeApp(appPath);

		oc = runNaiveUtility(analyzer, argsList);
		
		argsList2.add("false"); //TODO: remove this
		DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList2.get(3)));
		
		analyzer = new AnalyzeApp(appPath);
		oc = runNaiveUtility(analyzer, argsList2);
		
		oc.setCusterListNames();

		
		ExpandClusters ec= new ExpandClusters(oc.getClusters(),analyzer.getAppath());
		ec.getUsage();
		oc.setClusters(ec.getListofclusters());
		oc.CLeanClusters();
		
//		Below codes writes all the above variations in clusterall
		
		String d3filename = argsList2.get(1)+"/clusterall.html";//src/main/output/clusterall.html"; // TODO : Make argument 
		String htmlpath=argsList2.get(1);
		File dir = new File(htmlpath);
		String[] extensions = new String[] { "html"};
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
		StringBuilder strBuilder =
				new StringBuilder();
		int count=0;
		for (File f:files) {
			if(f.getName().contains("all"))
				continue;
			String temp="<li><a href=\"filepath\" target=\"_blank\">filename</a></li> \n";
			temp=temp.replace("filepath", f.getAbsolutePath());
			String fname=f.getName();
			fname=fname.replace(".html", "");
			count=count+1;
			fname=fname.replace("cluster", "Approach"+count+": ");
			temp=temp.replace("filename",fname);
			strBuilder.append(temp);
		}
		
		oc.savecLusterJSONALL(d3filename,strBuilder.toString());
	}
	
	
	public static Clustering runAllAlgorithms(AnalyzeApp analyzer,List<String> args) throws IOException {

	// total 8 outputs
		Clustering oc =null;
		
		List<List<ClusterDetails>>  allAlgoClusterList = new ArrayList<>();

		args.set(2, Constants.NAIVE); // has 2 variations as below 
		args.add(4, Constants.ONLY_MERGE);
//
		oc=runSingleAlgorithm(analyzer,args);
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());
		System.out.println("Currently total consolidated clusters: "+oc.getConsolidatedClusters().size());

		oc.setClusters(oc.getConsolidatedClusters());
		System.out.println(oc.getClusters().size());
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

	
		args.set(4, Constants.SPLIT);
		oc=runSingleAlgorithm(analyzer,args);
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());
		System.out.println("Currently total consolidated clusters: "+oc.getConsolidatedClusters().size());

		oc.setClusters(oc.getConsolidatedClusters());
		System.out.println(oc.getClusters().size());		
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));
		
		
		String k=Integer.toString(oc.getClusters().size());
		args.set(2, Constants.KMEANS);
		args.add(4, k);
		oc=runSingleAlgorithm(analyzer,args);
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));
		oc.setClusters(oc.getConsolidatedClusters());

//		
		args.set(2, Constants.DBSCAN);
		args.set(4, "0.0003");
		args.add(5,  "1");
		
		
		oc=runSingleAlgorithm(analyzer,args);
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));
		oc.setClusters(oc.getConsolidatedClusters());

		
		    args.set(2, Constants.NAIVE_TFIDF); // has 4 variations as below 
		
			args.set(4, Constants.COSINE);
			args.set(5,  Constants.ONLY_MERGE);
			oc=runSingleAlgorithm(analyzer,args);
			allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));
			oc.setClusters(oc.getConsolidatedClusters());

			
			args.set(4, Constants.EUCLIDIEAN);
			args.set(5,  Constants.ONLY_MERGE);
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

			
			args.set(4, Constants.COSINE);
			args.set(5,  Constants.SPLIT);
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

			
			args.set(4, Constants.EUCLIDIEAN);
			args.set(5,  Constants.SPLIT);
			oc=runSingleAlgorithm(analyzer,args);
			oc.setClusters(oc.getConsolidatedClusters());
			allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

//			
//			if(args.get(3).equals("true")) {
//				oc.extendClusters(oc.mergeRemainingClusters(allAlgoClusterList));
//				oc.setClusters(oc.getConsolidatedClusters());
//			}
			
		

		return oc;
	}

	
	public static void main(String[] args) throws IOException, Exception {
		
		String appPath = args[0]; // "/Users/shreya/git/digdeep";
		String outputDir= args[1];
//			
//		runNaive(appPath,outputDir);	
//		System.exit(0);
		
		
		List<String> argsList = new ArrayList<String>(Arrays.asList(args));;
		List<String> argsList2 = new ArrayList<String>(Arrays.asList(args));;
		
		

		
		try {
			JarApiList.createJARFile(args[0]);
			ReadJarMap.createJARCategoryMap();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error while creating jar map"+e.toString());
		}
		
		
		if(new File(outputDir).mkdir()){
			System.out.println("Result directory created at "+outputDir);
		}
		else {
			System.out.println("Output directory exists");

		}
		AnalyzeApp analyzer ;
		
		
		Clustering oc = null;
		
		if(args[2].equals(Constants.ALL)){
			argsList.add("true"); //TODO: remove this
			
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(3)));
			analyzer = new AnalyzeApp(appPath);
			
			oc=runAllAlgorithms(analyzer, argsList);
			oc.CLeanClusters();
		
			argsList2.add("false"); //TODO: remove this
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList2.get(3)));	
//			System.out.println("her"+DocumentParserUtil.getIgnoreNone());
			analyzer = new AnalyzeApp(appPath);
			oc=runAllAlgorithms(analyzer, argsList2);
			oc.setCusterListNames();

			ExpandClusters ec= new ExpandClusters(oc.getClusters(),analyzer.getAppath());
			ec.getUsage();
			oc.setClusters(ec.getListofclusters());
			oc.CLeanClusters();
		
//			oc.scorePartialClusters(oc.getClusters());	
			
			String d3filename = argsList2.get(1)+"/clusterall.html";//src/main/output/clusterall.html"; // TODO : Make argument 
			
			String htmlpath=argsList2.get(1);
			File dir = new File(htmlpath);
			String[] extensions = new String[] { "html"};
			List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
//			List<String> htmlpaths= new ArrayList<>();
			StringBuilder strBuilder =
					new StringBuilder();
			int count=0;
			for (File f:files) {
				if(f.getName().contains("all"))
					continue;
				String temp="<li><a href=\"filepath\" target=\"_blank\">filename</a></li> \n";
				temp=temp.replace("filepath", f.getAbsolutePath());
				String fname=f.getName();
				fname=fname.replace(".html", "");
				count=count+1;
				fname=fname.replace("cluster", "Approach"+count+": ");
				temp=temp.replace("filename",fname);
				strBuilder.append(temp);
//				htmlpaths.add(temp);
			}
			
			oc.savecLusterJSONALL(d3filename,strBuilder.toString());
			
		}
		else {
			DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(3)));
			analyzer = new AnalyzeApp(appPath);
			runSingleAlgorithm(analyzer,argsList); // TODO: remove setIgnoreNone from user input always generate both
		}

//		analyzer.computeMeasure();
//		analyzer.saveMeasure(null);

		
	}
}
