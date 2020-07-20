///*******************************************************************************
//* Licensed Materials - Property of IBM
//* (c) Copyright IBM Corporation 2020. All Rights Reserved.
//*
//* Note to U.S. Government Users Restricted Rights:
//* Use, duplication or disclosure restricted by GSA ADP Schedule
//* Contract with IBM Corp.
//*******************************************************************************/
package com.ibm.research.msr.api;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
//import org.apache.poi.xwpf.usermodel.Borders;
//import org.apache.poi.xwpf.usermodel.BreakType;
//import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFFooter;
//import org.apache.poi.xwpf.usermodel.XWPFParagraph;
//import org.apache.poi.xwpf.usermodel.XWPFRun;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.ibm.research.appmod.msr.model.Clusters;
//import com.ibm.research.appmod.msr.model.Microservice;
//import com.ibm.research.appmod.msr.model.Nodes;
//import com.ibm.research.msr.utils.Constants;
//import com.wordnik.swagger.annotations.Api;
//import com.wordnik.swagger.annotations.ApiOperation;
//
//
//@CrossOrigin
//@Controller
//@RequestMapping("/msr/report/")
//@Api(value = "Reports", description = "Candidate Microservice Recommendations Reports")
public class ReportService {
//
//	/** Logger. */
//	private static final Logger logger = LoggerFactory.getLogger(ReportService.class + "_swagger_log");
//
//	static String TOPIC_FONT_COLOR = "25467a";
//	static String CONTENT_FONT_COLOR = "000000";
//	
//	static int TOPIC_FONT_SIZE =  17;
//	static int CONTENT_FONT_SIZE = 13;
//	
//	public ReportService() {
//
//	}
//
//	/**
//	 * This API is exposed for CMA integration.
//	 * It takes projectId as input and returns the analyzed application summary report. If the result is still in progress, it gives appropriate status code
//	 * @param projectId
//	 * @return
//	 */
////	@RequestMapping(method = RequestMethod.GET, value = "summary", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
////	@ApiOperation(value = "Get Summary Report", notes = "", tags = "summary")
////	public @ResponseBody ResponseEntity<InputStreamResource> summaryReport(
////			@RequestParam(value = "projectId", required = true) String projectId) {
////		logger.info("summaryReport : {}", projectId);
////		JSONObject result = new JSONObject();
////		MongoDatabase db = DatabaseConnection.getDatabase();
////		
////		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
////		try {
////			sproj.execute();
////		
////			Project proj = sproj.getResult();
////			String rootPath = proj.getProjectLocation();
////			String tempFolder = rootPath + File.separator + "temp";
////			String resultFolder = rootPath + File.separator + "ui" + File.separator + "data" + File.separator;	
////			
////			String appModBaseDir = Commons.getMSRBaseDir();
////			String sourceDoc = appModBaseDir + "template.docx";
////			String destDir = tempFolder + File.separator;
////			FileUtils.copyFileToDirectory(new File(sourceDoc), new File(destDir));
////			
////			String cma_path = tempFolder + File.separator + "graph_clustering.json";
////			String trans_path = resultFolder + "transaction.json";
////			    
////		    Map<String, Nodes> nodeid2Obj = new HashMap<String, Nodes>();
////		    List<String> tables = new ArrayList<String>();
////		    int totalNumNodes = 0; //total number of nodes
////		    
////		    Microservice ms = new Microservice();
////		    ms.setProjectName(proj.getProjectName());
////		    ms.setProjectId(projectId);
////		    
////		    DocxStamper stamper = new DocxStamper(new DocxStamperConfiguration());
////	        DocxContext context = new DocxContext();
////	        context.setAppName(proj.getProjectName());
////		    
////		    JSONParser parser_cma   = new JSONParser();
////			
////			BufferedReader bufferedReader = new BufferedReader(new FileReader(cma_path));
////			JSONObject microservice  = (JSONObject) parser_cma.parse(bufferedReader);
////			
////			JSONArray nodes = (JSONArray) microservice.get("nodes");
////			totalNumNodes = nodes.size();
////	        JSONArray clusters = (JSONArray) microservice.get("clusters");
////	        Nodes[] nodesArr = new Nodes[nodes.size()];
////	       
////	        Map<String, Clusters> functionalClusters = new HashMap<String, Clusters>();
////	        Clusters unreachableClusters = new Clusters();
////	        Clusters utilityClusters = new Clusters();
////	        Clusters refactorClusters = new Clusters();
////	        Clusters unassignedClusters = new Clusters();
////	        
////		    for(int i= 0; i < totalNumNodes; i++) {
////		    	Nodes nodeElement = new Nodes();
////		    	JSONObject node = (JSONObject) nodes.get(i);
////		    	String id = node.get("id").toString();
////		    	String label = node.get("label").toString();
////		    	String type = node.get("entity_type").toString();
////		    	nodeElement.setEntity_type(type);
////		    	if(type.equals("table")) {
////		    		tables.add(label);
////		    	}
////		    	nodeElement.setId(id);
////		    	nodeElement.setLabel(label);
////		    	nodeid2Obj.put(id, nodeElement);
////		    	nodesArr[i] = nodeElement;
////		    }
////		    
////		    for(int i= 0; i < clusters.size(); i++) {
////		    
////		    	JSONObject cluster = (JSONObject) clusters.get(i);
////		    	String clusterId = cluster.get("id").toString();
////		    	String clusterLabel = cluster.get("label").toString();
////		    	String clusterType = cluster.get("type").toString();
////		    	
////		    	JSONArray nodesStrArr = (JSONArray)cluster.get("nodes");
////		    	String[] nodeNames = new String[nodesStrArr.size()];
////		    	for (int j = 0; j < nodesStrArr.size(); j++) {
////		    		nodeNames[j] = nodeid2Obj.get(nodesStrArr.get(j)).getLabel();
////				}
////		    	
////		    	JSONArray transactionsIdArr = (JSONArray)cluster.get("transactions");
////		    	String[] transactionsIds = null;
////		    	if(null != transactionsIdArr) {
////		    	transactionsIds = new String[transactionsIdArr.size()];
////			    	for (int j = 0; j < transactionsIdArr.size(); j++) {
////			    		transactionsIds[j] = (String)transactionsIdArr.get(j);
////					}
////		    	}
////		    	
////		    	switch(clusterType) 
////		        { 
////		            case Constants.FUNCTIONAL_GROUP:
////		            	Clusters clusterElement = new Clusters();
////		            	clusterElement.setId(clusterId);
////				    	clusterElement.setLabel(clusterLabel);
////				    	clusterElement.setType(clusterType);
////				    	clusterElement.setNodes(nodeNames);
////				    	clusterElement.setTransactions(transactionsIds);
////		            	functionalClusters.put(clusterLabel, clusterElement);
////		                break; 
////		            case Constants.UTILITY_GROUP: 
////		            	utilityClusters.setId(clusterId);
////		            	utilityClusters.setLabel(clusterLabel);
////		            	utilityClusters.setType(clusterType);
////		            	utilityClusters.setNodes(nodeNames);
////		            	utilityClusters.setTransactions(transactionsIds);
////		                break; 
////		            case Constants.UNREACHABLE_GROUP: 
////		            	unreachableClusters.setId(clusterId);
////		            	unreachableClusters.setLabel(clusterLabel);
////		            	unreachableClusters.setType(clusterType);
////		            	unreachableClusters.setNodes(nodeNames);
////		            	unreachableClusters.setTransactions(transactionsIds);
////		                break; 
////		            case Constants.REFACTOR_GROUP: 
////		            	refactorClusters.setId(clusterId);
////		            	refactorClusters.setLabel(clusterLabel);
////		            	refactorClusters.setType(clusterType);
////		            	refactorClusters.setNodes(nodeNames);
////		            	refactorClusters.setTransactions(transactionsIds); 
////		                break;  
////		            case Constants.UNASSIGNED_GROUP: 
////		            	unassignedClusters.setId(clusterId);
////		            	unassignedClusters.setLabel(clusterLabel);
////		            	unassignedClusters.setType(clusterType);
////		            	unassignedClusters.setNodes(nodeNames);
////		                break;      
////		        } 
////		    }
////		    
////		    
////	        System.out.println("Start of Report");
////	        StringBuffer contentStringBuffer = new StringBuffer();
////	        Iterator<String> fnClusterKeysIter = functionalClusters.keySet().iterator(); 
////	        while (fnClusterKeysIter.hasNext()) {
////				String clusterKey =  fnClusterKeysIter.next();	
////				Clusters cs = functionalClusters.get(clusterKey);
////				if(cs.getNodes() != null) {
////					contentStringBuffer.append(clusterKey+" ("+(int) Math.ceil(totalNumNodes/cs.getNodes().length)+"):\n");
////					if(cs.getTransactions() != null) {
////						contentStringBuffer.append("Supporting the following key transactions: ("+cs.getTransactions().length+")");
////						contentStringBuffer.append(String.join(",", cs.getTransactions()));
////					}
////					contentStringBuffer.append("List Of Entities ("+cs.getNodes().length+") : ");
////					contentStringBuffer.append(String.join(",", cs.getNodes()));
////				}
////				contentStringBuffer.append("\n");
////			}
////	        context.setFuncMSDetails(contentStringBuffer.toString());
////			
////	        contentStringBuffer = new StringBuffer();
////	    	if(utilityClusters.getNodes() != null) {
////		        contentStringBuffer.append("Utility Cluster ("+(int) Math.ceil(totalNumNodes/utilityClusters.getNodes().length)+"):\n");
////		        contentStringBuffer.append("The following entities are not needed in one or ore microservices. Decision needs to be taken either to duplicate or package them as jar in the dependent microservices. ");
////		        if(cs.getTransactions() != null && cs.getTransactions().length > 0) {
////					contentStringBuffer.append("Supports the following key transactions: ("+utilityClusters.getTransactions().length+")");
////					contentStringBuffer.append(String.join(",", utilityClusters.getTransactions()));
////		        }
////				contentStringBuffer.append("List Of Entities :("+utilityClusters.getNodes().length+")");
////				contentStringBuffer.append(String.join(",", utilityClusters.getNodes()));
////	    	}
////			contentStringBuffer.append("\n");
////			context.setUtilityClustersDetails(contentStringBuffer.toString());
////			
////			contentStringBuffer = new StringBuffer();
////			if(refactorClusters.getNodes() != null) {
////		        contentStringBuffer.append("Refactor Cluster ("+(int) Math.ceil(totalNumNodes/refactorClusters.getNodes().length)+"):\n");
////		        contentStringBuffer.append("The following entities needs to be refactored to have better independent microservices. ");
////		        if(refactorClusters.getTransactions() != null) {
////					contentStringBuffer.append("Supports the following key transactions: ("+refactorClusters.getTransactions().length+")");
////					contentStringBuffer.append(String.join(",", refactorClusters.getTransactions()));
////		        }
////				contentStringBuffer.append("List Of Entities :("+refactorClusters.getNodes().length+")");
////				contentStringBuffer.append(String.join(",", refactorClusters.getNodes()));
////			}
////			contentStringBuffer.append("\n");
////			context.setRefactorClustersDetails(contentStringBuffer.toString());
////			
////			contentStringBuffer = new StringBuffer();
////			if(unassignedClusters.getNodes() != null) {
////		        contentStringBuffer.append("Unassigned Cluster ("+(int) Math.ceil(totalNumNodes/unassignedClusters.getNodes().length)+"):\n");
////		        contentStringBuffer.append("The following entities needs deeper inspection and discussion for refactoring and assignment to the rightful microservice. ");
////		        if(unassignedClusters.getTransactions() != null) {
////					contentStringBuffer.append("Supports the following key transactions: ("+unassignedClusters.getTransactions().length+")");
////					contentStringBuffer.append(String.join(",", unassignedClusters.getTransactions()));
////		        }
////				contentStringBuffer.append("List Of Entities :("+unassignedClusters.getNodes().length+")");
////				contentStringBuffer.append(String.join(",", unassignedClusters.getNodes()));
////			}
////			contentStringBuffer.append("\n");
////			context.setUnassignedClustersDetails(contentStringBuffer.toString());
////			
////			contentStringBuffer = new StringBuffer();
////			if(unreachableClusters.getNodes() != null) {
////		        contentStringBuffer.append("Unreachable Cluster ("+(int) Math.ceil(totalNumNodes/unreachableClusters.getNodes().length)+"):\n");
////		        contentStringBuffer.append("The following entities are not part of any transactions that leads to a database operation. Few of them could be dead code and warrants removal. ");
////				contentStringBuffer.append("List Of Entities :("+unreachableClusters.getNodes().length+")");
////				contentStringBuffer.append(String.join(",", unreachableClusters.getNodes()));
////			}
////			contentStringBuffer.append("\n");
////			context.setUnreachableClustersDetails(contentStringBuffer.toString());
////			
////			context.setTables(String.join(",", tables));
////	        
////	        InputStream template = new FileInputStream(new File(sourceDoc));
////	        OutputStream out = new FileOutputStream(destDir+"summary_report.docx");
////	        stamper.stamp(template, context, out);
////	        out.close();
////	        
////	        File file = new File(destDir+"summary_report.docx");
////	        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
////	        HttpHeaders headers = new HttpHeaders();
////	        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
////	        return ResponseEntity.ok()
////	        	            .headers(headers)
////	        	            .contentLength(file.length())
////	        	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
////	        	            .body(resource);
////		} catch (Exception e) {
////			logger.error("Project id " + projectId + " is not registered");
////			result.put("status", "Project not registerd");
////
////		}
////		
//////		File file = new File("/Users/srikanth/Desktop/hybrid-cloud/temp/output_template.docx");
//////        InputStreamResource resource;
//////		try {
//////			resource = new InputStreamResource(new FileInputStream(file));
//////			 HttpHeaders headers = new HttpHeaders();
//////		        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
//////		        return ResponseEntity.ok()
//////		        	            .headers(headers)
//////		        	            .contentLength(file.length())
//////		        	            .contentType(MediaType.APPLICATION_OCTET_STREAM)
//////		        	            .body(resource);
//////		} catch (FileNotFoundException e) {
//////			// TODO Auto-generated catch block
//////			e.printStackTrace();
//////		}
////       
////		return null;
////	}
////	
//	/**
//	 * This API is exposed for CMA integration.
//	 * It takes projectId as input and returns the analyzed application summary report. If the result is still in progress, it gives appropriate status code
//	 * @param projectId
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.GET, value = "action", produces = "application/json")
//	@ApiOperation(value = "Get Action Report", notes = "", tags = "action")
//	public @ResponseBody JSONObject actionReport(
//			@RequestParam(value = "projectId", required = true) String projectId) {
//		logger.info("getActionyReport : {}", projectId);
//		JSONObject result = new JSONObject();
////		MongoDatabase db = DatabaseConnection.getDatabase();
////		
////		SelectAllOverlaysByProjectId transactionProj = new SelectAllOverlaysByProjectId(db, new ObjectId(projectId), logger);
////		transactionProj.execute();
////		List<Overlays> allTransactions= null;
////		JSONObject tempobj ;
////		try {
////			if(transactionProj.getResultSize()>0) {
////				allTransactions = transactionProj.getResult();
////			}
////			
////			for(Overlays partiton:allTransactions) {
//////				result = (JSONObject) partiton.getTransactionResult().get(Constants.TRANSACTIONS);
////				result.put("Result", partiton.getTransactionResult().get(Constants.TRANSACTIONS));	
////			}
////		}
////		catch(NullPointerException e) {
////			result.put("status", "Unable to fetch transactiosn.");
////		} 
//		
//		return result;
//	}
//	
//	static XWPFRun createParagraph(XWPFDocument doc, XWPFParagraph paragraph, String text, int size, String colour, boolean bold){
//	 	  XWPFRun run=paragraph.createRun(); 
//	 	  run.setText(text);
//	 	  run.setBold(bold);
//	 	  run.setFontSize(size);
//	 	  run.setColor(colour);
//	 	  
//	 	  return run;
//	 }
//	
//	 public static void main(String[] args) throws IOException, ParseException {
//		    String cma_path = "/Users/srikanth/Desktop/hybrid-cloud/temp/ms.json";
//		    String trans_path = "/Users/srikanth/Desktop/hybrid-cloud/temp/tr.json";
//		    
//		    Map<String, Nodes> nodeid2Obj = new HashMap<String, Nodes>();
//		    List<String> tables = new ArrayList<String>();
//		    int totalNumNodes = 0; //total number of nodes
//		    
//		    Microservice ms = new Microservice();
//		    ms.setProjectName("DayTrader");
//		    ms.setProjectId("123");
//		    
//		  //Blank Document
//	      XWPFDocument doc = new XWPFDocument(); 
//	      XWPFParagraph paragraph = doc.createParagraph();
//	      XWPFRun run = paragraph.createRun(); 
//	      
//	      CTSectPr sectPr = doc.getDocument().getBody().addNewSectPr();
//		  XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(doc, sectPr);
//		  
//		  //create header
////		  XWPFHeader header = headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT);		  
////		  XWPFParagraph paragraph = doc.createParagraph();
////		  paragraph=header.createParagraph();
////		  paragraph.setAlignment(ParagraphAlignment.LEFT);
////		  XWPFRun run = paragraph.createRun(); 
////		  run.setText("Header");	  
////		  CTTabStop tabStop = paragraph.getCTP().getPPr().addNewTabs().addNewTab();
////		  tabStop.setVal(STTabJc.LEFT);
////		  int twipsPerInch =  1440;
////		  tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch));
////		  run = paragraph.createRun();
////		  paragraph.setAlignment(ParagraphAlignment.LEFT);
////		  tabStop.setVal(STTabJc.LEFT);
////		  int twipsPerInch1 =  1440;
////		  tabStop.setPos(BigInteger.valueOf(6 * twipsPerInch1));
////		  run = paragraph.createRun(); 
//		  
//		  
//		 // create footer
//		  XWPFFooter footer = headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT);
//		  paragraph = footer.createParagraph();
//		  if (paragraph == null) 
//			  paragraph = footer.createParagraph();
//		  		 
//		  run = paragraph.createRun(); 
//		  paragraph.setAlignment(ParagraphAlignment.CENTER);
//		  run.setText("Page ");
//		  paragraph.getCTP().addNewFldSimple().setInstr("PAGE \\* MERGEFORMAT");
//		  run = paragraph.createRun();  
//		  run.setText(" of ");
//		  paragraph.getCTP().addNewFldSimple().setInstr("NUMPAGES \\* MERGEFORMAT");
////		  run = paragraph.createRun(); 
////		  run.setText("IBM Confidential");
//		  
//	      //Write the Document in file system
//	      FileOutputStream out = new FileOutputStream(new File("/Users/srikanth/Desktop/hybrid-cloud/temp/createparagraph.docx"));
//	        
////	      //create Body
//	      paragraph = doc.createParagraph();
//	      paragraph.setSpacingBeforeLines(1000);
//	      paragraph.setBorderBottom(Borders.SINGLE);
//	      paragraph.setAlignment(ParagraphAlignment.CENTER);
//	  	  run=createParagraph(doc, paragraph, "Summary Report", 30, "25467a",true);
////	  	  paragraph = doc.createParagraph();
////		  paragraph.setBorderTop(Borders.SINGLE);
//		  paragraph = doc.createParagraph();
//		  run=paragraph.createRun();
//	  	  run.addBreak(BreakType.PAGE);
//	      
//	  	  
//		    JSONParser parser_cma   = new JSONParser();
//			
//			BufferedReader bufferedReader = new BufferedReader(new FileReader(cma_path));
//			JSONObject microservice  = (JSONObject) parser_cma.parse(bufferedReader);
//			
//			JSONArray nodes = (JSONArray) microservice.get("nodes");
//			totalNumNodes = nodes.size();
//	        JSONArray clusters = (JSONArray) microservice.get("clusters");
//	        Nodes[] nodesArr = new Nodes[nodes.size()];
//	       
//	        Map<String, Clusters> functionalClusters = new HashMap<String, Clusters>();
//	        Clusters unreachableClusters = new Clusters();
//	        Clusters utilityClusters = new Clusters();
//	        Clusters refactorClusters = new Clusters();
//	        Clusters unassignedClusters = new Clusters();
//	        
//		    for(int i= 0; i < totalNumNodes; i++) {
//		    	Nodes nodeElement = new Nodes();
//		    	JSONObject node = (JSONObject) nodes.get(i);
//		    	String id = node.get("id").toString();
//		    	String label = node.get("label").toString();
//		    	String type = node.get("entity_type").toString();
//		    	nodeElement.setEntity_type(type);
//		    	if(type.equals("table")) {
//		    		tables.add(label);
//		    	}
//		    	nodeElement.setId(id);
//		    	nodeElement.setLabel(label);
//		    	nodeid2Obj.put(id, nodeElement);
//		    	nodesArr[i] = nodeElement;
//		    }
//		    
//		    for(int i= 0; i < clusters.size(); i++) {
//		    
//		    	JSONObject cluster = (JSONObject) clusters.get(i);
//		    	String clusterId = cluster.get("id").toString();
//		    	String clusterLabel = cluster.get("label").toString();
//		    	String clusterType = cluster.get("type").toString();
//		    	
//		    	JSONArray nodesStrArr = (JSONArray)cluster.get("nodes");
//		    	String[] nodeNames = new String[nodesStrArr.size()];
//		    	for (int j = 0; j < nodesStrArr.size(); j++) {
//		    		nodeNames[j] = nodeid2Obj.get(nodesStrArr.get(j)).getLabel();
//				}
//		    	
//		    	JSONArray transactionsIdArr = (JSONArray)cluster.get("transactions");
//		    	String[] transactionsIds = null;
//		    	if(null != transactionsIdArr) {
//		    	transactionsIds = new String[transactionsIdArr.size()];
//			    	for (int j = 0; j < transactionsIdArr.size(); j++) {
//			    		transactionsIds[j] = (String)transactionsIdArr.get(j);
//					}
//		    	}
//		    	
//		    	switch(clusterType) 
//		        { 
//		            case Constants.FUNCTIONAL_GROUP:
//		            	Clusters clusterElement = new Clusters();
//		            	clusterElement.setId(clusterId);
//				    	clusterElement.setLabel(clusterLabel);
//				    	clusterElement.setType(clusterType);
//				    	clusterElement.setNodes(nodeNames);
//				    	clusterElement.setTransactions(transactionsIds);
//		            	functionalClusters.put(clusterLabel, clusterElement);
//		                break; 
//		            case Constants.UTILITY_GROUP: 
//		            	utilityClusters.setId(clusterId);
//		            	utilityClusters.setLabel(clusterLabel);
//		            	utilityClusters.setType(clusterType);
//		            	utilityClusters.setNodes(nodeNames);
//		            	utilityClusters.setTransactions(transactionsIds);
//		                break; 
//		            case Constants.UNREACHABLE_GROUP: 
//		            	unreachableClusters.setId(clusterId);
//		            	unreachableClusters.setLabel(clusterLabel);
//		            	unreachableClusters.setType(clusterType);
//		            	unreachableClusters.setNodes(nodeNames);
//		            	unreachableClusters.setTransactions(transactionsIds);
//		                break; 
//		            case Constants.REFACTOR_GROUP: 
//		            	refactorClusters.setId(clusterId);
//		            	refactorClusters.setLabel(clusterLabel);
//		            	refactorClusters.setType(clusterType);
//		            	refactorClusters.setNodes(nodeNames);
//		            	refactorClusters.setTransactions(transactionsIds); 
//		                break;  
//		            case Constants.UNASSIGNED_GROUP: 
//		            	unassignedClusters.setId(clusterId);
//		            	unassignedClusters.setLabel(clusterLabel);
//		            	unassignedClusters.setType(clusterType);
//		            	unassignedClusters.setNodes(nodeNames);
//		                break;      
//		        } 
//		    }
//		    
//		    
//	        System.out.println("Start of Report");
//	        StringBuffer contentStringBuffer = new StringBuffer();
//	        Iterator<String> fnClusterKeysIter = functionalClusters.keySet().iterator(); 
//	        while (fnClusterKeysIter.hasNext()) {
//	        	
//				String clusterKey =  fnClusterKeysIter.next();	
//				Clusters cs = functionalClusters.get(clusterKey);
//				
//				paragraph=doc.createParagraph();
//		   		run=paragraph.createRun();
//		   		run=createParagraph(doc, paragraph, clusterKey+" ("+(int) Math.ceil(totalNumNodes/cs.getNodes().length)+"):", 17, "25467a", true);
//		   		
//				if(cs.getTransactions() != null && cs.getTransactions().length > 0) {
//					paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, "Supporting the following key transactions ("+cs.getTransactions().length+") : ", 13, "25467a", false);
//			   		
//			   		paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, String.join(",", cs.getTransactions()), 13, "000000", false);
//			   		contentStringBuffer.append(String.join(",", cs.getTransactions()));
//				}
//				
//				paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, "List Of Entities ("+cs.getNodes().length+") : ", 13, "25467a", false);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, String.join(",", cs.getNodes()), 13, "000000", false);
//		   		contentStringBuffer.append(String.join(",", cs.getTransactions()));
//		   		
//			}
//			
//	        contentStringBuffer = new StringBuffer();
//	    	if(utilityClusters.getNodes() != null) {
//	    		paragraph=doc.createParagraph();
//		   		run=paragraph.createRun();
//		   		run=createParagraph(doc, paragraph, "Utility Cluster ("+(int) Math.ceil(totalNumNodes/utilityClusters.getNodes().length)+"):", 17, "25467a", true);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		paragraph.setIndentationFirstLine(3000);
//		   		run=createParagraph(doc, paragraph, "The following entities are not needed in one or ore microservices. Decision needs to be taken either to duplicate or package them as jar in the dependent microservices. ", 13, "000000", false);
//		   		run.setItalic(true);
//		   		
//		        if(utilityClusters.getTransactions() != null && utilityClusters.getTransactions().length > 0) {
//		        	paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, "Supporting the following key transactions ("+utilityClusters.getTransactions().length+") : ", 13, "25467a", false);
//			   		
//			   		paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, String.join(",", utilityClusters.getTransactions()), 13, "000000", false);
//			   		contentStringBuffer.append(String.join(",", utilityClusters.getTransactions()));
//		        }
//		        paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, "List Of Entities ("+utilityClusters.getNodes().length+") : ", 13, "25467a", false);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, String.join(",", utilityClusters.getNodes()), 13, "000000", false);
//	    	}
//			
//			contentStringBuffer = new StringBuffer();
//			if(refactorClusters.getNodes() != null) {
//					paragraph=doc.createParagraph();
//			   		run=paragraph.createRun();
//			   		run=createParagraph(doc, paragraph, "Refactor Cluster ("+(int) Math.ceil(totalNumNodes/refactorClusters.getNodes().length)+"):", 17, "25467a", true);
//			   		
//			   		paragraph=doc.createParagraph();
//			   		paragraph.setIndentationFirstLine(3000);
//			   		run=createParagraph(doc, paragraph, "The following entities needs to be refactored to have better independent microservices. ", 13, "000000", false);
//			   		run.setItalic(true);
//			   		
//			   		if(refactorClusters.getTransactions() != null && refactorClusters.getTransactions().length > 0) {
//			        	paragraph=doc.createParagraph();
//				   		run=createParagraph(doc, paragraph, "Supporting the following key transactions ("+refactorClusters.getTransactions().length+") : ", 13, "25467a", false);
//				   		
//				   		paragraph=doc.createParagraph();
//				   		run=createParagraph(doc, paragraph, String.join(",", refactorClusters.getTransactions()), 13, "000000", false);
//				   		contentStringBuffer.append(String.join(",", refactorClusters.getTransactions()));
//			        }
//			        paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, "List Of Entities ("+refactorClusters.getNodes().length+") : ", 13, "25467a", false);
//			   		
//			   		paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, String.join(",", refactorClusters.getNodes()), 13, "000000", false);
//			}
//			
//			contentStringBuffer = new StringBuffer();
//			if(unassignedClusters.getNodes() != null) {
//				paragraph=doc.createParagraph();
//		   		run=paragraph.createRun();
//		   		run=createParagraph(doc, paragraph, "Unassigned Cluster ("+(int) Math.ceil(totalNumNodes/unassignedClusters.getNodes().length)+"):", 17, "25467a", true);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		paragraph.setIndentationFirstLine(3000);
//		   		run=createParagraph(doc, paragraph, "The following entities needs deeper inspection and discussion for refactoring and assignment to the rightful microservice. ", 13, "000000", false);
//		   		run.setItalic(true);
//		   		
//		   		if(unassignedClusters.getTransactions() != null && unassignedClusters.getTransactions().length > 0) {
//		        	paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, "Supporting the following key transactions ("+unassignedClusters.getTransactions().length+") : ", 13, "25467a", false);
//
//			   		paragraph=doc.createParagraph();
//			   		run=createParagraph(doc, paragraph, String.join(",", unassignedClusters.getTransactions()), 13, "000000", false);
//			   		contentStringBuffer.append(String.join(",", unassignedClusters.getTransactions()));
//		        }
//		        paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, "List Of Entities ("+unassignedClusters.getNodes().length+") : ", 13, "25467a", false);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, String.join(",", unassignedClusters.getNodes()), 13, "000000", false);
//		   		
//			}
//			contentStringBuffer.append("\n");
//			
//			contentStringBuffer = new StringBuffer();
//			if(unreachableClusters.getNodes() != null) {
//				paragraph=doc.createParagraph();
//		   		run=paragraph.createRun();
//		   		run=createParagraph(doc, paragraph, "Unreachable Cluster ("+(int) Math.ceil(totalNumNodes/unreachableClusters.getNodes().length)+"):", 17, "25467a", true);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		paragraph.setIndentationFirstLine(3000);
//		   		run=createParagraph(doc, paragraph, "The following entities are not part of any transactions that leads to a database operation. Few of them could be dead code and warrants removal. ", 13, "000000", false);
//		   		run.setItalic(true);
//		   		
//		        paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, "List Of Entities ("+unreachableClusters.getNodes().length+") : ", 13, "25467a", false);
//		   		
//		   		paragraph=doc.createParagraph();
//		   		run=createParagraph(doc, paragraph, String.join(",", unreachableClusters.getNodes()), 13, "000000", false);
//			}
//			
//			paragraph=doc.createParagraph();
//	   		run=paragraph.createRun();
//	   		run=createParagraph(doc, paragraph, "List of tables used in the application", 17, "25467a", true);
//	   		paragraph=doc.createParagraph();
//	   		run=createParagraph(doc, paragraph, String.join(",", tables), 13, "000000", false);
//
//	   		JSONParser parser_trans = new JSONParser();
//			// as it is a list, we can load it directly as a json array
//			//BufferedReader bufferedReader_t = new BufferedReader(new FileReader(trans_path));
//			JSONArray transactions = (JSONArray) parser_trans.parse(new FileReader(trans_path));
//			
//			
//			
//		  doc.write(out);
//	      out.close();
//	      System.out.println("createparagraph.docx written successfully");
//	    }
//
}