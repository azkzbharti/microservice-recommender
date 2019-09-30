package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.ibm.research.msr.extraction.Document;
import com.ibm.research.msr.utils.DocumentParserUtil;

public class ClusterDetails {

	List<Document> listOfDocuments = new ArrayList<>();
	double score;
	String clusterName;

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}

	public ClusterDetails() {
		listOfDocuments = new ArrayList<>();
	}

	public ClusterDetails(Document doc) {
		listOfDocuments = new ArrayList<>();
		listOfDocuments.add(doc);
	}

	public ClusterDetails(List<Document> docsList) {
		Set<Document> s = new HashSet<Document>();
		s.addAll(docsList);
//		 this.listOfDocuments = new ArrayList<Document>();
		listOfDocuments = new ArrayList<Document>(s);
	}

	public ClusterDetails(List<Document> docsList, double score) {
		Set<Document> s = new HashSet<Document>();
		s.addAll(docsList);
		listOfDocuments = new ArrayList<Document>(s);
		this.score = score;
	}

	public void removeDoc(Document doc) {
		this.listOfDocuments.remove(doc);

	}

	public void removeDuplicates() {
		Set<Document> s = new HashSet<Document>();
		s.addAll(this.listOfDocuments);
		this.listOfDocuments = new ArrayList<Document>();
		this.listOfDocuments.addAll(s);

	}

	public List<Document> getListOfDocuments() {
		return listOfDocuments;
	}

	public List<String> getListOfDocumentsNames() {
		List<String> docnames;
		docnames = listOfDocuments.stream().map(Document::getUniqueName).collect(Collectors.toList());

		return docnames;
	}

	public List<String> getListOfShortDocumentsNames() {
		List<String> docnames;
		docnames = listOfDocuments.stream().map(Document::getUniqueName).collect(Collectors.toList());

		List<String> shortNames = new ArrayList<String>();
		for (String s : docnames) {
			s = s.substring(s.lastIndexOf(".") + 1);
			shortNames.add(s);
		}

		return shortNames;
	}

	public void setListOfDocuments(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}

	public void assignDocToCluster(Document doc) {
		this.listOfDocuments.add(doc);

	}

	public void addDocumentToCluster(Document doc) {
		if (!listOfDocuments.contains(doc))
			this.listOfDocuments.add(doc);

	}

	public void showDetails() {
		for (Document doc : listOfDocuments) {
			System.out.println(doc.getName());
		}
	}

	public int getNoIntersection(ClusterDetails c1) {
		Set<String> docsNames1 = this.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> docsNames2 = c1.getListOfDocuments().stream().map(Document::getName).collect(Collectors.toSet());
		Set<String> intersectionNamesSet = Sets.intersection(docsNames1, docsNames2);
		return intersectionNamesSet.size();
	}

	public void setClusterName() {
		// TODO: improve this logic
		Map<String, Integer> alltokens = new HashMap();
		String clusterName = "";
		for (Document doc : listOfDocuments) {
			Map<String, Integer> tMap = doc.getTokenCountMap();
			for (String key : tMap.keySet()) {
				if (alltokens.containsKey(key))
					alltokens.put(key, alltokens.get(key) + (tMap.get(key) / listOfDocuments.size()));
				else
					alltokens.put(key, tMap.get(key) / listOfDocuments.size());
			}
			alltokens.putAll(doc.getTokenCountMap());
		}
		if (alltokens.containsKey("None") && alltokens.get("None") == listOfDocuments.size()) {
			this.clusterName = "None, ";
			return;
		}
		

//		int maxValueInMap = (Collections.max(alltokens.values()));
//		for (Entry<String, Integer> entry : alltokens.entrySet()) { // Iterate through hashmap
//			if (entry.getValue() == maxValueInMap) {
//				if (!entry.getKey().equals("None")) // cases where None and other jar are equal times then give weght to
//													// jar
//					clusterName = clusterName + entry.getKey().replace(".jar", ",");
//			}
//		}
//		if (clusterName.equals("") || clusterName.equals("None"))
//			clusterName = "None ,";
//		
//		clusterName = clusterName.substring(0, clusterName.length() - 1);
		
		final Map<String, Integer> sortedTokensByCount = alltokens.entrySet()
                .stream()
                .sorted((Map.Entry.<String, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
		for (Entry<String, Integer> entry : sortedTokensByCount.entrySet()) { // Iterate through hashmap
			if (!entry.getKey().equals("None")) // cases where None and other jar are equal times then give weght to
									// jar
				clusterName = clusterName + entry.getKey().replace(".jar", ",");	
	}
	if (clusterName.equals("") || clusterName.equals("None"))
		clusterName = "None ,";
	
	clusterName = clusterName.substring(0, clusterName.length() - 1);
		
		this.clusterName = clusterName;
	}

	/**
	 * @return the clusterName
	 */
	public String getClusterName() {
		return clusterName;
	}

	/**
	 * @param clusterName the clusterName to set
	 */
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getClusterJson(int count) {
		JSONObject clusterJson = new JSONObject();
		if (this.clusterName == "")
//		System.out.println(this.clusterName);
//		System.out.println(this.score);
			if (count < 0)
				count = count * -1;
		if (this.score == 0) {
//			clusterJson.put("name","Cluster:"+count);
			clusterJson.put("name", "Cluster:" + count + "Name:" + this.clusterName);
		} else {
//			clusterJson.put("name","Cluster:"+count);
			clusterJson.put("name",
					"Cluster:" + count + "Name:" + this.clusterName + "  (score== " + this.score + " ) ");
		}
		clusterJson.put("parent", "root");
		clusterJson.put("score", this.score);
		JSONArray documentarray = new JSONArray();
		for (Document doc : listOfDocuments) {
			JSONObject docobject = new JSONObject();
			Set<String> jarNames = new HashSet(doc.getTokens());
			jarNames.remove("None");

			docobject.put("name", doc.getName() + "jar's:" + jarNames);
			if (count < 0)
				docobject.put("parent", "None");
			else
				docobject.put("parent", "Cluster" + count);
//    		docobject.put("size", 1000*listOfDocuments.size());
			documentarray.add(docobject);
		}
		clusterJson.put("children", documentarray);
		return clusterJson;
	}

	public JSONObject getCustomJson(int count) {
		JSONObject clusterJson = new JSONObject();
		clusterJson.put("Cluster", "Cluster: " + count + " Name: " + this.clusterName);

		return clusterJson;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listOfDocuments == null) ? 0 : listOfDocuments.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClusterDetails other = (ClusterDetails) obj;
		if (listOfDocuments == null) {
			if (other.listOfDocuments != null)
				return false;
		} else if (!listOfDocuments.equals(other.listOfDocuments))
			return false;
		return true;
	}

}
