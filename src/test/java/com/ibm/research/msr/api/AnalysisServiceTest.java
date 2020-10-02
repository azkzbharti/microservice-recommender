package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.appmod.pa.expandcluster.InterClassUsageFinder;
import com.ibm.research.appmod.pa.jarlist.APIUsageStatsMiner;
import com.ibm.research.appmod.pa.jarlist.GradleDependencyDownloader;
import com.ibm.research.appmod.pa.jarlist.MavenCategoryEtAlExtractor;
import com.ibm.research.appmod.pa.jarlist.POMDependencyDownloader;
import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.dto.Analysis;
import com.ibm.research.msr.db.dto.Project;
import com.ibm.research.msr.db.dto.SourceProject;
import com.ibm.research.msr.db.queries.analysis.InsertIntoAnalysisQuery;
import com.ibm.research.msr.db.queries.analysis.SelectAnalysisByProjectIdAndType;
import com.ibm.research.msr.db.queries.project.SelectProjectByProjectId;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.ReadJarMap;
import com.ibm.research.msr.utils.Util;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AnalysisService.class, DatabaseConnection.class, FileUtils.class, Util.class, File.class })
public class AnalysisServiceTest {

	private static final String PROJECT_ID = new ObjectId().toHexString();

	AnalysisService analysisService = new AnalysisService();

	@Test
	public void getInterClassUsageExceptionTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		when(selectProjectByProjectId.execute()).thenThrow(new RuntimeException());

		JSONObject jsonObject = analysisService.getInterClassUsage(PROJECT_ID);

		assertEquals("Project not registerd", jsonObject.get("status"));
	}

	@Test
	public void getInterClassUsageFileNotFoundTest() throws Exception {
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

		JSONObject jsonObject = analysisService.getInterClassUsage(PROJECT_ID);

		assertEquals("Project files not found", jsonObject.get("status"));
	}

	@Test
	public void getInterClassUsageTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(project.getCodeType()).thenReturn(Constants.TYPE_SRC);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);

		InterClassUsageFinder interClassUsageFinder = mock(InterClassUsageFinder.class);
		whenNew(InterClassUsageFinder.class).withAnyArguments().thenReturn(interClassUsageFinder);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withNoArguments().thenReturn(jsonParser);

		when(jsonParser.parse(Mockito.any(Reader.class))).thenReturn(new JSONObject());

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		InsertIntoAnalysisQuery insertIntoAnalysisQuery = mock(InsertIntoAnalysisQuery.class);
		whenNew(InsertIntoAnalysisQuery.class).withAnyArguments().thenReturn(insertIntoAnalysisQuery);

		JSONObject jsonObject = analysisService.getInterClassUsage(PROJECT_ID);

		assertEquals("Successfully analyzed Inter-class usage.", jsonObject.get("status"));
	}

	@Test
	public void getJarAPIInfoExceptionTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		when(selectProjectByProjectId.execute()).thenThrow(new RuntimeException());

		JSONObject jsonObject = analysisService.getJarAPIInfo(PROJECT_ID);

		assertEquals("Project not registerd", jsonObject.get("status"));
	}

	@Test
	public void getJarAPIInfoFileNotFoundTest() throws Exception {
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

		JSONObject jsonObject = analysisService.getJarAPIInfo(PROJECT_ID);

		assertEquals("Project files not found", jsonObject.get("status"));
	}

	@Test
	public void getJarAPIInfoWithPomTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(project.getCodeType()).thenReturn(Constants.TYPE_SRC);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);
		when(file.getName()).thenReturn("pom.xml");

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		POMDependencyDownloader pomDependencyDownloader = mock(POMDependencyDownloader.class);
		whenNew(POMDependencyDownloader.class).withAnyArguments().thenReturn(pomDependencyDownloader);

		MavenCategoryEtAlExtractor mavenCategoryEtAlExtractor = mock(MavenCategoryEtAlExtractor.class);
		whenNew(MavenCategoryEtAlExtractor.class).withAnyArguments().thenReturn(mavenCategoryEtAlExtractor);

		mockStatic(Util.class);

		when(Util.dumpAPIInfo(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		APIUsageStatsMiner apiUsageStatsMiner = mock(APIUsageStatsMiner.class);
		whenNew(APIUsageStatsMiner.class).withAnyArguments().thenReturn(apiUsageStatsMiner);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withNoArguments().thenReturn(jsonParser);

		when(jsonParser.parse(Mockito.any(Reader.class))).thenReturn(new JSONObject());

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		Analysis analysis = mock(Analysis.class);
		whenNew(Analysis.class).withAnyArguments().thenReturn(analysis);

		InsertIntoAnalysisQuery insertIntoAnalysisQuery = mock(InsertIntoAnalysisQuery.class);
		whenNew(InsertIntoAnalysisQuery.class).withAnyArguments().thenReturn(insertIntoAnalysisQuery);

		JSONObject jsonObject = analysisService.getJarAPIInfo(PROJECT_ID);

		assertEquals("Successfully collected Jar API details.", jsonObject.get("status"));

	}

	@Test
	public void getJarAPIInfoWithGradleTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(project.getCodeType()).thenReturn(Constants.TYPE_SRC);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.exists()).thenReturn(true);
		when(file.getName()).thenReturn("gradle");

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		GradleDependencyDownloader gradleDependencyDownloader = mock(GradleDependencyDownloader.class);
		whenNew(GradleDependencyDownloader.class).withAnyArguments().thenReturn(gradleDependencyDownloader);
		ArrayList<String> list = new ArrayList<>();
		list.add("pom.xml");
		when(gradleDependencyDownloader.createPOMFiles(Mockito.any(), Mockito.any())).thenReturn(list);

		POMDependencyDownloader pomDependencyDownloader = mock(POMDependencyDownloader.class);
		whenNew(POMDependencyDownloader.class).withAnyArguments().thenReturn(pomDependencyDownloader);

		MavenCategoryEtAlExtractor mavenCategoryEtAlExtractor = mock(MavenCategoryEtAlExtractor.class);
		whenNew(MavenCategoryEtAlExtractor.class).withAnyArguments().thenReturn(mavenCategoryEtAlExtractor);

		mockStatic(Util.class);

		when(Util.dumpAPIInfo(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		APIUsageStatsMiner apiUsageStatsMiner = mock(APIUsageStatsMiner.class);
		whenNew(APIUsageStatsMiner.class).withAnyArguments().thenReturn(apiUsageStatsMiner);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withNoArguments().thenReturn(jsonParser);

		when(jsonParser.parse(Mockito.any(Reader.class))).thenReturn(new JSONObject());

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		Analysis analysis = mock(Analysis.class);
		whenNew(Analysis.class).withAnyArguments().thenReturn(analysis);

		InsertIntoAnalysisQuery insertIntoAnalysisQuery = mock(InsertIntoAnalysisQuery.class);
		whenNew(InsertIntoAnalysisQuery.class).withAnyArguments().thenReturn(insertIntoAnalysisQuery);

		JSONObject jsonObject = analysisService.getJarAPIInfo(PROJECT_ID);

		assertEquals("Successfully collected Jar API details.", jsonObject.get("status"));

	}

	@Test
	public void getJarAPIInfoTypeBinTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		Project project = mock(Project.class);
		when(project.getCodeType()).thenReturn(Constants.TYPE_BIN);
		when(selectProjectByProjectId.getResult()).thenReturn(project);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		mockStatic(Util.class);

		when(Util.dumpAPIInfo(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		APIUsageStatsMiner apiUsageStatsMiner = mock(APIUsageStatsMiner.class);
		whenNew(APIUsageStatsMiner.class).withAnyArguments().thenReturn(apiUsageStatsMiner);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withNoArguments().thenReturn(jsonParser);

		when(jsonParser.parse(Mockito.any(Reader.class))).thenReturn(new JSONObject());

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		Analysis analysis = mock(Analysis.class);
		whenNew(Analysis.class).withAnyArguments().thenReturn(analysis);

		InsertIntoAnalysisQuery insertIntoAnalysisQuery = mock(InsertIntoAnalysisQuery.class);
		whenNew(InsertIntoAnalysisQuery.class).withAnyArguments().thenReturn(insertIntoAnalysisQuery);

		JSONObject jsonObject = analysisService.getJarAPIInfo(PROJECT_ID);

		assertEquals("Successfully collected Jar API details.", jsonObject.get("status"));

	}

	@Test
	public void getAPIUsageInfoExceptionTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		SelectProjectByProjectId selectProjectByProjectId = mock(SelectProjectByProjectId.class);
		whenNew(SelectProjectByProjectId.class).withAnyArguments().thenReturn(selectProjectByProjectId);

		when(selectProjectByProjectId.execute()).thenThrow(new RuntimeException());

		JSONObject jsonObject = analysisService.getAPIUsageInfo(PROJECT_ID);

		assertEquals("Project not registerd", jsonObject.get("status"));
	}

	@Test
	public void getAPIUsageInfoFileNotFoundTest() throws Exception {
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

		JSONObject jsonObject = analysisService.getAPIUsageInfo(PROJECT_ID);

		assertEquals("Project files not found", jsonObject.get("status"));
	}

	@Test
	public void getAPIUsageInfoTest() throws Exception {
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

		ReadJarMap readJarMap = mock(ReadJarMap.class);
		whenNew(ReadJarMap.class).withNoArguments().thenReturn(readJarMap);

		AnalyzeApp analyzeApp = mock(AnalyzeApp.class);
		whenNew(AnalyzeApp.class).withAnyArguments().thenReturn(analyzeApp);

		JSONParser jsonParser = mock(JSONParser.class);
		whenNew(JSONParser.class).withNoArguments().thenReturn(jsonParser);

		when(jsonParser.parse(Mockito.any(Reader.class))).thenReturn(new JSONObject());

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		whenNew(Analysis.class).withAnyArguments().thenReturn(analysis);

		InsertIntoAnalysisQuery insertIntoAnalysisQuery = mock(InsertIntoAnalysisQuery.class);
		whenNew(InsertIntoAnalysisQuery.class).withAnyArguments().thenReturn(insertIntoAnalysisQuery);

		JSONObject jsonObject = analysisService.getAPIUsageInfo(PROJECT_ID);

		assertEquals("Successfully analyzed API usage.", jsonObject.get("status"));
	}

	@Test
	public void extractICUTest() throws Exception {
		MongoDatabase database = DatabaseConnection.getDatabase();
		SourceProject sourceProject = mock(SourceProject.class);
		when(sourceProject.getProjectLocation()).thenReturn("src/test/resources/apps/source/5f6888acab353118f79ea34e");
		when(sourceProject.getRootAnalyzePath()).thenReturn("");
		when(sourceProject.getCodeType()).thenReturn("bin");
		whenNew(InterClassUsageFinder.class).withAnyArguments().thenReturn(mock(InterClassUsageFinder.class));
		AnalysisService.extractICU("5f6888acab353118f79ea34e", database, sourceProject);
	}

}
