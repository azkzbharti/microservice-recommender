package com.ibm.research.msr.db.queries.base;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class UpdateManyQuery extends MongoQuery {
	
	private Bson filters;
	private Document updates;
	
	public UpdateManyQuery(MongoDatabase db, String collection, Logger logger) throws MongoException {
		super(db, collection, logger);
	}
	
	public void bind(Bson filters, Document updates) {
		this.filters = filters;
		this.updates = updates;
	}
	
	public void execute() throws MongoException {
		if(filters !=null && updates !=null)
		 collection.updateMany(filters, updates);
		
	}
}
