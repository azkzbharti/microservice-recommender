package com.ibm.research.msr.clustering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.msr.extraction.Document;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClusterDetails.class})
public class ClusterDetailsTest {

	@Test
	public void getListOfDocumentsNamesTest() {
		Document document = mock(Document.class);
		when(document.getUniqueName()).thenReturn("uniqueName");
		ClusterDetails clusterDetails = new ClusterDetails(document);
		List<String> listOfDocumentsNames = clusterDetails.getListOfDocumentsNames();
		assertEquals("uniqueName", listOfDocumentsNames.get(0));
	}
	
	@Test
	public void removeDuplicatesTest() {
		Document document = mock(Document.class);
		List<Document> list = new ArrayList<>();
		list.add(document);
		list.add(document);
		ClusterDetails clusterDetails = new ClusterDetails(list);
		clusterDetails.removeDuplicates();
		List<Document> listOfDocuments = clusterDetails.getListOfDocuments();
		assertEquals(1, listOfDocuments.size());
	}
	
	@Test
	public void getListOfShortDocumentsNamesTest() {
		Document document = mock(Document.class);
		when(document.getUniqueName()).thenReturn("uniqueName.doc");
		ClusterDetails clusterDetails = new ClusterDetails(document);
		List<String> listOfDocumentsNames = clusterDetails.getListOfShortDocumentsNames();
		assertEquals("doc", listOfDocumentsNames.get(0));
	}
	
	@Test
	public void getNoIntersectionTest() {
		Document document = mock(Document.class);
		when(document.getName()).thenReturn("name");
		ClusterDetails c1 = new ClusterDetails(document);
		ClusterDetails c2 = new ClusterDetails(document);
		int noIntersection = c2.getNoIntersection(c1);
		assertEquals(1, noIntersection);
	}
	
	@Test
	public void setClusterNameTest() {
		Map<String, Integer> tokenMap = new HashMap<>();
		tokenMap.put("clusters", 1);
		Document document = mock(Document.class);
		when(document.getTokenCountMap()).thenReturn(tokenMap);
		List<Document> list = new ArrayList<>();
		list.add(document);
		ClusterDetails clusterDetails = new ClusterDetails(list);
		clusterDetails.setClusterName();
		assertEquals("cluster", clusterDetails.getClusterName());
	}
	
	@Test
	public void getClusterJsonTest() {
		Document document = mock(Document.class);
		when(document.getTokens()).thenReturn(Arrays.asList("token"));
		List<Document> list = new ArrayList<>();
		list.add(document);
		ClusterDetails clusterDetails = new ClusterDetails(list);
		clusterDetails.setClusterName("cluster");
		JSONObject clusterJson = clusterDetails.getClusterJson(1);
		assertNotNull(clusterJson);
	}
	
	@Test
	public void getCustomJsonTest() {
		Document document = mock(Document.class);
		ClusterDetails clusterDetails = new ClusterDetails(document);
		JSONObject customJson = clusterDetails.getCustomJson(1);
		assertNotNull(customJson);
	}

}
