package com.ibm.research.msr.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ibm.research.msr.clustering.ClusterDetails;
import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.clustering.DBSCAN;
import com.ibm.research.msr.clustering.KMeans;
import com.ibm.research.msr.clustering.Naive;
import com.ibm.research.msr.clustering.NaiveTFIDF;
import com.ibm.research.msr.expandcluster.ExpandClusters;
import com.ibm.research.msr.expandcluster.InterClassUsageFinder;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.ibm.research.msr.utils.ReadJarMap;

/**
 * 
 *
 */
public class MSRdriver {

		
	public static Clustering runNaiveUtility(AnalyzeApp analyzer) throws IOException {
		Clustering oc = null;

		List<List<ClusterDetails>> allAlgoClusterList = new ArrayList<>();
		oc = new Naive(analyzer.getListOfDocuments(), Constants.SPLIT);
		oc.runClustering();
		oc.getClusters();
		oc.removeDuplicate();		
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());	
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

		return oc;

	}
	public static void metaExtractor(String appPath, String appType,String outputPath) throws IOException, Exception {
		ReadJarMap mapReader= new ReadJarMap();
		mapReader.createJARCategoryMap(outputPath + File.separator + "temp" + File.separator + "jar-to-packages.csv");
		String measureFile= outputPath + File.separator + "temp" + File.separator + "measure.csv";
		String classFiles=outputPath + File.separator + "temp" + File.separator + "ClassList.json";
		
		AnalyzeApp analyzer;
		analyzer = new AnalyzeApp(appPath, appType, outputPath,mapReader.getLibCatMap());
		analyzer.savetoFile(measureFile, classFiles);
		
		
	}
	
	public static void appClustering(String measurePath, String appType,String outputPath) throws IOException {
			// read app from files and create the analyzer object
		    ReadJarMap mapReader= new ReadJarMap();
			mapReader.createJARCategoryMap(outputPath + File.separator + "temp" + File.separator + "jar-to-packages.csv");
		    AnalyzeApp analyzer = null;
		    Clustering oc = null;
			try {
				analyzer = new AnalyzeApp(measurePath,appType,outputPath,mapReader.getLibCatMap());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			oc = runNaiveUtility(analyzer);
			oc.setCusterListNames();
		    // call load interclass loader
			// do cluster expansion
			
			

		
		
	}
	
	public static void runNaive(String appPath, String appType, String outputPath) throws IOException, Exception {

		ReadJarMap mapReader= new ReadJarMap();
		mapReader.createJARCategoryMap(outputPath + File.separator + "temp" + File.separator + "jar-to-packages.csv");
	   
		AnalyzeApp analyzer;
		Clustering oc = null;
		analyzer = new AnalyzeApp(appPath,appType,outputPath,mapReader.getLibCatMap());

		String measureFile= outputPath + File.separator + "temp" + File.separator + "measure.csv";
		String classFiles=outputPath + File.separator + "temp" + File.separator + "ClassList.json";
		
		analyzer.savetoFile(measureFile, classFiles);
		oc = runNaiveUtility(analyzer);
		oc.setCusterListNames();

		String opJsonFileName=outputPath + File.separator + "temp"+File.separator+"inter-class-usage.json";
		InterClassUsageFinder classUsage = new InterClassUsageFinder();
		classUsage.find(appPath,opJsonFileName);
		
		ExpandClusters ec = new ExpandClusters(oc.getClusters(), analyzer.getAppath(), false,classUsage);
		ec.getUsage();
		oc.setClusters(ec.getListofclusters());

        ec= new ExpandClusters(oc.getClusters(),analyzer.getAppath(),true,classUsage); // runs for single size clusters only -- reassigns them 
        ec.getUsage();
	  
		oc.setClusters(ec.getListofclusters());
		oc.CLeanClusters();

		String d3ClusterPackJSON = outputPath + File.separator + "ui" + File.separator + "data" + File.separator
				+ "clusterall.json";

		oc.saveClusterAsCirclePackJSON(d3ClusterPackJSON);

	}


}
