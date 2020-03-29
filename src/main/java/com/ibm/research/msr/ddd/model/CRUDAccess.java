package com.ibm.research.msr.ddd.model;

public class CRUDAccess {
	
	private String serviceName;
	private String tableName;
	private String accessTypes;
	private String className;
	
	public String getServiceName() {
		return this.serviceName;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getaccessTypes() {
		return this.accessTypes;
	}
	
	public void setServiceName(String name) {
		this.serviceName = name;
	}
	
	public void setClassName(String name) {
		this.className = name;
	}
	
	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public void setAccessTypes(String types) {
		this.accessTypes = types;
	}

}
