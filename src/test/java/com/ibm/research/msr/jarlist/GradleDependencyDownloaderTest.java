package com.ibm.research.msr.jarlist;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GradleDependencyDownloader.class, TransformerFactory.class, IOUtils.class })
public class GradleDependencyDownloaderTest {

	GradleDependencyDownloader downloader = new GradleDependencyDownloader();

	@Test
	public void createPOMFilesTest() throws Exception {
		ArrayList<String> list = new ArrayList<>();
		list.add("gradle");

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		mockStatic(TransformerFactory.class);

		TransformerFactory transformerFactory = mock(TransformerFactory.class);

		when(TransformerFactory.newInstance()).thenReturn(transformerFactory);

		Transformer transformer = mock(Transformer.class);

		when(transformerFactory.newTransformer()).thenReturn(transformer);

		ArrayList<String> pomFiles = downloader.createPOMFiles(list, "jarFolder");

		Assert.assertNotNull(pomFiles);
	}

	@Test
	public void findDependenciesByPlainTextParsingTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.isDirectory()).thenReturn(true);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);

		StreamResult stream = mock(StreamResult.class);
		whenNew(StreamResult.class).withAnyArguments().thenReturn(stream);

		mockStatic(TransformerFactory.class);

		TransformerFactory transformerFactory = mock(TransformerFactory.class);

		when(TransformerFactory.newInstance()).thenReturn(transformerFactory);

		Transformer transformer = mock(Transformer.class);

		when(transformerFactory.newTransformer()).thenReturn(transformer);

		downloader.findDependenciesByPlainTextParsing("projectRoot");
	}

	@Test
	public void download2Test() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		FileInputStream fileInput = mock(FileInputStream.class);
		whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInput);

		mockStatic(IOUtils.class);

		when(IOUtils.toString(Mockito.any(FileInputStream.class), Mockito.any(String.class))).thenReturn("result");
		downloader.download2("projectRoot");
	}
	
	@Test
	public void downloadTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		when(file.isDirectory()).thenReturn(true);

		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
		
		FileInputStream fileInput = mock(FileInputStream.class);
		whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInput);
		
		mockStatic(IOUtils.class);

		when(IOUtils.toString(Mockito.any(FileInputStream.class), Mockito.any(String.class))).thenReturn("result");
		
		Assert.assertTrue(downloader.download("projectRoot"));
	}

}
