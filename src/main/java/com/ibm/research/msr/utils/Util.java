package com.ibm.research.msr.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.ibm.research.appmod.pa.jarlist.JarApiList;

public class Util {

	public static String getProperty(String key) {
		System.out.println("Looking for +"+key+ " "+System.getenv(key));
		return System.getenv(key);
	}

	public static String getAffinityAlgoPythonFile() {
		String MSR_HOME = Util.getMSRBaseDir();
		return MSR_HOME + File.separator + "python" + File.separator + "affinity_algo.py ";

	}

	public static String getCohesionPythonFile() {
		String MSR_HOME = Util.getMSRBaseDir();
		return MSR_HOME + File.separator + "python" + File.separator + "recluster_driver.py ";

	}

	public static String getStopWordsFile() {

		String MSR_HOME = Util.getMSRBaseDir();

		return MSR_HOME + File.separator + Constants.STOP_WORDS_FILE;

	}

	public static boolean dumpAPIInfo(String folderPath, String outputPath) {

		Collection<File> jarList = null;
		ArrayList<String> jarFiles = null;

		jarList = FileUtils.listFiles(new File(folderPath), new String[] { "jar" }, true);

		Iterator<File> jarListItr = jarList.iterator();

		if (jarList.isEmpty())
			return false;

		jarFiles = new ArrayList<String>();

		while (jarListItr.hasNext()) {

			jarFiles.add(jarListItr.next().getAbsolutePath());

		}

		if (!jarFiles.isEmpty()) {

			// jar files found. Extract the api information
			JarApiList jarAPIList = new JarApiList();
			jarAPIList.dumpAPIInfoForJars(jarFiles, outputPath);

		}

		return true;

	}
	
	public static String getMSRBaseDir() {
		return System.getProperty("MSR_HOME");
	}

}
