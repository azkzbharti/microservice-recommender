package com.ibm.research.msr.db.queries.analysis;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoAnalysisQuery extends InsertOneQuery {

	public InsertIntoAnalysisQuery(MongoDatabase db, Analysis analysis, Logger logger) throws MongoException {

		super(db, "m2m_analyses", logger);
		
		SelectAnalysisByProjectIdAndType searchQuery 
			= new SelectAnalysisByProjectIdAndType(db, analysis.getProjectId(), analysis.getAnalysisType(), logger);
		
		Document result = searchQuery.execute();
		Bson replaceFilter = null;
		if (result != null) {
			ObjectId existingId = result.getObjectId("_id");
			replaceFilter = new Document().append("_id", existingId);
			analysis.set_id(existingId);
		}
		
		super.bind(analysis.toDocument(), replaceFilter);
		logger.info("Binding to insert" + analysis.getAnalysisType() + "analysis for project id:" 
				+ analysis.getProjectId() + "into the analysis collection.");
	}
}
