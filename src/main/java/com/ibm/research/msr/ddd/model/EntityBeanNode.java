package com.ibm.research.msr.ddd.model;

public class EntityBeanNode {
	private String beanname;
	private String fullname;
	
	public String getBeanName() {
		return this.beanname;
	}
	
	public String getFullName() {
		return this.fullname;
	}
	
	public void setBeanName(String name) {
		this.beanname=name;
	}
	
	public void setFullName(String fullname) {
		this.fullname= fullname;
	}

}
