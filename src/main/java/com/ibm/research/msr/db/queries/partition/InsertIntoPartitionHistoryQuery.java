package com.ibm.research.msr.db.queries.partition;

import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoPartitionHistoryQuery extends InsertOneQuery{

	public InsertIntoPartitionHistoryQuery(MongoDatabase db, Partition partition, Logger logger) throws MongoException {

		super(db, "m2m_partitions_history", logger);

		super.bind(partition.toDocument(), null);
		logger.info("Binding to insert updated partition: " + partition.getPartitionType()+  "for project id: " 
				+ partition.getProjectId() + "into the partitions history collection.");
	}
}