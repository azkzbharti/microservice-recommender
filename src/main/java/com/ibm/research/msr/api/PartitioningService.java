/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.overlays.InsertIntoOverlaysQuery;
import com.ibm.research.msr.db.queries.partition.InsertIntoPartitionHistoryQuery;
import com.ibm.research.msr.db.queries.partition.InsertIntoPartitionQuery;
import com.ibm.research.msr.db.queries.partition.Ownership;
import com.ibm.research.msr.db.queries.partition.SelectAllPartitionsByProjectId;
import com.ibm.research.msr.db.queries.partition.SelectEarliestPartitionsFromHistoryByProjectId;
import com.ibm.research.msr.db.queries.partition.UpdatePartitionByProjectId;
import com.ibm.research.msr.db.queries.project.SelectAllProjects;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.msr.db.queries.project.UpdateProjectStatusByProjectId;
import com.ibm.research.msr.model.Microservice;
import com.ibm.research.msr.model.Status;
import com.ibm.research.appmod.slicing.SlicingDriver;
//import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.ddd.EntityBeanAffinity;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;



@CrossOrigin
@Controller
@RequestMapping("/msr/partition/")
@Api(value = "Partitioning", description = "Microservices Recommender APIs for generating Partitions")
public class PartitioningService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(PartitioningService.class + "_swagger_log");

	public PartitioningService() {

	}

    // Community Clustering
	@RequestMapping(method = RequestMethod.GET, value = "CommunityDetection", produces = "application/json")
	@ApiOperation(value = "Generate community based Partitions", notes = "", tags = "partition")
	public @ResponseBody JSONObject CommunityDetection(
			@RequestParam(value = "projectId", required = true) String projectId) {

		logger.info("communityDetectionBasedClustering : {}", projectId);
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");

		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}
		
		
		String rootPath = proj.getProjectLocation();
		String type = proj.getCodeType();
		// TODO: use the temp folder
		String resultFolder = projectPath.getAbsolutePath() + File.separator + "ui" + File.separator + "data"
				+ File.separator;
		
		String tempStoreFolder = projectPath.getAbsolutePath() + File.separator + "temp";
		
		new File(tempStoreFolder).mkdirs();
		new File(resultFolder).mkdirs();
		
		String communityClusterJSON = resultFolder + "graph_clustering.json";
		String viscommunityClusterJSON = resultFolder + "vis_graph_clustering.json";
		String transactionFilePath = tempStoreFolder + File.separator + "transaction.json";
		String entryPointFilePath = tempStoreFolder + File.separator + "service.json";
		SelectAnalysisByProjectIdAndType sp = new SelectAnalysisByProjectIdAndType(db, new ObjectId(projectId),
				Constants.INTER_CLASS_USAGE, logger);
		Analysis anaclassUsage = null;
		try {
			sp.execute();
			anaclassUsage = sp.getResult();
		} catch (NullPointerException e) {
			logger.error("Project id " + projectId + " " + Constants.INTER_CLASS_USAGE + " analysis not found");
			result.put("status",
					"Project id " + projectId + " " + Constants.INTER_CLASS_USAGE + " analysis not found");
			return result;
		}
		String interclass = anaclassUsage.getAnalysisPath();
		APIUtilities.runCommunity(rootPath, communityClusterJSON, tempStoreFolder,interclass, type, projectPath.getAbsolutePath(), viscommunityClusterJSON, transactionFilePath, entryPointFilePath);
		
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonobject = new JSONObject();
		JSONObject pathobject = new JSONObject();

		try {
			jsonobject.put(Constants.CLUSTER_COMMUNITY, jsonParser.parse(new FileReader(communityClusterJSON)));
			pathobject.put(Constants.CLUSTER_COMMUNITY + "path", communityClusterJSON);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Project id " + projectId + " error during clustering");
			result.put("status", "Project not registerd");
			e.printStackTrace();
		}
		Document parameters = null;
		Partition clusters = new Partition(new ObjectId(projectId), Constants.COMMUNITY_CLUSTERING, parameters,
				Document.parse(jsonobject.toString()), Document.parse(pathobject.toString()), Constants.SOURCE_ALGO);
		
		InsertIntoPartitionQuery insertQuery = new InsertIntoPartitionQuery(DatabaseConnection.getDatabase(), clusters,
				logger);
		insertQuery.execute();
		

		
		result.put("status", "Successfully generated community based clusters.");
		return result;
	}
	
	
	/**
	 * This considers all relevant intermediate files for DDD analysis
	 * @param projectId
	 * @param callGraphFile
	 * @param icuFile
	 * @param entryPointsJSON
	 * @param crudJSON
	 * @param transactionsJSON
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "identifyMicroserviceCandidatesWithICU", produces = "application/json")
	@ApiOperation(value = "Generate Partitions", notes = "", tags = "partition")
	public @ResponseBody JSONObject identifyMicroserviceCandidatesWithICU(
			@RequestParam(value = "projectId", required = true) String projectId,
			@ApiParam(name = "callGraphFile", value = "callGraphFile", required = true)
			@RequestPart("callGraphFile") MultipartFile callGraphFile, 
			@ApiParam(name = "icuJSON", value = "icuJSON", required = true)
			@RequestPart("icuJSON") MultipartFile icuJSON, 
			@ApiParam(name = "entryPointsJSON", value = "entryPointsJSON", required = true)
			@RequestPart("entryPointsJSON") MultipartFile entryPointsJSON,
			@ApiParam(name = "crudJSON", value = "crudJSON", required = true)
			@RequestPart("crudJSON") MultipartFile crudJSON,
			@ApiParam(name = "transactionsJSON", value = "transactionsJSON", required = true)
			@RequestPart("transactionsJSON") MultipartFile transactionsJSON) {

		logger.info("Inside identifyMicroserviceCandidatesWithICU ");
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");

		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}
		
		// Creating files for community detection
		String rootPath = proj.getProjectLocation();
		String type = proj.getCodeType();
		String tempFolder = rootPath + File.separator + "temp";
		
		
		
		
		try {
			File callGraphDotFile = new File(tempFolder + File.separator + callGraphFile.getOriginalFilename());
			FileUtils.writeByteArrayToFile(callGraphDotFile, callGraphFile.getBytes());
			
			File entryPointsJSONFile = new File(tempFolder + File.separator + entryPointsJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(entryPointsJSONFile, entryPointsJSON.getBytes());
			
			File crudJSONFile = new File(tempFolder + File.separator + crudJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(crudJSONFile, crudJSON.getBytes());
			
			File transactionsJSONFile = new File(tempFolder + File.separator + transactionsJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(transactionsJSONFile, transactionsJSON.getBytes());
			
			File icuJSONFile = new File(tempFolder + File.separator + icuJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(icuJSONFile, transactionsJSON.getBytes());

//			String entryPointFilePath = tempFolder + File.separator + "service.json";
//			String crudPath = tempFolder + File.separator + "db.json";
//			String transactionFilePath = tempFolder + File.separator + "transaction.json";
			
			//DDD processing start
			
			peformDDDAnalysis(projectId, db, projectPath, rootPath, type, tempFolder, callGraphDotFile,
					entryPointsJSONFile, crudJSONFile, transactionsJSONFile);
			
		} catch(Exception e) {
			UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, projectId, Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjQuery.execute();
			e.printStackTrace();
		}
		return result;
	}

	public static void peformDDDAnalysis(String projectId, MongoDatabase db, File projectPath, String rootPath, String type,
			String tempFolder, File callGraphDotFile, File entryPointsJSONFile, File crudJSONFile,
			File transactionsJSONFile) throws IOException, ParseException, FileNotFoundException {
		String outputFileName = tempFolder + File.separator + "businessslices.json";
		String resultFolder = projectPath.getAbsolutePath() + File.separator + "ui" + File.separator + "data" + File.separator;		
		String tempStoreFolder = projectPath.getAbsolutePath() + File.separator + "temp";
		new File(resultFolder).mkdirs();
		String communityClusterJSON = resultFolder + "graph_clustering.json";
		String viscommunityClusterJSON = resultFolder + "vis_graph_clustering.json";
		String seedFilePath = tempFolder + File.separator + Constants.SEEDS_FILE;
		System.out.println("STARTING ANALYSIS");
		
		String icuPath = tempStoreFolder + File.separator + "inter_class_usage.json";
		String dbPath = tempStoreFolder  + File.separator + "db.json";
		String userInputFilePath = tempStoreFolder  + File.separator + "bo_package_terms_input";
		SlicingDriver.performSlicing(callGraphDotFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath(), icuPath, outputFileName, dbPath);
		
		EntityBeanAffinity dddanalysis = new EntityBeanAffinity();
		
		dddanalysis.runAnalysis(crudJSONFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath(), outputFileName, userInputFilePath, seedFilePath);
		System.out.println("END DDD Analysis");
		
		
		APIUtilities.runCommunity(rootPath, communityClusterJSON, tempStoreFolder, icuPath, type, projectPath.getAbsolutePath(), viscommunityClusterJSON, transactionsJSONFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath());
		
		//Make the data compatible
				JSONParser microserviceCompatibility   = new JSONParser();
				BufferedReader bufferedReaderTemp = new BufferedReader(new FileReader(communityClusterJSON));
				JSONObject microserviceData = (JSONObject) microserviceCompatibility.parse(bufferedReaderTemp);
				JSONObject microserviceDataTemp = new JSONObject();
				JSONObject partitionDataTemp = new JSONObject();
				microserviceDataTemp.put(Constants.CLUSTER_COMMUNITY, microserviceData);
				partitionDataTemp.put("partition_result", microserviceDataTemp);
				
				try (Writer writer = new FileWriter(communityClusterJSON)) {
					writer.write(partitionDataTemp.toJSONString());
				}

				// Adding Transaction data
				String cma_path    = communityClusterJSON;
				String trans_path  = transactionsJSONFile.getAbsolutePath();
				
				JSONParser parser_cma   = new JSONParser();
				JSONParser parser_trans = new JSONParser();
				
				BufferedReader bufferedReader = new BufferedReader(new FileReader(cma_path));
				JSONObject cma  = (JSONObject) parser_cma.parse(bufferedReader);
				
				JSONArray transactions = (JSONArray) parser_trans.parse(new FileReader(trans_path));
				
				System.out.println("executing: analyze");
				
				JSONArray output = Ownership.analyze(cma, transactions);
				
				System.out.println("executing: revise");
		        JSONObject revised_microservice = Ownership.revise(cma, output);
		        
		        try (Writer writer = new FileWriter(communityClusterJSON)) {
		        	JSONObject revisedMicroserviceTemp = (JSONObject)revised_microservice.get("partition_result");
		        	JSONObject revisedMicroserviceWrite = (JSONObject)revisedMicroserviceTemp.get("microservice");
		        			
		        	writer.write(revisedMicroserviceWrite.toJSONString());
		        }
		        
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonobject = new JSONObject();
		JSONObject pathobject = new JSONObject();
		
		jsonobject.put(Constants.CLUSTER_COMMUNITY, jsonParser.parse(new FileReader(communityClusterJSON)));
		pathobject.put(Constants.CLUSTER_COMMUNITY + "path", communityClusterJSON);
		
		Document parameters = null;
		Partition clusters = new Partition(new ObjectId(projectId), Constants.COMMUNITY_CLUSTERING, parameters,Document.parse(jsonobject.toString()), Document.parse(pathobject.toString()), Constants.SOURCE_ALGO);
		InsertIntoPartitionQuery insertQuery = new InsertIntoPartitionQuery(db, clusters, logger);
		insertQuery.execute();
		
		InsertIntoPartitionHistoryQuery insertPartitionsHistoryQuery = new InsertIntoPartitionHistoryQuery(db, clusters, logger); //add an entry to history to revert in future
		insertPartitionsHistoryQuery.execute();
		
		insertTransactionsToDB(projectId, transactionsJSONFile.getAbsolutePath());
		

		UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, projectId, Constants.CMA_ANALYZED_STATUS_MSG, logger);
		updateProjQuery.execute();
		
//		JSONParser parser_cma   = new JSONParser();
//		JSONParser parser_trans = new JSONParser();
//		
//		BufferedReader bufferedReader = new BufferedReader(new FileReader(cma_path));
//		JSONObject cma  = (JSONObject) parser_cma.parse(bufferedReader);
//
//		JSONArray transactions = (JSONArray) parser_trans.parse(new FileReader(transactionsJSONFile.getAbsolutePath()));
//		
//		System.out.println("executing: analyze");
//		JSONArray output = analyze(cma, transactions);
//        
//        //Saving to disk 
//        try (Writer writer = new FileWriter(output_path)) {
//        	writer.write(output.toJSONString());
//        }
//        
//        System.out.println("executing: revise");
//        JSONObject revised_microservice = revise(cma, output);
//        
//        //Saving to disk 
//        try (Writer writer = new FileWriter(cma_revised_output_path)) {
//        	writer.write(revised_microservice.toJSONString());
//        }
	}

	private static void insertTransactionsToDB(String projectId, String transactionPath)
			throws IOException, ParseException, FileNotFoundException {
		JSONParser jsonParserTransaction = new JSONParser();
		JSONObject jsonobjectTransaction  = new JSONObject();
		JSONObject pathobjectTransaction  = new JSONObject();
		jsonobjectTransaction .put(Constants.TRANSACTIONS, jsonParserTransaction .parse(new FileReader(transactionPath)));
		pathobjectTransaction .put(Constants.TRANSACTIONS + "path", transactionPath);
		
		Document parametersTransaction = null;
		Overlays transactions = new Overlays(new ObjectId(projectId), Constants.TRANSACTIONS, parametersTransaction, Document.parse(jsonobjectTransaction .toString()), Document.parse(pathobjectTransaction.toString()));
		
		InsertIntoOverlaysQuery insertQueryOverlay = new InsertIntoOverlaysQuery(DatabaseConnection.getDatabase(), transactions, logger);
		insertQueryOverlay.execute();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "identifyMicroserviceCandidates", produces = "application/json")
	@ApiOperation(value = "Generate community based Partitions", notes = "", tags = "partition")
	public @ResponseBody JSONObject identifyMicroserviceCandidates(
			@RequestParam(value = "projectId", required = true) String projectId,
			@ApiParam(name = "callGraphFile", value = "callGraphFile", required = true)
			@RequestPart("callGraphFile") MultipartFile callGraphFile, 
			@ApiParam(name = "entryPointsJSON", value = "entryPointsJSON", required = true)
			@RequestPart("entryPointsJSON") MultipartFile entryPointsJSON,
			@ApiParam(name = "crudJSON", value = "crudJSON", required = true)
			@RequestPart("crudJSON") MultipartFile crudJSON,
			@ApiParam(name = "transactionsJSON", value = "transactionsJSON", required = true)
			@RequestPart("transactionsJSON") MultipartFile transactionsJSON) {

		logger.info("Inside identifyMicroserviceCandidates Candidates");
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");

		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}
		
		// Creating files for community detection
		String rootPath = proj.getProjectLocation();
		String type = proj.getCodeType();
		String tempFolder = rootPath + File.separator + "temp";
		String outputFileName = tempFolder + File.separator + "businessslices.json";
		String resultFolder = projectPath.getAbsolutePath() + File.separator + "ui" + File.separator + "data" + File.separator;		
		String tempStoreFolder = projectPath.getAbsolutePath() + File.separator + "temp";
		String icuPath = tempStoreFolder + File.separator + "inter_class_usage.json";
		String dbPath = tempStoreFolder  + File.separator +"db.json";
		new File(resultFolder).mkdirs();
		
		
		try {
			File callGraphDotFile = new File(tempFolder + File.separator + callGraphFile.getOriginalFilename());
			FileUtils.writeByteArrayToFile(callGraphDotFile, callGraphFile.getBytes());
			
			File entryPointsJSONFile = new File(tempFolder + File.separator + entryPointsJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(entryPointsJSONFile, entryPointsJSON.getBytes());
			
			File crudJSONFile = new File(tempFolder + File.separator + crudJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(crudJSONFile, crudJSON.getBytes());
			
			File transactionsJSONFile = new File(tempFolder + File.separator + transactionsJSON.getOriginalFilename());
			FileUtils.writeByteArrayToFile(transactionsJSONFile, transactionsJSON.getBytes());

			SlicingDriver.performSlicing(callGraphDotFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath(), icuPath, outputFileName, dbPath);
			
			//DDD processing start
			
			peformDDDAnalysis(projectId, db, projectPath, rootPath, type, tempFolder, callGraphDotFile,
					entryPointsJSONFile, crudJSONFile, transactionsJSONFile);
			
		} catch(Exception e) {
			e.printStackTrace();
			UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, projectId, Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjQuery.execute();
		}
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and user edits as single json. Based on the difference, re-trigger the whole analysis again.
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "triggerAnalysis", consumes="application/json", produces = "application/json")
	@ApiOperation(value = "Retrigger CMA Analysis", notes = "", tags = "partition")
	public @ResponseBody JSONObject triggerAnalysis(
			@RequestBody Microservice microservice){

		logger.info("Re TriggerAnalysis : {}", microservice.getProjectId());
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, microservice.getProjectId(), Constants.CMA_ANALYZING_STATUS_MSG, logger);
		updateProjQuery.execute();
		
        ObjectMapper Obj = new ObjectMapper(); 
		try {
			String inputJSON = Obj.writeValueAsString(microservice);
			String projectId = (String)microservice.getProjectId();
			System.out.println(inputJSON);
			JSONParser jsonParser = new JSONParser();
			JSONObject msJsonObj = (JSONObject)jsonParser.parse(inputJSON);
			msJsonObj.remove("projectName");
			msJsonObj.remove("projectId");
			
			// Writing user edited graph to server
			JSONObject jsonobject = new JSONObject();			
			JSONObject pathobjectUser = new JSONObject();
			
			jsonobject.put(Constants.CLUSTER_COMMUNITY, msJsonObj);
			pathobjectUser.put(Constants.CLUSTER_COMMUNITY+"path", "");
			Document parameters = null;
			Partition clusters = new Partition(new ObjectId(projectId), Constants.COMMUNITY_CLUSTERING, parameters,Document.parse(jsonobject.toString()), Document.parse(pathobjectUser.toString()), Constants.SOURCE_USER);
			InsertIntoPartitionHistoryQuery insertQueryUser = new InsertIntoPartitionHistoryQuery(db, clusters,logger);
			System.out.println("Edited Saved");
			insertQueryUser.execute();
			
		    //Code for trigger analysis
		    //Obtaining Project-ID for input files to code
		    SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
			try {
				sproj.execute();
			} catch (Exception e) {
				logger.error("Project id " + projectId + " is not registered");
				result.put("status", "Project not registerd");

			}
			Project proj = sproj.getResult();
			File projectPath = new File(proj.getProjectLocation());
			if (!projectPath.exists()) {
				logger.error("Project id " + projectId + "files not found");
				result.put("status", "Project files not found");
				return result;
			}
			
			// Creating files for community detection
			String rootPath = proj.getProjectLocation();
			String type = proj.getCodeType();
			String tempFolder = rootPath + File.separator + "temp";
			String resultFolder = projectPath.getAbsolutePath() + File.separator + "ui" + File.separator + "data" + File.separator;
			
//			String tempStoreFolder = projectPath.getAbsolutePath() + File.separator + "
			String communityClusterJSON = resultFolder + "graph_clustering.json";
			String viscommunityClusterJSON = resultFolder + "vis_graph_clustering.json";
			String entryPointFilePath = tempFolder + File.separator + "service.json";
			String graphEditPath = tempFolder + File.separator + "graph_edit.json";
			String editOutput = resultFolder + "graph_user_edit.json";
			
			String interclass = tempFolder + File.separator + Constants.INTER_CLASS_USAGE+".json";
			
			try (FileWriter file = new FileWriter(graphEditPath)) {//Writing edited cluster to temporary file for analysis
	            file.write(msJsonObj.toJSONString());
	            file.flush();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			APIUtilities.runTrigger(rootPath, communityClusterJSON, tempFolder,interclass, type, projectPath.getAbsolutePath(), viscommunityClusterJSON, entryPointFilePath, graphEditPath);
			logger.info("Trigger Analysis Complete");
			
			
			JSONParser jsonParserNewCommunity = new JSONParser();
			JSONObject jsonobjectNewCommunity = new JSONObject();
			JSONObject pathobject = new JSONObject();
			
			jsonobjectNewCommunity.put(Constants.CLUSTER_COMMUNITY, jsonParserNewCommunity.parse(new FileReader(editOutput)));
			
			pathobject.put(Constants.CLUSTER_COMMUNITY + "path", editOutput);
			
			Partition clustersNew= new Partition(new ObjectId(projectId), Constants.COMMUNITY_CLUSTERING, parameters,Document.parse(jsonobjectNewCommunity.toString()), Document.parse(pathobject.toString()), Constants.SOURCE_ALGO);
			InsertIntoPartitionHistoryQuery insertQuery = new InsertIntoPartitionHistoryQuery(DatabaseConnection.getDatabase(), clustersNew,logger);
			insertQuery.execute();
			logger.info("Processed after edit saved");
			
		    UpdatePartitionByProjectId updatePartitionsQuery = new UpdatePartitionByProjectId(db, microservice.getProjectId(), Document.parse(jsonobjectNewCommunity.toString()), Constants.SOURCE_ALGO, logger);
		    updatePartitionsQuery.execute();
			
		    updateProjQuery = new UpdateProjectStatusByProjectId(db, microservice.getProjectId(), Constants.CMA_ANALYZED_STATUS_MSG, logger);
			updateProjQuery.execute();
			
		    result.put("status", ProjectStatus.OK);	
		} catch (Exception e) {
			e.printStackTrace();
			result.put("status", ProjectStatus.INTERNAL_SERVER_ERROR);
			updateProjQuery = new UpdateProjectStatusByProjectId(db, microservice.getProjectId(), Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjQuery.execute();
		} 
		logger.info("Completed triggerAnalysis : {}", microservice.getProjectId());
		return result;
	}
	
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes the projectId and reverts to the optional checkpoint. If checkpoint is not provided, it reverts to the initial recommendations
	 * @param projectId
	 * @param checkPoint
	 * @return
	 */
	@RequestMapping(method=RequestMethod.GET,value="revertRecommendations", produces = "application/json")
	@ApiOperation(value = "Revert Analysis to previously saved checkpoint", notes = "", tags = "revert")
	public @ResponseBody JSONObject revertRecommendations(
			@RequestParam(value = "projectId", required = true) String projectId,	
			@RequestParam(value = "checkpoint", required = false) String checkPoint) {
		

		logger.info("RevertRecommendations : {}", projectId);
		JSONObject result = new JSONObject();
		
		MongoDatabase db = DatabaseConnection.getDatabase();
		SelectEarliestPartitionsFromHistoryByProjectId partitionProj = new SelectEarliestPartitionsFromHistoryByProjectId(db, new ObjectId(projectId), logger);
		partitionProj.execute();
		List<Partition> allpartition= null;
		try {
			if(partitionProj.getResultSize()>0) { //only one result is expected due to limit 1
				allpartition = partitionProj.getResult();
				Partition initialPartition = allpartition.get(0);
				
				UpdatePartitionByProjectId updatePartitionsQuery = new UpdatePartitionByProjectId(db, projectId, initialPartition.getPartitionResult(), Constants.SOURCE_ALGO, logger);
			    updatePartitionsQuery.execute();
			}
		result.put("status", ProjectStatus.OK);	
		result.put("message", "Recommendations reverted Successfully");	
		}
		catch(NullPointerException e) {
			result.put("status", ProjectStatus.INTERNAL_SERVER_ERROR);	
			result.put("status", "Unable to revert microservices recommendations.");
		} 
		
		return result;
		
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and returns the generated result. If the result is still in progress, it gives appropriate status code
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "microservices", produces = "application/json")
	@ApiOperation(value = "Get Microservice Candidates", notes = "", tags = "partitions")
	public @ResponseBody JSONObject getMicroservices(
			@RequestParam(value = "projectId", required = true) String projectId) {
		logger.info("getMicroservices : {}", projectId);
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
				if(partiton.getPartitionType().equals(Constants.COMMUNITY_CLUSTERING)) {
//					return partiton.getPartitionResult().get(Constants.clustercommunity));
//					result = (JSONOject) partiton.getPartitionResult().get(Constants.clustercommunity);
					result.put("Result", partiton.getPartitionResult().get(Constants.CLUSTER_COMMUNITY));
				}
			}
		}
		catch(NullPointerException e) {
			result.put("status", "Unable to fetch microservices.");
		} 
		
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and returns the status of the analysis. If the result is still in progress, it gives appropriate status code
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "microserviceStatus", produces = "application/json")
	@ApiOperation(value = "Get Microservice Analysis Status", notes = "", tags = "partitions")
	public @ResponseBody JSONObject microserviceStatus(
			@RequestParam(value = "projectId", required = true) String projectId) {
		logger.info("getMicroserviceStatus : {}", projectId);
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		SelectAllPartitionsByProjectId partitionProj = new SelectAllPartitionsByProjectId(db, new ObjectId(projectId), logger);
		partitionProj.execute();
		List<Partition> allpartition= null;
		JSONObject tempobj ;
		try {
			if(partitionProj.getResultSize()>0) {
				result.put("status", "Analyzed");
				result.put("message", "Application analyzed successfully");
			}else {
				result.put("status", "Analyzing");
				result.put("message", "Application is still being analyzed");
			}
		}
		catch(NullPointerException e) {
			result.put("status", "Failed");
			result.put("message", "Application analysis Failed. Please get in touch with the CMA administrator");
		} 
		
		return result;
	}
	
	/**
	 * This API is exposed for CMA integration.
	 * It returns all ms ids and their status. If the result is still in progress, it gives appropriate status code
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "microservicesStatus", produces = "application/json")
	@ApiOperation(value = "Get All Projects Microservices Analysis Status", notes = "", tags = "partitions")
	public @ResponseBody JSONArray microservicesStatus() {
		logger.info("Get All MicroservicesStatus ");
		JSONArray jsonArr = new JSONArray();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		SelectAllProjects partitionProj = new SelectAllProjects(db, logger);
		partitionProj.execute();
		try {
			if(partitionProj.getResult().size()>0) {
				Iterator<Project> iter = partitionProj.getResult().iterator();
				while(iter.hasNext()) {
					JSONObject result = new JSONObject();
					Project proj = iter.next();
					result.put("id", proj.get_id().toString());
					if(proj.getStatus() != null) {
						result.put("status", proj.getStatus());
						if(proj.getStatus().equals(Constants.CMA_ANALYZED_STATUS_MSG)) {
							result.put("message", "Application analyzed successfully");	
						}else if(proj.getStatus().equals(Constants.CMA_ANALYZING_STATUS_MSG)) {
							result.put("message", "Application analyzing is still ongoing");	
						}else {
							result.put("message", "Application analysis failed.Please contact CMA admin");	
						}
					}else { //just for now TODO : change 
						result.put("status", "Analyzed");
						result.put("message", "Application analyzed successfully");	
					}
					result.put("proj_name", proj.getProjectName());
					jsonArr.add(result);
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
	 * It takes JSON as an input that contains status, message including projectId to update the current status
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes="application/json", value = "microserviceStatus", produces = "application/json")
	@ApiOperation(value = "Post Microservices Analysis Status", notes = "", tags = "partitions")
	public @ResponseBody JSONObject updateMicroservicesStatus(@RequestBody Status status) {
		logger.info("POST Microservice Status For the projectId ");
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		JSONObject result = new JSONObject();
		UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, status.getProjectId(), status.getStatus(), logger);
		updateProjQuery.execute();
		result.put("status", ProjectStatus.OK); 
		logger.info("Completed post microservicesStatus : {}", status.getProjectId());
		return result;
	}
	
	
	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and updates the modified result.
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes="application/json", value = "microservices", produces = "application/json")
	@ApiOperation(value = "Update Microservice Candidates", notes = "", tags = "partitions")
	public @ResponseBody JSONObject updateMicroservices(
			@RequestBody Microservice microservice){
		logger.info("updateMicroservices : {}", microservice.getProjectId());
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();

        ObjectMapper Obj = new ObjectMapper(); 
        String jsonStr;
		try {
			jsonStr = Obj.writeValueAsString(microservice);
			System.out.println(jsonStr); 
			
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonobject = new JSONObject();
			
			JSONObject msJsonObj = (JSONObject)jsonParser.parse(jsonStr);
			msJsonObj.remove("projectId"); 
			msJsonObj.remove("projectName");
			
			jsonobject.put(Constants.CLUSTER_COMMUNITY, msJsonObj);
		
			
			JSONObject pathobject = new JSONObject();
			pathobject.put(Constants.CLUSTER_COMMUNITY + "path", "");
			
			Document parameters = null;
			Partition clusters = new Partition(new ObjectId(microservice.getProjectId()), Constants.COMMUNITY_CLUSTERING, parameters,Document.parse(jsonobject.toString()), Document.parse(pathobject.toString()), Constants.SOURCE_USER);
			InsertIntoPartitionHistoryQuery insertQuery = new InsertIntoPartitionHistoryQuery(DatabaseConnection.getDatabase(), clusters,logger);
			insertQuery.execute();
			
		    UpdatePartitionByProjectId updatePartitionsQuery = new UpdatePartitionByProjectId(db, microservice.getProjectId(), Document.parse(jsonobject.toString()), Constants.SOURCE_USER, logger);
		    updatePartitionsQuery.execute();
		    
		    result.put("status", ProjectStatus.OK);
		} catch (JsonProcessingException | ParseException e) {
			e.printStackTrace();
			result.put("status", ProjectStatus.INTERNAL_SERVER_ERROR);
			UpdateProjectStatusByProjectId updateProjQuery = new UpdateProjectStatusByProjectId(db, microservice.getProjectId(), Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjQuery.execute();
		} 
		logger.info("Completed updatingMicroservices : {}", microservice.getProjectId());
		return result;
	}
	

}