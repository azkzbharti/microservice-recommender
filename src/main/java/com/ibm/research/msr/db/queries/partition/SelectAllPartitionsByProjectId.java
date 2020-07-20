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

public class SelectAllPartitionsByProjectId extends FindManyQuery {

	public SelectAllPartitionsByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException  {
		super(db, "m2m_partitions", logger);
		
		Bson filter = Filters.eq("project_id", projectId);	
		super.bind(filter, null, null, -1);

		logger.info("Binding to select all the paritions for project id:" + projectId);
	}
	
	public List <Partition> getResult() {
		ArrayList<Partition> paritionsList = new ArrayList<Partition>();
		
		for(Document d : resultList) {
			paritionsList.add(new Partition(d));
		}
		
		return paritionsList;
	}
}