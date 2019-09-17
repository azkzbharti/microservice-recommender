package com.ibm.research.msr.jarlist;

public class APIUsageStats {

	String jarName = null;

	int totalAPIs = 0;

	int usedAPIs = 0;

	double percentUsedAPIs = 0.0;

	public APIUsageStats(String jarName, int totalAPIsInJar, int distinctUsedAPIsFromJar) {
		super();
		this.jarName = jarName;
		this.totalAPIs = totalAPIsInJar;
		this.usedAPIs = distinctUsedAPIsFromJar;
		percentUsedAPIs = (distinctUsedAPIsFromJar * 100.0) / totalAPIsInJar;
	}
}
