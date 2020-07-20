package com.ibm.research.msr.db.queries.analysis;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.ibm.research.msr.db.dto.Analysis;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAnalysisByProjectIdAndType extends FindOneQuery{
	
	public SelectAnalysisByProjectIdAndType(MongoDatabase db, ObjectId projectId, String type, Logger logger) throws MongoException {

		super(db, "m2m_analyses", logger);
		Bson filter = Filters.and(Filters.eq("project_id", projectId), Filters.eq("analysis_type", type));		
		super.bind(filter, null, null);
		logger.info("Binding to select analysis of project id " + projectId.toString() + 
				"and type:" + type + "from the Analysis collection.");

	}

	public Analysis getResult() {
		return new Analysis(result);
	}
}
