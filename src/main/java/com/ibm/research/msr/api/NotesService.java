/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;
import java.util.List;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.queries.base.DeleteManyQuery;
import com.ibm.research.msr.db.queries.notes.InsertIntoNotesQuery;
import com.ibm.research.msr.db.queries.notes.SelectAllNotesForProjectId;
import com.ibm.research.msr.db.queries.notes.UpdateNotesByNotesId;
import com.ibm.research.msr.model.Notes;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@CrossOrigin
@Controller
@RequestMapping("/msr/notes/")
@Api(value = "Notes", description = "Share Notes Services")
public class NotesService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(NotesService.class + "_swagger_log");


	public NotesService() {

	}

	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and deletes all notes associated for the project id. 
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteNotesForProjId", produces = "application/json")
	@ApiOperation(value = "Delete all Notes ", notes = "", tags = "delete")
	public @ResponseBody JSONObject notesForProjectId(
			@RequestParam(value = "projectIds", required = true) String projectId) {
		logger.info("Delete all Notes for the project : {}", projectId);
		
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		Bson filterByProjId = Filters.eq("project_id", projectId);
		DeleteManyQuery query = new DeleteManyQuery(db, "m2m_notes", filterByProjId, logger);
		query.execute();
		
		result.put("status", ProjectStatus.OK);	
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and deletes specific notes associated for the project id. But project Id itself is not needed for executing, since notes id is unique
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "deleteNotes", produces = "application/json")
	@ApiOperation(value = "Delete specific Note ", notes = "", tags = "delete")
	public @ResponseBody JSONObject notes(
			@RequestParam(value = "noteIds", required = true) String notes) {
		logger.info("Delete specific Notes for the project : {}", notes);
		
		String[] notesIdArr = notes.split(",");
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		for (int i = 0; i < notesIdArr.length; i++) {
			Bson filterById = Filters.eq("_id", new ObjectId(notesIdArr[i]));
			DeleteManyQuery query = new DeleteManyQuery(db, "m2m_notes", filterById, logger);
			query.execute();
		}
		
		result.put("status", ProjectStatus.OK);	
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and return all notes associated with the project id. 
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "getNotesForProjId", produces = "application/json")
	@ApiOperation(value = "Get all Notes for Proj ID ", notes = "", tags = "get")
	public @ResponseBody JSONArray getNotesForProjId(
			@RequestParam(value = "projectIds", required = true) String projectId) {
		logger.info("Delete all Notes for the project : {}", projectId);
		
		JSONArray jsonArr = new JSONArray();
		MongoDatabase db = DatabaseConnection.getDatabase();
		

		SelectAllNotesForProjectId notesProj = new SelectAllNotesForProjectId(db, projectId, logger);
		notesProj.execute();
		List<Notes> notesList= null;
		JSONObject tempobj ;
		try {
			if(notesProj.getResultSize()>0) {
				notesList = notesProj.getResult();
				JSONArray allresults = new JSONArray();
				tempobj = new JSONObject();
				for(Notes notes:notesList) {
						JSONObject tempJsonObj = new JSONObject();
						tempJsonObj.put("notesId", notes.getNotesId().toString());
						tempJsonObj.put("notes", notes.getNotes());
						tempJsonObj.put("clusterId", notes.getClusterId());
						tempJsonObj.put("timeStampInMS", notes.getTimeStampInMS());
						tempJsonObj.put("userId", notes.getUserId());
						
						
						jsonArr.add(tempJsonObj);
				}
			}
		}
		catch(NullPointerException e) {
			JSONObject result = new JSONObject();
			result.put("status", ProjectStatus.INTERNAL_SERVER_ERROR);
			jsonArr.add(result);
		} 
		return jsonArr;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It updates notes for the notes Id.
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "insertNotes", consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Insert Note for Project, Cluster Id ", notes = "", tags = "insert")
	public @ResponseBody JSONObject notes(@RequestBody Notes notes) {
		logger.info("Insert Notes for the project : {}", notes.getProjectId());
		
		MongoDatabase db = DatabaseConnection.getDatabase();
		JSONObject result = new JSONObject();
		InsertIntoNotesQuery updateProjQuery = new InsertIntoNotesQuery(db, notes, logger);
		updateProjQuery.execute();
		result.put("status", ProjectStatus.OK); 
		logger.info("Completed post notes for project: {}", notes.getProjectId());
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It updates notes for the notes Id.
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "updateNotes", consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Update Notes ", notes = "", tags = "update")
	public @ResponseBody JSONObject updateNotes(@RequestBody Notes notes) {
		logger.info("Update specific Notes for the project : {}", notes.getProjectId());
		
		MongoDatabase db = DatabaseConnection.getDatabase();
		JSONObject result = new JSONObject();
		UpdateNotesByNotesId updateProjQuery = new UpdateNotesByNotesId(db, notes.getNotesId(), notes.getNotes(), notes.getUserId(), logger);
		updateProjQuery.execute();
		result.put("status", ProjectStatus.OK); 
		logger.info("Completed Update notes for project: {}", notes.getProjectId());
		return result;
	}

}