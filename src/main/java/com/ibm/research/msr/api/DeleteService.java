/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.queries.base.DeleteManyQuery;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@CrossOrigin
@Controller
@RequestMapping("/msr/delete/")
@Api(value = "Delete", description = "Delete Services")
public class DeleteService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(DeleteService.class + "_swagger_log");


	public DeleteService() {

	}

	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and deletes all onboarded applciation details.
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "project", produces = "application/json")
	@ApiOperation(value = "Delete Onboarded Project ", notes = "", tags = "delete")
	public @ResponseBody JSONObject deleteProjs(
			@RequestParam(value = "projectIds", required = true) String projectIds) {
		logger.info("Delete Onboarded Projects : {}", projectIds);
		
		String[] projIdArr = projectIds.split(",");
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		for (int i = 0; i < projIdArr.length; i++) {
			Bson filterById = Filters.eq("_id", new ObjectId(projIdArr[i]));
			Bson filterByProjId = Filters.eq("project_id", new ObjectId(projIdArr[i]));
			DeleteManyQuery query = new DeleteManyQuery(db, "m2m_projects", filterById, logger);
			query.execute();
			
			query = new DeleteManyQuery(db, "m2m_analyses", filterByProjId, logger);
			query.execute();
			
			query = new DeleteManyQuery(db, "m2m_overlays", filterByProjId, logger);
			query.execute();
			
			query = new DeleteManyQuery(db, "m2m_partitions", filterByProjId, logger);
			query.execute();
			
			query = new DeleteManyQuery(db, "m2m_partitions_history", filterByProjId, logger);
			query.execute();
			
			query = new DeleteManyQuery(db, "m2m_notes", filterByProjId, logger);
			query.execute();
		}
		result.put("status", ProjectStatus.OK);	
		return result;
	}

}