package com.ibm.research.msr.jarlist;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.ibm.research.appmod.pa.binaryextractor.ReferencedClassesExtractor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIUsageStatsMiner.class, FileUtils.class, ReferencedClassesExtractor.class })
public class APIUsageStatsMinerTest {

	APIUsageStatsMiner miner = new APIUsageStatsMiner();

	@Test
	public void mineTest() throws Exception {

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		when(br.readLine()).thenReturn("header", "jar,,class", null);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		OverallProjectLibUsageStats overallProjectLibUsageStats = mock(OverallProjectLibUsageStats.class);
		whenNew(OverallProjectLibUsageStats.class).withAnyArguments().thenReturn(overallProjectLibUsageStats);

		miner.mine("srcRoot", "jarToPkgsClassesCsv", "opJSONFileNameWithPath");
	}

	@Test
	public void processOneFileTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		ReflectionTestUtils.setField(miner, "classPathEntries", new String[] {});

		miner.processOneFile(file, "srcRoot");
	}

	@Test
	public void mineFromClassFilesTest() throws Exception {

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		when(br.readLine()).thenReturn("header", "jar,,class", null);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		OverallProjectLibUsageStats overallProjectLibUsageStats = mock(OverallProjectLibUsageStats.class);
		whenNew(OverallProjectLibUsageStats.class).withAnyArguments().thenReturn(overallProjectLibUsageStats);

		ReferencedClassesExtractor referencedClassesExtractor = mock(ReferencedClassesExtractor.class);
		whenNew(ReferencedClassesExtractor.class).withAnyArguments().thenReturn(referencedClassesExtractor);

		when(referencedClassesExtractor.getFullyQualifiedClassName(Mockito.anyString())).thenReturn("name");

		mockStatic(ReferencedClassesExtractor.class);
		
		miner.mineFromClassFiles("classFilesRoot", "jarToPkgsClassesCsv", "opJSONFileNameWithPath");
	}

}
