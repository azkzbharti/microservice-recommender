package com.ibm.research.msr.db.queries.analysis;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.FindManyQuery;
import com.ibm.research.msr.db.dto.Analysis;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectAllAnalysisByProjectId extends FindManyQuery {

	public SelectAllAnalysisByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException  {
		super(db, "m2m_analyses", logger);
		
		Bson filter = Filters.eq("project_id", projectId);	
		super.bind(filter, null, null, -1);

		logger.info("Binding to select all the analyses for project id:" + projectId);
	}
	
	public List <Analysis> getResult() {
		ArrayList<Analysis> analysisList = new ArrayList<Analysis>();
		
		for(Document d : resultList) {
			analysisList.add(new Analysis(d));
		}
		
		return analysisList;
	}
}