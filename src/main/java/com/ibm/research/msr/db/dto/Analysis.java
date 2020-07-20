package com.ibm.research.msr.db.dto;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Analysis {
	private ObjectId _id;
	private ObjectId projectId;
	private String analysisType;
	private String analysisResult;

	private String analysisPath;
	
	public Analysis(ObjectId projectId, String analysisType, String analysisResult,String analysisPath) {
		this._id = new ObjectId();
		this.projectId = projectId;
		this.analysisType = analysisType;
		this.analysisResult = analysisResult;
		this.analysisPath=analysisPath;
	}
	
	public Analysis(Document d) {
		if(d.containsKey("_id")) {
			this._id = d.getObjectId("_id");
		}
		else {
			this._id = new ObjectId();
		}
		this.projectId = d.getObjectId("project_id");
		this.analysisType = d.getString("analysis_type");
		this.analysisResult =  d.getString("analysis_result");
		this.analysisPath = d.getString("analysis_path");
	}
	
	public Document toDocument() {
		Document d = new Document()
				.append("_id", this._id)
				.append("project_id", this.projectId)
				.append("analysis_type", this.analysisType)
				.append("analysis_result", this.analysisResult)
				.append("analysis_path", this.analysisPath);
		
		return d;
	}

	/**
	 * @return the analysisPath
	 */
	public String getAnalysisPath() {
		return analysisPath;
	}

	/**
	 * @param analysisPath the analysisPath to set
	 */
	public void setAnalysisPath(String analysisPath) {
		this.analysisPath = analysisPath;
	}
	
	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public ObjectId get_id() {
		return _id;
	}

	public ObjectId getProjectId() {
		return projectId;
	}

	public void setProjectId(ObjectId projectId) {
		this.projectId = projectId;
	}

	public String getAnalysisType() {
		return analysisType;
	}

	public void setAnalysisType(String analysisType) {
		this.analysisType = analysisType;
	}
	
	public String getAnalysisResult() {
		return analysisResult;
	}

	public void setAnalysisResult(String analysisResult) {
		this.analysisResult = analysisResult;
	}

	
}
