package com.ibm.research.msr.jarlist;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * extracts category, tags et al from a jar by going to maven repo website
 * (using its group id, artifact id which is
 * extracted from the jar)
 * @author GiriprasadSridhara
 *
 */
public class MavenCategoryEtAlExtractor {

	public void find(String jarRootPath)
	{
		try {
			
			//String libFolder="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib";
			//String root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
			//PrintWriter pw=new PrintWriter(root+File.separator+"jar-to-packages-classes.csv");
			String csvFile="src/main/resources/jar-to-maven-categories-etal.csv";
			//PrintWriter pw=new PrintWriter("src/main/output/jar-to-packages-classes.csv");
			PrintWriter pw=new PrintWriter(csvFile);
			pw.println("jarName,license,Categories,tags,used by");
			//File flf=new File(libFolder);
			
			//File[] files=flf.listFiles();
		
			
			File fRoot = new File(jarRootPath);
			String[] extensions = new String[] { "jar"};
			System.out.println("Getting all .jar  in " + fRoot.getPath()
					+ " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
			Set<String> processedJars=new HashSet<String>();
			for (File f : files) 
			{
				String fn=f.getName();
				if (!fn.endsWith("jar"))
				{
					continue;
				}
				if (processedJars.contains(fn))
				{
					continue;
				}
				processedJars.add(fn);
	        	//String jarName="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib\\guava-14.0.1.jar";
	        	String jarName=f.getAbsolutePath();
	        	
//	        	System.out.println(jarName);
	        	
				processOneJar(jarName);
					pw.flush();
			}
			pw.close();
//			System.out.println("processedJars ="+processedJars.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void processOneJar(String jarName)
	{
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarName);
            Enumeration<JarEntry> entries = jarFile.entries();
            String groupId=null;
            String artifactId=null;

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                System.out.println(entry.getName());
                //looking for META-INF/maven/com.google.guava/guava/
                String name=entry.getName();
                if (name.contains("META-INF/maven/"))
                {
                	String[] parts=name.split("/");
                	if (parts.length ==4)
                	{
                		groupId=parts[2];
                		artifactId=parts[3];
                	}
                    if (groupId==null || artifactId==null)
                    {
                    	continue;
                    }

                }
                else
                {
                	continue;
                }
                if (groupId==null || artifactId==null)
                {
                	System.err.println("group/artifact id null " + groupId + " , "+artifactId);
                	return;
                }
                
                // for url like:
                //https://mvnrepository.com/artifact/com.google.guava/guava
                String sUrl="https://mvnrepository.com/artifact/"+groupId+"/"+artifactId;
    			Document d = Jsoup.connect(sUrl).get();
    			System.out.println(d.html());
    			Elements tables=d.select(".grid");
    			if (tables!=null)
    			{
    				Element gridTable = tables.get(0);
    				if (gridTable != null)
    				{
    					Elements rows = gridTable.select("tr");

    				    for (int i = 0; i < rows.size(); i++) { 
    				        Element row = rows.get(i);
    				        Elements th = row.select("th");
    				        Elements td = row.select("td");

    				        System.out.println("th="+th.get(0).text());
    				        for (int j=0;j<td.size();j++)
    				        {
    				        	Element el = td.get(j);
    				        	System.out.println("td " + j + " " + el.text());
    				        }
    				        
    				        //System.out.println("td="+td.get(0).text());
    				        //if (cols.get(3).text().equals("Titan")) {
    				        	
    				        //}
    				        
    				    }	
    				}
    			}
    			return;
            }

            if (groupId==null || artifactId==null)
            {
            	System.err.println("group/artifact id null " + groupId + " , "+artifactId);
            	return;
            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (jarFile != null)
			{
				try {
					jarFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MavenCategoryEtAlExtractor m=new MavenCategoryEtAlExtractor();
		int choice=2;
		
		if (choice==1)
		{
			if (args.length < 1)
			{
				System.err.println("USAGE: <root folder under which jars are located (recursively searched)");
				System.exit(-1);
			}
			//root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
			String root=args[0];
			m.find(root);
		}
		else
		{
			//String jarName="C:\\Users\\GiriprasadSridhara\\Documents\\demo-july\\july-30\\guava-27.1-jre.jar";
			String jarName="C:\\Users\\GiriprasadSridhara\\.m2\\repository\\commons-cli\\commons-cli\\1.4\\commons-cli-1.4.jar";
			m.processOneJar(jarName);
		}
	}

}
