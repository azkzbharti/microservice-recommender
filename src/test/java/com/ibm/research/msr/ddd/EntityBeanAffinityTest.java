package com.ibm.research.msr.ddd;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ EntityBeanAffinity.class, Runtime.class })
public class EntityBeanAffinityTest {

	EntityBeanAffinity affinity = new EntityBeanAffinity();

	@Test
	public void readEntryPoint_newformatTest() throws Exception {
		
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		JSONParser parser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(parser);
		
		JSONArray jsonArray = mock(JSONArray.class);
		
		JSONObject jsonObject = mock(JSONObject.class);
		when(jsonObject.get(Mockito.eq("service_entry_name"))).thenReturn("epname");
		when(jsonObject.get(Mockito.any())).thenReturn(jsonArray);
		
		jsonArray.add(jsonObject);
		
		when(parser.parse(Mockito.any(FileReader.class))).thenReturn(jsonArray);
		
		ReflectionTestUtils.invokeMethod(affinity, "readEntryPoint_newformat", "epFile");
		
	}
	
	@Test
	public void readBOPathInputTest1() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
		
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);
		
		when(br.readLine()).thenReturn("val", null);
		
		ReflectionTestUtils.invokeMethod(affinity, "readBOPathInput", "path");
	}
	
	@Test
	public void readBOPathInputTest2() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
		
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.exists()).thenReturn(true);
		
		ReflectionTestUtils.invokeMethod(affinity, "readBOPathInput", "path");
	}
	
	@Test
	public void readEntryPointTest() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		JSONParser parser = mock(JSONParser.class);
		whenNew(JSONParser.class).withAnyArguments().thenReturn(parser);
		
		JSONArray jsonArray = mock(JSONArray.class);
		jsonArray.add("val");
		
		JSONObject jsonObject = mock(JSONObject.class);
		when(jsonObject.keySet()).thenReturn(new HashSet<>(Arrays.asList("key")));
		when(jsonObject.get(Mockito.any())).thenReturn(jsonArray);
		
		when(parser.parse(Mockito.any(FileReader.class))).thenReturn(jsonObject);
		
		Object object = ReflectionTestUtils.invokeMethod(affinity, "readEntryPoint", "epFile");
		
		Assert.assertNotNull(object);
	}
	
	@Test
	public void writeToCoDataAccessFileTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getCanonicalPath()).thenReturn("canonical");
		
		FileWriter fw = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fw);
		
		BufferedWriter bw = mock(BufferedWriter.class);
		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bw);
		
		SortedSet<String> sortedSet = new TreeSet<>();
		sortedSet.add("val");
		
		HashMap<String,List<SortedSet<String>>> map = new HashMap<>();
		map.put("key", Arrays.asList(sortedSet));
		
		ReflectionTestUtils.setField(affinity, "ep_bean_pair_acc", map);
		
		ReflectionTestUtils.invokeMethod(affinity, "writeToCoDataAccessFile", "path");
	}
	
	@Test
	public void writeToSeedFileTest1() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getCanonicalPath()).thenReturn("canonical");
		
		FileWriter fw = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fw);
		
		BufferedWriter bw = mock(BufferedWriter.class);
		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bw);
		
		Set<String> set = new HashSet<>();
		set.add("val");
		
		Map<String, Set<String>> map = new HashMap<>();
		map.put("key", set);
		
		ReflectionTestUtils.invokeMethod(affinity, "writeToSeedFile", map, "path");
	}
	
	@Test
	public void writeToSeedFileTest2() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getCanonicalPath()).thenReturn("canonical");
		
		FileWriter fw = mock(FileWriter.class);
		whenNew(FileWriter.class).withAnyArguments().thenReturn(fw);
		
		BufferedWriter bw = mock(BufferedWriter.class);
		whenNew(BufferedWriter.class).withAnyArguments().thenReturn(bw);
		
		Set<String> set = new HashSet<>();
		set.add("val");
		
		Map<String, Set<String>> map = new HashMap<>();
		map.put("key", set);
		
		ReflectionTestUtils.setField(affinity, "seed_map", map);
		
		ReflectionTestUtils.invokeMethod(affinity, "writeToSeedFile", "path");
	}
	
	@Test
	public void filterHTMLendpointsTest() {
		
		Set<String> set = new HashSet<>();
		set.add("html");
		
		Map<String, Set<String>> map = new HashMap<>();
		map.put("key", set);
		
		ReflectionTestUtils.setField(affinity, "epmap", map);
		
		Object object = ReflectionTestUtils.invokeMethod(affinity, "filterHTMLendpoints");
		
		Assert.assertNotNull(object);
	}
	
	@Test
	public void collectEPMethodCallsInHTMLsTest() {
		Set<String> set = new HashSet<>();
		set.add("html");
		
		Map<String, Set<String>> map = new HashMap<>();
		map.put("key", set);
		
		ReflectionTestUtils.setField(affinity, "epmap", map);
		
		Object object = ReflectionTestUtils.invokeMethod(affinity, "collectEPMethodCallsInHTMLs");
		
		Assert.assertNotNull(object);
	}
	
	@Test
	public void computeEntityAffinityGroupTest() throws Exception {
		
		mockStatic(Runtime.class);
		Runtime runtime = mock(Runtime.class);
		Process process = mock(Process.class);
		
		when(Runtime.getRuntime()).thenReturn(runtime);
		
		when(runtime.exec(Mockito.anyString())).thenReturn(process);
		
		InputStream inputStream = mock(InputStream.class);
		when(process.getErrorStream()).thenReturn(inputStream);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		
		List<Set<String>> computeEntityAffinityGroup = affinity.computeEntityAffinityGroup();
		
		Assert.assertNotNull(computeEntityAffinityGroup);
	}
}
