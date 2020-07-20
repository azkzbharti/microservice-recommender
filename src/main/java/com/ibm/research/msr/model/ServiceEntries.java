package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class ServiceEntries {


    @ApiModelProperty(notes = "id", required = false)
	String id;
	
	@ApiModelProperty(notes = "method_invocation", required = false)
	String method_invocation;
	
	@ApiModelProperty(notes = "entry_type", required = false)
	String entry_type;
	
	@ApiModelProperty(notes = "label", required = false)
	String label;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMethod_invocation() {
		return method_invocation;
	}

	public void setMethod_invocation(String method_invocation) {
		this.method_invocation = method_invocation;
	}

	public String getEntry_type() {
		return entry_type;
	}

	public void setEntry_type(String entry_type) {
		this.entry_type = entry_type;
	}

}
