package com.ibm.research.msr.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import weka.gui.simplecli.Exit;

public class ReadJarMap {

	private static Map<String, String> libCatMap = new HashMap<String, String>();
	
	
	public static void createJARCategoryMap(String csvFile) {
		
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			if(br.readLine()==null) {
				System.out.println("Empty jar mapping error while parsing jar's.");
				System.exit(-1);
			}
			br = new BufferedReader(new FileReader(csvFile));	
			while ((line = br.readLine()) != null) {
				String[] libCat = line.split(cvsSplitBy);
				String[] temp = libCat[0].split("/");
				String jarname=temp[temp.length-1];
//				 System.out.println(jarname);
				if(libCat[1].length()!=0) { // TODO: check giri code on why they are coming 
//					System.out.println(libCat[1]);
			    	libCatMap.put(libCat[1], jarname);
				}
				else {
					System.out.println("File has empty mapping for"+line);
				}
			}
		} catch (FileNotFoundException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	@SuppressWarnings("resource")
	public static void createJARCategoryMap() {
//		String csvFile = "src/main/resources/jar-imp.csv";
		String csvFile="src/main/resources/jar-to-packges.csv";
		
		 createJARCategoryMap(csvFile);
		
	}
	


	public static Map<String, String> getLibCatMap() {
		return libCatMap;
	}

	public static void setLibCatMap(Map<String, String> libCatMap) {
		ReadJarMap.libCatMap = libCatMap;
	}

}
