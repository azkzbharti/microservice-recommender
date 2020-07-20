package com.ibm.research.msr.db.queries.base;

import org.bson.conversions.Bson;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class DeleteOneQuery extends MongoQuery{
	
	private Bson filters;
	
	public DeleteOneQuery(MongoDatabase db, String collection, Bson filters, Logger logger) throws MongoException {
		super(db,collection, logger);
		this.filters = filters;
	}
	
	public void execute() throws MongoException {
		collection.deleteOne(filters);
	}
}
