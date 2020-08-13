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
import com.ibm.research.msr.db.dto.Overlays;
import com.ibm.research.msr.db.queries.overlays.SelectAllOverlaysByProjectId;
import com.ibm.research.msr.utils.Constants;
import com.mongodb.client.MongoDatabase;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@CrossOrigin
@Controller
@RequestMapping("/msr/data/")
@Api(value = "Overlays", description = "Transaction Details of the Application")
public class OverlayService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(OverlayService.class + "_swagger_log");


	public OverlayService() {

	}

	/**
	 * This API is exposed for CMA integration.
	 * It takes projectId as input and returns the transaction details of the monolith applciation. If the result is still in progress, it gives appropriate status code
	 * @param projectId
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "transactions", produces = "application/json")
	@ApiOperation(value = "Get Transaction Details", notes = "", tags = "transactions")
	public @ResponseBody JSONObject getMicroservices(
			@RequestParam(value = "projectId", required = true) String projectId) {
		logger.info("getTransactions : {}", projectId);
		JSONObject result = new JSONObject();
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		SelectAllOverlaysByProjectId transactionProj = new SelectAllOverlaysByProjectId(db, new ObjectId(projectId), logger);
		transactionProj.execute();
		List<Overlays> allTransactions= null;
		JSONObject tempobj ;
		try {
			if(transactionProj.getResultSize()>0) {
				allTransactions = transactionProj.getResult();
				for(Overlays partiton:allTransactions) {
//					result = (JSONObject) partiton.getTransactionResult().get(Constants.TRANSACTIONS);
					result.put("Result", partiton.getTransactionResult().get(Constants.TRANSACTIONS));	
				}
			}else{
				result.put("Result", new JSONArray());
				result.put("status", "No traansaction Data Available.");
			}
			
			
		}
		catch(NullPointerException e) {
			result.put("status", "Unable to fetch transactions.");
		} 
		
		return result;
	}

}