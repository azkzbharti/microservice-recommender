package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.research.msr.db.DatabaseConnection;
import com.ibm.research.msr.db.queries.project.InsertIntoProjectQuery;
import com.ibm.research.msr.git.GitConnect;
import com.ibm.research.msr.utils.Constants.ProjectStatus;
import com.ibm.research.msr.utils.UnzipUtility;
import com.mongodb.client.MongoDatabase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RegistrationService.class, DatabaseConnection.class, FileUtils.class, GitConnect.class,
		UnzipUtility.class, AnalysisService.class, APIUtilities.class })
public class RegistrationServiceTest {

	RegistrationService registrationService = new RegistrationService();

	@Test
	public void registerProjectWithHttpTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		mockStatic(FileUtils.class);
		mockStatic(GitConnect.class);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "srcLanguage",
				"http://gitURL", "analyseRootPath", "branch");

		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}

	@Test
	public void registerProjectWithSshTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		mockStatic(FileUtils.class);
		mockStatic(GitConnect.class);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "srcLanguage",
				"ssh://gitURL", "analyseRootPath", "branch");

		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}

	@Test
	public void registerProjectEarWithHttpTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		mockStatic(FileUtils.class);
		mockStatic(GitConnect.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		mockStatic(AnalysisService.class);
		mockStatic(APIUtilities.class);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "http://gitURL",
				"token", "branch");

		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}

	@Test
	public void registerProjectEarWithSshTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		mockStatic(FileUtils.class);
		mockStatic(GitConnect.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		mockStatic(AnalysisService.class);
		mockStatic(APIUtilities.class);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "ssh://gitURL",
				"token", "branch");

		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}

	@Test
	public void registerProjectEarTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		MultipartFile multipartFile = mock(MultipartFile.class);
		when(multipartFile.getOriginalFilename()).thenReturn("file.ear");

		mockStatic(UnzipUtility.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "srcLanguage",
				multipartFile);
		
		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}
	
	@Test
	public void registerProjectZipTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);

		MultipartFile multipartFile = mock(MultipartFile.class);
		when(multipartFile.getOriginalFilename()).thenReturn("file.zip");

		mockStatic(UnzipUtility.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);

		JSONObject jsonObject = registrationService.registerProject("projectName", "projectDesc", "srcLanguage",
				multipartFile);
		
		assertEquals(objectId.toString(), jsonObject.get("projectID"));
	}
	
	@Test
	public void registerLocalProjectJarTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getName()).thenReturn("file.jar");
		
		Collection<File> collection = new ArrayList<>();
		collection.add(file);
		
		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		mockStatic(UnzipUtility.class);
		mockStatic(AnalysisService.class);
		mockStatic(APIUtilities.class);
		
		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);
		
		JSONObject jsonObject = registrationService.registerLocalProject("projectName", "projectDesc", "/temp");
		
		assertEquals(objectId.toString(), jsonObject.get("projectID"));
		assertEquals(ProjectStatus.OK, jsonObject.get("Status"));
	}
	
	@Test
	public void registerLocalProjectZipTest() throws Exception {
		mockStatic(DatabaseConnection.class);
		MongoDatabase mongoDatabase = mock(MongoDatabase.class);
		when(DatabaseConnection.getDatabase()).thenReturn(mongoDatabase);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getName()).thenReturn("file.zip");
		
		Collection<File> collection = new ArrayList<>();
		collection.add(file);
		
		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		mockStatic(UnzipUtility.class);
		mockStatic(AnalysisService.class);
		mockStatic(APIUtilities.class);
		
		InsertIntoProjectQuery ip = mock(InsertIntoProjectQuery.class);
		whenNew(InsertIntoProjectQuery.class).withAnyArguments().thenReturn(ip);

		ObjectId objectId = new ObjectId();

		when(ip.execute()).thenReturn(objectId);
		
		JSONObject jsonObject = registrationService.registerLocalProject("projectName", "projectDesc", "/temp/test.zip");
		
		assertEquals(objectId.toString(), jsonObject.get("projectID"));
		assertEquals(ProjectStatus.OK, jsonObject.get("Status"));
	}

}
