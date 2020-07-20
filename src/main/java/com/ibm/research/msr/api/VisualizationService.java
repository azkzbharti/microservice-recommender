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
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
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
import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.partition.SelectAllPartitionsByProjectId;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.msr.utils.Constants;
import com.mongodb.client.MongoDatabase;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@CrossOrigin
@Controller
@RequestMapping("/msr/init/")
@Api(value = "Visualization", description = "Microservices Recommender APIs to retrieve data for visualization")
public class VisualizationService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(VisualizationService.class + "_swagger_log");


	public VisualizationService() {

	}

	@RequestMapping(method=RequestMethod.GET,value="getPartitions", produces = "application/json")
	@ApiOperation(value = "Get the generated partitions", notes = "", tags = "partition")
	public @ResponseBody JSONObject getPartitions(
			@RequestParam(value = "projectId", required = true) String projectId) {

		logger.info("runGetParitions : {}", projectId);
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		SelectAllPartitionsByProjectId partitionProj = new SelectAllPartitionsByProjectId(db, new ObjectId(projectId), logger);
		partitionProj.execute();
		List<Partition> allpartition= null;
		JSONObject tempobj ;
		try {
			if(partitionProj.getResultSize()>0) {
				allpartition = partitionProj.getResult();
			}
			JSONArray allresults = new JSONArray();
			tempobj = new JSONObject();
			for(Partition partiton:allpartition) {
				
				if(partiton.getPartitionType().equals(Constants.API_CLUSTERING)) {
					
					tempobj.put(Constants.CLUSTER_API, partiton.getPartitionResult().get(Constants.CLUSTER_API));
					allresults.add(tempobj);
					tempobj.put(Constants.CLUSTER_COHESION_API, partiton.getPartitionResult().get(Constants.CLUSTER_COHESION_API));
					allresults.add(tempobj);

				}
				else if(partiton.getPartitionType().equals(Constants.AFFINITY_CLUSTERING)) {
//					tempobj = new JSONObject();
					tempobj.put(Constants.CLUSTER_AFFINITY, partiton.getPartitionResult().get(Constants.CLUSTER_AFFINITY));
					allresults.add(tempobj);
					tempobj.put(Constants.CLUSTER_COHESION_AFFINITY, partiton.getPartitionResult().get(Constants.CLUSTER_COHESION_AFFINITY));
					allresults.add(tempobj);
				}
				
				else if(partiton.getPartitionType().equals(Constants.COMMUNITY_CLUSTERING)) {
//					tempobj = new JSONObject();
					tempobj.put(Constants.CLUSTER_COMMUNITY, partiton.getPartitionResult().get(Constants.CLUSTER_COMMUNITY));
					allresults.add(tempobj);
				}

			}
			SelectAnalysisByProjectIdAndType sp = new SelectAnalysisByProjectIdAndType(db, new ObjectId(projectId), Constants.BAR_DATA, logger);
			sp.execute();
//			if(sp.getResultSize()>0) {
//				Analysis barnaalysis= sp.getResult();
//				//				JSONObject obj = new JSONObject();
//				//				obj.put("bar-data", barnaalysis.getAnalysisResult().get(key));
//				allresults.add( barnaalysis.getAnalysisResult());
//				tempobj.put(ConstantsAPI.barData, barnaalysis.getAnalysisResult());
//
//			}
			result.put("Results", tempobj);
		}
		catch(NullPointerException e) {
			result.put("status", "Unable to fetch data for Visualization.");
		}

		//TODO: update this to return the appropriate status
		//		
		return result;
	}

	

	@RequestMapping(method=RequestMethod.GET,value="getClassDetails", produces = "application/json")
	@ApiOperation(value = "Get the generated ClassDetails", notes = "", tags = "analysis")
	public @ResponseBody JSONObject getClassDetails(
			@RequestParam(value = "projectId", required = true) String projectId) {

		logger.info("rungetClassDetails : {}", projectId);
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		Project proj = null;
		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
			proj = sproj.getResult();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");

		}
		
		SelectAnalysisByProjectIdAndType sp;
		// get classdetails analysis
				sp = new SelectAnalysisByProjectIdAndType(db, new ObjectId(projectId), Constants.CLASS_DETAILS, logger);
				Analysis classDEtails = null;
				try {
					sp.execute();
					classDEtails = sp.getResult();
					result.put("Result", classDEtails.getAnalysisResult());
				} catch (NullPointerException e) {
					logger.error("Project id " + projectId + " " + Constants.CLASS_DETAILS + " analysis not found");
					result.put("status", "Project id " + projectId + " " + Constants.CLASS_DETAILS + " analysis not found");
					result.put("status", "Unable to fetch data for Visualization.");

					return result;
				}
				JSONObject tempobj = new JSONObject();
				
				

		//TODO: update this to return the appropriate status
		//		
		return result;
	}

}
