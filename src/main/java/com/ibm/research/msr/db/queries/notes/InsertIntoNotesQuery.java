package com.ibm.research.msr.db.queries.notes;

import org.bson.Document;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.ibm.research.msr.model.Notes;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoNotesQuery extends InsertOneQuery{

	public InsertIntoNotesQuery(MongoDatabase db, Notes notes, Logger logger) throws MongoException {
		super(db, "m2m_notes", logger);
		
		Document d = new Document()
				.append("project_id", notes.getProjectId())
				.append("cluster_id", notes.getClusterId())
				.append("notes", notes.getNotes())
				.append("timeStampInMS", System.currentTimeMillis())
				.append("user_id", notes.getUserId());
		
		super.bind(d, null);
		logger.info("Binding to insert Notes for project id: " 
				+ notes.getProjectId());
		
	}
}