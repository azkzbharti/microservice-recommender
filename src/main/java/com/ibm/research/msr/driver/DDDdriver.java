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
package com.ibm.research.msr.driver;
import com.ibm.research.appmod.slicing.SlicingDriver;
import com.ibm.research.msr.ddd.*;

public class DDDdriver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String crud_path = "C:/Users/SHIVALIAGARWAL/Downloads/daytrader-db.json";
		//String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader/service.json";
		String entrypt_path = "C:/Users/SHIVALIAGARWAL/Downloads/daytrader-service.json";
		//String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/daytrader.sample.json";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader/seeds.txt";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/daytrader.newpaths.json";*/
		
		String crud_path = "C:/Users/SHIVALIAGARWAL/Downloads/estore-db.json";
		String entrypt_path = "C:/Users/SHIVALIAGARWAL/Downloads/estore-service.json";
		String call_graph = "C:/Users/SHIVALIAGARWAL/Downloads/estore.newpaths.json";
		String call_graph_dot ="";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/estore/seeds.txt";
		//String outputFileName = "C:/Work/HC/AppMod/MVP1-M2M/Input/call_graph.json";
		
		/*String crud_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/pbw-db.json";
		String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/pbw-service.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/call_graph.json";
		String call_graph_dot ="C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/pbw-call.dot";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/seeds.txt";*/
		//String outputFileName = "C:/Work/HC/AppMod/MVP1-M2M/Input/pbw/call_graph.json";
		
		/*String crud_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/BNK_IB-db.json";
		String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/BNK_IB-service.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/call_graph.json";
		String call_graph_dot ="C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/BNK_IB-call.dot";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/MUFG/seeds.txt";*/
		
		/*String crud_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/UPS-FGV/FGVUIServices-v2-db.json";
		String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/UPS-FGV/FGVUIServices-v2-service.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/UPS-FGV/upspaths.v2.json";
		//String call_graph_dot ="C:/Work/HC/AppMod/MVP1-M2M/Input/UPS-FGV/FGVUIServices-v2-call.dot";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/UPS-FGV/seeds.txt";*/
		
		/*String crud_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/Viper/db.json";
		String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/Viper/rbkme-service-m2m.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/Viper/viper.paths.json";
		String call_graph_dot ="C:/Work/HC/AppMod/MVP1-M2M/Input/Viper/rbkme-call.dot";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/Viper/seeds.txt";*/
		
		/*String crud_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader_Latest/db.json";
		String entrypt_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader_Latest/service.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader_Latest/businessslices.json";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/DayTrader_Latest/seeds.txt";*/
		
		String user_bl_input="C:/Work/HC/AppMod/MVP1-M2M/Input/bo_package_terms_input.txt";
		String user_ep_input="init";
		
		//generateCallGraphJson(call_graph_dot, entrypt_path,call_graph);
		
		runDDDSeedAnalysis(crud_path,entrypt_path,call_graph,user_bl_input,user_ep_input,seed_path);
	}
	
	private static void runDDDSeedAnalysis(String crud_path, String entrypt_path, String call_graph,String user_bl_input, String user_ep_input, String seed_path) {
		System.out.println("STARTING ANALYSIS");
		EntityBeanAffinity dddanalysis = new EntityBeanAffinity();
		dddanalysis.runAnalysis(crud_path, entrypt_path, call_graph, user_bl_input, seed_path);
		System.out.println("END");
	}
	
	private static void generateCallGraphJson(String call_graph_dot, String entrypt_path, String icu_path, String outputFileName, String dbjson_path) {
		System.out.println("STARTING Generation");
		
		SlicingDriver.performSlicing(call_graph_dot, entrypt_path, icu_path, outputFileName, dbjson_path);
		System.out.println("END");
	}

}

