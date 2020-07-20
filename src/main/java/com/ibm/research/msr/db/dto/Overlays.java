package com.ibm.research.msr.db.dto;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Overlays {
	private ObjectId _id;
	private ObjectId projectId;
	private Document transactionResult;
	private Document transactionPath;
	
	public Overlays(ObjectId projectId, String partitionType, Document parameters, Document transactionResult,Document transactionPath) {
		this._id = new ObjectId();
		this.projectId = projectId;
		this.transactionResult = transactionResult;
		this.transactionPath=transactionPath;
	}
	
	public Overlays(Document d) {
		if(d.containsKey("_id")) {
			this._id = d.getObjectId("_id");
		}
		else {
			this._id = new ObjectId();
		}
		this.projectId = d.getObjectId("project_id");
		this.transactionResult = (Document) d.get("transaction_result");
		this.transactionPath= (Document) d.get("transaction_path");
	}
	
	public Document toDocument() {
		Document d = new Document()
				.append("_id", this._id)
				.append("project_id", this.projectId)
				.append("transaction_result", this.transactionResult)
				.append("transaction_path",this.transactionPath);
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

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Document getTransactionResult() {
		return transactionResult;
	}

	public void setTransactionResult(Document transactionResult) {
		this.transactionResult = transactionResult;
	}

	public Document getTransactionPath() {
		return transactionPath;
	}

	public void setTransactionPath(Document transactionPath) {
		this.transactionPath = transactionPath;
	}
	
	
}
