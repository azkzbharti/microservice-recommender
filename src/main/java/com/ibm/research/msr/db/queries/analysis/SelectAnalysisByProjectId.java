package com.ibm.research.msr.db.queries.analysis;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.ibm.research.msr.db.dto.Analysis;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAnalysisByProjectId extends FindOneQuery{
	
	public SelectAnalysisByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException {

		super(db, "m2m_analyses", logger);
		Bson filter = Filters.eq("project_id", projectId);		
		super.bind(filter, null, null);
		logger.info("Binding to select analysis only by project id " + projectId.toString() + 
				" from the Analysis collection.");

	}

	public Analysis getResult() {
		return new Analysis(result);
	}
}
