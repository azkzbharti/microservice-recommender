package com.ibm.research.msr.driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Path;

import com.ibm.research.msr.jarlist.GradleDependencyDownloader;
import com.ibm.research.msr.jarlist.JarApiList;
import com.ibm.research.msr.jarlist.POMDependencyDownloader;
import com.ibm.research.msr.utils.Constants;

public class MSRLauncher {

	public static void main(String[] args) {
		// src <root folder path> ( contains .java files)
		// bin < root folder path> ( contains .class files)

		if (args.length != 4) {
			System.out.println(
					"Usage: java -DMSR_HOME=<absolute path of the resources folder> MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
			return;
		}

		String type = args[0];
		String rootPath = args[1];
		String outputPath = args[2];
		String algo = args[3];

		String tempFolder = outputPath + File.separator + "temp";
		String jarFolder = outputPath + File.separator + "temp" + File.separator + "jars";
		String unzipFolder = outputPath + File.separator + "temp" + File.separator + "unzip";
		String uiFolder = outputPath + File.separator + "ui" ;
		
		
		String MSR_HOME = System.getProperty("MSR_HOME");
	

		if (!(new File(outputPath).exists())) {
			// if output folder is not created, create that first.
			new File(outputPath).mkdir();
		}

		// creating the temp folder and jar folders inside output folder.
		new File(tempFolder).mkdir();
		new File(jarFolder).mkdir();
		new File(unzipFolder).mkdir();
		new File(uiFolder).mkdir();
		
		// copy the UI folder to the output folder;
		try {
		FileUtils.copyDirectory(new File( MSR_HOME + File.separator + "ui"), new File(uiFolder));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println( " Unable to copy UI folder ");
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
				parsedJars = dumpAPIInfo(rootPath, tempFolder);

			} else if (!pomFiles.isEmpty()) {
				// we have POM, we need to parse pom and download all jar files

				POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
				pomDownloader.download(pomFiles, jarFolder);

				parsedJars = dumpAPIInfo(jarFolder, tempFolder);

			} else {
				// we have gradle file, we need to parse gradle and download all jar files

				// create pom files out of gradle file
				GradleDependencyDownloader gradleDownloader = new GradleDependencyDownloader();
				pomFiles = gradleDownloader.createPOMFiles(gradleFiles, jarFolder);

				if (!pomFiles.isEmpty()) {
					// use the pom file logic to download the jars now
					POMDependencyDownloader pomDownloader = new POMDependencyDownloader();
					pomDownloader.download(pomFiles, jarFolder);
					parsedJars = dumpAPIInfo(jarFolder, tempFolder);
				}

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
				MSRdriver.runNaive(rootPath, type, outputPath);
				
				// generate stats information
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

				try {
					unzip(rootPath, unzipFolder + File.separator + dirName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (rootPath.endsWith(".ear")) {

				try {

					// unzip the mar ear file
					unzip(rootPath, unzipFolder);

					// check if the folder now contains wars and ejb-jar files

					Collection<File> warjarList = FileUtils.listFiles(new File(unzipFolder),
							new String[] { "war", "jar" }, true);
					if (!warjarList.isEmpty()) {
						Iterator<File> warjarListItr = warjarList.iterator();
						while (warjarListItr.hasNext()) {
							unzip(warjarListItr.next().getAbsolutePath(), unzipFolder);
						}

					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				parsedJars = dumpAPIInfo(unzipFolder, tempFolder);

			} else {
				System.out.println(
						"Usage: java MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
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
				MSRdriver.runNaive(unzipFolder, type, outputPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			System.out.println(
					"Usage: java MSRLauncher src|bin <path to the root folder> <path to the output folder> <algo to run>");
			return;
		}

	}

	private static boolean dumpAPIInfo(String folderPath, String outputPath) {

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

	public static void unzip(String zipFile, String destDirPath) throws IOException {
		String fileZip = zipFile;
		File destDir = new File(destDirPath);
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			File newFile = newFile(destDir, zipEntry);

			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {

				fos.write(buffer, 0, len);
			}
			fos.close();
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}

	public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		System.out.println(" Creating File " + destinationDir.getAbsolutePath() + File.separator + zipEntry.getName());
		File destFile = new File(destinationDir.getAbsolutePath() + File.separator + zipEntry.getName());
		// create directories for sub directories in zip
		new File(destFile.getParent()).mkdirs();

		String destDirPath = destinationDir.getCanonicalPath();
		String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
		}

		return destFile;
	}

}
