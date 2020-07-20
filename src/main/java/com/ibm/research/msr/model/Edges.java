package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Edges {
	
	@ApiModelProperty(notes = "type", required = true)
	String type;
	
	@ApiModelProperty(notes = "weight", required = false)
	String weight;
	
	@ApiModelProperty(notes = "description", required = false)
	String description;
	
	
	@ApiModelProperty(notes = "relationship", required = false)
	EdgeRelationship[] relationship;


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getWeight() {
		return weight;
	}


	public void setWeight(String weight) {
		this.weight = weight;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public EdgeRelationship[] getRelationship() {
		return relationship;
	}


	public void setRelationship(EdgeRelationship[] relationship) {
		this.relationship = relationship;
	}
}
