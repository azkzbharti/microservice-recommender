package com.ibm.research.msr.jarlist;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.ibm.research.appmod.pa.jarlist.APIUsageStats;

public class OverallProjectLibUsageStats {

	// TODO: ideally these libMethods etc should be named
	// or serialized using the @SerializedName as "Calls",
	// as we have stats on methods i.e., MethodDeclarations also below
	int libMethods = 0;

	int nonLibMethods = 0;

	int totalMethods = 0;

	double percentLibMethods = 0.0;

	@SerializedName("Declared Methods")
	int methods=0;
	
	@SerializedName("Declared Methods With Lib Call")
	int methodsWithLibCall=0;
	
	@SerializedName("Percent Declared Methods With Lib Call")
	double percentMethodsWithLibCall=0.0;

	List<APIUsageStats> apiUsageStats = null;

	
	public OverallProjectLibUsageStats(int overallLibraryMethodInvocations, int overallNonLibraryMethodInvocations,
			List<APIUsageStats> apiUsageStatsList,
			int methods, int methodsWithLibCall) {
		super();
		this.libMethods = overallLibraryMethodInvocations;
		this.nonLibMethods = overallNonLibraryMethodInvocations;
		totalMethods = overallLibraryMethodInvocations + overallNonLibraryMethodInvocations;
		this.apiUsageStats = apiUsageStatsList;

		percentLibMethods = (overallLibraryMethodInvocations * 100.0) / overallNonLibraryMethodInvocations;
	
		this.methods=methods;
		this.methodsWithLibCall=methodsWithLibCall;

		percentMethodsWithLibCall=(methodsWithLibCall*100.0)/methods;

	}

}
