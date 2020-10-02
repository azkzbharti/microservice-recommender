package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.research.appmod.slicing.SlicingDriver;
import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.dto.Partition;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.overlays.InsertIntoOverlaysQuery;
import com.ibm.research.msr.db.queries.partition.InsertIntoPartitionHistoryQuery;
import com.ibm.research.msr.db.queries.partition.InsertIntoPartitionQuery;
import com.ibm.research.msr.db.queries.partition.Ownership;
import com.ibm.research.msr.db.queries.partition.SelectAllPartitionsByProjectId;
import com.ibm.research.msr.db.queries.partition.SelectEarliestPartitionsFromHistoryByProjectId;
import com.ibm.research.msr.db.queries.partition.UpdatePartitionByProjectId;
import com.ibm.research.msr.db.queries.project.InsertIntoProjectQuery;
import com.ibm.research.msr.db.queries.project.SelectAllProjects;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.msr.db.queries.project.UpdateProjectStatusByProjectId;
import com.ibm.research.msr.ddd.EntityBeanAffinity;
import com.ibm.research.msr.model.Microservice;
import com.ibm.research.msr.model.Status;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PartitioningService.class, DatabaseConnection.class, FileUtils.class, SlicingDriver.class,
		APIUtilities.class, Ownership.class })
public class PartitioningServiceTest {

	private static final String PROJECT_ID = new ObjectId().toHexString();

	private static final String PROJECT_LOCATION = "src/test/resources/apps/source/5f6888acab353118f79ea34e/temp";

	PartitioningService partitioningService = new PartitioningService();

	@Test
	public void communityDetectionFileNotFoundTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(false);

		JSONObject jsonObject = partitioningService.CommunityDetection(PROJECT_ID);

		assertEquals("Project files not found", jsonObject.get("status"));
	}

	@Test
	public void communityDetectionTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);

		SelectAnalysisByProjectIdAndType selectAnalysisByProjectIdAndType = mock(
				SelectAnalysisByProjectIdAndType.class);
		whenNew(SelectAnalysisByProjectIdAndType.class).withAnyArguments().thenReturn(selectAnalysisByProjectIdAndType);

		Analysis analysis = mock(Analysis.class);
		when(selectAnalysisByProjectIdAndType.getResult()).thenReturn(analysis);

		InsertIntoPartitionQuery insertIntoPartitionQuery = mock(InsertIntoPartitionQuery.class);
		whenNew(InsertIntoPartitionQuery.class).withAnyArguments().thenReturn(insertIntoPartitionQuery);

		JSONObject jsonObject = partitioningService.CommunityDetection(PROJECT_ID);

		assertEquals("Successfully generated community based clusters.", jsonObject.get("status"));
	}

	@Test
	public void identifyMicroserviceCandidatesWithICUFileNotFoundTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(false);

		JSONObject jsonObject = partitioningService.identifyMicroserviceCandidatesWithICU(PROJECT_ID, null, null, null,
				null, null);

		assertEquals("Project files not found", jsonObject.get("status"));
	}

	@Test
	public void identifyMicroserviceCandidatesWithICUTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);
		when(file.getAbsolutePath()).thenReturn("\"\"\"");

		mockStatic(FileUtils.class);

		MultipartFile multipartFile = mock(MultipartFile.class);

		mockStatic(SlicingDriver.class);
		EntityBeanAffinity entityBeanAffinity = mock(EntityBeanAffinity.class);
		whenNew(EntityBeanAffinity.class).withAnyArguments().thenReturn(entityBeanAffinity);

		mockStatic(APIUtilities.class);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(jsonParser);
		when(jsonParser.parse(bufferedReader)).thenReturn(new JSONObject());

		FileWriter fileWriter = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fileWriter);

		mockStatic(Ownership.class);
		when(Ownership.analyze(Mockito.any(), Mockito.any())).thenReturn(new JSONArray());

		JSONObject microservice = new JSONObject();
		JSONObject partition = new JSONObject();
		partition.put("microservice", microservice);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("partition_result", partition);

		when(Ownership.revise(Mockito.any(), Mockito.any())).thenReturn(jsonObject);

		InsertIntoPartitionQuery insertIntoPartitionQuery = mock(InsertIntoPartitionQuery.class);
		whenNew(InsertIntoPartitionQuery.class).withAnyArguments().thenReturn(insertIntoPartitionQuery);

		InsertIntoPartitionHistoryQuery insertIntoPartitionHistoryQuery = mock(InsertIntoPartitionHistoryQuery.class);
		whenNew(InsertIntoPartitionHistoryQuery.class).withAnyArguments().thenReturn(insertIntoPartitionHistoryQuery);

		InsertIntoOverlaysQuery insertIntoOverlaysQuery = mock(InsertIntoOverlaysQuery.class);
		whenNew(InsertIntoOverlaysQuery.class).withAnyArguments().thenReturn(insertIntoOverlaysQuery);

		UpdateProjectStatusByProjectId updateProjectStatusByProjectId = mock(UpdateProjectStatusByProjectId.class);
		whenNew(UpdateProjectStatusByProjectId.class).withAnyArguments().thenReturn(updateProjectStatusByProjectId);

		partitioningService.identifyMicroserviceCandidatesWithICU(PROJECT_ID, multipartFile, multipartFile,
				multipartFile, multipartFile, multipartFile);

	}

	@Test
	public void triggerAnalysisTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		UpdateProjectStatusByProjectId updateProjectStatusByProjectId = mock(UpdateProjectStatusByProjectId.class);
		whenNew(UpdateProjectStatusByProjectId.class).withAnyArguments().thenReturn(updateProjectStatusByProjectId);

		InsertIntoPartitionHistoryQuery insertIntoPartitionHistoryQuery = mock(InsertIntoPartitionHistoryQuery.class);
		whenNew(InsertIntoPartitionHistoryQuery.class).withAnyArguments().thenReturn(insertIntoPartitionHistoryQuery);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		FileWriter fileWriter = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fileWriter);

		mockStatic(APIUtilities.class);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("projectName", "projectName");
		jsonObject.put("projectId", "projectId");

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(jsonParser);
		when(jsonParser.parse(fileReader)).thenReturn(new JSONObject());
		when(jsonParser.parse(Mockito.anyString())).thenReturn(jsonObject);

		UpdatePartitionByProjectId updatePartitionByProjectId = mock(UpdatePartitionByProjectId.class);
		whenNew(UpdatePartitionByProjectId.class).withAnyArguments().thenReturn(updatePartitionByProjectId);

		Microservice microservice = new Microservice();
		microservice.setProjectId(PROJECT_ID);

		JSONObject json = partitioningService.triggerAnalysis(microservice);

		assertEquals(ProjectStatus.OK, json.get("status"));
	}

	@Test
	public void identifyMicroserviceCandidates() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);
		when(file.getAbsolutePath()).thenReturn("\"\"\"");

		mockStatic(FileUtils.class);

		MultipartFile multipartFile = mock(MultipartFile.class);

		mockStatic(SlicingDriver.class);
		EntityBeanAffinity entityBeanAffinity = mock(EntityBeanAffinity.class);
		whenNew(EntityBeanAffinity.class).withAnyArguments().thenReturn(entityBeanAffinity);

		mockStatic(APIUtilities.class);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(jsonParser);
		when(jsonParser.parse(bufferedReader)).thenReturn(new JSONObject());

		FileWriter fileWriter = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fileWriter);

		mockStatic(Ownership.class);
		when(Ownership.analyze(Mockito.any(), Mockito.any())).thenReturn(new JSONArray());

		JSONObject microservice = new JSONObject();
		JSONObject partition = new JSONObject();
		partition.put("microservice", microservice);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("partition_result", partition);

		when(Ownership.revise(Mockito.any(), Mockito.any())).thenReturn(jsonObject);

		InsertIntoPartitionQuery insertIntoPartitionQuery = mock(InsertIntoPartitionQuery.class);
		whenNew(InsertIntoPartitionQuery.class).withAnyArguments().thenReturn(insertIntoPartitionQuery);

		InsertIntoPartitionHistoryQuery insertIntoPartitionHistoryQuery = mock(InsertIntoPartitionHistoryQuery.class);
		whenNew(InsertIntoPartitionHistoryQuery.class).withAnyArguments().thenReturn(insertIntoPartitionHistoryQuery);

		InsertIntoOverlaysQuery insertIntoOverlaysQuery = mock(InsertIntoOverlaysQuery.class);
		whenNew(InsertIntoOverlaysQuery.class).withAnyArguments().thenReturn(insertIntoOverlaysQuery);

		UpdateProjectStatusByProjectId updateProjectStatusByProjectId = mock(UpdateProjectStatusByProjectId.class);
		whenNew(UpdateProjectStatusByProjectId.class).withAnyArguments().thenReturn(updateProjectStatusByProjectId);

		partitioningService.identifyMicroserviceCandidates(PROJECT_ID, multipartFile, multipartFile, multipartFile,
				multipartFile);
	}

	@Test
	public void revertRecommendationsTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectEarliestPartitionsFromHistoryByProjectId sp = mock(SelectEarliestPartitionsFromHistoryByProjectId.class);
		whenNew(SelectEarliestPartitionsFromHistoryByProjectId.class).withAnyArguments().thenReturn(sp);

		when(sp.getResultSize()).thenReturn(1);

		Partition partition = mock(Partition.class);

		when(sp.getResult()).thenReturn(Arrays.asList(partition));

		UpdatePartitionByProjectId updatePartitionByProjectId = mock(UpdatePartitionByProjectId.class);
		whenNew(UpdatePartitionByProjectId.class).withAnyArguments().thenReturn(updatePartitionByProjectId);

		JSONObject jsonObject = partitioningService.revertRecommendations(PROJECT_ID, "");

		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}

	@Test
	public void getMicroservicesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);

		Document document = mock(Document.class);
		when(document.get(Mockito.any())).thenReturn("cluster");
		Partition partition = mock(Partition.class);
		when(partition.getPartitionResult()).thenReturn(document);
		when(partition.getPartitionType()).thenReturn(Constants.COMMUNITY_CLUSTERING);

		when(sp.getResult()).thenReturn(Arrays.asList(partition));
		when(sp.getResultSize()).thenReturn(1);

		JSONObject jsonObject = partitioningService.getMicroservices(PROJECT_ID);

		assertEquals("cluster", jsonObject.get("Result"));
	}

	@Test
	public void microserviceStatusAnalyzedTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);

		when(sp.getResultSize()).thenReturn(1);

		JSONObject jsonObject = partitioningService.microserviceStatus(PROJECT_ID);

		assertEquals("Analyzed", jsonObject.get("status"));
	}

	@Test
	public void microserviceStatusAnalyzingTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllPartitionsByProjectId sp = mock(SelectAllPartitionsByProjectId.class);
		whenNew(SelectAllPartitionsByProjectId.class).withAnyArguments().thenReturn(sp);

		when(sp.getResultSize()).thenReturn(0);

		JSONObject jsonObject = partitioningService.microserviceStatus(PROJECT_ID);

		assertEquals("Analyzing", jsonObject.get("status"));
	}

	@Test
	public void microservicesStatusAnalyzedTest1() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllProjects sp = mock(SelectAllProjects.class);
		whenNew(SelectAllProjects.class).withAnyArguments().thenReturn(sp);

		Project project = mock(Project.class);
		when(project.get_id()).thenReturn(new ObjectId());
		when(project.getStatus()).thenReturn(null);

		when(sp.getResult()).thenReturn(Arrays.asList(project));

		JSONArray jsonArray = partitioningService.microservicesStatus();
		JSONObject jsonObject = (JSONObject) jsonArray.get(0);
		assertEquals("Application analyzed successfully", jsonObject.get("message"));

	}

	@Test
	public void microservicesStatusAnalyzedTest2() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllProjects sp = mock(SelectAllProjects.class);
		whenNew(SelectAllProjects.class).withAnyArguments().thenReturn(sp);

		Project project = mock(Project.class);
		when(project.get_id()).thenReturn(new ObjectId());
		when(project.getStatus()).thenReturn(Constants.CMA_ANALYZED_STATUS_MSG);

		when(sp.getResult()).thenReturn(Arrays.asList(project));

		JSONArray jsonArray = partitioningService.microservicesStatus();
		JSONObject jsonObject = (JSONObject) jsonArray.get(0);
		assertEquals("Application analyzed successfully", jsonObject.get("message"));

	}

	@Test
	public void microservicesStatusAnalyzingTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllProjects sp = mock(SelectAllProjects.class);
		whenNew(SelectAllProjects.class).withAnyArguments().thenReturn(sp);

		Project project = mock(Project.class);
		when(project.get_id()).thenReturn(new ObjectId());
		when(project.getStatus()).thenReturn(Constants.CMA_ANALYZING_STATUS_MSG);

		when(sp.getResult()).thenReturn(Arrays.asList(project));

		JSONArray jsonArray = partitioningService.microservicesStatus();
		JSONObject jsonObject = (JSONObject) jsonArray.get(0);
		assertEquals("Application analyzing is still ongoing", jsonObject.get("message"));

	}

	@Test
	public void microservicesStatusFailTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectAllProjects sp = mock(SelectAllProjects.class);
		whenNew(SelectAllProjects.class).withAnyArguments().thenReturn(sp);

		Project project = mock(Project.class);
		when(project.get_id()).thenReturn(new ObjectId());
		when(project.getStatus()).thenReturn("failure");

		when(sp.getResult()).thenReturn(Arrays.asList(project));

		JSONArray jsonArray = partitioningService.microservicesStatus();
		JSONObject jsonObject = (JSONObject) jsonArray.get(0);
		assertEquals("Application analysis failed.Please contact CMA admin", jsonObject.get("message"));

	}

	@Test
	public void updateMicroservicesStatusTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		UpdateProjectStatusByProjectId up = mock(UpdateProjectStatusByProjectId.class);
		whenNew(UpdateProjectStatusByProjectId.class).withAnyArguments().thenReturn(up);

		Status status = new Status();
		status.setProjectId(PROJECT_ID);

		JSONObject jsonObject = partitioningService.updateMicroservicesStatus(status);

		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}

	@Test
	public void updateMicroservicesTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		Microservice microservice = new Microservice();
		microservice.setProjectId(PROJECT_ID);

		InsertIntoPartitionHistoryQuery sp = mock(InsertIntoPartitionHistoryQuery.class);
		whenNew(InsertIntoPartitionHistoryQuery.class).withAnyArguments().thenReturn(sp);

		UpdatePartitionByProjectId up = mock(UpdatePartitionByProjectId.class);
		whenNew(UpdatePartitionByProjectId.class).withAnyArguments().thenReturn(up);

		JSONObject jsonObject = partitioningService.updateMicroservices(microservice);

		assertEquals(ProjectStatus.OK, jsonObject.get("status"));
	}

	@Test
	public void identifyMicroserviceCandidatesTest() throws Exception {
		SourceProject project = new SourceProject("daytradertest", "daytraderdesc", "src/test/resources/apps/source/",
				"java", "source", "bin", "");

		project.setProjectZipLocation("src/test/resources/apps/source/5f6888acab353118f79ea34e");

		MongoDatabase db = DatabaseConnection.getDatabase();

		ObjectId projID = new InsertIntoProjectQuery(db, project, LoggerFactory.getLogger(getClass())).execute();
		System.out.println(projID.toHexString());

		MultipartFile callGraphFile = new MockMultipartFile("callgraph.dot", "callgraph.dot", "text/plain",
				Files.readAllBytes(Paths.get(PROJECT_LOCATION + "/callgraph.dot")));

		MultipartFile entryPointsJSON = new MockMultipartFile("service.json", "service.json", "application/json",
				Files.readAllBytes(Paths.get(PROJECT_LOCATION + "/service.json")));

		MultipartFile crudJSON = new MockMultipartFile("resource.json", "resource.json", "application/json",
				Files.readAllBytes(Paths.get(PROJECT_LOCATION + "/resource.json")));

		MultipartFile transactionsJSON = new MockMultipartFile("transaction.json", "transaction.json",
				"application/json", Files.readAllBytes(Paths.get(PROJECT_LOCATION + "/transaction.json")));

		JSONObject jsonObject = partitioningService.identifyMicroserviceCandidates(projID.toHexString(), callGraphFile,
				entryPointsJSON, crudJSON, transactionsJSON);

		System.out.println(jsonObject.toJSONString());
	}

	@Test
	public void peformDDDAnalysisTest() throws Exception {
		SourceProject project = new SourceProject("daytraderDDDAnalysistest", "daytraderdesc", "src/test/resources/apps/source/",
				"java", "source", "bin", "");

		project.setProjectZipLocation("src/test/resources/apps/source/5f6888acab353118f79ea34e");

		MongoDatabase db = DatabaseConnection.getDatabase();

		ObjectId projID = new InsertIntoProjectQuery(db, project, LoggerFactory.getLogger(getClass())).execute();
		System.out.println(projID.toHexString());

		SelectProjectByProjectId sproj = new SelectProjectByProjectId(db, new ObjectId(projID.toHexString()),
				LoggerFactory.getLogger(getClass()));
		
		sproj.execute();
		
		Project proj = sproj.getResult();
		File projectPath = new File(proj.getProjectLocation());
		
		String rootPath = proj.getProjectLocation();
		String type = proj.getCodeType();
		String tempFolder = rootPath + File.separator + "temp";
		
		File callGraphDotFile = new File(PROJECT_LOCATION + "/callgraph.dot");
		File entryPointsJSONFile = new File(PROJECT_LOCATION + "/service.json");
		File crudJSONFile = new File(PROJECT_LOCATION + "/resource.json");
		File transactionsJSONFile = new File(PROJECT_LOCATION + "/transaction.json");
		
		PartitioningService.peformDDDAnalysis(projID.toHexString(), db, projectPath, rootPath, type, tempFolder, callGraphDotFile,
				entryPointsJSONFile, crudJSONFile, transactionsJSONFile);
	}

}
