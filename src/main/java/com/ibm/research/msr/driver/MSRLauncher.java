package com.ibm.research.msr.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Path;

import com.ibm.research.msr.clustering.Affinity;
import com.ibm.research.msr.clustering.CohesionCouplingProcessing;
import com.ibm.research.msr.jarlist.APIUsageStats;
import com.ibm.research.msr.jarlist.APIUsageStatsMiner;
import com.ibm.research.msr.jarlist.GradleDependencyDownloader;
import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.jarlist.MavenCategoryEtAlExtractor;
import com.ibm.research.msr.jarlist.POMDependencyDownloader;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.ReadJarMap;
import com.ibm.research.msr.utils.UnzipUtility;
import com.ibm.research.msr.utils.Util;

import weka.gui.explorer.ClustererAssignmentsPlotInstances;

public class MSRLauncher {

	private static void printUsage() {
		System.out.println(
				"Usage: java -DMSR_HOME=<absolute path of the resources folder> MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
	}

	/**
	 * Deletes a directory on disk by recursively deleting all of its content.
	 *
	 * @param directory the root directory to delete
	 * @return whether this operation was successful
	 */
	public static boolean deleteDirectory(File directory) {
		boolean success = true;
		if (directory.exists()) { // If it doesn't exist anyway return true
			File[] files = directory.listFiles();
			if (files != null) { // some JVMs return null for empty dirs
				for (File file : files) {
					if (file.isDirectory()) {
						success = success && deleteDirectory(file);
					} else {
						success = success && file.delete();
					}
				}
			}
			success = success && directory.delete();
		}
		return success; // Will be false if even one item could not be deleted
	}

	public static void main(String[] args) {
		// src <root folder path> ( contains .java files)
		// bin < root folder path> ( contains .class files)

		if (args.length != 4) {
			printUsage();
			return;
		}

		String type = args[0];
		String rootPath = args[1];
		String outputPath = args[2];
		String algo = args[3];

		String tempFolder = outputPath + File.separator + "temp";
		String jarFolder = outputPath + File.separator + "temp" + File.separator + "jars";
		String unzipFolder = outputPath + File.separator + "temp" + File.separator + "unzip";
		String uiFolder = outputPath + File.separator + "ui";
		String jarPackagestoCSV = outputPath + File.separator + "temp" + File.separator + "jar-to-packages.csv";
		String mavenMetaJSON = outputPath + File.separator + "temp" + File.separator + "maven-meta.json";
		String barDataJSON = uiFolder + File.separator + "data" + File.separator + "bar-data.json";

		String affinityClusterJSON = uiFolder + File.separator + "data" + File.separator + "cluster-affinity.json";
		String clusterAllJSON = uiFolder + File.separator + "data" + File.separator + "clusterall.json";

		String cohesionJSON = uiFolder + File.separator + "data" + File.separator + "cohesion-affinity.json";
		String cohesionAllJSON = uiFolder + File.separator + "data" + File.separator + "cohesion-all.json";
		String interClassUsageJSON = outputPath + File.separator + "temp" + File.separator + "inter-class-usage.json";

		String MSR_HOME = System.getProperty("MSR_HOME");

		// if output folder allready exist - delete it.
		File outputFolder = new File(outputPath);

		if (!deleteDirectory(outputFolder)) {
			System.err.println("************* Unable to Delete TEMP Folder *****************");
			// throw new Exception("Unable to delete temp directory: " +
			// outputFolder.getAbsolutePath());
		}

		// creating the temp folder and jar folders inside output folder.
		outputFolder.mkdir();
		new File(tempFolder).mkdir();
		new File(jarFolder).mkdir();
		new File(unzipFolder).mkdir();
		new File(uiFolder).mkdir();

		// copy the UI folder to the output folder;
		try {
			FileUtils.copyDirectory(new File(MSR_HOME + File.separator + "ui"), new File(uiFolder));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println(" Unable to copy UI folder ");
		}

		Collection<File> buildFieList = null;
		ArrayList<String> pomFiles = null;
		ArrayList<String> gradleFiles = null;

		boolean parsedJars = false;

		if (type.trim().toLowerCase().equals(Constants.SRC)) {
			// src can contain either a pom.xml, build.gradle | a lib folder with jars
			// inside it.

			buildFieList = FileUtils.listFiles(new File(rootPath), new String[] { "xml", "gradle" }, true);

			Iterator<File> fileListItr = buildFieList.iterator();

			pomFiles = new ArrayList<String>();
			gradleFiles = new ArrayList<String>();

			File buildFile = null;

			while (fileListItr.hasNext()) {
				buildFile = fileListItr.next();
				if (buildFile.getName().equals("pom.xml")) {
					pomFiles.add(buildFile.getAbsolutePath());
				} else if (buildFile.getName().endsWith("gradle")) {
					gradleFiles.add(buildFile.getAbsolutePath());
				}
			}

			if (pomFiles.isEmpty() && gradleFiles.isEmpty()) {
				// no pom or gradle files found, hence it might contain a jars directly in lib
				// folder
				parsedJars = Util.dumpAPIInfo(rootPath, tempFolder);
				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(rootPath, mavenMetaJSON);

			} else if (!pomFiles.isEmpty()) {
				// we have POM, we need to parse pom and download all jar files

				POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
				pomDownloader.download(pomFiles, jarFolder);

				parsedJars = Util.dumpAPIInfo(jarFolder, tempFolder);
				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(jarFolder, mavenMetaJSON);

			} else {
				// we have gradle file, we need to parse gradle and download all jar files

				// create pom files out of gradle file
				GradleDependencyDownloader gradleDownloader = new GradleDependencyDownloader();
				pomFiles = gradleDownloader.createPOMFiles(gradleFiles, jarFolder);

				if (!pomFiles.isEmpty()) {
					// use the pom file logic to download the jars now
					POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
					pomDownloader.download(pomFiles, jarFolder);
					parsedJars = Util.dumpAPIInfo(jarFolder, tempFolder);
				}

				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(jarFolder, mavenMetaJSON);

			}

			if (!parsedJars && pomFiles.isEmpty()) {
				System.out.println(
						" The application source does not contain pom.xml. It also does not have any dependency on third party jars. We can't apply our micro-service recommendation approach.");
				return;
			}

			System.out.println(" Need to invoke the algo-driver now");
			System.out.println(" App Src Root Folder " + rootPath);
			System.out.println(" JAR API Info " + tempFolder + Path.SEPARATOR + "jar-to-packages.csv");
			System.out.println(" Output folder " + outputPath);
			System.out.println(" Temo Folder " + tempFolder);
			System.out.println(" Algo to run " + algo);

			try {
				MSRdriver.runNaive(rootPath, type, outputPath, jarPackagestoCSV);
				runAffinity(rootPath, affinityClusterJSON, tempFolder, type);

				// cohesion-coupling
				runCohesionCoupling(affinityClusterJSON, interClassUsageJSON, cohesionJSON);
				runCohesionCoupling(clusterAllJSON, interClassUsageJSON, cohesionAllJSON);

				// generate stats information
				APIUsageStatsMiner statsMiner = new APIUsageStatsMiner();
				statsMiner.mine(rootPath, jarPackagestoCSV, barDataJSON);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (type.trim().toLowerCase().equals(Constants.BIN)) {
			// it has to be either packaged as JAR or EAR.

			if (rootPath.endsWith(".jar")) {

				String[] splits = rootPath.split(File.separator);
				String jarName = splits[splits.length - 1];
				String dirName = jarName.substring(0, jarName.length() - 4);

				UnzipUtility.unzip(rootPath, unzipFolder + File.separator + dirName);

			} else if (rootPath.endsWith(".ear")) {

				// unzip the mar ear file
				UnzipUtility.unzip(rootPath, unzipFolder);

				// check if the folder now contains wars and ejb-jar files

				Collection<File> warjarList = FileUtils.listFiles(new File(unzipFolder), new String[] { "war", "jar" },
						true);
				if (!warjarList.isEmpty()) {
					Iterator<File> warjarListItr = warjarList.iterator();
					while (warjarListItr.hasNext()) {
						UnzipUtility.unzip(warjarListItr.next().getAbsolutePath(), unzipFolder);
					}

				}

				parsedJars = Util.dumpAPIInfo(unzipFolder, tempFolder);

			} else {
				printUsage();
				System.out.println("We only support .ear and .jar for bin option");
				return;
			}

			System.out.println(" Need to invoke the algo-driver now");
			System.out.println(" App Src Root Folder " + unzipFolder);
			System.out.println(" JAR API Info " + tempFolder + Path.SEPARATOR + "jar-to-packages.csv");
			System.out.println(" Output folder " + outputPath);
			System.out.println(" Temo Folder " + tempFolder);
			System.out.println(" Algo to run " + algo);

			try {
				MSRdriver.runNaive(unzipFolder, type, outputPath, jarPackagestoCSV);
				runAffinity(unzipFolder, affinityClusterJSON, tempFolder, type);

				// cohesion-coupling
				runCohesionCoupling(affinityClusterJSON, interClassUsageJSON, cohesionJSON);
				runCohesionCoupling(clusterAllJSON, interClassUsageJSON, cohesionAllJSON);

				APIUsageStatsMiner statsMiner = new APIUsageStatsMiner();
				statsMiner.mine(unzipFolder, jarPackagestoCSV, barDataJSON);

				MavenCategoryEtAlExtractor mavenExtractor = new MavenCategoryEtAlExtractor();
				mavenExtractor.find(unzipFolder, mavenMetaJSON);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			printUsage();
			return;
		}

	}

	private static ArrayList<String> getJavaFileNames(String rootPath, String type) {

		Collection<File> javaFiles = null;
		String endsWith = "";
		if (type.equals(Constants.SRC)) {
			javaFiles = FileUtils.listFiles(new File(rootPath), new String[] { "java" }, true);
			endsWith = ".java";

		} else {
			javaFiles = FileUtils.listFiles(new File(rootPath), new String[] { "class" }, true);
			endsWith = ".class";
		}

		ArrayList<String> javaFileNames = new ArrayList<String>();

		if (!javaFiles.isEmpty()) {

			for (File f : javaFiles) {
				String name = f.getName();
				if (name.endsWith(endsWith)) {
					name = name.substring(0, name.indexOf("."));
					javaFileNames.add(name);
				}
			}

		}

		return javaFileNames;

	}

	private static void runAffinity(String rootPath, String outputJSONFile, String tempFolder, String type) {

		// check for type and support getting this info from binary
		ArrayList<String> javaFiles = getJavaFileNames(rootPath, type);

		Object[] gfg = javaFiles.toArray();
		String[] str = Arrays.copyOf(gfg, gfg.length, String[].class);

		Affinity affinity = new Affinity(str, tempFolder + File.separator + "cluster-affinity.properties",
				outputJSONFile);
		affinity.runClustering();

	}

	public static void runCohesionCoupling(String clusterAllJSON, String usageJSON, String outputFile) {

		CohesionCouplingProcessing postProcessor = new CohesionCouplingProcessing(clusterAllJSON, usageJSON,
				outputFile);
		postProcessor.runClustering();

	}

}
