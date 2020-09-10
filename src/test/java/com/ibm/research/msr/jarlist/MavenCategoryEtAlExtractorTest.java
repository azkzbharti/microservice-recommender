package com.ibm.research.msr.jarlist;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MavenCategoryEtAlExtractor.class, FileUtils.class, Jsoup.class})
public class MavenCategoryEtAlExtractorTest {

	MavenCategoryEtAlExtractor extractor = new MavenCategoryEtAlExtractor();

	@Test
	public void findTest() throws Exception {

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getName()).thenReturn("jar");
		when(file.getAbsolutePath()).thenReturn("jar");

		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		JarFile jarFile = mock(JarFile.class);
		whenNew(JarFile.class).withAnyArguments().thenReturn(jarFile);

		JarEntry jarEntry = mock(JarEntry.class);

		Enumeration<JarEntry> enumeration = Collections.enumeration(Arrays.asList(jarEntry));

		when(jarFile.entries()).thenReturn(enumeration);
		
		when(jarEntry.getName()).thenReturn("META-INF/maven/");

		extractor.find("jarRootPath", "outputJSONFile");
	}
	
	@Test
	public void extractFromHTMLPageTest() throws IOException {
		mockStatic(Jsoup.class);
		Connection connection = mock(Connection.class);
		
		when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);
		
		Document document = mock(Document.class);
		
		when(connection.get()).thenReturn(document);
		
		extractor.extractFromHTMLPage("sUrl", extractor.new MavenCategoryEtAl("name"));
	}
	
	@Test
	public void extractDivTextTest() {
		Document document = mock(Document.class);
		Elements elements = mock(Elements.class);
		
		when(document.select(Mockito.anyString())).thenReturn(elements);
		
		Element element = mock(Element.class);
		
		when(elements.get(0)).thenReturn(element);
		
		when(element.text()).thenReturn("text");
		
		String text = extractor.extractDivText(document, "divClass");
		
		Assert.assertEquals("text", text);
	}
	
	@Test
	public void findMavenRepositoryHomePageByGoogleSearchTest() {
		extractor.findMavenRepositoryHomePageByGoogleSearch("query");
	}

}
