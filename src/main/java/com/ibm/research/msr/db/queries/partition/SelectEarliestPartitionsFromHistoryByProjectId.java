package com.ibm.research.msr.db.queries.partition;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.queries.base.FindManyQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectEarliestPartitionsFromHistoryByProjectId extends FindManyQuery {

	public SelectEarliestPartitionsFromHistoryByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException  {
		super(db, "m2m_partitions_history", logger);
		
		Bson filter = Filters.eq("project_id", projectId);
		Document sort = new Document().append("timeStampInMS", 1);
		super.bind(filter, null, sort, 1);

		logger.info("Binding to select the earliest paritions from history for project id:" + projectId);
	}
	
	public List <Partition> getResult() {
		ArrayList<Partition> paritionsList = new ArrayList<Partition>();
		
		for(Document d : resultList) {
			paritionsList.add(new Partition(d));
		}
		
		return paritionsList;
	}
	
}