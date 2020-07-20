package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class NodeProperties {
    		
	@ApiModelProperty(notes = "technical_type", required = false)
	String technical_type;
	
	@ApiModelProperty(notes = "business_type", required = false)
	String business_type;
	
	@ApiModelProperty(notes = "used_by", required = false)
	String used_by;
	
	@ApiModelProperty(notes = "used_to", required = false)
	String used_to;
	
	@ApiModelProperty(notes = "num_ext_dependency", required = false)
	String num_ext_dependency;
	
	@ApiModelProperty(notes = "crud_operations", required = false)
	String crud_operations;
	
	@ApiModelProperty(notes = "service_entries", required = false)
	ServiceEntries[] service_entries;
	
	
	public String getTechnical_type() {
		return technical_type;
	}

	public void setTechnical_type(String technical_type) {
		this.technical_type = technical_type;
	}

	public String getBusiness_type() {
		return business_type;
	}

	public void setBusiness_type(String business_type) {
		this.business_type = business_type;
	}

	public String getUsed_by() {
		return used_by;
	}

	public void setUsed_by(String used_by) {
		this.used_by = used_by;
	}

	public String getUsed_to() {
		return used_to;
	}

	public void setUsed_to(String used_to) {
		this.used_to = used_to;
	}

	public ServiceEntries[] getService_entries() {
		return service_entries;
	}

	public void setService_entries(ServiceEntries[] service_entries) {
		this.service_entries = service_entries;
	}

	public String getNum_ext_dependency() {
		return num_ext_dependency;
	}

	public void setNum_ext_dependency(String num_ext_dependency) {
		this.num_ext_dependency = num_ext_dependency;
	}

	public String getCrud_operations() {
		return crud_operations;
	}

	public void setCrud_operations(String crud_operations) {
		this.crud_operations = crud_operations;
	}
}
