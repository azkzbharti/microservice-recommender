package com.ibm.research.msr.api;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.utils.Constants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUtilities.class, HttpClients.class, EntityUtils.class, FileUtils.class,
	Runtime.class})
public class APIUtilitiesTest {

	@Test
	public void sendPostTest() throws Exception {
		HttpPost httpPost = mock(HttpPost.class);
		whenNew(HttpPost.class).withAnyArguments().thenReturn(httpPost);

		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);

		mockStatic(HttpClients.class);
		when(HttpClients.createDefault()).thenReturn(httpClient);
		CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
		when(httpClient.execute(Mockito.any())).thenReturn(closeableHttpResponse);

		mockStatic(EntityUtils.class);
		when(EntityUtils.toString(Mockito.any())).thenReturn("result");

		assertEquals("result", APIUtilities.sendPost("url", "req", "contentType"));
	}

	@Test
	public void runAffinityJavaTest() {

		File file = mock(File.class);
		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		when(file.getName()).thenReturn("file.java");

		APIUtilities.runAffinity("rootPath", "outputJSONFile", "tempFolder", Constants.TYPE_SRC);
	}
	
	@Test
	public void runAffinityClassTest() {

		File file = mock(File.class);
		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		when(file.getName()).thenReturn("file.class");

		APIUtilities.runAffinity("rootPath", "outputJSONFile", "tempFolder", "type");
	}
	
	@Test
	public void runTriggerTest() throws Exception {
		
		mockStatic(Runtime.class);
		Runtime runtime = mock(Runtime.class);
		Process process = mock(Process.class);
		
		when(Runtime.getRuntime()).thenReturn(runtime);
		
		when(runtime.exec(Mockito.anyString())).thenReturn(process);
		
		InputStream inputStream = mock(InputStream.class);
		when(process.getErrorStream()).thenReturn(inputStream);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		
		APIUtilities.runTrigger("rootPath", "originalGraph", "tempFolder", "interclass", "type", "projectPath",
				"visPath", "serviceFilePath", "userEditGraph");
	}
	
	@Test
	public void runCommunityTest() throws Exception {
		
		mockStatic(Runtime.class);
		Runtime runtime = mock(Runtime.class);
		Process process = mock(Process.class);
		
		when(Runtime.getRuntime()).thenReturn(runtime);
		
		when(runtime.exec(Mockito.anyString())).thenReturn(process);
		
		InputStream inputStream = mock(InputStream.class);
		when(process.getErrorStream()).thenReturn(inputStream);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		
		APIUtilities.runCommunity("rootPath", "originalGraph", "tempFolder", "interclass", "type", "projectPath",
				"visPath", "transactionFilePath", "serviceFilePath");
	}
	
	@Test
	public void deleteDirectoryTest() {
		File file = mock(File.class);
		File file1 = mock(File.class);
		File file2 = mock(File.class);
		
		when(file.listFiles()).thenReturn(new File[] {file1, file2});
		
		when(file1.getName()).thenReturn("file.class");
		when(file2.getName()).thenReturn("file.java");
		
		APIUtilities.deleteDirectory(file, "file.java");
	}

}
