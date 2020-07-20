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
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.GitProject;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.project.InsertIntoProjectQuery;
import com.ibm.research.msr.db.queries.project.UpdateProjectStatusByProjectId;
import com.ibm.research.msr.git.GitConnect;
import com.ibm.research.msr.utils.Commons;
import com.ibm.research.msr.utils.UnzipFiles;
import com.ibm.research.msr.utils.UnzipUtility;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;


@CrossOrigin
@Controller
@RequestMapping("/msr/init/")
@Api(value = "Registration", description = "Microservices Recommender APIs for all kinds of registration services")
public class RegistrationService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class + "_swagger_log");


	public RegistrationService() {

	}

	@RequestMapping(method=RequestMethod.POST,value="registerGitProject", produces = "application/json")
	@ApiOperation(value = "Register a Git Project with the application",
	produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JSONObject registerProject(
			@RequestParam(value = "projectName", required = true) String projectName,	
			@RequestParam(value = "projectDescription", required = true) String projectDesc,	
			@RequestParam(value = "sourceLanguage", required = true) String srcLanguage,
			@RequestParam(value = "gitURL", required = true) String gitURL,
			@RequestParam(value = "analyseRootPath", required = false) String analyseRootPath,
			@RequestParam(value = "branch", required = false) String branch){
//			@RequestParam(value = "privateSSHKey", required = false) String loc,
//			@RequestParam(value = "passPhrase", required = false) String passphrase){
		
		logger.info("Register Project: " + projectName);
		String appModBaseDir = Commons.getMSRBaseDir();
		String type = Constants.SOURCE_GIT; //this is to indicate its from git repo
		String codeType = Constants.TYPE_SRC; //this is to indicate if its code files or binary
		JSONObject newProjJSON = new JSONObject();
		
		System.out.println("appModBaseDir -- "+appModBaseDir);
		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "git" + File.separator;
		GitProject project = new GitProject(projectName, projectDesc, gitURL, branch, dirPath, srcLanguage, type, codeType, analyseRootPath);
		System.out.println("Git Dir -- "+project.getProjectLocation());
		
        try {
        	File f = new File(project.getProjectLocation());
        	if(f.exists()) {
				FileUtils.cleanDirectory(f);
				FileUtils.forceDelete(f); 
        	}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			
			if(gitURL.startsWith("http")) {//assume public and no auth required
				if(null != branch && !branch.equals("")) {
					GitConnect.connectToPublicHttp(project.getProjectLocation(), gitURL, branch);
				}else {
					GitConnect.connectToPublicHttp(project.getProjectLocation(), gitURL, null); //default branch
				}
			}else {
				//srikanth commented below since we will not give user permission to point to ssh file rather ask them to copy the public key to their enterprise
//				if(null != loc && !loc.equals("")) {
//					project.setSshKeyFileLocation(loc);
//					if(null == passphrase || passphrase.equals("")) {
//						passphrase = "passphrase"; //default
//					}
//				   connect = new GitConnect(loc, passphrase);
//				}
				
				if(null != branch && !branch.equals("")) {
					GitConnect.connectToPublicSSH(project.getProjectLocation(), gitURL, branch);
				}else {
					GitConnect.connectToPublicSSH(project.getProjectLocation(), gitURL, null); //default branch
				}
			}
			
			
			 ObjectId projID = new InsertIntoProjectQuery(DatabaseConnection.getDatabase(), project, logger).execute();
			 newProjJSON.put("projectID", projID.toString());
			 logger.info("RegisterModel complete with Model Id: {}", projID.toString());
			 
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

		return newProjJSON;
	}
	
	
	/**
	 * THis API will be exposed for THINK demo
	 * The API does the following
	 * 1) Git Clones the project using tokens
	 * 2) Creates an entry to the DB with the request(project) meta
	 * 3) Calls TRL's API for CRUD Analysis
	 * 4) Call ICU Analysis
	 * 5) Call Seed Expansion Partitions
	 * @param projectName
	 * @param projectDesc
	 * @param gitURL
	 * @param token
	 * @param branch
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST,value="registerProject", produces = "application/json")
	@ApiOperation(value = "Register a Git Project with the application",
	produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JSONObject registerProject(
			@RequestParam(value = "projectName", required = true) String projectName,	
			@RequestParam(value = "projectDescription", required = true) String projectDesc,	
			@RequestParam(value = "gitURL", required = true) String gitURL,
			@RequestParam(value = "token", required = true) String token,
			@RequestParam(value = "branch", required = false) String branch){
		
		logger.info("Register Project: " + projectName);
		String appModBaseDir = Commons.getMSRBaseDir();
		String type = Constants.SOURCE_GIT; //this is to indicate its from git repo or local upload
		String codeType = Constants.TYPE_BIN; //this is to indicate if its code files or binary
		String srcLang = Constants.JAVA_LANG; //for THINK demo we only support Java
		JSONObject newProjJSON = new JSONObject();
		
		MongoDatabase db = DatabaseConnection.getDatabase();
		
		System.out.println("appModBaseDir -- "+appModBaseDir);
		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "git" + File.separator;
		GitProject project = new GitProject(projectName, projectDesc, gitURL, token, branch, dirPath, srcLang, type, codeType, "");
		System.out.println("Git Dir -- "+project.getProjectLocation());
		
        try {
        	File f = new File(project.getProjectLocation());
        	if(f.exists()) {
				FileUtils.cleanDirectory(f);
				FileUtils.forceDelete(f); 
        	}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			//Git clones the project
			if(null != branch && !branch.equals("")) {
				GitConnect.connectUsingToken(project.getProjectLocation(), gitURL, token, branch);
			}else {
				GitConnect.connectUsingToken(project.getProjectLocation(), gitURL, token, null); //default branch
			}
			
			// check if the folder now contains ear which might internally contain war/jar files and extract them for analysis
			Collection<File> earList = FileUtils.listFiles(new File(project.getProjectLocation()), new String[] { "ear" },
					true);
			if (!earList.isEmpty()) {
				Iterator<File> earListListItr = earList.iterator();
				while (earListListItr.hasNext()) {
					UnzipUtility.unzip(earListListItr.next().getAbsolutePath(), project.getProjectLocation());
				}

			}
						
			// check if the folder now contains wars and ejb-jar files and extract them for analysis
			Collection<File> warjarList = FileUtils.listFiles(new File(project.getProjectLocation()), new String[] { "war", "jar"},
					true);
			if (!warjarList.isEmpty()) {
				Iterator<File> warjarListItr = warjarList.iterator();
				while (warjarListItr.hasNext()) {
					UnzipUtility.unzip(warjarListItr.next().getAbsolutePath(), project.getProjectLocation());
				}

			}
			
		//save data to DB
		 ObjectId projID = new InsertIntoProjectQuery(db, project, logger).execute();
		 
		
		 
		//extract ICU Info
		 AnalysisService.extractICU(projID.toString(), db, project);
		 
		 //Call TRL API for CRUD Analysis
		 String reqContentType = "application/json";
		 String req = "{\"giturl\":\""+gitURL+"\", \"gittoken\": \""+token+"\", \"upi\":\""+projID.toString()+"\"}";
		 APIUtilities.sendPost(APIUtilities.getDataAnalysisAPI()+"/api/crud/static", req, reqContentType);
		 
		 newProjJSON.put("projectID", projID.toString());
		 logger.info("RegisterModel complete with Model Id:  {}", projID.toString());
		 //if everything goes fine
		 newProjJSON.put("Status", ProjectStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			UpdateProjectStatusByProjectId updateProjsQuery = new UpdateProjectStatusByProjectId(db, project.get_id().toString(), Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjsQuery.execute();
			newProjJSON.put("Status", ProjectStatus.INTERNAL_SERVER_ERROR);
		}

		return newProjJSON;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="registerSourceProject", produces = "application/json")
	@ApiOperation(value = "Upload a binary/source project of the application",
	produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JSONObject registerProject(
			@RequestParam(value = "projectName", required = true) String projectName,	
			@RequestParam(value = "projectDescription", required = true) String projectDesc,	
			@RequestParam(value = "sourceLanguage", required = true) String srcLanguage,
			@ApiParam(name = "sourceZip", value = "sourceZip", required = true)
			@RequestPart("sourceZip") MultipartFile sourceZip) {
		
//		System.out.println("Register Project: -- "+projectName);
		logger.info("Register Project: " + projectName);
		String appModBaseDir = Commons.getMSRBaseDir();
		String type = Constants.SOURCE_FILE; //this is to indicate its uploaded
		String codeType = Constants.TYPE_SRC;
		JSONObject newProjJSON = new JSONObject();
		
		System.out.println("appModBaseDir -- "+appModBaseDir);
		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "source" +  File.separator;
		
		SourceProject project = new SourceProject(projectName, projectDesc, dirPath, srcLanguage, type, codeType, ""); //TODO: infer or take user argument
		
		System.out.println("Source DirPath -- "+project.getProjectLocation());
		
		
        try {
        	File f = new File(project.getProjectLocation());
        	if(f.exists()) {
				FileUtils.cleanDirectory(f);
				FileUtils.forceDelete(f); 
        	}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			
			File sourceZipFile = new File(project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename());
			FileUtils.writeByteArrayToFile(sourceZipFile, sourceZip.getBytes());
			System.out.println("After saving before unzipping -- "+project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename());
			
			if(!sourceZip.getOriginalFilename().endsWith(".zip")) {
				project.setCodeType(Constants.TYPE_BIN);
				UnzipUtility.unzip(project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename(), project.getProjectLocation());
				if(sourceZip.getOriginalFilename().endsWith(".ear")) {//there will be war inside, extract that too
					
					
					// check if the folder now contains wars and ejb-jar files
					Collection<File> warjarList = FileUtils.listFiles(new File(project.getProjectLocation()), new String[] { "war", "jar" },
							true);
					if (!warjarList.isEmpty()) {
						Iterator<File> warjarListItr = warjarList.iterator();
						while (warjarListItr.hasNext()) {
							UnzipUtility.unzip(warjarListItr.next().getAbsolutePath(), project.getProjectLocation());
						}

					}
					
					
				}
			}else {
				try {
				UnzipFiles.unzip(project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename(), project.getProjectLocation());
				}catch (IOException ie) {
					System.out.println(sourceZip.getOriginalFilename()+" failed with UnzipFiles. Try unzipUitlity");
					APIUtilities.deleteDirectory(new File(project.getProjectLocation()), sourceZip.getOriginalFilename());
					UnzipUtility.unzip(project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename(), project.getProjectLocation());
				}
			}
			System.out.println("After unzip -- "+project.getProjectLocation() + File.separator + sourceZip.getOriginalFilename());
			
	       ObjectId projID = new InsertIntoProjectQuery(DatabaseConnection.getDatabase(), project, logger).execute();
		   newProjJSON.put("projectID", projID.toString());
		   logger.info("RegisterModel complete with Model Id: {}", projID.toString());
		      
		} catch (IOException e) {
			e.printStackTrace();
			newProjJSON.put("Status", ProjectStatus.INTERNAL_SERVER_ERROR);
			return newProjJSON;
		}

		newProjJSON.put("Status", ProjectStatus.OK);
		return newProjJSON;
	}
	
	
	@RequestMapping(method=RequestMethod.POST,value="registerLocalProject", produces = "application/json")
	@ApiOperation(value = "Register a local project with the application",
	produces = "application/json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public @ResponseBody JSONObject registerLocalProject(
			@RequestParam(value = "projectName", required = true) String projectName,	
			@RequestParam(value = "projectDescription", required = true) String projectDesc,	
			@RequestParam(value = "sourceLocation", required = true) String localFilePath) {
		
		logger.info("Register Project: " + projectName);
		String appModBaseDir = Commons.getMSRBaseDir();
		String type = Constants.SOURCE_FILE; //this is to indicate its uploaded method
		String codeType = Constants.TYPE_BIN;
		JSONObject newProjJSON = new JSONObject();
		ObjectId projID = null;
		System.out.println("appModBaseDir -- "+appModBaseDir);
		String dirPath = appModBaseDir + File.separator + "apps" + File.separator + "source" +  File.separator;
		String srcLanguage = "java";
		if(localFilePath.endsWith(".zip")) {
			srcLanguage = ".Net";
			codeType = Constants.TYPE_SRC;
		}
		SourceProject project = new SourceProject(projectName, projectDesc, dirPath, srcLanguage, type, codeType, ""); //TODO: infer or take user argument
		
		logger.info("DirPath -- "+project.getProjectLocation());
		MongoDatabase db = DatabaseConnection.getDatabase();	
		
		try {
			File srcFile = new File(localFilePath);

			String destination = project.getProjectLocation();
			File destDir = new File(destination);
			
			FileUtils.copyFileToDirectory(srcFile, destDir);
			
			
			if(!srcFile.getName().endsWith(".zip")) { //for java binary
				
				// check if the folder now contains an ear which might internally contain war/jar files and extract them for analysis
				Collection<File> earList = FileUtils.listFiles(new File(project.getProjectLocation()), new String[] { "ear" },
						true);
				if (!earList.isEmpty()) {
					Iterator<File> earListListItr = earList.iterator();
					while (earListListItr.hasNext()) {
						UnzipUtility.unzip(earListListItr.next().getAbsolutePath(), project.getProjectLocation());
					}

				}
							
				// check if the folder now contains wars and ejb-jar files and extract them for analysis
				Collection<File> warjarList = FileUtils.listFiles(new File(project.getProjectLocation()), new String[] { "war", "jar"},
						true);
				if (!warjarList.isEmpty()) {
					Iterator<File> warjarListItr = warjarList.iterator();
					while (warjarListItr.hasNext()) {
						UnzipUtility.unzip(warjarListItr.next().getAbsolutePath(), project.getProjectLocation());
					}

				}
				
				
				//save data to DB
				 projID = new InsertIntoProjectQuery(db, project, logger).execute();
				
				//extract ICU Info
				 AnalysisService.extractICU(projID.toString(), db, project);
				 
				 String reqContentType = "application/json";
				 String req = "{\"src\":\""+localFilePath+"\", \"upi\":\""+projID.toString()+"\"}";
				 Runnable r = new Runnable() {
						public void run() {
							try {
								logger.info("call C2C with req -- "+req);
								APIUtilities.sendPost(APIUtilities.getDataAnalysisAPI()+"/api/static/apps", req, reqContentType);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
			 
					Thread t = new Thread(r);
					t.start(); // starts thread in background..
				 
				 
				
			}else { //for cast based .Net application intermediate 4 json files
				String rootPath = project.getProjectLocation();
				String tempFolder = rootPath + File.separator + "temp";
				try {
				UnzipFiles.unzip(rootPath + File.separator + srcFile.getName(), tempFolder);
				}catch (IOException ie) {
					logger.info(srcFile.getName()+" failed with UnzipFiles. Try unzipUitlity");
					APIUtilities.deleteDirectory(new File(project.getProjectLocation()), srcFile.getName());
					UnzipUtility.unzip(rootPath + File.separator + srcFile.getName(),tempFolder);
				}
				
				projID = new InsertIntoProjectQuery(db, project, logger).execute();
				 
				// Creating files for community detection
				File projectPath = new File(project.getProjectLocation());
				String resultFolder = projectPath.getAbsolutePath() + File.separator + "ui" + File.separator + "data" + File.separator;		
				new File(resultFolder).mkdirs();
				
				String entryPointFilePath = tempFolder + File.separator + "service.json";
				String callGraphDotFilePath = tempFolder + File.separator + "callgraph.dot";
				String crudPath = tempFolder + File.separator + "db.json";
				String transactionFilePath = tempFolder + File.separator + "transaction.json";
				
				//DDD processing start
				PartitioningService.peformDDDAnalysis(project.get_id().toString(), db, projectPath, rootPath, project.getCodeType(), tempFolder, new File(callGraphDotFilePath),
						new File(entryPointFilePath), new File(crudPath), new File(transactionFilePath));
				
				
			}
			
		 newProjJSON.put("projectID", projID.toString());	
		 newProjJSON.put("Status", ProjectStatus.OK);
		 logger.info("RegisterModel complete with Model Id:  {}", projID.toString());
		} catch (Exception e) {
			UpdateProjectStatusByProjectId updateProjsQuery = new UpdateProjectStatusByProjectId(db, projID.toString(), Constants.CMA_FAILED_STATUS_MSG, logger);
			updateProjsQuery.execute();
			newProjJSON.put("Status", ProjectStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();

		}
		return newProjJSON;
	}
	
	public static void main(String ag[]) throws IOException {
//		try {
//			Process p = Runtime.getRuntime().exec("jar -xvf /Users/srikanth/Desktop/hybrid-cloud/test/test_extract/Petstore.war /Users/srikanth/Desktop/hybrid-cloud/test/test_extract/");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		final String command = "jar -xvf /Users/srikanth/Desktop/hybrid-cloud/test/test_extract/Petstore.war /Users/srikanth/Desktop/hybrid-cloud/test/test_extract/";
//        final Runtime r = Runtime.getRuntime();
//        Process p;
//		try {
//			p = r.exec(command);
//			final int returnCode = p.waitFor();
//			System.out.println(returnCode);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		UnzipUtility.unzip("/Users/srikanth/Desktop/hybrid-cloud/test/test_extract/bias_api.zip", "/Users/srikanth/Desktop/hybrid-cloud/test/test_extract/");
		UnzipFiles.unzip("/Users/srikanth/Desktop/hybrid-cloud/test/test_extract/bias_api.zip", "/Users/srikanth/Desktop/hybrid-cloud/test/test_extract/");
		System.out.println("done");
        
	}

}