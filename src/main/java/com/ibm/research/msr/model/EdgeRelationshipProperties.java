package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class EdgeRelationshipProperties {
	
	@ApiModelProperty(notes = "start", required = false)
	String start;
	
	@ApiModelProperty(notes = "end", required = false)
	String end;
	
	@ApiModelProperty(notes = "frequency", required = false)
	String frequency;
	
	@ApiModelProperty(notes = "methods", required = false)
	String[] methods;

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String[] getMethods() {
		return methods;
	}

	public void setMethods(String[] methods) {
		this.methods = methods;
	}

}
