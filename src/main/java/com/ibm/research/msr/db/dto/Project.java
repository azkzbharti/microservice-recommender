package com.ibm.research.msr.db.dto;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.ibm.research.msr.utils.Constants;

public abstract class Project {
	private ObjectId _id;
	private String projectName;
	private String projectDesc;
	private String projectLocation;
	private String type;
	private String codeType;
	private String rootAnalyzePath;
	private String status;
	private String sourceLanguage;

	public Project(String name, String desc, String projLocation, String srcLang, String type,String codeType, String rootAnalyzePath) {
		this._id = new ObjectId();
		this.projectName = name;
		this.projectDesc = desc;
		this.projectLocation = projLocation + this._id;
		this.sourceLanguage = srcLang;
		this.type = type;
		this.codeType = codeType;
		this.rootAnalyzePath = rootAnalyzePath;
		this.status = Constants.CMA_ANALYZING_STATUS_MSG;
	}

	public Project(Document d) {
		if(d.containsKey("_id")) {
			this._id = d.getObjectId("_id");
		}
		else {
			this._id = new ObjectId();
		}
		this.projectName = d.getString("project_name");
		this.projectDesc = d.getString("project_desc");
		this.projectLocation = d.getString("project_zip_location");
		this.sourceLanguage = d.getString("src_language");
		this.type = d.getString("type");
		this.codeType=d.getString("code_type");
		this.rootAnalyzePath = d.getString("root_analyze_path");
		if(d.containsKey("status")) {
			this.status = d.getString("status");
		}
		
	}

	/**
	 * @return the codeType
	 */
	public String getCodeType() {
		return codeType;
	}

	/**
	 * @param codeType the codeType to set
	 */
	public void setCodeType(String codeType) {
		this.codeType = codeType;
	}

	public ObjectId get_id() {
		return _id;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectDesc() {
		return projectDesc;
	}

	public void setProjectDesc(String projectDesc) {
		this.projectDesc = projectDesc;
	}

	public String getProjectLocation() {
		return projectLocation;
	}

	public void setProjectZipLocation(String projectLocation) {
		this.projectLocation = projectLocation;
	}

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public void setSourceLanguage(String sourceLanguage) {
		this.sourceLanguage = sourceLanguage;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRootAnalyzePath() {
		return rootAnalyzePath;
	}

	public void setRootAnalyzePath(String rootAnalyzePath) {
		this.rootAnalyzePath = rootAnalyzePath;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
