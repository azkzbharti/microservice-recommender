package com.ibm.research.msr.db.queries.project;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.UpdateManyQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class UpdateProjectStatusByProjectId extends UpdateManyQuery{

	public UpdateProjectStatusByProjectId(MongoDatabase db, String projectId, String status, Logger logger) throws MongoException {

		super(db, "m2m_projects", logger);
		Bson filter = Filters.eq("_id", new ObjectId(projectId));	
		
		Document newValue = new Document("status", status);
		Document update = new Document("$set", newValue);
		
		super.bind(filter, update);	
		logger.info("Binding udpated microservices project status for the projectId: " + projectId.toString());
		
		
		
//		SelectPartitionById searchQuery = new SelectPartitionById(db, new ObjectId(projectId), logger);
//		
//		Document update = searchQuery.execute();
//		Bson replaceFilter = null;
//		if (update != null) {
//			replaceFilter = update.append("$set", new Document("partition_result", microservices));
//		}
//
//		super.bind(replaceFilter, update);
	}

}
