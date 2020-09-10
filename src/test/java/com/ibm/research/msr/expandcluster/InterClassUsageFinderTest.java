package com.ibm.research.msr.expandcluster;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ InterClassUsageFinder.class, FileUtils.class })
public class InterClassUsageFinderTest {

	InterClassUsageFinder finder = new InterClassUsageFinder();

	@Test
	public void loaderTest() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		JSONParser parser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(parser);

		JSONObject jsonObject1 = mock(JSONObject.class);
		when(parser.parse(Mockito.any(FileReader.class))).thenReturn(jsonObject1);

		when(jsonObject1.keySet()).thenReturn(new HashSet<>(Arrays.asList("key1")));

		JSONObject jsonObject2 = mock(JSONObject.class);
		when(jsonObject1.get(Mockito.any())).thenReturn(jsonObject2);

		JSONObject jsonObject3 = mock(JSONObject.class);
		when(jsonObject2.get(Mockito.eq("usedClassesToCount"))).thenReturn(jsonObject3);

		when(jsonObject3.keySet()).thenReturn(new HashSet<>(Arrays.asList("key1")));

		when(jsonObject3.get(Mockito.any())).thenReturn(1L);

		Map<ClassPair, Integer> map = finder.loader("jsonPath");

		assertTrue(map.containsValue(1));
	}

	@Test
	public void findTest() throws Exception {

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		when(file.getAbsolutePath()).thenReturn("absPath");
		
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
		
		finder.find("srcFilesRoot", "outPath", "optionalUserPath");
	}
	

}
