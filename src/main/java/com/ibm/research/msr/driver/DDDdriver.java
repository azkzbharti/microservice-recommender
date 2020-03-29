package com.ibm.research.msr.driver;
import com.ibm.research.msr.ddd.*;

public class DDDdriver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*String crud_path = "C:/Users/SHIVALIAGARWAL/Downloads/daytrader-db.json";
		String entrypt_path = "C:/Users/SHIVALIAGARWAL/Downloads/daytrader-service.json";
		//String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/daytrader.sample.json";
		String call_graph = "C:/Work/HC/AppMod/MVP1-M2M/Input/daytrader.newpaths.json";*/
		
		String crud_path = "C:/Users/SHIVALIAGARWAL/Downloads/estore-db.json";
		String entrypt_path = "C:/Users/SHIVALIAGARWAL/Downloads/estore-service.json";
		String call_graph = "C:/Users/SHIVALIAGARWAL/Downloads/estore.newpaths.json";
		String seed_path = "C:/Work/HC/AppMod/MVP1-M2M/Input/seeds.txt";
		
		runDDDSeedAnalysis(crud_path,entrypt_path,call_graph,seed_path);
	}
	
	private static void runDDDSeedAnalysis(String crud_path, String entrypt_path, String call_graph, String seed_path) {
		System.out.println("STARTING ANALYSIS");
		EntityBeanAffinity dddanalysis = new EntityBeanAffinity();
		dddanalysis.runAnalysis(crud_path, entrypt_path, call_graph, seed_path);
		System.out.println("END");
	}

}
