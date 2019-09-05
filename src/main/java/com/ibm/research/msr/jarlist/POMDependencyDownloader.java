package com.ibm.research.msr.jarlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


import com.ibm.research.msr.utils.Constants;

public class POMDependencyDownloader {

	class Dependency {
		

		String groupId;

		String artifactId;

		String version;

		public Dependency(String groupId, String artifactId, String version) {
			super();
			this.groupId = groupId;
			this.artifactId = artifactId;
			this.version = version;
		}

		/**
		 * @return the groupId
		 */
		public String getGroupId() {
			return groupId;
		}

		/**
		 * @return the artifactId
		 */
		public String getArtifactId() {
			return artifactId;
		}

		/**
		 * @return the version
		 */
		public String getVersion() {
			return version;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + "]";
		}
	}

	/**
	 *  Method used by MSRLauncher
	 *  
	 * @param pomFiles
	 * @param tempFolder
	 */
	public void download(ArrayList<String> pomFiles, String jarFolder) {
		Iterator<String> itr = pomFiles.iterator();
		while (itr.hasNext()) {
			download(itr.next(), jarFolder, false);
		}
	}

	public void download(String pomFileWithPath, String downloadPath, boolean bDownloadJavaDocAndSourcesJarAlso) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(pomFileWithPath));
			String l = null;
			StringBuffer sb = new StringBuffer();
			while ((l = br.readLine()) != null) {
				sb.append(l + " ");
			}

			List<Dependency> pomDependencies = new ArrayList<Dependency>();

			Document xmldoc = Jsoup.parse(sb.toString(), "", Parser.xmlParser());
			Elements es = xmldoc.getAllElements();
			for (int i = 0; i < es.size(); i++) {
				Element e = es.get(i);
				System.out.println(i + " " + e.tagName() + " " + e.text());
				if (e.tagName().compareTo("dependency") == 0) {
					Elements depElements = e.getAllElements();
					if (depElements.size() < 4) {
						System.err.println(
								"atleast one of group id, artifact id, version is missing " + depElements.size());
						continue;
					}
					
					//depElements.get(index)
					
					String groupId = null;
					String artifactId = null;
					String version = null;
//					String groupId = depElements.get(1).text();
//					String artifactId = depElements.get(2).text();
//					// version need not always be the 4th element see pom.xml in acme monolith
//					
//					String version = depElements.get(3).text();

					for (int j = 0; j < depElements.size(); j++) {
						Element de = depElements.get(j);
						System.out.println("\tDependency element " + j + "-" + de.tagName() + "-" + de.text());
						
						if (de.tagName().compareTo("groupId")==0)
						{
							groupId=de.text();
						}
						else if (de.tagName().compareTo("artifactId")==0)
						{
							artifactId=de.text();
						}
						else if (de.tagName().compareTo("version")==0)
						{
							version=de.text();
						}

					}
					
					if (version==null)
					{
						System.out.println("\t version NULL, extracting from maven url");
						version=extractLatestVersionNumberFromMaven(groupId, artifactId);
						System.out.println("\t version NULL, extracted from maven url="+version);
					}
					
					Dependency d = new Dependency(groupId, artifactId, version);
					pomDependencies.add(d);

				}
			}

			int j=0;
			for (Dependency d : pomDependencies) {
				j++;
				System.out.println(j+"\t"+d);
			}
			int k = 0;
			for (Dependency d : pomDependencies) {

				k++;
//				if (k > 3) {
//					break;
//				}

				String groupIdWithSlash = d.getGroupId().replace('.', '/');
				String jarFileName = d.getArtifactId() + "-" + d.getVersion() + ".jar";
				downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash, jarFileName);

				if (bDownloadJavaDocAndSourcesJarAlso) {
					String javaDocJarFileName = d.getArtifactId() + "-" + d.getVersion() + "-javadoc.jar";
					downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash, javaDocJarFileName);

					String sourceJarFileName = d.getArtifactId() + "-" + d.getVersion() + "-sources.jar";
					downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash, sourceJarFileName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void downloadFileFromURLAndCopyToLocalDisk(String downloadPath, Dependency d, String groupIdWithSlash,
			String jarFileName) throws MalformedURLException {

		// sample download URL
		// https://repo.maven.apache.org/maven2/org/eclipse/jdt/org.eclipse.jdt.core/3.18.0/
		String sURL = Constants.URL_BASE + groupIdWithSlash + "/" + d.getArtifactId() + "/" + d.getVersion() + "/"
				+ jarFileName;
		URL url = new URL(sURL);

		String fullPath = downloadPath + File.separator + jarFileName;

		File destination = new File(fullPath);

		System.out.println("downloading " + sURL + " -to-" + fullPath);

		try {
			FileUtils.copyURLToFile(url, destination, Constants.CONNECTION_TIME_OUT, Constants.READ_TIME_OUT);
			System.out.println("\t successfully downloaded and copied " + fullPath);
		} catch (IOException ioe) {
			System.err.println("HANDLED IOException while trying to download jar file and copy " + jarFileName);
			ioe.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if (args.length < 2) {
			System.err.println("USAGE:  <POM XML File With Full Path> <Location where jars should be downloaded>");
			return;
		}
		POMDependencyDownloader p = new POMDependencyDownloader();
//		String pomFileWithPath="C:\\Users\\GiriprasadSridhara\\msr\\microservice-recommender\\pom.xml";
//		String downloadPath="C:\\temp";
		String pomFileWithPath = args[0];
		String downloadPath = args[1];

		boolean bDownloadJavaDocAndSourcesJarAlso = false;
		if (args.length == 3) {
			if (args[2].equalsIgnoreCase("0")) {
				bDownloadJavaDocAndSourcesJarAlso = false;
				System.out.println("Will download only jar");
			} else {
				bDownloadJavaDocAndSourcesJarAlso = true;
				System.out.println("Will download only javadoc and sources jar also");

			}
		}

		p.download(pomFileWithPath, downloadPath, bDownloadJavaDocAndSourcesJarAlso);
		System.out.println("done");
	}

	public String extractLatestVersionNumberFromMaven(String groupId,String artifactId)
	{
		try
		{
	        String groupIdWithSlash=groupId.replace('.', '/');
			String sUrl="https://repo.maven.apache.org/maven2/"+groupIdWithSlash+"/"+artifactId+"/maven-metadata.xml";
	        Document d = Jsoup.connect(sUrl).get();
	//		System.out.println(d.html());
	
			Document xmldoc = Jsoup.parse(d.html(), "", Parser.xmlParser());
	
			Elements es=xmldoc.getAllElements();
	//		System.out.println("getAllElements");
			for (Element e : es) {
				
	//		    System.out.println("\t id =" + e.id());
	//		    System.out.println("\t tag name =" + e.tagName());
	//		    System.out.println("\t text =" + e.text());
	
				
			    String tagName=e.tagName();
			    String latestVersionNumber=null;
			    if (tagName.compareTo("latest")==0)
			    {
	//                System.out.println("\n\t groupid="+groupId+" \tartifact id="+artifactId);
			    	latestVersionNumber=e.text();
	//		    	System.out.println("\t latest version = " + latestVersionNumber);
			    	return latestVersionNumber;
			    }
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
