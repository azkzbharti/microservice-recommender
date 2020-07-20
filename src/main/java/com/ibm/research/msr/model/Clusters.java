package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Clusters {
	
	@ApiModelProperty(notes = "type", required = true)
	String type;
	
	@ApiModelProperty(notes = "label", required = true)
	String label;
	
	@ApiModelProperty(notes = "id", required = true)
	String id;
	
	@ApiModelProperty(notes = "description", required = false)
	String description;
	
	@ApiModelProperty(notes = "nodes", required = false)
	String[] nodes;
	
	@ApiModelProperty(notes = "transactions", required = false)
	String transactions[];
	
	@ApiModelProperty(notes = "properties", required = false)
	ClusterProperties properties;

	@ApiModelProperty(notes = "metrics", required = false)
	Metrics metrics;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String[] getNodes() {
		return nodes;
	}

	public void setNodes(String[] nodes) {
		this.nodes = nodes;
	}

	public Metrics getMetrics() {
		return metrics;
	}

	public void setMetrics(Metrics metrics) {
		this.metrics = metrics;
	}

	public ClusterProperties getProperties() {
		return properties;
	}

	public void setProperties(ClusterProperties properties) {
		this.properties = properties;
	}

	public String[] getTransactions() {
		return transactions;
	}

	public void setTransactions(String[] transactions) {
		this.transactions = transactions;
	}

	
}