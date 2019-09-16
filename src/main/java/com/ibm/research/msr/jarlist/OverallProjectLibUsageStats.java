package com.ibm.research.msr.jarlist;

import java.util.List;

public class OverallProjectLibUsageStats {

	int overallLibraryMethodInvocations=0;
	
	int overallNonLibraryMethodInvocations=0;

	int totalMethodInvocations=0;
	
	double percentageOfLibraryCalls=0.0;
	
	List<APIUsageStats> apiUsageStats=null;

	public OverallProjectLibUsageStats(int overallLibraryMethodInvocations, int overallNonLibraryMethodInvocations,
			List<APIUsageStats> apiUsageStatsList) {
		super();
		this.overallLibraryMethodInvocations = overallLibraryMethodInvocations;
		this.overallNonLibraryMethodInvocations = overallNonLibraryMethodInvocations;
		totalMethodInvocations=overallLibraryMethodInvocations+overallNonLibraryMethodInvocations;
		this.apiUsageStats = apiUsageStatsList;
		
		percentageOfLibraryCalls=(overallLibraryMethodInvocations*100.0)/overallNonLibraryMethodInvocations;
	}
	
	
	
}
