package com.ibm.research.msr.db.queries.overlays;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectOverlaysByProjectId extends FindOneQuery{
	
	public SelectOverlaysByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException {

		super(db, "m2m_overlays", logger);
		Bson filter = Filters.eq("project_id", projectId);		
		super.bind(filter, null, null);
		logger.info("Binding to select transaction of project id " + projectId.toString() + 
				"from the Overlays collection.");

	}

	public Overlays getResult() {
		return new Overlays(result);
	}
}
