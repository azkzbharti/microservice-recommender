package com.ibm.research.msr.db.queries.notes;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.FindManyQuery;
import com.ibm.research.msr.model.Notes;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAllNotesForProjectId extends FindManyQuery {

	public SelectAllNotesForProjectId(MongoDatabase db, String projectId, Logger logger) throws MongoException  {
		super(db, "m2m_notes", logger);
		
		Document sort = new Document().append("timeStampInMS", -1);
		Bson filter = Filters.eq("project_id", projectId);	
		
		super.bind(filter, null, sort, -1);

		logger.info("Binding to select all the Notes for project id:" + projectId);
	}
	
	public List <Notes> getResult() {
		ArrayList<Notes> analysisList = new ArrayList<Notes>();
		
		for(Document d : resultList) {
			analysisList.add(new Notes(d));
		}
		
		return analysisList;
	}
}