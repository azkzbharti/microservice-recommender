package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Microservice {
	
	@ApiModelProperty(notes = "projectId", required = true)
	String projectId;
	
	@ApiModelProperty(notes = "projectName", required = false)
	String projectName;
	
	@ApiModelProperty(notes = "nodes", required = true)
	Nodes nodes[];
	
	@ApiModelProperty(notes = "edges", required = true)
	Edges[] edges;
	
	@ApiModelProperty(notes = "clusters", required = true)
	Clusters[] clusters;

	public Clusters[] getClusters() {
		return clusters;
	}

	public void setClusters(Clusters[] clusters) {
		this.clusters = clusters;
	}

	public Nodes[] getNodes() {
		return nodes;
	}

	public void setNodes(Nodes[] nodes) {
		this.nodes = nodes;
	}

	public Edges[] getEdges() {
		return edges;
	}

	public void setEdges(Edges[] edges) {
		this.edges = edges;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}