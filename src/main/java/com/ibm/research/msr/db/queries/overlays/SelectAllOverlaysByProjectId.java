package com.ibm.research.msr.db.queries.overlays;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.queries.base.FindManyQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAllOverlaysByProjectId extends FindManyQuery {

	public SelectAllOverlaysByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException  {
		super(db, "m2m_overlays", logger);
		
		Bson filter = Filters.eq("project_id", projectId);	
		super.bind(filter, null, null, -1);

		logger.info("Binding to select all the transactions for project id:" + projectId);
	}
	
	public List <Overlays> getResult() {
		ArrayList<Overlays> analysisList = new ArrayList<Overlays>();
		
		for(Document d : resultList) {
			analysisList.add(new Overlays(d));
		}
		
		return analysisList;
	}
}