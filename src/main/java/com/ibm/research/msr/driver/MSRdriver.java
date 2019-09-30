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
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.ibm.research.msr.utils.ReadJarMap;

/**
 * 
 *
 */
public class MSRdriver {

		
	public static Clustering runNaiveUtility(AnalyzeApp analyzer, List<String> args) throws IOException {
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

	public static void runNaive(String appPath, String appType, String outputPath) throws IOException, Exception {

		ReadJarMap.createJARCategoryMap(outputPath + File.separator + "temp" + File.separator + "jar-to-packages.csv");

		AnalyzeApp analyzer;
		Clustering oc = null;
		List<String> argsList = new ArrayList<String>();
		argsList.add(appPath); // workaround:: argList created just to keep code changes minimal
		argsList.add(outputPath);
		argsList.add(Constants.ALL); // TODO: will be removed minimial code changes
		List<String> argsList2 = new ArrayList<String>(argsList);
		argsList.add("true"); // "will ignore none category
//		DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList.get(3)));
//		analyzer = new AnalyzeApp(appPath, appType,outputPath);
//		oc = runNaiveUtility(analyzer, argsList);

		argsList2.add("false"); // TODO: remove this
		DocumentParserUtil.setIgnoreNone(Boolean.parseBoolean(argsList2.get(3)));

		analyzer = new AnalyzeApp(appPath, appType, outputPath);

		oc = runNaiveUtility(analyzer, argsList2);
		oc.setCusterListNames();

		System.out.println("Calling Inter class usage");

		ExpandClusters ec = new ExpandClusters(oc.getClusters(), analyzer.getAppath(), false);
		ec.getUsage();
		oc.setClusters(ec.getListofclusters());


        ec= new ExpandClusters(oc.getClusters(),analyzer.getAppath(),true); // runs for single size clusters only -- reassigns them 
        ec.getUsage();
	  
		oc.setClusters(ec.getListofclusters());
		oc.CLeanClusters();

		String d3ClusterPackJSON = outputPath + File.separator + "ui" + File.separator + "data" + File.separator
				+ "clusterall.json";

		oc.saveClusterAsCirclePackJSON(d3ClusterPackJSON);

	}


}
