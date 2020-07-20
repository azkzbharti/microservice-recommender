/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/

package com.ibm.research.msr.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Status {
	
	@ApiModelProperty(notes = "status", required = true)
	String status;
	
	@ApiModelProperty(notes = "message", required = false)
	String message;
	
	@ApiModelProperty(notes = "projectId", required = true)
	String projectId;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	

}
