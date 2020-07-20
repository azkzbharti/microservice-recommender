package com.ibm.research.msr.db.queries.base;

import org.bson.Document;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoQuery {
	
	protected MongoCollection<Document> collection;
	protected MongoDatabase db;
	protected Logger logger;
	
	public MongoQuery(MongoDatabase database, String collection_name, Logger logger) throws MongoException {
		this.db = database;
		this.collection = db.getCollection(collection_name);
		this.logger = logger;
	}
}
