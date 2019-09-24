package com.ibm.research.msr.clustering;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.research.msr.utils.Util;

public class Affinity {

	String[] words = null;
	String outputFile = null;
	String propFile = null;
	HashMap<String, String> stopWordsMap = null;
	HashMap<String, String> originalMap = null;

	public Affinity(String[] words, String propFile, String outputFile) {
		this.words = words;
		this.outputFile = outputFile;
		this.propFile = propFile;

	}

	public void runClustering() {

		String affinityPythonFile = Util.getAffinityAlgoPythonFile();

		// get the stop words
		populateStopWords();
		// replace stop words
		replaceStopWords();

		String words_cmd = "";

		for (String word : words) {
			words_cmd = words_cmd + word + " ";
		}

		String cmd = Util.getPythonCommand() + " " + affinityPythonFile + " --inputArray " + words_cmd.trim()
				+ " --outPutFilePath " + propFile;

		try {
			// System.out.println(cmd);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// introduce the stop-words back
		saveClusterJSON();

	}

	private String getOriginalName(String s) {
		String returnValue = s;
		if (originalMap != null) {
			returnValue =  originalMap.get(s);
		}
		
		if(returnValue == null)
			return s;
		
		return returnValue;
	}

	private void saveClusterJSON() {

		// load the properties file and fix the stop-words removal
		Properties prop = new Properties();
		Properties modProp = new Properties();
		try {
			prop.load(new FileReader(propFile));
			Iterator<Object> itr = prop.keySet().iterator();
			while (itr.hasNext()) {
				String key = (String) itr.next();
				String value = prop.getProperty(key);

				if (stopWordsMap != null) {

					Iterator<String> itrs = stopWordsMap.keySet().iterator();
					while (itrs.hasNext()) {
						String replace = itrs.next();
						String find = stopWordsMap.get(replace);
						key = key.replaceAll(find, replace);
						value = value.replaceAll(find, replace);

					}

				}

				modProp.setProperty(key, value);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// save the properties file as JSON

		JSONObject rootObject = new JSONObject();

		rootObject.put("name", "clusters");

		JSONArray rootChildrenArray = new JSONArray();

		Iterator<Object> itr = modProp.keySet().iterator();

		while (itr.hasNext()) {
			String name = (String) itr.next();
			JSONObject clusterobj = new JSONObject();
			clusterobj.put("name", getOriginalName(name));

			String value = modProp.getProperty(name);
			String[] values = value.split(",");

			JSONArray childrenArray = new JSONArray();

			for (String s : values) {

				JSONObject childObj = new JSONObject();
				childObj.put("name", getOriginalName(s));
				// TODO: size has to be determined properly, right now using the name of the
				// class
				childObj.put("size", s.length());

				childrenArray.add(childObj);

			}

			clusterobj.put("children", childrenArray);

			rootChildrenArray.add(clusterobj);

		}

		rootObject.put("children", rootChildrenArray);

		if (rootObject != null) {
			try {
				Files.write(Paths.get(outputFile), rootObject.toString().getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				System.out.println("Error while writing file to  " + outputFile);
			}
		} else {
			System.out.println("Error while writing file to  " + outputFile);
		}

		System.out.println("File written at " + outputFile);

	}

	private void replaceStopWords() {
		StringBuffer buff = new StringBuffer();
		originalMap = new HashMap<String, String>();
		for (String word : words) {
			originalMap.put(word.toLowerCase(), word);
			buff.append(word.toLowerCase() + " ");
		}

		String wordString = buff.toString().trim();

		if (stopWordsMap != null) {

			Iterator<String> itr = stopWordsMap.keySet().iterator();
			while (itr.hasNext()) {
				String find = itr.next();
				String replace = stopWordsMap.get(find);
				wordString = wordString.replaceAll(find, replace);

			}

		}

		words = wordString.split(" ");
	}

	private void populateStopWords() {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(Util.getStopWordsFile()));
			String line = reader.readLine();
			int i = 1;
			stopWordsMap = new HashMap<String, String>();
			while (line != null) {
				line = line.toLowerCase().trim();
				String s = i+"";
				stopWordsMap.put(line,s+s+s);
				i++;

				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		Affinity affinity = new Affinity(
				new String[] { "BackOrder", "HelpBean", "CatalogMgr", "AccountBean", "OrderInfo", "Order", "Util",
						"BackOrderMgr", "ValidatorUtils", "AccountServlet", "AdminServlet", "OrderItem", "ImageServlet",
						"ValidatePassword", "ShoppingCartBean", "ShoppingBean", "LogInfo", "SuppliersBean",
						"PopulateDBBean", "ShoppingItem", "CustomerMgr", "Customer", "ResetDBBean", "MailAction",
						"MailerBean", "Inventory Supplier" },
				"/Users/senthil/app-mod-test/cluster.properties", "/Users/senthil/app-mod-test/clusterall.json");
		affinity.runClustering();
	}

}
