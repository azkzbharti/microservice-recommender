package com.ibm.research.msr.jarlist;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.FileReader;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({POMDependencyDownloader.class, Jsoup.class})
public class POMDependencyDownloaderTest {
	
	POMDependencyDownloader downloader = new POMDependencyDownloader();

	@Test
	public void downloadTest() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);

		BufferedReader br = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(br);
		
		Document document = mock(Document.class);
		Elements elements = mock(Elements.class);
		
		mockStatic(Jsoup.class);
		
		Connection connection = mock(Connection.class);
		
		when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);
		
		when(connection.get()).thenReturn(document);
		
		when(Jsoup.parse(Mockito.anyString(), Mockito.anyString(), Mockito.any())).thenReturn(document);
		
		when(document.getAllElements()).thenReturn(elements);
		when(document.html()).thenReturn("html");
		
		Element element = mock(Element.class);
		when(elements.size()).thenReturn(1, 5);
		when(elements.get(Mockito.anyInt())).thenReturn(element);
		when(element.tagName()).thenReturn("dependency", "groupId");
		when(element.getAllElements()).thenReturn(elements);
		when(element.text()).thenReturn("groupId");
		
		downloader.download("pomFileWithPath", "downloadPath", true);
	}

}
