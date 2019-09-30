package com.ibm.research.msr.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class Util {

	private static Properties prop = null;

	private static Properties getProperties() {

		if (prop == null) {

			String MSR_HOME = System.getProperty("MSR_HOME");

			Collection<File> propList = FileUtils.listFiles(new File(MSR_HOME), new String[] { "properties" }, true);

			Iterator<File> fileListItr = propList.iterator();
			File propFile = null;

			while (fileListItr.hasNext()) {
				propFile = fileListItr.next();
				break;
			}

			prop = new Properties();

			if (propFile != null) {

				try {
					prop.load(new FileReader(propFile));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(" Properties file not found");
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

	public static String getStopWordsFile() {
		
		String MSR_HOME = System.getProperty("MSR_HOME");
		
		return MSR_HOME + File.separator + "stop_words.txt";
		
	}

}