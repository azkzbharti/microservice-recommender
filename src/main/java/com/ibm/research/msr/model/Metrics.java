package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Metrics {
	
    @ApiModelProperty(notes = "cohesion_score", required = false)
	String cohesion_score;
	
	@ApiModelProperty(notes = "conceptual_independence", required = false)
	String conceptual_independence;
	
	@ApiModelProperty(notes = "coupling_score", required = false)
	String coupling_score;
	
	@ApiModelProperty(notes = "independence_score", required = false)
	String independence_score;	
	
	@ApiModelProperty(notes = "data_independence_score", required = false)
	String data_independence_score;
	
	@ApiModelProperty(notes = "volume_inter_partition_calls", required = false)
	String volume_inter_partition_calls;
	
	@ApiModelProperty(notes = "transacton_independence_score", required = false)
	String transacton_independence_score;
	
	@ApiModelProperty(notes = "functional_encapsulation", required = false)
	String functional_encapsulation;
	
	@ApiModelProperty(notes = "modularity", required = false)
	String modularity;
	
	@ApiModelProperty(notes = "structural_cohesivity", required = false)
	String structural_cohesivity;

	public String getCohesion_score() {
		return cohesion_score;
	}

	public void setCohesion_score(String cohesion_score) {
		this.cohesion_score = cohesion_score;
	}

	public String getConceptual_independence() {
		return conceptual_independence;
	}

	public void setConceptual_independence(String conceptual_independence) {
		this.conceptual_independence = conceptual_independence;
	}

	public String getCoupling_score() {
		return coupling_score;
	}

	public void setCoupling_score(String coupling_score) {
		this.coupling_score = coupling_score;
	}

	public String getIndependence_score() {
		return independence_score;
	}

	public void setIndependence_score(String independence_score) {
		this.independence_score = independence_score;
	}

	public String getData_independence_score() {
		return data_independence_score;
	}

	public void setData_independence_score(String data_independence_score) {
		this.data_independence_score = data_independence_score;
	}

	public String getVolume_inter_partition_calls() {
		return volume_inter_partition_calls;
	}

	public void setVolume_inter_partition_calls(String volume_inter_partition_calls) {
		this.volume_inter_partition_calls = volume_inter_partition_calls;
	}

	public String getTransacton_independence_score() {
		return transacton_independence_score;
	}

	public void setTransacton_independence_score(String transacton_independence_score) {
		this.transacton_independence_score = transacton_independence_score;
	}

	public String getFunctional_encapsulation() {
		return functional_encapsulation;
	}

	public void setFunctional_encapsulation(String functional_encapsulation) {
		this.functional_encapsulation = functional_encapsulation;
	}

	public String getModularity() {
		return modularity;
	}

	public void setModularity(String modularity) {
		this.modularity = modularity;
	}

	public String getStructural_cohesivity() {
		return structural_cohesivity;
	}

	public void setStructural_cohesivity(String structural_cohesivity) {
		this.structural_cohesivity = structural_cohesivity;
	}

	
}
