package com.ibm.research.msr.db.queries.base;

import org.bson.conversions.Bson;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class DeleteManyQuery extends MongoQuery{
	
	private Bson filters;
	
	public DeleteManyQuery(MongoDatabase db, String collection, Bson filters, Logger logger) throws MongoException {
		super(db,collection, logger);
		this.filters = filters;
	}
	
	public DeleteResult execute() throws MongoException {
		return collection.deleteMany(filters);
	}
}
