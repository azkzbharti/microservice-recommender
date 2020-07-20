package com.ibm.research.msr.db.queries.project;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;

import com.ibm.research.msr.db.dto.GitProject;
import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.base.FindManyQuery;
import com.ibm.research.msr.utils.Constants;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

public class SelectAllProjects extends FindManyQuery{
	
public SelectAllProjects(MongoDatabase db, Logger logger) throws MongoException {
		
		super(db, "m2m_projects", logger);
		super.bind(null,null, null, -1);
		logger.info("Binding all projects from the Projects collection.");
		 
	}
	
	public List <Project> getResult() {
		List<Project> projs = new ArrayList<Project>();
		for(Document result : resultList) {
			String type = result.getString("type");
			if(type.equals(Constants.SOURCE_GIT)) {
				projs.add(new GitProject(result));
			}else {
				projs.add(new SourceProject(result));
			}
		}
		
		return projs;
	}
}
