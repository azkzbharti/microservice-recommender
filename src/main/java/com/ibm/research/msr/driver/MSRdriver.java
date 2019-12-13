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
//		oc = new NaiveTFIDF(analyzer.getListOfDocuments(), Constants.EUCLIDIEAN,Constants.SPLIT);
		oc.runClustering();
		System.out.println(oc.getUniquedocs(oc.getClusters()));
		oc.removeDuplicate();		
		System.out.println("New clusters size:");
		System.out.println(oc.getClusters().size());	
		allAlgoClusterList.add(oc.getNonScoreClusters().stream().collect(Collectors.toList()));

		return oc;

	}
	public static void metaExtractor(String appPath, String appType,String outputPath, String jarPackagesCsv) throws IOException, Exception {
		ReadJarMap mapReader= new ReadJarMap();	
		mapReader.createJARCategoryMap(jarPackagesCsv);
		String measureFile= outputPath + File.separator + "temp" + File.separator + "measure.csv";
		String classFiles=outputPath + File.separator + "temp" + File.separator + "ClassList.json";
		
		AnalyzeApp analyzer;
		analyzer = new AnalyzeApp(appPath, appType, outputPath,mapReader.getLibCatMap());
		analyzer.savetoFile(measureFile, classFiles);
		
		
	}
	
	public static void appClustering(String measurePath, String appType,String outputPath, String jarPackagesCsv) throws IOException {
			// read app from files and create the analyzer object
		    ReadJarMap mapReader= new ReadJarMap();
			mapReader.createJARCategoryMap(jarPackagesCsv);
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
	
	public static void runNaive(String appPath, String appType, String outputPath, String jarPackagesCsv) throws IOException, Exception {

		ReadJarMap mapReader= new ReadJarMap();
		mapReader.createJARCategoryMap(jarPackagesCsv);
	   
		AnalyzeApp analyzer;

		Clustering oc = null;
		analyzer = new AnalyzeApp(appPath,appType,outputPath,mapReader.getLibCatMap());

		String measureFile= outputPath + File.separator + "temp" + File.separator + "measure.csv";
		String classFiles=outputPath + File.separator + "temp" + File.separator + "ClassList.json";
		
		analyzer.savetoFile(measureFile, classFiles);
//		AnalyzeApp analyzer2 = new AnalyzeApp(classFiles, measureFile);

//		oc = runNaiveUtility(analyzer2);
		oc = runNaiveUtility(analyzer);
		oc.setCusterListNames();

		String opJsonFileName=outputPath + File.separator + "temp"+File.separator+"inter-class-usage.json";

		InterClassUsageFinder classUsage = new InterClassUsageFinder();
		if	(appType.equals(Constants.SRC)) {
			classUsage.find(appPath,opJsonFileName,null);
		}
		else {
			classUsage.findFromBinaryClassFiles(appPath,opJsonFileName,null);
			}
		
		System.out.println(oc.getUniquedocs(oc.getClusters()));
		ExpandClusters ec = new ExpandClusters(oc.getClusters(), analyzer.getAppath(), false,classUsage);
		ec.getUsage();
		oc.setClusters(ec.getListofclusters());
		System.out.println(oc.getUniquedocs(ec.getListofclusters()));
		
        ec= new ExpandClusters(oc.getClusters(),analyzer.getAppath(),true,classUsage); // runs for single size clusters only -- reassigns them 
        ec.getUsage();
		System.out.println(oc.getUniquedocs(ec.getListofclusters()));

	  
		oc.setClusters(ec.getListofclusters());
		oc.CLeanClusters();

		String d3ClusterPackJSON = outputPath + File.separator + "ui" + File.separator + "data" + File.separator
				+ "clusterall.json";

		oc.saveClusterAsCirclePackJSON(d3ClusterPackJSON);

	}


}
