/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.research.msr.utils.Commons;
import com.ibm.research.appmod.slicing.SlicingDriver;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;


@CrossOrigin
@Controller
@RequestMapping("/msr/slicing/")
@Api(value = "Business Slicing", description = "Microservices Recommender APIs for Business Slicing of Application")
public class BusinessSliceService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(VisualizationService.class + "_swagger_log");


	public BusinessSliceService() {

	}

//	@RequestMapping(method=RequestMethod.POST, value="getSlicesForAllEntrypoints", produces = "application/json")
//	@ApiOperation(value = "Get the business slices for en entry pointsall giv", notes = "", tags = "slicing")
//	public @ResponseBody JSONObject getSlicesForAllEntrypoints(
//			@ApiParam(name = "callGraphFile", value = "callGraphFile", required = true)
//			@RequestPart("callGraphFile") MultipartFile callGraphFile, 
//			@ApiParam(name = "entryPointsJSON", value = "entryPointsJSON", required = true)
//			@RequestPart("entryPointsJSON") MultipartFile entryPointsJSON) {
//
//		logger.info("Running to get all program slices");
//		JSONObject result = new JSONObject();
//		
//		String appModBaseDir = Commons.getMSRBaseDir();
//		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "slicing";
//
//		try {
//			File callGraphDotFile = new File(dirPath + File.separator + callGraphFile.getOriginalFilename());
//			FileUtils.writeByteArrayToFile(callGraphDotFile, callGraphFile.getBytes());
//			
//			File entryPointsJSONFile = new File(dirPath + File.separator + entryPointsJSON.getOriginalFilename());
//			FileUtils.writeByteArrayToFile(entryPointsJSONFile, entryPointsJSON.getBytes());
//			
//			String outputFileName = dirPath + File.separator + "businessslices.json";
//			
//			SlicingDriver.performSlicing(callGraphDotFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath(), outputFileName);
//			
//			result = (JSONObject)(new JSONParser().parse(new FileReader(outputFileName)));
//	
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		return result;
//	}
//	
//	@RequestMapping(method=RequestMethod.POST, value="getSlicesForQuery", produces = "application/json")
//	@ApiOperation(value = "Get the business slices related to the given user query", notes = "", tags = "slicing")
//	public @ResponseBody JSONObject getSlicesForQuery(
//			@RequestParam(value = "query", required = true) String query,
//			@ApiParam(name = "callGraphFile", value = "callGraphFile", required = true)
//			@RequestPart("callGraphFile") MultipartFile callGraphFile, 
//			@ApiParam(name = "entryPointsJSON", value = "entryPointsJSON", required = true)
//			@RequestPart("entryPointsJSON") MultipartFile entryPointsJSON) {
//
//		logger.info("Running to get program slices for user query:");
//		JSONObject result = new JSONObject();
//		
//		String appModBaseDir = Commons.getMSRBaseDir();
//		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "slicing";
//
//		try {
//			File callGraphDotFile = new File(dirPath + File.separator + callGraphFile.getOriginalFilename());
//			FileUtils.writeByteArrayToFile(callGraphDotFile, callGraphFile.getBytes());
//			
//			File entryPointsJSONFile = new File(dirPath + File.separator + entryPointsJSON.getOriginalFilename());
//			FileUtils.writeByteArrayToFile(entryPointsJSONFile, entryPointsJSON.getBytes());
//			
//			String outputFileName = dirPath + File.separator + "businessslices.json";
//			
//			SlicingDriver.performQueryBasedSlicing(callGraphDotFile.getAbsolutePath(), entryPointsJSONFile.getAbsolutePath(), query, outputFileName);
//			
//			result = (JSONObject)(new JSONParser().parse(new FileReader(outputFileName)));
//	
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//		
//		return result;
//	}
}
