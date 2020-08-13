package com.ibm.research.msr.db.queries.notes;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.queries.base.UpdateManyQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class UpdateNotesByNotesId extends UpdateManyQuery{

	public UpdateNotesByNotesId(MongoDatabase db, String notesId, String notes, String userId, Logger logger) throws MongoException {

		super(db, "m2m_notes", logger);
		Bson filter = Filters.eq("_id", new ObjectId(notesId));	
		
		
		Document newValue = new Document("notes", notes).
				append("timeStampInMS",System.currentTimeMillis()).
				append("user_id", userId);  
		Document update = new Document("$set", newValue);
		
		super.bind(filter, update);	
		logger.info("Binding udpated Notes the Id: " + notesId.toString());
		
	}

}
