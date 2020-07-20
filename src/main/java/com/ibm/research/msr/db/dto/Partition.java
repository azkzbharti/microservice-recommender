package com.ibm.research.msr.db.dto;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Partition {
	private ObjectId _id;
	private ObjectId projectId;
	private String partitionType;
	private long timeStampInMS;
	private String source;
	private Document parameters;
	private Document partitionResult;
	private Document partitionPath;
	
	public Partition(ObjectId projectId, String partitionType, Document parameters, Document partitionResult,Document partionPath, String source) {
		this._id = new ObjectId();
		this.projectId = projectId;
		this.partitionType = partitionType;
		this.parameters = parameters;
		this.partitionResult = partitionResult;
		this.partitionPath=partionPath;
		this.source = source;
		this.timeStampInMS = System.currentTimeMillis();
	}
	
	public Partition(Document d) {
		if(d.containsKey("_id")) {
			this._id = d.getObjectId("_id");
		}
		else {
			this._id = new ObjectId();
		}
		this.projectId = d.getObjectId("project_id");
		this.partitionType = d.getString("partition_type");
		this.parameters = (Document) d.get("parameters");
		this.partitionResult = (Document) d.get("partition_result");
		this.partitionPath= (Document) d.get("partion_path");
		this.source = d.getString("source");
		if(d.containsKey("timeStampInMS")) {
			this.timeStampInMS = d.getLong("timeStampInMS");
		}
	}
	
	public Document toDocument() {
		Document d = new Document()
				.append("_id", this._id)
				.append("project_id", this.projectId)
				.append("partition_type", this.partitionType)
				.append("parameters", this.parameters)
				.append("partition_result", this.partitionResult)
				.append("source", this.source)
				.append("timeStampInMS", this.timeStampInMS)
				.append("partion_path",this.partitionPath);
		return d;
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

	public String getPartitionType() {
		return partitionType;
	}

	public void setPartitionType(String partitionType) {
		this.partitionType = partitionType;
	}

	public Document getParameters() {
		return parameters;
	}

	public void setParameters(Document parameters) {
		this.parameters = parameters;
	}

	public Document getPartitionResult() {
		return partitionResult;
	}

	public void setPartitionResult(Document partitionResult) {
		this.partitionResult = partitionResult;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public long getTimeStampInMS() {
		return timeStampInMS;
	}

	public void setTimeStampInMS(long timeStampInMS) {
		this.timeStampInMS = timeStampInMS;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}
