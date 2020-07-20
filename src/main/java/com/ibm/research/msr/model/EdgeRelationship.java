package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class EdgeRelationship {
	
	@ApiModelProperty(notes = "label", required = false)
	String label;
	
	@ApiModelProperty(notes = "id", required = true)
	String id;
	
	@ApiModelProperty(notes = "description", required = false)
	String description;
	
	@ApiModelProperty(notes = "properties", required = false)
	EdgeRelationshipProperties properties;
	
	@ApiModelProperty(notes = "frequency", required = false)
	String frequency;

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

	public EdgeRelationshipProperties getProperties() {
		return properties;
	}

	public void setProperties(EdgeRelationshipProperties properties) {
		this.properties = properties;
	}
	
	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	
}
