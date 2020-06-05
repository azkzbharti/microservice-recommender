package com.ibm.research.msr.driver;

import com.ibm.research.msr.clustering.CohesionCouplingProcessing;

public class CohesionTest {

	public static void main(String[] args) {
		String clusterAllJSON = "/Users/utkarsh/Projects/msrdemo/pythonexpts/pbw.json";
		String usageJSON = "/Users/utkarsh/Projects/msrdemo/pythonexpts/newpbwjson.usage";
		String outputFile = "/Users/utkarsh/Projects/msrdemo/pythonexpts/tmpout";

		CohesionCouplingProcessing postProcessor = new CohesionCouplingProcessing(clusterAllJSON, usageJSON, outputFile);
		postProcessor.runClustering();
	}

}
