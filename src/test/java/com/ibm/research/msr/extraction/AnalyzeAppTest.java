package com.ibm.research.msr.extraction;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.utils.DocumentParserUtil;
import com.opencsv.CSVReader;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AnalyzeApp.class, FileUtils.class })
public class AnalyzeAppTest {

	@Test
	public void analyzeAppTest() throws Exception {
		DocumentParserUtil documentParserUtil = mock(DocumentParserUtil.class);
		whenNew(DocumentParserUtil.class).withAnyArguments().thenReturn(documentParserUtil);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.isDirectory()).thenReturn(true);

		mockStatic(FileUtils.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		Document document = mock(Document.class);
		whenNew(Document.class).withAnyArguments().thenReturn(document);

		when(document.getTokens()).thenReturn(Arrays.asList("token"));
		when(document.getNumberOfTokens()).thenReturn(1);

		new AnalyzeApp("appPath", "appType", "outputPath", new HashMap<>());
	}

	@Test
	public void saveMeasureTest() throws Exception {

		DocumentParserUtil documentParserUtil = mock(DocumentParserUtil.class);
		whenNew(DocumentParserUtil.class).withAnyArguments().thenReturn(documentParserUtil);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.isDirectory()).thenReturn(true);

		mockStatic(FileUtils.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		Document document = mock(Document.class);
		whenNew(Document.class).withAnyArguments().thenReturn(document);

		when(document.getTokens()).thenReturn(Arrays.asList("token"));
		when(document.getNumberOfTokens()).thenReturn(1);

		AnalyzeApp analyzeApp = new AnalyzeApp("appPath", "appType", "outputPath", new HashMap<>());

		when(document.getFile()).thenReturn(file);
		when(document.getDocVector()).thenReturn(Arrays.asList(1.0d));

		analyzeApp.saveMeasure("measurePath");
	}

	@Test
	public void read_measure_tf_fileTest() throws Exception {
		DocumentParserUtil documentParserUtil = mock(DocumentParserUtil.class);
		whenNew(DocumentParserUtil.class).withAnyArguments().thenReturn(documentParserUtil);

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.isDirectory()).thenReturn(true);

		mockStatic(FileUtils.class);

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		Document document = mock(Document.class);
		whenNew(Document.class).withAnyArguments().thenReturn(document);

		when(document.getTokens()).thenReturn(Arrays.asList("token"));
		when(document.getNumberOfTokens()).thenReturn(1);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		CSVReader csvReader = mock(CSVReader.class);
		whenNew(CSVReader.class).withAnyArguments().thenReturn(csvReader);

		when(csvReader.readNext()).thenReturn(new String[] { "header" }, new String[] { "line1", "line2", "line3" }, null);

		AnalyzeApp analyzeApp = new AnalyzeApp("appPath", "appType", "outputPath", new HashMap<>());

		analyzeApp.read_measure_tf_file("appPath2");
	}

}
