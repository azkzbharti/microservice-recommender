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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.queries.analysis.InsertIntoAnalysisQuery;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.appmod.pa.expandcluster.InterClassUsageFinder;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.jarlist.APIUsageStatsMiner;
import com.ibm.research.msr.jarlist.GradleDependencyDownloader;
import com.ibm.research.msr.jarlist.MavenCategoryEtAlExtractor;
import com.ibm.research.msr.jarlist.POMDependencyDownloader;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.ReadJarMap;
import com.ibm.research.msr.utils.Util;
import com.mongodb.client.MongoDatabase;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;


@CrossOrigin
@Controller
@RequestMapping("/msr/staticanalysis/")
@Api(value = "Analysis", description = "Microservices Recommender APIs for Static analyses")
public class AnalysisService {

	/** Logger. */
	private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class + "_swagger_log");

	public AnalysisService() {

	}

	@RequestMapping(method = RequestMethod.GET, value = "getInterClassUsage", produces = "application/json")
	@ApiOperation(value = "Analyze the Inter-Class Usage in the given project", notes = "", tags = "analysis")
	public @ResponseBody JSONObject getInterClassUsage(
			@RequestParam(value = "projectId", required = true) String projectId) {

		logger.info("getInterClassUsage : {}", projectId);
		MongoDatabase db = DatabaseConnection.getDatabase();
		JSONObject result = new JSONObject();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");
			return result;
		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}

		try {
			extractICU(projectId, db, proj);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			result.put("status", "Failed to extract Inter-class usage.");
			return result;
		}

		// TODO: update this to return the appropriate status
		result.put("status", "Successfully analyzed Inter-class usage.");
		return result;
	}

	public static void extractICU(String projectId, MongoDatabase db, Project proj) throws FileNotFoundException, IOException, ParseException {
		String rootPath = null;

		rootPath = proj.getProjectLocation();
		String type = proj.getCodeType();
		String tempFolder = rootPath + File.separator + "temp";
		
		File directory = new File(tempFolder);
	    if (! directory.exists()){
	        directory.mkdir();
	    }

		String opJsonFileName = tempFolder + File.separator + Constants.INTER_CLASS_USAGE + ".json";
		Document parameters = null;

		InterClassUsageFinder classUsage = new InterClassUsageFinder();
		if (type.equals(Constants.TYPE_SRC)) {
			classUsage.find(rootPath, opJsonFileName, proj.getRootAnalyzePath());
		} else {
			classUsage.findFromBinaryClassFiles(rootPath, opJsonFileName, proj.getRootAnalyzePath());
		}

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonobject = new JSONObject();
		JSONObject jsonobject2 = new JSONObject();

		jsonobject2 = (JSONObject) jsonParser.parse(new FileReader(opJsonFileName));
		jsonobject.put(Constants.INTER_CLASS_USAGE, jsonobject2);
	
		Analysis classDetails = new Analysis(new ObjectId(projectId), Constants.INTER_CLASS_USAGE, jsonobject.toString(), opJsonFileName);
//		Analysis classDetails = new Analysis(new ObjectId(projectId), Constants.interclassUsage, null,
//				opJsonFileName);
		InsertIntoAnalysisQuery iq = new InsertIntoAnalysisQuery(db, classDetails, logger);
		iq.execute();
	}

	@RequestMapping(method = RequestMethod.GET, value = "getJarAPIInfo", produces = "application/json")
	@ApiOperation(value = "Collect details about the dependencies of the project", notes = "", tags = "analysis")
	public @ResponseBody JSONObject getJarAPIInfo(
			@RequestParam(value = "projectId", required = true) String projectId) {

		logger.info("getJarAPIInfo : {}", projectId);
		MongoDatabase db = DatabaseConnection.getDatabase();
		JSONObject result = new JSONObject();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");
			return result;
		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}

		// TODO: using the project id - figure out the path where the project has been
		// checked out.
		String rootPath = null;
		// TODO: either it comes from environment variable - like HOME or
		// auto-constructed from where git-hub project is checked out.
		rootPath = proj.getProjectLocation();
		String tempFolder = rootPath + File.separator + "temp";
		// TODO: get the type of the project- is it srouce (git url) , or binary ( war,
		// ear)
		String type = proj.getCodeType();// "src";
		// TODO: where to write the output file
		new File(tempFolder).mkdir();

		String jarPackagestoCSV = tempFolder + File.separator + "jar-to-packages.csv";
		String barDataJSON = tempFolder + File.separator + File.separator + "bar-data.json";
		// TODO: folder where all the jars are downloaded and kept
		String jarFolder = tempFolder + File.separator + "jars";
		new File(tempFolder).mkdir();
		new File(jarFolder).mkdir();
		// TODO: where to write the output file
		String mavenMetaJSON = tempFolder + File.separator + "maven-meta.json";

		// TODO: folder where all the jars are downloaded and kept

		Collection<File> buildFieList = null;
		ArrayList<String> pomFiles = null;
		ArrayList<String> gradleFiles = null;

		boolean parsedJars = false;
		new File(tempFolder).mkdir();
		new File(jarFolder).mkdir();

		if (type.trim().toLowerCase().equals(Constants.TYPE_SRC)) {
			// src can contain either a pom.xml, build.gradle | a lib folder with jars
			// inside it.

			buildFieList = FileUtils.listFiles(new File(rootPath), new String[] { "xml", "gradle" }, true);

			Iterator<File> fileListItr = buildFieList.iterator();

			pomFiles = new ArrayList<String>();
			gradleFiles = new ArrayList<String>();

			File buildFile = null;

			while (fileListItr.hasNext()) {
				buildFile = fileListItr.next();
				if (buildFile.getName().equals("pom.xml")) {
					pomFiles.add(buildFile.getAbsolutePath());
				} else if (buildFile.getName().endsWith("gradle")) {
					gradleFiles.add(buildFile.getAbsolutePath());
				}
			}

			if (pomFiles.isEmpty() && gradleFiles.isEmpty()) {
				// no pom or gradle files found, hence it might contain a jars directly in lib
				// folder
				parsedJars = Util.dumpAPIInfo(rootPath, tempFolder);
				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(rootPath, mavenMetaJSON);

			} else if (!pomFiles.isEmpty()) {
				// we have POM, we need to parse pom and download all jar files

				POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
				pomDownloader.download(pomFiles, jarFolder);

				parsedJars = Util.dumpAPIInfo(jarFolder, tempFolder);
				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(jarFolder, mavenMetaJSON);

			} else {
				// we have gradle file, we need to parse gradle and download all jar files

				// create pom files out of gradle file
				GradleDependencyDownloader gradleDownloader = new GradleDependencyDownloader();
				pomFiles = gradleDownloader.createPOMFiles(gradleFiles, jarFolder);

				if (!pomFiles.isEmpty()) {
					// use the pom file logic to download the jars now
					POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
					pomDownloader.download(pomFiles, jarFolder);
					parsedJars = Util.dumpAPIInfo(jarFolder, tempFolder);
				}

				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(jarFolder, mavenMetaJSON);

			}

			if (!parsedJars && pomFiles.isEmpty()) {
				System.out.println(
						" The application source does not contain pom.xml. It also does not have any dependency on third party jars. We can't apply our micro-service recommendation approach.");
				// TODO: return error message
			}

		} else if (type.trim().toLowerCase().equals(Constants.TYPE_BIN)) {
			// it has to be either packaged as JAR or EAR.

			parsedJars = Util.dumpAPIInfo(rootPath, tempFolder);

		}

		// generate stats information
		APIUsageStatsMiner statsMiner = new APIUsageStatsMiner();
		statsMiner.mine(rootPath, jarPackagestoCSV, barDataJSON);

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonobject = new JSONObject();
		JSONObject barjsonobject = new JSONObject();
		JSONObject mavenjsonobject = new JSONObject();

		try {
			jsonobject.put("JAR-API", jarPackagestoCSV);
//			barjsonobject = (JSONObject) jsonParser.parse(new FileReader(barDataJSON));
			barjsonobject.put("bar-data", jsonParser.parse(new FileReader(barDataJSON)));
//			mavenjsonobject = (JSONObject) jsonParser.parse(new FileReader(mavenMetaJSON));
			mavenjsonobject.put("maven-meta", jsonParser.parse(new FileReader(mavenMetaJSON)));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Analysis details = null;
		InsertIntoAnalysisQuery iq;

		details = new Analysis(new ObjectId(projectId), Constants.MAVEN_ANALYSIS, mavenjsonobject.toString(), mavenMetaJSON);
		iq = new InsertIntoAnalysisQuery(db, details, logger);
		iq.execute();

		details = new Analysis(new ObjectId(projectId), Constants.BAR_DATA, barjsonobject.toString(), barDataJSON);
		iq = new InsertIntoAnalysisQuery(db, details, logger);
		iq.execute();

		// TODO: write this JSON back into DB
		details = new Analysis(new ObjectId(projectId), Constants.JAR_USAGE, null, jarPackagestoCSV);
		iq = new InsertIntoAnalysisQuery(db, details, logger);
		iq.execute();

		// TODO: update this to return the appropriate status
		result.put("status", "Successfully collected Jar API details.");
		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "getAPIUsageInfo", produces = "application/json")
	@ApiOperation(value = "Analyze the various APIs used by the project", notes = "", tags = "analysis")
	public @ResponseBody JSONObject getAPIUsageInfo(
			@RequestParam(value = "projectId", required = true) String projectId) {
		JSONObject result = new JSONObject();
		logger.info("getAPIUsageInfo : {}", projectId);
		MongoDatabase db = DatabaseConnection.getDatabase();

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projectId), logger);
		try {
			sproj.execute();
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Project id " + projectId + " is not registered");
			result.put("status", "Project not registerd");
			return result;
		}
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());

		if (!projectPath.exists()) {
			logger.error("Project id " + projectId + "files not found");
			result.put("status", "Project files not found");
			return result;
		}

		String type;
		type = proj.getCodeType();

		// TODO: CHECK IF JAR analyser exists
		String jarPackagesCsv = "";
		SelectAnalysisByProjectIdAndType sp1 = new SelectAnalysisByProjectIdAndType(DatabaseConnection.getDatabase(),
				new ObjectId(projectId), Constants.JAR_USAGE, logger);
		sp1.execute();
		Analysis jaranalyser;
		try { // TODO These exception handling should be part of queries and not here
			jaranalyser = sp1.getResult();
		} catch (Exception NullPointerException) {
			// TODO: handle exception
			logger.error("Project id " + projectId + "JarApiInfo not found");
			result.put("status", "Execute getJarApiInfo first");
			return result;
		}
		{

		}
		jarPackagesCsv = jaranalyser.getAnalysisPath();

		String measurePath = projectPath.getAbsolutePath() + File.separator + "temp" + File.separator + "measure.csv";
		String classFiles = projectPath.getAbsolutePath() + File.separator + "temp" + File.separator + "ClassList.json";

		ReadJarMap mapReader = new ReadJarMap();
		mapReader.createJARCategoryMap(jarPackagesCsv);

		AnalyzeApp analyzer = null;
		try {
			analyzer = new AnalyzeApp(projectPath.getAbsolutePath(), type, measurePath, mapReader.getLibCatMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			analyzer.savetoFile(measurePath, classFiles); // Check how to store csv in db or change the contents to as
															// pass string
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonobject = new JSONObject();
		InsertIntoAnalysisQuery iq = null;
		try {
			jsonobject.put("", jsonParser.parse(new FileReader(classFiles)));
			Analysis classDetails = new Analysis(new ObjectId(projectId), Constants.CLASS_DETAILS, jsonobject.toString(), classFiles);
			iq = new InsertIntoAnalysisQuery(db, classDetails, logger);
			iq.execute();
			classDetails = new Analysis(new ObjectId(projectId), Constants.MEASURE, null, measurePath);
			iq = new InsertIntoAnalysisQuery(db, classDetails, logger);
			iq.execute();

		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO: update this to return the appropriate status
		result.put("status", "Successfully analyzed API usage.");
		return result;
	}

}
