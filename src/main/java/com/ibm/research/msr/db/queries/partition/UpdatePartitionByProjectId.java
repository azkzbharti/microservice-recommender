package com.ibm.research.msr.db.queries.partition;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.UpdateManyQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class UpdatePartitionByProjectId extends UpdateManyQuery{

	public UpdatePartitionByProjectId(MongoDatabase db, String projectId, Object microservices, String source, Logger logger) throws MongoException {

		super(db, "m2m_partitions", logger);
		Bson filter = Filters.eq("project_id", new ObjectId(projectId));	
		
		Document newValue = new Document("source", source).
				append("timeStampInMS",System.currentTimeMillis()).
				append("partition_result", microservices);  
		Document update = new Document("$set", newValue);
		
//		Document update = new Document()
//				.append("$set", new Document("source", Constants.SOURCE_USER))
//				.append("$set", new Document("partition_result", microservices));
		
		super.bind(filter, update);	
		logger.info("Binding udpated microservices results for the projectId: " + projectId.toString());
		
		
		
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