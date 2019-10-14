package com.ibm.research.msr.clustering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ibm.research.msr.utils.Util;

public class CohesionCouplingProcessing {
	
	private String clusterAllJSONFile = null;
	private String usageJSONFile = null;
	private String outputFile = null;

	public CohesionCouplingProcessing(String clusterAllJSON, String usageJSON, String outputLocation) {
		this.clusterAllJSONFile = clusterAllJSON;
		this.usageJSONFile = usageJSON;
		this.outputFile = outputLocation;
	}

	public void runClustering() {
		
		String cohesionPythonFile = Util.getCohesionPythonFile();
		
		String cmd = Util.getPythonCommand() + " " + cohesionPythonFile + " " + this.clusterAllJSONFile + " "
		+ usageJSONFile + " " + outputFile;
		
		try {
			System.out.println(cmd);
			Runtime rt = Runtime.getRuntime();

			// generate the clusters
			Process proc = rt.exec(cmd);
			InputStream stderr = proc.getErrorStream();
			//InputStream stderr = proc.getInputStream();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
