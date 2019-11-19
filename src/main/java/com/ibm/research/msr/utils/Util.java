package com.ibm.research.msr.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.ibm.research.msr.clustering.Affinity;
import com.ibm.research.msr.jarlist.JarApiList;

public class Util {

	private static Properties prop = null;

	private static Properties getProperties() {

		if (prop == null) {

			String MSR_HOME = System.getProperty("MSR_HOME");
			String propFile = MSR_HOME + File.separator + "msr.properties";

			prop = new Properties();

			try {
				prop.load(new FileReader(propFile));
			} catch (IOException e) {
				System.out.println(" Issues while reading msr.properties");
				e.printStackTrace();

			}

		}

		return prop;
	}

	public static String getPythonCommand() {

		return getProperties().getProperty("PYTHON_HOME");
	}

	private static String getProjectName(String gitURL) {

		String[] gitUrlParts = gitURL.split("/");
		String projectName = gitUrlParts[gitUrlParts.length - 1];

		return projectName;

	}

	public static String[] getArgumentsForGit(String gitURL) {

		String arguments = (String) getProperties().get(getProjectName(gitURL));
		if (arguments != null) {
			String[] args = arguments.split(",");
			return args;
		} else {
			return null;
		}

	}

	public static String getOutputFolder(String gitURL) {
		String arguments = (String) getProperties().get(getProjectName(gitURL));
		if (arguments != null) {
			String[] args = arguments.split(",");
			return args[2];
		} else {
			return null;
		}

	}

	public static String getAffinityAlgoPythonFile() {
		String MSR_HOME = System.getProperty("MSR_HOME");
		return MSR_HOME + File.separator + "python" + File.separator + "affinity_algo.py ";

	}

	public static String getCohesionPythonFile() {
		String MSR_HOME = System.getProperty("MSR_HOME");
		return MSR_HOME + File.separator + "python" + File.separator + "recluster_driver.py ";

	}

	public static String getStopWordsFile() {

		String MSR_HOME = System.getProperty("MSR_HOME");

		return MSR_HOME + File.separator + "stop_words.txt";

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

}
