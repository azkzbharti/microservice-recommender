package com.ibm.research.msr.jarlist;

public class APIUsageStats {
	
	String jarName=null;
	
	int totalAPIsInJar=0;
	
	int distinctUsedAPIsFromJar=0;
	
	double percentageOfUsedAPIsFromJar=0.0;

	public APIUsageStats(String jarName, int totalAPIsInJar, int distinctUsedAPIsFromJar) {
		super();
		this.jarName = jarName;
		this.totalAPIsInJar = totalAPIsInJar;
		this.distinctUsedAPIsFromJar = distinctUsedAPIsFromJar;
		percentageOfUsedAPIsFromJar=(distinctUsedAPIsFromJar*100.0)/totalAPIsInJar;
	}
}
