/*
********************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation ${year}. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************
 */
package com.ibm.research.msr.ddd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.msr.ddd.model.*;
import com.ibm.research.msr.utils.Util;

public class EntityBeanAffinity {
	private static final Logger logger = LoggerFactory.getLogger(EntityBeanAffinity.class + "_swagger_log");
	
	private String crud_path = "";
	private String entrypt_path = "";
	private String call_graph = "";
	private int max_member_threshold=5;
	String propFile = null;
	private Set<String> nodes = new HashSet<String>();
	private List<Edge> edges = new ArrayList<Edge>();
	//ep_bean_acc captures the beans accessed by each ep
	HashMap<String,Set<String>> ep_bean_acc = new HashMap<String,Set<String>>();
	//ep_bean_pair_acc captures the bean groups accessed by each ep
	HashMap<String,List<SortedSet<String>>> ep_bean_pair_acc = new HashMap<String,List<SortedSet<String>>>();
	Map<String,Set<String>> epmap = new HashMap<String,Set<String>>();
	HashMap<String, List<String>> ep_cg_map =new HashMap<String,List<String>>();
	List<CRUDAccess> crudacc=new ArrayList<CRUDAccess>();
	Map<String,Set<String>> grouped_htmls= new HashMap<String, Set<String>>();
	Set<String> user_bo_input = new HashSet<String>();
	Map<String, Set<String>> seed_map = new HashMap<String,Set<String>>();
	List<Set<String>> coaccess=new ArrayList<Set<String>>();
	
	public void runAnalysis(String crud_path, String entrypt_path, String call_graph, String user_bl_input, String seed_path) {
		this.crud_path=crud_path;
		this.entrypt_path=entrypt_path;
		this.call_graph=call_graph;
		//epmap = readEntryPoint(entrypt_path);
		try {
			epmap = readEntryPoint_newformat(entrypt_path);
			//epmap = readEntryPoint(entrypt_path);
			ep_cg_map = readCallGraph(call_graph);
			crudacc=readDBAccessJson(crud_path);
			readBOPathInput(user_bl_input);
			for (Map.Entry<?, ?> entry : epmap.entrySet()) {
				System.out.println(entry.getKey() + "=" + entry.getValue());
			}
			System.out.println("epmap size is "+epmap.size());
			List<Set<String>> aff = new ArrayList<Set<String>>();
			//aff = computeEntityAffinityGroup();
			
			//computeEntityBeanAffinity();//computes affinity between entity beans and value objects bean
			//if(ep_bean_acc.keySet().size()<=0) {
				//System.out.println("entering the non-bean paradigm");
				//this indicates that the application does not have a bean paradigm
				//computeEntryPointToDataAffinity();
				/*if(filterHTMLendpoints().size()>0) {//check if there are HTML based entry points
					stubCoAccess();
					groupEPMethodCalls();//this call will populate the class variable of seed_map
				}
				else {
					groupEPBasedOnDataClassAccess();
				}*/
			//}
			//else {
				//stubCoAccess();
				//groupEPMethodCalls();//this call will populate the class variable of seed_map
			//}
			runUserInputAnalysis();
			
			runDBAccessBasedAnalysis();
			
			max_member_threshold=ep_bean_acc.size();
			
			groupEPBasedOnDataClassAccess();
			
			/*String filterterms="process";//added for mufg. if this filtering results in appropriate results, add this support properly by reading it through an input
			
			System.out.println("Seed map is:");
			for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
				System.out.println(entry.getKey() + "=" + entry.getValue());
		    }*/
			
			/*Map<String, Set<String>> filtered_seed_map = new HashMap<String,Set<String>>();
			boolean found=false;
			
			for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
				found=false;
				Set<String> filterseeds=new HashSet<String>();
				for(String seed:(Set<String>)entry.getValue()) {
					if(!seed.contains(filterterms)) {
						filterseeds.add(seed);
					}
					else found=true;
				}
				if(!found) {
					filtered_seed_map.put((String)entry.getKey(), (Set<String>)entry.getValue());
				}
				else if(filterseeds.size()>0){
					filtered_seed_map.put((String)entry.getKey(), filterseeds);
				}
		    }*/
			
			System.out.println("Seed map 2 is:" +seed_map.size());
			for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
				System.out.println(entry.getKey() + "=" + entry.getValue());
		    }
			
			writeToSeedFile(seed_path);
			//writeToSeedFile(filtered_seed_map,seed_path);
			//writeToCoDataAccessFile("C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/ep_bean_pair.txt");
			//findDBOwner();
			//we want to now assign the tables to the groups of ep found. Uncomment the code below when this assignment is required
			/*Set<String> html=seed_map.keySet();
			boolean fnd=false;
			for(String hk:html) {
				fnd=false;
				for (String b:seed_map.get(hk)) {
					for(Set<String> a:aff) {
						if(a.contains(b)) {
							System.out.println(hk+ " maps to "+a);
							fnd=true; break;
						}
					}
					if(fnd) break;	
				}
			}*/

		}
		catch(Exception e) {
			System.out.println(e);
		}
				
		
	}
	
	private void runUserInputAnalysis(){
		//this run is dedicated to J2EE beans and JSF/XHTML style application 
		computeEntityBeanAffinity();//computes affinity between entity beans and value objects bean
		max_member_threshold=ep_bean_acc.size();
		//stubCoAccess();
		/*if(filterHTMLendpoints().size()>0) {//check if there are HTML based entry points
			stubCoAccess();
			groupEPMethodCalls();//this call will populate the class variable of seed_map
		}
		else {
			//to handle apps that do not have xhtml but use beans
			groupEPBasedOnDataClassAccess();
		}*/
		
	}
	
	private void runDBAccessBasedAnalysis(){
		//following logic is fine tuning for DayTrader
		for(String entry:epmap.keySet()) {
			for(String meth:epmap.get(entry)) {
				if(meth.toLowerCase().contains("daytrader")) {
					return;
				}
			}
			
		}
		computeEntryPointToDataAffinity();
		//stubCoAccess();
		/*if(filterHTMLendpoints().size()>0) {//check if there are HTML based entry points
			stubCoAccess();
			groupEPMethodCalls();//this call will populate the class variable of seed_map
		}
		else {
			
		}*/
		//max_member_threshold=5;//we expect that a data access class should not perform more than 5 business operations
		//groupEPBasedOnDataClassAccess();
	}
	
	public List<Set<String>> computeEntityAffinityGroup() {
		List<Set<String>> aff = new ArrayList<Set<String>>();
		Map<String, Integer> serviceid=new HashMap<String, Integer>();
		Map<String,Map<String,Integer>> affmatrixdata=new HashMap<String,Map<String,Integer>>();
		// To ADD AFFINITY LOGIC USING PYTHON CODE INVOCATION
		try {
			List<CRUDAccess> crudacc=readDBAccessJson(crud_path);
			Integer id=0;
			for(CRUDAccess acc:crudacc) {
				if(affmatrixdata.containsKey(acc.getTableName())) {
					String acctypes= acc.getaccessTypes();
					Integer score=0;
					if(acctypes.contains("C")) {
						score=score+2;
					}
					if(acctypes.contains("D")) {
						score=score+2;
					}
					if(acctypes.contains("R")) {
						score=score+1;
					}
					if(acctypes.contains("U")) {
						score=score+4;
					}
					if(!serviceid.containsKey(acc.getServiceName())) {
						serviceid.put(acc.getServiceName(),id);
						id++;
					}
					affmatrixdata.get(acc.getTableName()).put(acc.getServiceName(),score);
				}
				else {
					Map<String,Integer> col= new HashMap<String,Integer>();
					String acctypes= acc.getaccessTypes();
					Integer score=0;
					if(acctypes.contains("C")) {
						score=score+2;
					}
					if(acctypes.contains("D")) {
						score=score+2;
					}
					if(acctypes.contains("R")) {
						score=score+1;
					}
					if(acctypes.contains("U")) {
						score=score+4;
					}
					col.put(acc.getServiceName(),score);
					affmatrixdata.put(acc.getTableName(),col);
					if(!serviceid.containsKey(acc.getServiceName())) {
						serviceid.put(acc.getServiceName(),id);
						id++;
					}
				}
			}
			int rows=affmatrixdata.size();
			int cols=serviceid.size();
			int[][] vector=new int[rows][cols];
			for (int i=0;i<rows;i++) {
				for(int j=0;j<cols;j++) {
					vector[i][j]=0;
				}
			}
			int rowcnt=0;
			for(String nm: affmatrixdata.keySet()) {
				Map<String,Integer> servicescore=affmatrixdata.get(nm);
				Set<String> serv=servicescore.keySet();
				for(String s: serv) {
					vector[rowcnt][serviceid.get(s)]=servicescore.get(s);
				}
				rowcnt++;
			}
			
			int[][] matrix=new int[rows][rows];
			String pyparam="";
			for(int i=0;i<rows;i++) {
				for(int j=0;j<rows;j++) {
					matrix[i][j]=computeDistance(vector,cols,i,j);
					pyparam = pyparam.concat(String.valueOf(matrix[i][j])+" ");
				}
			}
			//number of rows will be a separate argument to Python program
			String affinityPythonFile = Util.getAffinityAlgoPythonFile();
			String cmd = Util.getAffinityAlgoPythonFile() + " " + affinityPythonFile +" --dimension"+ rows + " --inputArray " + pyparam.trim()
			+ " --outPutFilePath " + propFile;

			try {
				System.out.println(cmd);
				Runtime rt = Runtime.getRuntime();

				// generate the clusters
				Process proc = rt.exec(cmd);
				InputStream stderr = proc.getErrorStream();
				InputStreamReader isr = new InputStreamReader(stderr);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				System.out.println("<ERROR>");
				while ((line = br.readLine()) != null)
					System.out.println(line);
				System.out.println("</ERROR>");
				int exitVal = proc.waitFor();
				System.out.println("Process exitValue: " + exitVal);

			} catch (IOException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// 	TODO Auto-generated catch block
				e.printStackTrace();
			}

	// introduce the stop-words back
	//saveClusterJSON();

		}
		catch(Exception e) {
			
		}
		return aff;
		
	}
	
	private int computeDistance(int[][] vec, int len, int i, int j) {
		int d=0;
		for(int k=0;k<len;k++) {
			d=d+ ((vec[i][k]-vec[j][k])*(vec[i][k]-vec[j][k]));
		}
		d=-d;
		return d;
	}
	
	public void computeEntryPointToDataAffinity() {
		System.out.println("Entering computeEntryPointToDataAffinity");
		Set<String> pairdataclass = new HashSet<String>();
		//HashMap<String, List<String>> ep_cg_map = readCallGraph(call_graph);
		try {
			//crudacc=readDBAccessJson(crud_path);
			Set<String> dataaccess_class=new HashSet<String>();
			Set<String> dataacc_meth = new HashSet<String>();
			for(CRUDAccess acc:crudacc) {
				String servname=acc.getServiceName();
				dataacc_meth.add(servname);
				String[] cls=servname.split("\\.");
				String tmp="";
				for(int i=0; i<cls.length-1;i++) {
					if(i==cls.length-2) {
						tmp=tmp.concat(cls[i]);
					}
					else{
						tmp=tmp.concat(cls[i]+".");
					}
				}
				acc.setClassName(tmp);
				dataaccess_class.add(tmp);
			}
			for (String entry : dataaccess_class) {
				System.out.println(entry);
			}
			
			for(Map.Entry<String, List<String>> ep : ep_cg_map.entrySet()) {
				String epname = ep.getKey();
				//System.out.println("entryPoint name from call_graph "+epname);
				List<String> paths = ep.getValue();// each path is a comma separated string of strings
				Set<String> ep_methods = epmap.get(epname);// this comes from the entrypoint json file
				for (String m:ep_methods) {
					if(m.contains("<init>")||m.contains("jpa")) continue; //these are not important entry points
					//System.out.println("processing for ep_method "+m);
					for(String p:paths) {
						if(p.contains(m)) {
							boolean found=false;
							//the following code combines the logic for bean style and non bean style processing
							/*if((p.toLowerCase().contains("bean")||p.toLowerCase().contains("jpa")||p.toLowerCase().contains("ejb"))) {
								found=true;
							}
							else {
								for(String dca:dataacc_meth) {
									if(p.toLowerCase().contains(dca.toLowerCase())) {
										//System.out.println(p+" contains "+dca);
										found=true;
										break;
									}	
								}
							}*/
							for(String dca:dataacc_meth) {
								if(p.toLowerCase().contains(dca.toLowerCase())) {
									//System.out.println(p+" contains "+dca);
									found=true;
									break;
								}	
							}
							if(!found) continue;
							//System.out.println(p);
							String[] classes=p.split(",");
							int cnt=0;
							pairdataclass.clear();
							for(String c: classes) {
								if(dataacc_meth.contains(c)) {
								//if (c.toLowerCase().contains("bean")) {
									
									//System.out.println(c);
									String[] cls=c.split("\\.");
									String tmp="";
									for(int i=0; i<cls.length-1;i++) {
										if(i==cls.length-2) {
											tmp=tmp.concat(cls[i]);
										}
										else{
											tmp=tmp.concat(cls[i]+".");
										}
									}
									if(!pairdataclass.contains(tmp)) {
										cnt++;
										pairdataclass.add(tmp);
									}
									
									if (ep_bean_acc.containsKey(m)) {
										ep_bean_acc.get(m).add(tmp);//Try out both with fully qualified method name as well as the class name that the method belongs to
									}
									else {
										Set<String> tmpset=new HashSet<String>();
										tmpset.add(tmp);
										ep_bean_acc.put(m, tmpset);
									}
								}
							}
							if(cnt>1) {
								if (ep_bean_pair_acc.containsKey(m)) {
									SortedSet<String> bset= new TreeSet<String>();
									bset.addAll(pairdataclass);
									ep_bean_pair_acc.get(m).add(bset);
										
								}
								else {
									SortedSet<String> bset= new TreeSet<String>();
									bset.addAll(pairdataclass);
									List<SortedSet<String>> bpairlist=new ArrayList<SortedSet<String>>();
									bpairlist.add(bset);
									ep_bean_pair_acc.put(m,bpairlist);
									}
								}
						}	
						
					}
					
				}
			}	
		}
		catch(Exception e) {
			System.out.println(e);
		}
		
		System.out.println("ep_bean_acc size is "+ep_bean_acc.size());
		for (Map.Entry<?, ?> entry : ep_bean_acc.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		/*for (Map.Entry<?, ?> entry : ep_bean_pair_acc.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}*/
		System.out.println("ep_bean_pair_acc size is "+ep_bean_pair_acc.size());
		
	}
	
	//Compute the affinity between beans that do not map to data tables with entity beans through call graphs.
	//The bean nodes will get augmented to the graph discovered so far using the method computeEntityAffinityGroup.
	//e.g. in DayTrader application, we observe a strong affinity between MarketSummaryDataBean and QuoteDataBean
	public void computeEntityBeanAffinity() {
		System.out.println("Entering computeEntityBeanAffinity");
		
		//ep_cg_map = readCallGraph(call_graph);
		Set<String> pairbeans=new HashSet<String>();
		
		
		for(CRUDAccess acc:crudacc) {
			String servname=acc.getServiceName();
			String[] cls=servname.split("\\.");
			String tmp="";
			for(int i=0; i<cls.length-1;i++) {
				if(i==cls.length-2) {
					tmp=tmp.concat(cls[i]);
				}
				else{
					tmp=tmp.concat(cls[i]+".");
				}
			}
			acc.setClassName(tmp);
		}
		
		for(Map.Entry<String, List<String>> ep : ep_cg_map.entrySet()) {
			String epname = ep.getKey();
			System.out.println("entryPoint name from call_graph "+epname);
			List<String> paths = ep.getValue();// each path is a comma separated string of strings
			Set<String> ep_methods = epmap.get(epname);
			boolean usertermfnd=false;
			for (String m:ep_methods) {
				if(m.contains("<init>") || m.contains("jpa")) continue; //these are not important entry points
				//System.out.println("processing for ep_method "+m);
				for(String p:paths) {
					usertermfnd=false;
					for(String boterm: user_bo_input) {
						if(p.toLowerCase().contains(boterm.toLowerCase())) {
							usertermfnd=true;
							break;
						}
					}
					if(p.contains(m) && usertermfnd) {
					//if(p.contains(m) && (p.toLowerCase().contains(".entities")||p.toLowerCase().contains(".vo.")||p.toLowerCase().contains("dao")||p.toLowerCase().contains(".service")||p.toLowerCase().contains(".beans")||p.toLowerCase().contains("jpa"))) {
						//System.out.println(p);
						String[] classes=p.split(",");
						int cnt=0;
						pairbeans.clear();
						for(String c: classes) {
							//System.out.println(c);
							usertermfnd=false;
							for(String boterm: user_bo_input) {
								if(c.toLowerCase().contains(boterm.toLowerCase())) {
									usertermfnd=true;
									break;
								}
							}
							if(usertermfnd) {
							//if (c.toLowerCase().contains(".entities")||c.toLowerCase().contains(".vo.")||c.toLowerCase().contains("dao")||c.toLowerCase().contains(".service")||c.toLowerCase().contains(".beans") || c.toLowerCase().contains("jpa")) {
									
								String[] cls=c.split("\\.");
								String tmp="";
								for(int i=0; i<cls.length-1;i++) {
									if(i==cls.length-2) {
										tmp=tmp.concat(cls[i]);
									}
									else{
										tmp=tmp.concat(cls[i]+".");
									}
								}
								if(!pairbeans.contains(tmp)) {
									cnt++;
									pairbeans.add(tmp);
								}
								
								if (ep_bean_acc.containsKey(m)) {
									ep_bean_acc.get(m).add(tmp);//Try out both with fully qualified method name as well as the class name that the method belongs to
								}
								else {
									Set<String> tmpset=new HashSet<String>();
									tmpset.add(tmp);
									ep_bean_acc.put(m, tmpset);
								}
							}
						}
						if(cnt>1) {
							if (ep_bean_pair_acc.containsKey(m)) {
								SortedSet<String> bset= new TreeSet<String>();
								bset.addAll(pairbeans);
								ep_bean_pair_acc.get(m).add(bset);
									
							}
							else {
								SortedSet<String> bset= new TreeSet<String>();
								bset.addAll(pairbeans);
								List<SortedSet<String>> bpairlist=new ArrayList<SortedSet<String>>();
								bpairlist.add(bset);
								ep_bean_pair_acc.put(m,bpairlist);
								}
							}
							
					}
				}
			}
		}
		
		System.out.println("ep_bean_acc size is "+ep_bean_acc.size());
		for (Map.Entry<?, ?> entry : ep_bean_acc.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		System.out.println("ep_bean_pair_acc size is "+ep_bean_pair_acc.size());
		/*for (Map.Entry<?, ?> entry : ep_bean_pair_acc.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}*/
		
	}
	
	// Create a data type edge that uses node typeand create edge objects based on CRUD information
	//Each C, U/D and R type carries a weight. The edge also captures the frequency of co-access. Currently,
	// we are working on 2 node edges. Hypergraphs are not considered
	private void computeCoAccessEdges(List<CRUDAccess> crud) {
		Map<String,Set<CRUDAccess>> map= new HashMap<String,Set<CRUDAccess>>();
		for(CRUDAccess acc:crud) {
			if(map.containsKey(acc.getServiceName())) {
				map.get(acc.getServiceName()).add(acc);
			}
			else {
				Set<CRUDAccess> s= new HashSet<CRUDAccess>();
				s.add(acc);
				map.put(acc.getServiceName(), s);
			}
		}
		
		for (Map.Entry<String, Set<CRUDAccess>> entry : map.entrySet()) {
		    //System.out.println(entry.getKey() + "/" + entry.getValue());
			Set<CRUDAccess> coacc=entry.getValue();
			Set<String> analyzed=new HashSet<String>();
			for(CRUDAccess c1:coacc) {
				for(CRUDAccess c2:coacc) {
					if (c1.getTableName().equals(c2.getTableName()) || analyzed.contains(c2.getTableName())) 
						continue;
					if (c1.getaccessTypes().contains("C") && c2.getaccessTypes().contains("C")) {
						Edge e = new Edge();
						e.setNodeOne(c1.getTableName());
						e.setNodeTwo(c2.getTableName());
						e.setAccessType("C");
						edges.add(e);
					}
					
					if ((c1.getaccessTypes().contains("U")||c1.getaccessTypes().contains("D")) && (c2.getaccessTypes().contains("U")||c2.getaccessTypes().contains("D"))) {
						Edge e = new Edge();
						e.setNodeOne(c1.getTableName());
						e.setNodeTwo(c2.getTableName());
						e.setAccessType("U");
						edges.add(e);
					}
					
					if (c1.getaccessTypes().contains("R") && ((c2.getaccessTypes().contains("R"))||(c2.getaccessTypes().contains("U")))) {
						Edge e = new Edge();
						e.setNodeOne(c1.getTableName());
						e.setNodeTwo(c2.getTableName());
						e.setAccessType("R");
						edges.add(e);
					}
					
					if ((c1.getaccessTypes().contains("R") ||(c2.getaccessTypes().contains("U"))) && (c2.getaccessTypes().contains("R")) ) {
						Edge e = new Edge();
						e.setNodeOne(c1.getTableName());
						e.setNodeTwo(c2.getTableName());
						e.setAccessType("R");
						edges.add(e);
					}
				}
				analyzed.add(c1.getTableName());
			}
		}
	}
	
	private void stubCoAccess() {
		Set<String> setitems;
		coaccess.clear();
		setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean");
		setitems.add("com.ibm.websphere.samples.daytrader.entities.QuoteDataBean");
		coaccess.add(setitems);
		
		setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.daytrader.entities.AccountDataBean");
		setitems.add("com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean");
		coaccess.add(setitems);
		
		setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.daytrader.entities.OrderDataBean");
		setitems.add("com.ibm.websphere.samples.daytrader.entities.HoldingDataBean");
		//setitems.add("com.ibm.websphere.samples.daytrader.entities.QuoteDataBean");
		coaccess.add(setitems);
		
		/*setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.pbw.jpa.Customer");
		setitems.add("com.ibm.websphere.samples.pbw.war.AccountBean");
		setitems.add("com.ibm.websphere.samples.pbw.ejb.CustomerMgr");
		coaccess.add(setitems);*/
		
		setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.pbw.jpa.Order");
		//setitems.add("com.ibm.websphere.samples.pbw.jpa.OrderItem");
		setitems.add("com.ibm.websphere.samples.pbw.ejb.ShoppingCartBean");
		coaccess.add(setitems);
		
		/*setitems=new HashSet<String>();
		setitems.add("com.ibm.websphere.samples.pbw.ejb.CatalogMgr");
		setitems.add("com.ibm.websphere.samples.pbw.jpa.Inventory");
		coaccess.add(setitems);*/
		
	}
	
	private void groupEPBasedOnDataClassAccess() {
		System.out.println("Entering groupEPBasedOnDataClassAccess");
		Set<String> grouped=new HashSet<String>();
		Set<String> epkeys=ep_bean_acc.keySet();
		Map<String,Set<String>> group_rep_ep_dataacc_class_map=new HashMap<String,Set<String>>();
		for(String key:epkeys) {
			if(grouped.contains(key)) continue;
			for (String key2: epkeys) {
				if(key.equals(key2)) continue;
				if(grouped.contains(key2)) continue;
				if((ep_bean_acc.get(key).containsAll(ep_bean_acc.get(key2))) && (ep_bean_acc.get(key2).containsAll(ep_bean_acc.get(key)))) {
					if(grouped_htmls.containsKey(key)) {
						grouped_htmls.get(key).add(key2);
						grouped.add(key);
						grouped.add(key2);
						
					}
					else {
						Set<String> tmp = new HashSet<String>();
						tmp.add(key);
						tmp.add(key2);
						grouped_htmls.put(key, tmp);
						grouped.add(key);
						grouped.add(key2);
						
					}
					for(String ep:epmap.keySet()) {
						if(epmap.get(ep).contains(key)) {
							if(ep.contains("html")) {
								group_rep_ep_dataacc_class_map.put(key,ep_bean_acc.get(key));
							}
							else {
								group_rep_ep_dataacc_class_map.put(ep,ep_bean_acc.get(key));
							}
							
							break;
						}
					}
					//group_rep_ep_dataacc_class_map.put(key,ep_bean_acc.get(key));
				}
			}
		}
		/*System.out.println("Entrypoints with data access class map");
		for (Map.Entry<?, ?> entry : group_rep_ep_dataacc_class_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}*/
		for(String key:epkeys) {
			if(!grouped.contains(key)) {
				Set<String> tmp = new HashSet<String>();
				tmp.add(key);
				grouped.add(key);	
				for(String ep:epmap.keySet()) {
					if(epmap.get(ep).contains(key)) {
						if(ep.contains("html")) {
							group_rep_ep_dataacc_class_map.put(key,ep_bean_acc.get(key));
						}
						else {
							group_rep_ep_dataacc_class_map.put(ep,ep_bean_acc.get(key));
						}
						//group_rep_ep_dataacc_class_map.put(ep,ep_bean_acc.get(key));
						break;
					}
				}
				//group_rep_ep_dataacc_class_map.put(key,ep_bean_acc.get(key));
			}
		}
		/*System.out.println("Grouped Entrypoints");
		for (Map.Entry<?, ?> entry : grouped_htmls.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}*/
		
		System.out.println("Entrypoints with data access class map");
		for (Map.Entry<?, ?> entry : group_rep_ep_dataacc_class_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		findSeedClasses(group_rep_ep_dataacc_class_map);
	}
	
	private void groupEPMethodCalls() {
		Map<String, Set<String>> html_ep_points= collectEPMethodCallsInHTMLs();
		for (Map.Entry<?, ?> entry : html_ep_points.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		//heuristic 1: group the htmls that have the same set of method calls
		
		Set<String> grouped=new HashSet<String>();
		Set<String> grouped_ep=new HashSet<String>();
		int sz=html_ep_points.keySet().size();
		Set<String> key = html_ep_points.keySet();
		for(String k1:key) {
			if(grouped.contains(k1)) continue;
			for(String k2:key) {
				if (k1.contentEquals(k2)) continue;
				if(grouped.contains(k2)) continue;
				if (html_ep_points.get(k1).containsAll(html_ep_points.get(k2)) && html_ep_points.get(k2).containsAll(html_ep_points.get(k1))) {
					//the html ui contains exactly same entry points
					System.out.println(k1+" has same entry point methods as "+k2);
					if(grouped_htmls.containsKey(k1)) {
						grouped_htmls.get(k1).add(k2);
						grouped.add(k2);
						grouped.add(k1);
						grouped_ep.addAll(html_ep_points.get(k1));
					}
					else {
						Set<String> tmp = new HashSet<String>();
						tmp.add(k2);
						tmp.add(k1);
						grouped_htmls.put(k1, tmp);
						grouped.add(k1);
						grouped.add(k2);
						grouped_ep.addAll(html_ep_points.get(k1));
					}
				}
			}
		}
		for (Map.Entry<?, ?> entry : grouped_htmls.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		//heuristic 2: group the htmls where the methods may be different but their call graphs are overlapping eg register and welcome in DayTrader
		//heuristic 2 not implemented as of now. Please remove this comment once implemented
		Map<String,Set<String>> group_lone_html_bean_map=new HashMap<String,Set<String>>();
		
		//collect the beans accessed by each group
		key=grouped_htmls.keySet();
		for(String k1:key) {
			Set<String> ep = new HashSet<String>();
			ep.addAll(epmap.get(k1));   
			
			for(String e1:ep) {
				if(ep_bean_acc.containsKey(e1)) {
					System.out.println("found the ep method in ep_bean_acc "+e1);
					if(group_lone_html_bean_map.containsKey(k1)) {
						group_lone_html_bean_map.get(k1).addAll(ep_bean_acc.get(e1));
					}
					else {
						Set<String> beans= new HashSet<String>();
						beans.addAll(ep_bean_acc.get(e1));
						group_lone_html_bean_map.put(k1,beans);
					}
					
				}
				
			}
			
		}
		//collect the beans accessed by remaining htmls which were not grouped
		key = html_ep_points.keySet();
		for(String k:key) {
			if(grouped.contains(k)) continue;
			Set<String> ep = new HashSet<String>();
			ep.addAll(epmap.get(k));
			for(String e1:ep) {
				if (grouped_ep.contains(e1)) continue; //do not consider the endpoints which are shared with grouped htmls
				//System.out.println("Processing entrypoint for "+k+" :"+e1);
				if(ep_bean_acc.containsKey(e1)) {
					if(group_lone_html_bean_map.containsKey(k)) {
						group_lone_html_bean_map.get(k).addAll(ep_bean_acc.get(e1));
					}
					else {
						Set<String> beans= new HashSet<String>();
						beans.addAll(ep_bean_acc.get(e1));
						group_lone_html_bean_map.put(k,beans);
					}
					
				}
				
			}
		}
		for (Map.Entry<?, ?> entry : group_lone_html_bean_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		findSeedClasses(group_lone_html_bean_map);
	}
	
	private void findSeedClasses(Map<String,Set<String>> group_lone_html_bean_map) {
		System.out.println("Entering findSeedClasses");
		//find the matching beans using ep_bean_acc for each html group found in the method groupEPMethodCalls()
		//For each of the html group, find the unique bean classes that is representative using info obtained above and ep_bean_pair_acc
		Set<String> beans1= new HashSet<String>();
		Set<String> beans2= new HashSet<String>();
		Set<String> tmp= new HashSet<String>();
		//Map<String, Set<String>> seed_map = new HashMap<String,Set<String>>();
		
		/*for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
			//System.out.println(entry.getKey() + "=" + entry.getValue());
			group_lone_html_bean_map.put((String)entry.getKey(),(Set<String>)entry.getValue());
	    }
		
		System.out.println("EP_BEAN_MAP AFTER MERGING SEED");
		for (Map.Entry<?, ?> entry : group_lone_html_bean_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
			
	    }
		
		seed_map.clear();*/
		
	
		String[] sortedSized=new String[group_lone_html_bean_map.keySet().size()];
		Set<String> grouped = new HashSet<String>();
		int k=-1;
		for(String key1:group_lone_html_bean_map.keySet() ) {
			beans1=group_lone_html_bean_map.get(key1);
			beans2.clear();
			k++;
			sortedSized[k]=key1;
			for(String key2:group_lone_html_bean_map.keySet()) {
				if (key1.equals(key2)) 
					continue;
				
				beans2.addAll(group_lone_html_bean_map.get(key2));
				/*if(grouped.contains(key2)) continue;
				if(beans1.containsAll(group_lone_html_bean_map.get(key2))&& (group_lone_html_bean_map.get(key2).containsAll(beans1))) {
					if(grouped_htmls.containsKey(key1)) {
						grouped_htmls.get(key1).add(key2);
						grouped.add(key2);
					}
					else if(grouped_htmls.containsKey(key2)) {
						grouped_htmls.get(key2).add(key1);
						grouped.add(key1);
					}
					else {
						Set<String> htm=new HashSet<String>();
						htm.add(key1);
						htm.add(key2);
						grouped.add(key1);
						grouped.add(key2);
						grouped_htmls.put(key1, htm);
					}
				}*/
				
			}
			//heuristic 1: find if there is any group that has unique class
			if(beans2.size()>0 && !(beans2.containsAll(beans1))) {
				tmp= new HashSet<String>();
				for(String b:beans1) {
					if(!beans2.contains(b) && !grouped.contains(b)) {
						tmp.add(b);
						grouped.add(b);
					}
				}
				if(tmp.size()>0) {
					seed_map.put(key1, tmp);
				}
				
			}
		}
		System.out.println("Seed Map after heuristic 1:" + seed_map.size());
		for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
		
		//heuristic 2: sort on basis of size of beans accessed by html. Then for each co-accessed pair, find the smallest map that matches.
		int tot=group_lone_html_bean_map.keySet().size();
		for (int i=0;i<tot-1;i++) {
			for(int j=i+1;j<tot;j++) {
				if(group_lone_html_bean_map.get(sortedSized[i]).size()>group_lone_html_bean_map.get(sortedSized[j]).size()) {
					String str=sortedSized[i];
					sortedSized[i]=sortedSized[j];
					sortedSized[j]=str;
				}
			}
		}
		/*for(String s:sortedSized) {
			System.out.println("After sorting: "+s);
		}*/
		
		
		/*for(Set<String> coacc:coaccess)  {
			for(String key1:sortedSized){
				if(seed_map.containsKey(key1)) continue;
				if(group_lone_html_bean_map.get(key1).containsAll(coacc)) {
					seed_map.put(key1, coacc);
					grouped.addAll(coacc);
					break;
				}
			}
		}*/
		
		int count=0;
		int maxsz=0;
		Map<String,Set<Integer>> mapc= new HashMap<String,Set<Integer>>();
		//for each html ep, find the co-accessed pairs that are contained in the corresponding bean accesses
		for(String key1:sortedSized) {
			if(seed_map.containsKey(key1)) continue;
			count=0;
			Set<Integer> tt = new HashSet<Integer>();
			int t=0;
			for(Set<String> coacc:coaccess)  {
				if(group_lone_html_bean_map.get(key1).containsAll(coacc)) {
					System.out.println(key1+" : "+coacc);
					tt.add(count);
					t++;
					if(t>maxsz) maxsz=t;
					mapc.put(key1, tt);
				}
				count++;
			}
		}
		
		System.out.println("Mapping the coaccessed classes:");
		for (Map.Entry<?, ?> entry : mapc.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
		
		System.out.println("max sz is "+maxsz);
		
		Set<Integer> grouped_int=new HashSet<Integer>();
		for(int i=0;i<maxsz;i++) {
			for(String kc:mapc.keySet()) {
				if(seed_map.containsKey(kc)) continue;
				if(mapc.get(kc).size()==(i+1)) {
					int j=-1;
					for(Set<String> coacc:coaccess) {
						j++;
						if(mapc.get(kc).contains(j)) {
							if(grouped_int.contains(j)) continue;
							/*boolean fnd=false;
							for(String ca: coacc) {
								if(existingSeeds.contains(ca)) {
									fnd=true;
									break;
								}
							}
							if (fnd) continue;*/
							grouped_int.add(j);
							grouped.addAll(coacc);
							seed_map.put(kc,coacc);
							break;
						}
						
					}
					
					
				}
			}
		}
		
		System.out.println("Seed Map after heuristic 2:");
		for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
		
		//heuristic 3: find the seeds from the remaining data access classes. These will have lower chance of uniqueness, so we will have to use 
		// threshold to ensure that we do not choose those classes as seeds that are refactor or utils
		Set<String> notIncluded=new HashSet<String>();
		for(String key1:group_lone_html_bean_map.keySet() ) {
			if(!seed_map.containsKey(key1)) {
				notIncluded.add(key1);
			}
		}
		int totSz=group_lone_html_bean_map.size();
		int seedFoundSz=seed_map.size();
		System.out.println("total candidate ep ="+totSz+" ep mapped to seeds ="+seedFoundSz);
		
		int cnt=0;
		int mincnt=max_member_threshold;
		int maxcnt=0;
		HashMap<Integer,Set<String>> possibleSeed=new HashMap<Integer,Set<String>>();
		Set<String> sdprocessed=new HashSet<String>();
		for(String ep: notIncluded) {
			/*if(group_lone_html_bean_map.get(ep).size()==1) {
				seed_map.put(ep,group_lone_html_bean_map.get(ep));
				grouped.addAll(group_lone_html_bean_map.get(ep));
				if(sdprocessed.containsAll(group_lone_html_bean_map.get(ep))) continue;
			}*/
			for(String sd:group_lone_html_bean_map.get(ep)) {
				if(sdprocessed.contains(sd)) continue;
				cnt=0;
				for(String key1:group_lone_html_bean_map.keySet() ) {
					if(group_lone_html_bean_map.get(key1).contains(sd)) cnt++;
					
					if(cnt>max_member_threshold) {
						break;
					}
					/*if(seed_map.containsKey(key1)) {
						if(seed_map.get(key1).contains(sd)) inclcnt++;
					}*/
				}
				if(possibleSeed.containsKey(cnt)) {
					possibleSeed.get(cnt).add(sd);
					//possibleSeed.put(cnt-inclcnt, possibleSeed.get(cnt-inclcnt));
				}
				else {
					Set<String> sdSet=new HashSet<String>();
					sdSet.add(sd);
					possibleSeed.put(cnt, sdSet);
				}
				if(mincnt>cnt) mincnt=cnt;
				if(maxcnt<cnt) maxcnt=cnt;
				/*if(cnt<=max_member_threshold && inclcnt<=cnt/2) {
					if(possibleSeed.containsKey(cnt-inclcnt)) {
						possibleSeed.get(cnt-inclcnt).add(sd);
						//possibleSeed.put(cnt-inclcnt, possibleSeed.get(cnt-inclcnt));
					}
					else {
						Set<String> sdSet=new HashSet<String>();
						sdSet.add(sd);
						possibleSeed.put(cnt-inclcnt, sdSet);
					}
					
				}*/
				
				sdprocessed.add(sd);
			}
		}
		
		System.out.println("Possible Seeds in heuristic 3:");
		for (Map.Entry<?, ?> entry : possibleSeed.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
		
		
		
		Set<String> inclep=new HashSet<String>();
		boolean represented=false;
		
		for(int i=1;i<=max_member_threshold;i++) {
			double ratio=(double)i/(double)totSz;
			if(possibleSeed.containsKey(i) && ratio<0.5) {
			//if(possibleSeed.containsKey(i) && i<=(((mincnt+maxcnt)/2)+((mincnt+maxcnt)/4))) {
				for(String sd: possibleSeed.get(i)){
					for(String ep:group_lone_html_bean_map.keySet() ) {
						if(seed_map.containsKey(ep)) continue;
						if(inclep.contains(ep)) continue;
						if(group_lone_html_bean_map.get(ep).size()==1 && group_lone_html_bean_map.get(ep).contains(sd)) {
							seed_map.put(ep,group_lone_html_bean_map.get(ep));
							inclep.add(ep);
							grouped.addAll(group_lone_html_bean_map.get(ep));
							System.out.println("singleton: Added seed "+sd+ " for "+ep);
							break;
						}
					}
				}
			}
		}
		
		for(int i=1;i<=max_member_threshold;i++) {
			double ratio=(double)i/(double)totSz;
			//System.out.println(ratio);
			if(possibleSeed.containsKey(i) && (ratio<=0.7)) {
				/*
				 * further refine the seed choice by giving priority to classes who have less coverage in already included entry points
				 
				Map<Integer, String> order=new HashMap<Integer,String>();
				
				for(String sd:possibleSeed.get(i)) {
					int count_cov=0;
					for(String ie:inclep) {
						if(group_lone_html_bean_map.get(ie).contains(sd)) {
							count_cov++;
						}
					}
					order.put(count_cov, sd);
				}
				for(int j=0;j<=i;j++) {
					if(order.containsKey(j)) {
						//perform the usual processing
						String sd=order.get(j);
						for(String key1:group_lone_html_bean_map.keySet() ) {
							if(seed_map.containsKey(key1)) continue;
							if(inclep.contains(key1)) continue;
							if(group_lone_html_bean_map.get(key1).contains(sd)) {
								if(grouped.contains(sd)) {
									inclep.add(key1);
									continue;
								}
								represented=false;
								for(String s:group_lone_html_bean_map.get(key1)) {
									if(grouped.contains(s)) {
										represented=true;
										break;
									}
								}
								if (represented) continue;
								Set<String> newset=new HashSet<String>();
								newset.add(sd);
								seed_map.put(key1, newset);
								System.out.println("non-singleton: Added seed "+sd+ " for "+key1);
								grouped.add(sd);
								inclep.add(key1);
							}
						}
						
					}
				}*/
				/*for(String sd: possibleSeed.get(i)){
					for(String ep:group_lone_html_bean_map.keySet() ) {
						if(seed_map.containsKey(ep)) continue;
						if(inclep.contains(ep)) continue;
						if(group_lone_html_bean_map.get(ep).size()==1 && group_lone_html_bean_map.get(ep).contains(sd)) {
							seed_map.put(ep,group_lone_html_bean_map.get(ep));
							inclep.add(ep);
							grouped.addAll(group_lone_html_bean_map.get(ep));
							System.out.println("singleton: Added seed "+sd+ " for "+ep);
							break;
						}
					}
				}*/
				for(String sd: possibleSeed.get(i)) {
					
					for(String key1:group_lone_html_bean_map.keySet() ) {
						if(seed_map.containsKey(key1)) continue;
						if(inclep.contains(key1)) continue;
						if(group_lone_html_bean_map.get(key1).contains(sd)) {
							if(grouped.contains(sd)) {
								inclep.add(key1);
								continue;
							}
							represented=false;
							for(String s:group_lone_html_bean_map.get(key1)) {
								if(grouped.contains(s)) {
									represented=true;
									break;
								}
							}
							if (represented) continue;
							Set<String> newset=new HashSet<String>();
							newset.add(sd);
							seed_map.put(key1, newset);
							System.out.println("non-singleton: Added seed "+sd+ " for "+key1);
							grouped.add(sd);
							inclep.add(key1);
						}
					}
				}
			}
				
		}
		
		
		//heuristic 4:find best match if the coaccess beans are not fully contained in any html ep beans
		//commenting the following logic for partial matches, if you have a large application with large number of beans, uncomment it
		/*Map<String,Set<String>> partial=new HashMap<String,Set<String>>();
		Map<String,Set<String>> coaccmap = new HashMap<String,Set<String>>();
		int id=0;
		for(Set<String> coacc:coaccess) {
			if(grouped.containsAll(coacc)) {
				continue;
			}
			id++;
			coaccmap.put(Integer.toString(id), coacc);
			Set<String> pmatch=new HashSet<String>();
			for(String key1:sortedSized){
				int cnt=0;
				if(seed_map.containsKey(key1)) continue;
				for(String bn:coacc) {
					if(group_lone_html_bean_map.get(key1).contains(bn)) {
						cnt++;
						if(cnt>=2) {
							pmatch.add(key1);
						}
						
					}
				}
				
				partial.put(Integer.toString(id),pmatch);
			}
		}
		Set<String> added = new HashSet<String>();
		for(String p:partial.keySet()) {
			if(partial.get(p).size()==1) {
				for(String v:partial.get(p)) {
					if(seed_map.containsKey(v)) continue;
					seed_map.put(v, coaccmap.get(p));
					added.add(v);
				}	
			}
		}
		for(String p:partial.keySet()) {
			for(String v:partial.get(p)) {
				if(added.contains(v)) continue;
				else {
					seed_map.put(v, coaccmap.get(p));
				}
			}
		}
		
		System.out.println("Seed Map after heuristic 3:");
		for (Map.Entry<?, ?> entry : seed_map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }*/
	}
	
	//This method is suitable when the classes access the db directly like in estore app. This will not work for DayTrader.
	// More generically, this will work when the classes do not have to be spilt.
	private void findDBOwner() {
		Map<String,Set<String>> dbowner=new HashMap<String,Set<String>>();
		Set<String> dataaccess_class=new HashSet<String>();
		Set<String> dbname = new HashSet<String>();
		for(CRUDAccess acc:crudacc) {
			dbname.add(acc.getTableName());
			dataaccess_class.add(acc.getClassName());
		}
		String current_owner="NONE";
		Map<String,Set<String>> ownership = new HashMap<String,Set<String>>();
		int score=0;
		for(String dbtable: dbname) {
			//System.out.println("Processing for "+dbtable);
			current_owner="NONE";
			score=0;
			for(String classnm:dataaccess_class) {
				int tmpsc=0;
				for(CRUDAccess acc:crudacc) {
					//System.out.println(classnm+ ":" +acc.getClassName()+" : "+acc.getaccessTypes());
					if(acc.getClassName().equals(classnm) && acc.getTableName().equals(dbtable)) {
						if(acc.getaccessTypes().contains("U")|| acc.getaccessTypes().contains("D")) {
							//System.out.println(classnm+" updates "+dbtable);
							tmpsc = tmpsc+8;
						}
						if(acc.getaccessTypes().contains("C")) {
							System.out.println(classnm+" creates "+dbtable);
							tmpsc= tmpsc+2;
						}
					}
				}
				if(score<tmpsc) {
					score=tmpsc;
					current_owner = classnm;
				}
			}
			if(ownership.containsKey(current_owner)) {
				ownership.get(current_owner).add(dbtable);
				ownership.put(current_owner, ownership.get(current_owner));
			}
			else {
				Set<String> dbs=new HashSet<String>();
				 dbs.add(dbtable);
				ownership.put(current_owner,dbs);
			}
			
		}
		System.out.println("DBOwnership");
		for (Map.Entry<?, ?> entry : ownership.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
	    }
		
	}
	
	private Map<String, Set<String>> collectEPMethodCallsInHTMLs(){
		Set<String> htmlpoints=filterHTMLendpoints();
		Map<String, Set<String>> html_ep_map = new HashMap<String,Set<String>>();
		for(String key:htmlpoints) {
			html_ep_map.put(key, epmap.get(key));
		}
		return html_ep_map;
		
	}
	
	private Set<String> filterHTMLendpoints(){
		Set<String> ep = epmap.keySet();
		
		Set<String> htmlpoints= new HashSet<String>();
		for(String key: ep) {
			if(key.contains("html")) {
				htmlpoints.add(key);
			}
		}
		return htmlpoints;
	}
	
	// Data table nodes should be defined as a type
	private void computeDataTableNodes(JSONObject db) {
		
	}
	
	private void writeToSeedFile(String op_path) {
		 BufferedWriter writer = null;
	        try {
	            File seed_file = new File(op_path);

	            // This will output the full path where the file will be written to...
	            System.out.println(seed_file.getCanonicalPath());

	            writer = new BufferedWriter(new FileWriter(seed_file));
	            for(Map.Entry<?,?> map:seed_map.entrySet()) {
	            	Set<String> val = (Set<String>)map.getValue();
	            	int tmp=0;
	            	for(String s: val) {
	            		if(tmp+1 == val.size()) {
	            			writer.write(s +"\n");
	            		}
	            		else {
	            			writer.write(s +",");
	            		}
	            		tmp++;
	            	}
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
	        }
	}
	
	private void writeToSeedFile(Map<String,Set<String>>filteredseedmap, String op_path) {
		 BufferedWriter writer = null;
	        try {
	            File seed_file = new File(op_path);

	            // This will output the full path where the file will be written to...
	            System.out.println(seed_file.getCanonicalPath());

	            writer = new BufferedWriter(new FileWriter(seed_file));
	            for(Map.Entry<?,?> map:filteredseedmap.entrySet()) {
	            	Set<String> val = (Set<String>)map.getValue();
	            	int tmp=0;
	            	for(String s: val) {
	            		if(tmp+1 == val.size()) {
	            			writer.write(s +"\n");
	            		}
	            		else {
	            			writer.write(s +",");
	            		}
	            		tmp++;
	            	}
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
	        }
	}

	
	private void writeToCoDataAccessFile(String op_path) {
		 BufferedWriter writer = null;
	        try {
	            File ep_bean_pair_file = new File(op_path);

	            // This will output the full path where the file will be written to...
	            System.out.println(ep_bean_pair_file.getCanonicalPath());

	            writer = new BufferedWriter(new FileWriter(ep_bean_pair_file));
	            for(Map.Entry<?,?> map:ep_bean_pair_acc.entrySet()) {
	            	writer.write(map.getKey()+"=");
	            	for(Set S:(List<SortedSet<String>>)map.getValue()) {
	            		writer.write("(");
	            		for(String s: (SortedSet<String>)S) {
	            			writer.write(s + ",");
	            		}
	            		writer.write(")");
	            	}
	            	writer.write("\n");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                // Close the writer regardless of what happens...
	                writer.close();
	            } catch (Exception e) {
	            }
	        }
	}
	
	private List<CRUDAccess> readDBAccessJson(String dbFile) throws Exception{
		JSONArray db_crud = new JSONArray();
		List<CRUDAccess> service_db = new ArrayList<CRUDAccess>();
		try {
			Object obj = new JSONParser().parse(new FileReader(dbFile));
			db_crud = (JSONArray)obj;
			Iterator<JSONObject> itr = db_crud.iterator(); 
			JSONObject tmp;
			while(itr.hasNext()) {
				tmp=(JSONObject)itr.next();
				CRUDAccess crud = new CRUDAccess();
				crud.setServiceName((String)tmp.get("service_name"));
				crud.setTableName((String)tmp.get("db_name"));
				crud.setAccessTypes((String)tmp.get("crud"));
				service_db.add(crud);
				if(!nodes.contains(crud.getTableName())) {
					nodes.add(crud.getTableName());
				}
			}
		}
		catch(Exception e) {
			
		}
		return service_db;
		
	}
	private Map<String,Set<String>> readEntryPoint(String epFile) {
		Set<String> epSet = new HashSet<String>();
		Map<String,Set<String>> epmap = new HashMap<String, Set<String>>();
		try {
			Object obj = new JSONParser().parse(new FileReader(epFile));
			JSONObject entrypoints = (JSONObject)obj;
			Set<?> keys = entrypoints.keySet();
			Iterator<?> i = keys.iterator();
			while(i.hasNext())
			{
		        String k = i.next().toString();
		        JSONArray ep_methods=(JSONArray)entrypoints.get(k);
		        epSet = new HashSet<String>();
		        for(int j=0;j<ep_methods.size();j++) {
		        	epSet.add((String)ep_methods.get(j));
		        }
		        epmap.put(k, epSet);
		    }
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return epmap;
	}
	
	private Map<String,Set<String>> readEntryPoint_newformat(String epFile) {
		Set<String> epSet = new HashSet<String>();
		Map<String,Set<String>> epmap = new HashMap<String, Set<String>>();
		try {
			Object obj = new JSONParser().parse(new FileReader(epFile));
			JSONArray entrypoints = (JSONArray)obj;
			for(Object item:entrypoints){
				JSONObject tmp=(JSONObject)item;
				String epname = (String)tmp.get("service_entry_name");
				JSONArray ep_meth = (JSONArray)tmp.get("class_method_name");
				epSet = new HashSet<String>();
		        for(int j=0;j<ep_meth.size();j++) {
		        	epSet.add((String)ep_meth.get(j));
		        }
		        epmap.put(epname, epSet);
			}
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return epmap;
	}
	
	private void readBOPathInput(String path){
		BufferedReader reader = null;
		try {
			File user_ip=new File(path);
			if(user_ip.exists()) {
				reader = new BufferedReader(new FileReader(user_ip));
				String st;
				while((st = reader.readLine())!=null) {
					user_bo_input.add(st);
					System.out.println(st);
				}
				if(user_bo_input.size()==0) {
					// the user has not provided any path input. We will initialize it with our path terms
					user_bo_input.add(".jpa");
					user_bo_input.add(".vo.");
					user_bo_input.add(".beans");
					user_bo_input.add(".entities");
					user_bo_input.add("dao");
					user_bo_input.add(".service");
					
				}
			}
			else {
				System.out.println("User Input BO File does not Exist");
				user_bo_input.add(".jpa");
				user_bo_input.add(".vo.");
				user_bo_input.add(".beans");
				user_bo_input.add(".entities");
				user_bo_input.add("dao");
				user_bo_input.add(".service");
			}
			
		}
		catch(Exception e) {
			System.out.println(e);
		}	
	}
	
	/*private Map<String,Set<String>> readEntryPoint_newformat(String epFile) {
		Set<String> epSet = new HashSet<String>();
		String epname="";
		Map<String,Set<String>> epmap = new HashMap<String, Set<String>>();
		try {
			// create Gson instance
		    Gson gson = new Gson();

		    // create a reader
		    Reader reader = new FileReader(epFile);

		    // convert JSON file to map
		    //Map<?, ?> map = gson.fromJson(reader, Map.class);
		    
		    //convert JSON File to list
		    List<Map<?,?>> list = gson.fromJson(reader, new TypeToken<Map<?,?>>(){}.getType());
		    
		    for(Map<?,?> item : list) {
		    	
		    	for (Map.Entry<?, ?> epitem : item.entrySet()) {
		    		if(((String)epitem.getKey()).equals("service_entry_name")) {
		    			epname = (String)epitem.getValue();
		    		}
		    		if(((String)epitem.getKey()).equals("class_method_name")) {
		    			List<String> mname= (List<String>)epitem.getValue();
		    			epSet=new HashSet<String>();
		    			epSet.addAll(mname);
		    		}
		    	}
		    	epmap.put(epname, epSet);
		    }
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return epmap;
	}*/
	
	private HashMap<String,List<String>> readCallGraph(String pathsFile) {
		HashMap<String,List<String>> ep_call_graphs = new HashMap<String, List<String>>();
		// read the paths field for each entrypoint. Create a string for each path under paths just by appending them using comma and add it to the list. This list is mapped to entrypoint string.
		try {
			//JsonParser parser = new JsonParser();
			
			// create Gson instance
		    Gson gson = new Gson();

		    // create a reader
		    Reader reader = new FileReader(pathsFile);

		    // convert JSON file to map
		    Map<?, ?> map = gson.fromJson(reader, Map.class);
		    String pathString="";
		    
		    System.out.println("paths json has "+ map.size() +" fields");
		    for (Map.Entry<?, ?> entry : map.entrySet()) {
		    	String fieldname = (String)entry.getKey();
		        if(fieldname.equals("entryPoints")) {
		        	System.out.println("found entryPoints");
		        	List<Map<?,?>> fieldval = (List<Map<?,?>>)entry.getValue();
		        	System.out.println("number of entryPoints "+fieldval.size());
		        	for(Map<?,?> e2 : fieldval) {
		        		List<String> plist=new ArrayList<String>();
		        		String entryptname="";
		        		for (Map.Entry<?, ?> pathmap : e2.entrySet()) {
		        			if(((String)pathmap.getKey()).equals("paths")) {
		        				List<Map<?,?>> pathlist = (List<Map<?,?>>)pathmap.getValue();
		        				for(Map<?,?> path: pathlist) {
		        					
		        					for (Map.Entry<?, ?> p : path.entrySet()) {
		        						pathString="";
		        						if(((String)p.getKey()).equals("path")) {
		        							List<String> meth = (List<String>)p.getValue();
		        							for(String m:meth) {
		        								pathString=pathString.concat(m+",");
		        							}
		        							plist.add(pathString);
		        						}
		        					}
		        				}
		        			}
		        			else if(((String)pathmap.getKey()).equals("entryPoint")) {
		        				entryptname=(String)pathmap.getValue();
		        			}
		        		}
		        		ep_call_graphs.put(entryptname,plist);
		        	}
		        	break;
		        }
		    }

		    // print map entries
		    /*for (Map.Entry<?, ?> entry : map.entrySet()) {
		        System.out.println(entry.getKey() + "=" + entry.getValue());
		    }*/

		    // close reader
		    reader.close();
			
			
		}
		catch(Exception e) {
			System.out.println(e);
		}
		return ep_call_graphs;
	}
	

}
