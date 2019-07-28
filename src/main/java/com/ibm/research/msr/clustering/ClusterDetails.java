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
import java.util.stream.Collectors;

import org.apache.commons.collections.map.HashedMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.google.common.collect.Sets;
import com.ibm.research.msr.extraction.Document;
import com.ibm.research.msr.utils.DocumentParserUtil;

public class ClusterDetails {
	
    List<Document> listOfDocuments = new ArrayList<>();
    double score ;
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
		listOfDocuments = docsList;
	}
	public ClusterDetails(List<Document> docsList,double score) {
		listOfDocuments = docsList;
		this.score=score;
	}
	public List<Document> getListOfDocuments() {
		return listOfDocuments;
	}
	
	public void setListOfDocuments(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}
	
	public void assignDocToCluster(Document doc) {
		this.listOfDocuments.add(doc);
		
	}
	
	public void addDocumentToCluster(Document doc) {
		this.listOfDocuments.add(doc);
		
	}

	public void showDetails() {
		for(Document doc : listOfDocuments) {
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
		//TODO: improve this logic
		Map<String,Integer> alltokens= new HashMap();
		String clusterName="";
		for(Document doc:listOfDocuments) {
			Map<String,Integer>tMap = doc.getTokenCountMap();
//			if(tMap.containsKey("None"))
//				tMap.remove("None");
			for(String key:tMap.keySet()) {
				if(alltokens.containsKey(key))
				alltokens.put(key,alltokens.get(key)+(tMap.get(key)/listOfDocuments.size()));
				else
				alltokens.put(key,tMap.get(key)/listOfDocuments.size());
			}
			alltokens.putAll(doc.getTokenCountMap());
		}
		if(alltokens.containsKey("None") && alltokens.get("None") == listOfDocuments.size()) {
			this.clusterName="None, ";
			return;
		}
//		alltokens.remove("None");
//		System.out.println(clusterName);

		int maxValueInMap=(Collections.max(alltokens.values()));
		for (Entry<String, Integer> entry : alltokens.entrySet()) {  // Itrate through hashmap
            if (entry.getValue()==maxValueInMap) {
            	clusterName=clusterName+entry.getKey().replace(".jar", ",");
            }
        }
		if(clusterName.equals(""))
			clusterName = "None ,";
		clusterName=clusterName.substring(0, clusterName.length() - 1);
		System.out.println(clusterName);

		this.clusterName=clusterName;
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
		JSONObject clusterJson  = new JSONObject();
		if(this.score==0) {
	    	clusterJson.put("name","Cluster: "+count+ " Name: "+this.clusterName );
		}
		else {
	    	clusterJson.put("name","Cluster: "+count+ " Name: "+this.clusterName +"  (score== "+this.score+" ) ");
		}
    	clusterJson.put("parent", "root");
		JSONArray  documentarray = new JSONArray();
		for(Document doc:listOfDocuments) {
			JSONObject docobject = new JSONObject();
			Set<String> jarNames= new HashSet(doc.getTokens());
			jarNames.remove("None");
    		docobject.put("name", doc.getName()+" jar's:"+jarNames);
    		docobject.put("parent", "Cluster"+count);
//    		docobject.put("size", 1000*listOfDocuments.size());
    		documentarray.add(docobject);
		}
		clusterJson.put("children",documentarray);
		return clusterJson;
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((listOfDocuments == null) ? 0 : listOfDocuments.hashCode());
		return result;
	}


	/* (non-Javadoc)
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
