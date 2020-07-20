package com.ibm.research.msr.db.queries.base;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

public class InsertOneQuery extends MongoQuery {

	private Document new_document;
	private Bson replaceFilter;

	public InsertOneQuery(MongoDatabase db, String collection, Logger logger) throws MongoException {
		super(db,collection, logger);
	}

	public void bind(Document new_document, Bson filter) {
		this.new_document = new_document;
		this.replaceFilter = filter;
	}

	public ObjectId execute() throws MongoException {
		if (replaceFilter != null) {
			collection.replaceOne(replaceFilter, new_document, new ReplaceOptions().upsert(true));
			this.logger.info("Replaced with a new document with id: " + new_document.getObjectId("_id").toString() + 
					" in " + this.collection.toString() +" collection.");
		}
		else {
			collection.insertOne(new_document);
			this.logger.info("Inserted document with id: " + new_document.getObjectId("_id").toString() + 
					" in " + this.collection.toString() +" collection.");
		}

		return (ObjectId) new_document.get("_id");
	}
}
