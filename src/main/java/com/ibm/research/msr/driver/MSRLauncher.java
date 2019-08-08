package com.ibm.research.msr.driver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.jarlist.POMDependencyDownloader;
import com.ibm.research.msr.utils.Constants;

public class MSRLauncher {

	public static void main(String[] args) {
		// src <root folder path> ( contains .java files)
		// bin < root folder path> ( contains .class files)

		if (args.length != 4) {
			System.out.println(
					"Usage: java MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
			return;
		}

		String type = args[0];
		String rootPath = args[1];
		String outputPath = args[2];
		String algo = args[3];

		String tempFolder = outputPath + File.separator + "temp";
		String jarFolder = outputPath + File.separator + "temp" + File.separator + "jars";
		
		if(!(new File(outputPath).exists())){
			// if output folder is not created, create that first.
			new File(outputPath).mkdir();
		}

		//creating the temp folder and jar folders inside output folder.
		new File(tempFolder).mkdir();
		new File(jarFolder).mkdir();

		Collection<File> xmlFileList = null;
		ArrayList<String> pomFiles = null;

		Collection<File> jarList = null;
		ArrayList<String> jarFiles = null;

		if (type.trim().toLowerCase().equals(Constants.SRC)) {
			// src can contain either a pom.xml | a lib folder with jars inside it.

			xmlFileList = FileUtils.listFiles(new File(rootPath), new String[] { "xml" }, true);

			Iterator<File> fileListItr = xmlFileList.iterator();

			pomFiles = new ArrayList<String>();

			while (fileListItr.hasNext()) {
				File xmlFile = fileListItr.next();
				if (xmlFile.getName().equals("pom.xml")) {
					pomFiles.add(xmlFile.getAbsolutePath());
				}
			}

			if (pomFiles.isEmpty()) {
				// no pom files found, hence it might contain a jars directly in lib folder

				jarList = FileUtils.listFiles(new File(rootPath), new String[] { "jar" }, true);

				Iterator<File> jarListItr = jarList.iterator();

				jarFiles = new ArrayList<String>();

				while (jarListItr.hasNext()) {

					jarFiles.add(jarListItr.next().getAbsolutePath());

				}
				
				if (!jarFiles.isEmpty()) {
					
					// jar files found. Extract the api information
					JarApiList jarAPIList = new JarApiList();
					jarAPIList.dumpAPIInfoForJars(jarFiles, tempFolder);
					
				}

			} else {
				// we have POM, we need to parse pom and download all jar files
				
				POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
				pomDownloader.download(pomFiles, tempFolder);

			}

			if (jarFiles.isEmpty() && pomFiles.isEmpty()) {
				System.out.println(
						" The application source does not contain pom.xml. It also does not have any dependency on third party jars. We can't apply our micro-service recommendation approach.");
				return;
			}

		} else if (type.trim().toLowerCase().equals(Constants.BIN)) {

		} else {
			System.out.println("Usage: java MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
			return;
		}

	}

}
