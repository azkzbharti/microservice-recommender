package com.ibm.research.msr.db.queries.base;


import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class FindOneQuery extends MongoQuery {

	private Bson filters;
	private Document projections;
	private Document sortBy;
	protected Document result;

	public FindOneQuery(MongoDatabase db, String collection, Logger logger) throws MongoException {
		super(db, collection, logger);
	}

	public void bind(Bson filters, Document projections, Document sortBy) {
		this.filters = filters;
		this.projections = projections;
		this.sortBy = sortBy;
	}
	
	public int getResultSize() {
		if (result == null || result.getObjectId("_id") == null)
			return 0;
		else
			return 1;
	}

	public Document execute() throws MongoException {
		FindIterable<Document> intermediate_result = null;

		try {
			if (filters != null) {
				// TODO: thoroughly test this with different filters and nested filters
				// TODO: i think we are missing some more types of filters other than Bson
				intermediate_result = collection.find(filters);
			}
			else {
				intermediate_result = collection.find();
			}

			if (sortBy != null) {
				intermediate_result = intermediate_result.sort(sortBy);
			}

			if(projections != null) {
				intermediate_result = intermediate_result.projection(projections);
			}

			result =  intermediate_result.first();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
