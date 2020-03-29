package com.ibm.research.msr.ddd.model;

import com.ibm.research.msr.ddd.model.*;

public class Edge {
	private String nodeone;
	private String nodetwo;
	private String accesstype;
	
	public String getNodeOne() {
		return nodeone;
	}
	
	public String getNodeTeo() {
		return nodetwo;
	}
	
	public String getAccessType() {
		return accesstype;
	}
	
	public void setNodeOne(String n1) {
		nodeone=n1;
	}
	
	public void setNodeTwo(String n2) {
		nodeone=n2;
	}
	
	public void setAccessType(String type) {
		accesstype = type;
	}

}
