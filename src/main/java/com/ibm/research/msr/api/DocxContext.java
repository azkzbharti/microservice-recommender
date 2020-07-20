/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/

package com.ibm.research.msr.api;

public class DocxContext {

    private String appName;
    private String funcMSDetails;
    private String unreachableClustersDetails;
    private String utilityClustersDetails;
    private String refactorClustersDetails;
    private String unassignedClustersDetails;
    private String tables;
    
    

	public String getFuncMSDetails() {
		return funcMSDetails;
	}

	public void setFuncMSDetails(String funcMSDetails) {
		this.funcMSDetails = funcMSDetails;
	}

	public String getUnreachableClustersDetails() {
		return unreachableClustersDetails;
	}

	public void setUnreachableClustersDetails(String unreachableClustersDetails) {
		this.unreachableClustersDetails = unreachableClustersDetails;
	}

	public String getUtilityClustersDetails() {
		return utilityClustersDetails;
	}

	public void setUtilityClustersDetails(String utilityClustersDetails) {
		this.utilityClustersDetails = utilityClustersDetails;
	}

	public String getRefactorClustersDetails() {
		return refactorClustersDetails;
	}

	public void setRefactorClustersDetails(String refactorClustersDetails) {
		this.refactorClustersDetails = refactorClustersDetails;
	}

	public String getUnassignedClustersDetails() {
		return unassignedClustersDetails;
	}

	public void setUnassignedClustersDetails(String unassignedClustersDetails) {
		this.unassignedClustersDetails = unassignedClustersDetails;
	}

	public String getTables() {
		return tables;
	}

	public void setTables(String tables) {
		this.tables = tables;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}


}
