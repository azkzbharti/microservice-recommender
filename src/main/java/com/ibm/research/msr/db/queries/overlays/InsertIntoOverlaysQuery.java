package com.ibm.research.msr.db.queries.overlays;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoOverlaysQuery extends InsertOneQuery{

	public InsertIntoOverlaysQuery(MongoDatabase db, Overlays overlays, Logger logger) throws MongoException {

		super(db, "m2m_overlays", logger);
			
		SelectOverlaysByProjectId searchQuery 
				= new SelectOverlaysByProjectId(db, overlays.getProjectId(), logger);
			
		Document result = searchQuery.execute();
		Bson replaceFilter = null;
		if (result != null) {
			ObjectId existingId = result.getObjectId("_id");
			replaceFilter = new Document().append("_id", existingId);
			overlays.set_id(existingId);
		}
		
		super.bind(overlays.toDocument(), replaceFilter);
		logger.info("Binding to insert transactions for project id:" 
				+ overlays.getProjectId() + "into the Overlays collection.");
	}
}