package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class DataMapClusterjson {

	@ApiModelProperty(notes = "Cluster name", required = true)
	String clusterName;
	@ApiModelProperty(notes = "Cluster description", required = true)
	String clusterDesc;
	@ApiModelProperty(notes = "Cluster Type", required = false)
	String clusterType;
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	public String getClusterDesc() {
		return clusterDesc;
	}
	public void setClusterDesc(String clusterDesc) {
		this.clusterDesc = clusterDesc;
	}
	public String getClusterType() {
		return clusterType;
	}
	public void setClusterType(String clusterType) {
		this.clusterType = clusterType;
	}
	
	
	
	
}
