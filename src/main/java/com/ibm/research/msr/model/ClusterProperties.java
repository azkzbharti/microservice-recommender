package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class ClusterProperties {
	
    @ApiModelProperty(notes = "affected_business_domains", required = false)
	String[] affected_business_domains;
	
	@ApiModelProperty(notes = "db_dependence", required = false)
	DbDependence db_dependence;
	
	public String[] getAffected_business_domains() {
		return affected_business_domains;
	}

	public void setAffected_business_domains(String[] affected_business_domains) {
		this.affected_business_domains = affected_business_domains;
	}

	public DbDependence getDb_dependence() {
		return db_dependence;
	}

	public void setDb_dependence(DbDependence db_dependence) {
		this.db_dependence = db_dependence;
	}
	
}
