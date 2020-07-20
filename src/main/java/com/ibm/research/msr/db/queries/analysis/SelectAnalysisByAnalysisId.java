package com.ibm.research.msr.db.queries.analysis;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.ibm.research.msr.db.dto.Analysis;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAnalysisByAnalysisId extends FindOneQuery{

	public SelectAnalysisByAnalysisId(MongoDatabase db, ObjectId analysisId, Logger logger) throws MongoException {

		super(db, "m2m_analyses", logger);
		Bson filter = Filters.eq("_id", analysisId);		
		super.bind(filter, null, null);
		logger.info("Binding to select analysis id " + analysisId.toString() + " from the Analysis collection.");

	}

	public Analysis getResult() {
		return new Analysis(result);
	}

}
