package com.ibm.research.msr.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class POMDependencyDownloadDriver {

	final int CONNECTION_TIME_OUT=60*1000;
	
	final int READ_TIME_OUT=60*1000;
	
	final String URL_BASE="https://repo.maven.apache.org/maven2/";
	
	class Dependency
	{
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
		
		
	}

	public void download(String pomFileWithPath,String downloadPath, 
			boolean bDownloadJavaDocAndSourcesJarAlso)
	{
		try
		{
			BufferedReader br=new BufferedReader(new FileReader(pomFileWithPath));
			String l=null;
			StringBuffer sb=new StringBuffer();
			while ((l=br.readLine())!=null)
			{
				sb.append(l+" ");
			}

			List<Dependency> pomDependencies=new ArrayList<Dependency>();
			
			Document xmldoc = Jsoup.parse(sb.toString(), "", Parser.xmlParser());
			Elements es = xmldoc.getAllElements();
			for (int i=0;i<es.size();i++)
			{
				Element e = es.get(i);
				System.out.println(i+" "+e.tagName() + " " + e.text());
				if (e.tagName().compareTo("dependency")==0)
				{
					Elements depElements = e.getAllElements();
					if (depElements.size()<4)
					{
						System.err.println("atleast one of group id, artifact id, version is missing " + depElements.size());
						continue;
					}
					String groupId=depElements.get(1).text();
					String artifactId=depElements.get(2).text();
					String version=depElements.get(3).text();

					Dependency d=new Dependency(groupId, artifactId, version);
					pomDependencies.add(d);
					for (int j=0;j<depElements.size();j++)
					{
						Element de = depElements.get(j);
						System.out.println("\tDependency element "+j+ "-" +de.tagName()+ "-"+de.text());
						
					}
				}
			}
			
			int k=0;
			for (Dependency d: pomDependencies)
			{
				
				k++;
				if (k>3)
				{
					break;
				}
				
				String groupIdWithSlash=d.getGroupId().replace('.', '/');
				String jarFileName=d.getArtifactId()+"-"+d.getVersion()+".jar";
				downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash,jarFileName);
				
				if (bDownloadJavaDocAndSourcesJarAlso)
				{
					String javaDocJarFileName=d.getArtifactId()+"-"+d.getVersion()+"-javadoc.jar";
					downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash,javaDocJarFileName);

					String sourceJarFileName=d.getArtifactId()+"-"+d.getVersion()+"-sources.jar";
					downloadFileFromURLAndCopyToLocalDisk(downloadPath, d, groupIdWithSlash,sourceJarFileName);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

	private void downloadFileFromURLAndCopyToLocalDisk(String downloadPath, 
			Dependency d, String groupIdWithSlash,String jarFileName)
			throws MalformedURLException {

		// sample download URL 
		// https://repo.maven.apache.org/maven2/org/eclipse/jdt/org.eclipse.jdt.core/3.18.0/
		String sURL=URL_BASE+groupIdWithSlash+"/"+d.getArtifactId()+"/"+d.getVersion()+"/"+jarFileName;
		URL url=new URL(sURL);

		String fullPath=downloadPath+File.separator+jarFileName;
		
		File destination=new File(fullPath);
		
		System.out.println("downloading "+sURL + " -to-"+fullPath);

		try
		{
			FileUtils.copyURLToFile(url, destination,CONNECTION_TIME_OUT,READ_TIME_OUT);
			System.out.println("\t successfully downloaded and copied " + fullPath);
		}
		catch(IOException ioe)
		{
			System.err.println("HANDLED IOException while trying to download jar file and copy " + jarFileName);
			ioe.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if (args.length < 2)
		{
			System.err.println("USAGE:  <POM XML File With Full Path> <Location where jars should be downloaded>");
			return;
		}
		POMDependencyDownloadDriver p=new POMDependencyDownloadDriver();
//		String pomFileWithPath="C:\\Users\\GiriprasadSridhara\\msr\\microservice-recommender\\pom.xml";
//		String downloadPath="C:\\temp";
		String pomFileWithPath=args[0];
		String downloadPath=args[1];

		boolean bDownloadJavaDocAndSourcesJarAlso=false;
		if (args.length == 3)
		{
			if (args[2].equalsIgnoreCase("0"))
			{
				bDownloadJavaDocAndSourcesJarAlso=false;
				System.out.println("Will download only jar");
			}
			else
			{
				bDownloadJavaDocAndSourcesJarAlso=true;
				System.out.println("Will download only javadoc and sources jar also");

			}
		}
		
		p.download(pomFileWithPath, downloadPath,bDownloadJavaDocAndSourcesJarAlso);
		System.out.println("done");
	}

}
