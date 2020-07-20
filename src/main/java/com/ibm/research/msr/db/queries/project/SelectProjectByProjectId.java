package com.ibm.research.msr.db.queries.project;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.GitProject;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.base.FindOneQuery;
import com.ibm.research.msr.utils.Constants;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class SelectProjectByProjectId extends FindOneQuery{
	
public SelectProjectByProjectId(MongoDatabase db, ObjectId projectId, Logger logger) throws MongoException {
		
		super(db, "m2m_projects", logger);
		Bson filter = Filters.eq("_id", projectId);		
		super.bind(filter, null, null);
		logger.info("Binding to select project id " + projectId.toString() + " from the Projects collection.");
		 
	}
	
	public Project getResult() {
		String type = result.getString("type");
		if(type.equals(Constants.SOURCE_GIT)) {
			return new GitProject(result);
		}else {
			return new SourceProject(result);
		}
	}
}
