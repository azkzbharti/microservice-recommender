package com.ibm.research.msr.db.queries.partition;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoPartitionQuery extends InsertOneQuery{

	public InsertIntoPartitionQuery(MongoDatabase db, Partition partition, Logger logger) throws MongoException {

		super(db, "m2m_partitions", logger);

		SelectPartitionByPartitionIdAndType searchQuery 
		= new SelectPartitionByPartitionIdAndType(db, partition.getProjectId(), partition.getPartitionType(), logger);

		Document result = searchQuery.execute();
		Bson replaceFilter = null;
		if (result != null) {
			//replaceFilter = new Document().append("_id", result.getObjectId("_id"));
			ObjectId existingId = result.getObjectId("_id");
			replaceFilter = new Document().append("_id", existingId);
			partition.set_id(existingId);
		}

		super.bind(partition.toDocument(), replaceFilter);
		logger.info("Binding to insert partition: " + partition.getPartitionType()+  "for project id: " 
				+ partition.getProjectId() + "into the partitions collection.");
	}
}