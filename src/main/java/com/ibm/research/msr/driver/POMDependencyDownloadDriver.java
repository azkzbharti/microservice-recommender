package com.ibm.research.msr.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

	public void download(String pomFileWithPath,String downloadPath)
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
				
//				k++;
//				if (k>3)
//				{
//					break;
//				}
				
				String groupIdWithSlash=d.getGroupId().replace('.', '/');
				// sample download URL 
				// https://repo.maven.apache.org/maven2/org/eclipse/jdt/org.eclipse.jdt.core/3.18.0/
				String urlBase="https://repo.maven.apache.org/maven2/";
				
				
				String jarFileName=d.getArtifactId()+"-"+d.getVersion()+".jar";

				String sURL=urlBase+groupIdWithSlash+"/"+d.getArtifactId()+"/"+d.getVersion()+"/"+jarFileName;
				URL url=new URL(sURL);

				String fullPath=downloadPath+File.separator+jarFileName;
				
				File destination=new File(fullPath);
				
				System.out.println("downloading "+sURL + " -to-"+fullPath);
				
				FileUtils.copyURLToFile(url, destination,CONNECTION_TIME_OUT,READ_TIME_OUT);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
		
		p.download(pomFileWithPath, downloadPath);
		System.out.println("done");
	}

}
