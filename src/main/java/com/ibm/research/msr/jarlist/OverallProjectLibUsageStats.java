package com.ibm.research.msr.jarlist;

import java.util.List;

public class OverallProjectLibUsageStats {

	int libMethods = 0;

	int nonLibMethods = 0;

	int totalMethods = 0;

	double percentLibMethods = 0.0;

	List<APIUsageStats> apiUsageStats = null;

	public OverallProjectLibUsageStats(int overallLibraryMethodInvocations, int overallNonLibraryMethodInvocations,
			List<APIUsageStats> apiUsageStatsList) {
		super();
		this.libMethods = overallLibraryMethodInvocations;
		this.nonLibMethods = overallNonLibraryMethodInvocations;
		totalMethods = overallLibraryMethodInvocations + overallNonLibraryMethodInvocations;
		this.apiUsageStats = apiUsageStatsList;

		percentLibMethods = (overallLibraryMethodInvocations * 100.0) / overallNonLibraryMethodInvocations;
	}

}
