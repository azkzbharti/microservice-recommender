package com.ibm.research.msr.db.queries.project;

import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.GitProject;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.base.InsertOneQuery;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class InsertIntoProjectQuery extends InsertOneQuery {

	public InsertIntoProjectQuery(MongoDatabase db, Project project, Logger logger) throws MongoException {

		super(db, "m2m_projects", logger);
		
		if(project instanceof GitProject) {
			GitProject gitProject = (GitProject) project;
			super.bind(gitProject.toDocument(), null);
		}else {
			SourceProject sourceProject = (SourceProject) project;
			super.bind(sourceProject.toDocument(), null);
		}
		
		logger.info("Binding to insert project:" + project.getProjectName() + "into the projects collection.");
	}
}
