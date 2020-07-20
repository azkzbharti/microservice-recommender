package com.ibm.research.msr.db.queries.partition;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Ownership{
	
	public static String get_class(String method){
		int lidx = method.lastIndexOf(".");
		String mclass = method.substring(0, lidx); 
		return mclass;
	}
	
	public static String get_owner(ArrayList<String> clusters_in_transaction, 
			ArrayList<String> real_clusters){
		
		final Counter<String> counts = new Counter<>();
		for(String cluster : clusters_in_transaction)
			counts.add(cluster);

		boolean valid_cluster_exist = false;
		for(String cluster : counts.get_counter().keySet()){
			if(real_clusters.contains(cluster)) valid_cluster_exist = true;
		}
		
		String owner = "";
		int max_freq = 0;
		
		if(valid_cluster_exist){
			// choose the max across valid clusters
			for (Map.Entry<String, Integer> entry : counts.get_counter().entrySet()) {
			    String cluster = entry.getKey();
			    int freq = entry.getValue();
			    
			    if(freq >  max_freq && real_clusters.contains(cluster)) {
			    	owner = cluster;
			    	max_freq = freq;
			    }
			}
		}else{
			// no valid clusters, just pick the max freq
			for (Map.Entry<String, Integer> entry : counts.get_counter().entrySet()) {
			    String cluster = entry.getKey();
			    int freq = entry.getValue();
			    
			    if(freq >  max_freq) {
			    	owner = cluster;
			    	max_freq = freq;
			    	
			    }
			}
		}
		
		return owner;
	}
	

	public static JSONArray analyze(JSONObject cma, JSONArray transactions) {
		
		// for cma
		HashMap<String, String> nodeid2label = new HashMap<String, String>();
        HashMap<String, String> label2nodeid = new HashMap<String, String>();
        HashMap<String, ArrayList<String>> cluster2nodes = new HashMap<String, ArrayList<String>>();
        HashMap<String, String> node2cluster = new HashMap<String,String>();
        HashMap<String, String> clusterid2name = new HashMap<String,String>();
        ArrayList<String> real_clusters = new ArrayList<String>();
        
        // for transactions
        //HashMap<String, ArrayList<JSONObject>> entry_trans = new HashMap<String,ArrayList<JSONObject>>();
        
        // for storing results
        HashMap<String, String> pre_output = new HashMap<String,String>();
        JSONArray  output = new JSONArray();
        
        JSONObject partition_result = (JSONObject) cma.get("partition_result");
        JSONObject microservice     = (JSONObject) partition_result.get("microservice");
        JSONArray nodes             = (JSONArray) microservice.get("nodes");
        JSONArray clusters          = (JSONArray) microservice.get("clusters");
        
        
        for(int i= 0; i < nodes.size(); i++) {
	    	JSONObject node = (JSONObject) nodes.get(i);
	    	String id       = node.get("id").toString();
	    	String label    = node.get("label").toString();
	    	
	    	nodeid2label.put(id, label);
	    	label2nodeid.put(label, id);
	    }
        
	    for(int i= 0; i < clusters.size(); i++) {
	    	
	    	JSONObject cluster      = (JSONObject) clusters.get(i);
	    	String cluster_id       = cluster.get("id").toString();
	    	String cluster_label    = cluster.get("label").toString();
	    	JSONArray cluster_nodes = (JSONArray) cluster.get("nodes");
	    	
	    	ArrayList<String> temp = new ArrayList<String>();
	    	for(int j = 0 ; j < cluster_nodes.size(); j++) {
	    		temp.add(cluster_nodes.get(j).toString());
	    	}
	    	cluster2nodes.put(cluster_id, temp);
	    	clusterid2name.put(cluster_id, cluster_label);
	    }
	    
	    for(Map.Entry<String, ArrayList<String>> entry : cluster2nodes.entrySet()) {
	    	
	    	String cluster_id               = entry.getKey();
	    	ArrayList<String> cluster_nodes = entry.getValue();
	    	
	    	for(int j =0; j < cluster_nodes.size(); j++ ){
	    		node2cluster.put(cluster_nodes.get(j), cluster_id);
	    	}
	    }
	    
	    for(Map.Entry<String, String> entry : clusterid2name.entrySet()) {
	    	
	    	String name = entry.getValue();
	    	if( name.substring(0,7).equals("cluster")) {
	    		real_clusters.add(name);
	    	}
	    }
        

	    // Main logic with new structure (using transaction id):
	    for(int k = 0 ; k < transactions.size(); k ++) {
        	JSONObject transaction = (JSONObject)transactions.get(k);
        	//String entry = transaction.get("entry").toString();
        	String id = transaction.get("id").toString();
        	
        	ArrayList<String> clusters_in_transaction = new ArrayList<String>();
    		JSONArray trans_seq = (JSONArray) transaction.get("transaction");
    		
    		for(int i = 0; i < trans_seq.size(); i++) { // for each callgraph
    			JSONArray callgraph = (JSONArray) ((JSONObject) trans_seq.get(i)).get("callgraph");
    			
    			for(int j = 0; j < callgraph.size(); j++){ // for each method in callgraph
    				
    				String method = callgraph.get(j).toString();
    				String mclass = get_class(method);
    				String nodeid = label2nodeid.get(mclass);
    				String clusterid = node2cluster.get(nodeid);
    				
    				clusters_in_transaction.add(clusterid2name.get(clusterid));
    			}
    		}
    		
    		String owner = get_owner(clusters_in_transaction, real_clusters);
        	
        	
        	
        	pre_output.put(id, owner);
        	//JSONObject res = new JSONObject();
			//res.put("transaction_id", id);
			//res.put("owned_cluster", owner);
			//output.add(res);
        	
        }
	    
	    
	    for(Map.Entry<String,String> entry : pre_output.entrySet()) {
	    	
	    	String id    = entry.getKey();
	    	String owner = entry.getValue();
	    	
	    	JSONObject temp = new JSONObject();
	    	temp.put("id", id);
	    	temp.put("owned_cluster", owner);
	    	output.add(temp);
	    	
	    }
	    
	    return output;
	}
	
	
	
	
	public static JSONObject revise(JSONObject cma, JSONArray output){

		
		JSONObject partition_result = (JSONObject) cma.get("partition_result");
		JSONObject microservice     = (JSONObject) partition_result.get("microservice");
		JSONArray clusters          = (JSONArray) microservice.get("clusters");
		
		// make a map cluster -> [transactions] 
	    HashMap<String, ArrayList<String>> cluster2trans = new HashMap<String,ArrayList<String>>();
	    for(int i = 0 ; i < output.size(); i++){
	    	
	    	JSONObject entry = (JSONObject)output.get(i);
	    	String tid       = (String) entry.get("id");
	    	String clusterid = (String) entry.get("owned_cluster");
	    	
	    	ArrayList<String> li = cluster2trans.get(clusterid);
	    	if(li == null) {
	    		// has not been initialized
	    		ArrayList<String> temp = new ArrayList<String>();
	    		temp.add(tid);
	    		cluster2trans.put(clusterid,temp);
	    		
	    	}else{
	    		cluster2trans.get(clusterid).add(tid);
	    	}
	    }
	    

	    // putting everything back int cma
	    JSONArray new_clusters = new JSONArray();
	    for(int i = 0 ; i < clusters.size(); i++) {
	    	 
	    	 JSONObject cluster  = (JSONObject) clusters.get(i);
	    	 String clusterlabel = (String)cluster.get("label");
	    	 
	    	 if(cluster2trans.containsKey(clusterlabel)) {
	    		 cluster.put("transactions", cluster2trans.get(clusterlabel));

	    	 }else{
	    		 cluster.put("transactions", new ArrayList<String>());
	    		 
	    	 }	 
	    	 new_clusters.add(cluster);
	    	 
	    } //end loop

	    microservice.put("clusters", new_clusters);
	    partition_result.put("microservice", microservice);
	    cma.put("partition_result", partition_result);
	    
	    
		return cma;
	}	
	
	public static void main(String args[])  throws Exception {

		// input
		String cma_path    = "/Users/pablo/work-code/ownership/daytrader-cma-results-fixed.json";
		String trans_path  = "/Users/pablo/work-code/ownership/daytrader-transaction.json";
		// output
		String output_path = "/Users/pablo/work-code/ownership/output/transaction_result.json";
		String cma_revised_output_path = "/Users/pablo/work-code/ownership/output/cma_revised.json";
		
		JSONParser parser_cma   = new JSONParser();
		JSONParser parser_trans = new JSONParser();
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(cma_path));
		JSONObject cma  = (JSONObject) parser_cma.parse(bufferedReader);

		JSONArray transactions = (JSONArray) parser_trans.parse(new FileReader(trans_path));
		
		System.out.println("executing: analyze");
		JSONArray output = analyze(cma, transactions);
        
        //Saving to disk 
        try (Writer writer = new FileWriter(output_path)) {
        	writer.write(output.toJSONString());
        }
        
        System.out.println("executing: revise");
        JSONObject revised_microservice = revise(cma, output);
        
        //Saving to disk 
        try (Writer writer = new FileWriter(cma_revised_output_path)) {
        	writer.write(revised_microservice.toJSONString());
        }
        
	}// end main 
}// end Ownership class