package com.ibm.research.msr.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReadJarMap {

	private static Map<String, String> libCatMap = new HashMap<String, String>();

	public static void createJARCategoryMap() {
//		String csvFile = "src/main/resources/jar-imp.csv";
		String csvFile="src/main/resources/digdeep-jar-to-packges.csv";
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		

		try {
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
				String[] libCat = line.split(cvsSplitBy);
				getLibCatMap().put(libCat[1], libCat[0]);
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

	public static Map<String, String> getLibCatMap() {
		return libCatMap;
	}

	public static void setLibCatMap(Map<String, String> libCatMap) {
		ReadJarMap.libCatMap = libCatMap;
	}

}
