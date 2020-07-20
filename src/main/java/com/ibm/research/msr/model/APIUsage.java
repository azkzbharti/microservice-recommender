package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class APIUsage {

	@ApiModelProperty(notes = "API name", required = true)
	String apiName;
	@ApiModelProperty(notes = "API description", required = true)
	String apiDesc;
	@ApiModelProperty(notes = "Cluster Type", required = false)
	String clusterType;
	
	
	public String getClusterType() {
		return clusterType;
	}
	public void setClusterType(String clusterType) {
		this.clusterType = clusterType;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	public String getApiDesc() {
		return apiDesc;
	}
	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}
	
	
	
}
