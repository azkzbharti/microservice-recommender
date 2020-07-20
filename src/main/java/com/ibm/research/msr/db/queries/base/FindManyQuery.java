package com.ibm.research.msr.db.queries.base;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;

import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class FindManyQuery extends MongoQuery {
	private Bson filters;
	private Document projections;
	private Document sortBy;
	private int limit;
	protected List<Document> resultList;
	
	public FindManyQuery(MongoDatabase db, String collection, Logger logger) throws MongoException {	
		super(db, collection, logger);
	}
	
	public void bind(Bson filters, Document projections, Document sortBy, int limit) {
		this.filters = filters;
		this.projections = projections;
		this.sortBy = sortBy;
		this.limit = limit;
	}
	
	public int getResultSize() {
		if (resultList == null || resultList.size() == 0)
			return 0;
		else
			return resultList.size();
	}
	
	public List<Document> execute() throws MongoException {
		FindIterable<Document> result = null;
		
		if (filters != null) {
			// TODO: thoroughly test this with different filters and nested filters
			// TODO: i think we are missing some more types of filters other than Bson
			result = collection.find(filters);
		}
		else {
			result = collection.find();
		}
		
		if (sortBy != null) {
			result = result.sort(sortBy);
		}
		
		if(projections != null) {
			result = result.projection(projections);
		}
		
		if (limit > 0) {
			result = result.limit(limit);
		}
		
		resultList =  result.into(new ArrayList<Document>());
		return resultList;
			
	}
}
