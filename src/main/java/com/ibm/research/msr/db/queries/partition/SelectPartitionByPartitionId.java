package com.ibm.research.msr.db.queries.partition;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectPartitionByPartitionId extends FindOneQuery{

	public SelectPartitionByPartitionId(MongoDatabase db, ObjectId partitionId, Logger logger) throws MongoException {

		super(db, "m2m_partitions", logger);
		Bson filter = Filters.eq("_id", partitionId);		
		super.bind(filter, null, null);
		logger.info("Binding to select partition id " + partitionId.toString() + " from the Partitions collection.");

	}

	public Partition getResult() {
		return new Partition(result);
	}

}
