package com.ibm.research.msr.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.research.msr.extraction.Document;

public class ClusterDetails {
	
    List<Document> listOfDocuments = new ArrayList<>();
    int votes=0 ;
   
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
	

	
	@SuppressWarnings("unchecked")
	public JSONObject getClusterJson(int count) {
		JSONObject clusterJson  = new JSONObject();
    	clusterJson.put("name","Cluster"+count);
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
