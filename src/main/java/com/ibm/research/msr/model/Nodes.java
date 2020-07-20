package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Nodes {
	
	@ApiModelProperty(notes = "entity_type", required = true)
	String entity_type;
	
	@ApiModelProperty(notes = "label", required = true)
	String label;
	
	@ApiModelProperty(notes = "id", required = true)
	String id;
	
	
	@ApiModelProperty(notes = "description", required = false)
	String description;
	
	@ApiModelProperty(notes = "properties", required = false)
	NodeProperties properties;

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

	public NodeProperties getProperties() {
		return properties;
	}

	public void setProperties(NodeProperties properties) {
		this.properties = properties;
	}

	public String getEntity_type() {
		return entity_type;
	}

	public void setEntity_type(String entity_type) {
		this.entity_type = entity_type;
	}
}
