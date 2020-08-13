/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/

package com.ibm.research.msr.model;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.wordnik.swagger.annotations.ApiModelProperty;

public class Notes {
	
	@ApiModelProperty(notes = "userId", required = true)
	String userId;
	
	@ApiModelProperty(notes = "projectId", required = true)
	String projectId;
	
	@ApiModelProperty(notes = "clusterId", required = true)
	String clusterId;
	
	@ApiModelProperty(notes = "notes", required = false)
	String notes;
	
	@ApiModelProperty(notes = "timeStampInMS", required = false)
	String timeStampInMS;
	
	@ApiModelProperty(notes = "notesId", required = false)
	String notesId;
	
	public Notes() {
		
	}
	

	
	public Notes(Document d) {
		if(d.containsKey("_id")) {
			this.notesId = d.getObjectId("_id").toString();
		}
		else {
			this.notesId = new ObjectId().toString();
		}
		this.projectId = d.getString("project_id");
		this.clusterId = d.getString("cluster_id");
		this.notes = d.getString("notes");
		this.timeStampInMS = d.getLong("timeStampInMS").toString();
		this.userId = d.getString("user_id");
	}
	
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getTimeStampInMS() {
		return timeStampInMS;
	}

	public void setTimeStampInMS(String timeStampInMS) {
		this.timeStampInMS = timeStampInMS;
	}



	public String getNotesId() {
		return notesId;
	}



	public void setNotesId(String notesId) {
		this.notesId = notesId;
	}
	
}
