package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class DbDependence {
	
	@ApiModelProperty(notes = "db", required = false)
	String db;
	
	@ApiModelProperty(notes = "tables", required = false)
	String[] tables;

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String[] getTables() {
		return tables;
	}

	public void setTables(String[] tables) {
		this.tables = tables;
	}
	
}
