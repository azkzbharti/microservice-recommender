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

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FileUtils;

/*
 * Given the app path, it will extract the apiList for the .jar 
 * and save it in "src/main/resources/jar-to-packges.csv"
 */
public class JarApiList {
	
	public static void createJARFile(String appath) {
		// functionality here
		//		 write as "src/main/resources/jar-to-packges.csv"
		JarApiList j=new JarApiList();
		j.find(appath);
		
	}

	public void find(String root)
	{
		try {
			
			//String libFolder="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib";
			//String root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
			//PrintWriter pw=new PrintWriter(root+File.separator+"jar-to-packages-classes.csv");
			String csvFile="src/main/resources/jar-to-packges.csv";
			//PrintWriter pw=new PrintWriter("src/main/output/jar-to-packages-classes.csv");
			PrintWriter pw=new PrintWriter(csvFile);
			pw.println("jarName,packageName,className");
			//File flf=new File(libFolder);
			
			//File[] files=flf.listFiles();
		
			
			File fRoot = new File(root);
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
	        	
	        	System.out.println(jarName);
	        	
				JarFile jarFile = new JarFile(jarName);
	        	
	        	//File file  = new File(jarName);
	
				
				//Set<String> pkgs;
				//	pkgs = 
				collectJavaPackagesAndClasses(jarName, jarFile,pw);
	
					//System.out.println(f.getAbsolutePath());
					//System.out.println("jar name=" + fn);
					//System.out.println("Packages=");
//					for (String p:pkgs)
//					{
//						//System.out.println(fn+","+p);
//						pw.println(fn+","+p);
//					}
					pw.flush();
			}
			pw.close();
			System.out.println("processedJars ="+processedJars.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 public void collectJavaPackagesAndClasses(
		        String jarName, JarFile jarFile, PrintWriter pw) 
		            throws ClassFormatException, IOException
		    {
				Set<String> packages=new HashSet<String>();
				
//		        Map<String, JavaClass> javaClasses =
//		            new LinkedHashMap<String, JavaClass>();
		        Enumeration<JarEntry> entries = jarFile.entries();
		        while (entries.hasMoreElements())
		        {
		            JarEntry entry = entries.nextElement();
		            //System.out.println("jar entry name="+entry.getName());
		            
		            if (!entry.getName().endsWith(".class"))
		            {
		                continue;
		            }

		            ClassParser parser = 
		                new ClassParser(jarName, entry.getName());
		            JavaClass javaClass = parser.parse();
		            String cName=javaClass.getClassName();
		            String pName=javaClass.getPackageName();
		            //System.out.println(javaClass.getPackageName());
		            //packages.add(javaClass.getPackageName());
		            //System.out.println("\t super class name = " + javaClass.getSuperclassName());
		            //javaClasses.put(javaClass.getClassName(), javaClass);
		            pw.println(jarName +","+pName+","+cName);
		        }
		        //return packages;
		    }



	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JarApiList p=new JarApiList();
		if (args.length < 1)
		{
			System.err.println("USAGE: <root folder under which jars are located (recursively searched)");
			System.exit(-1);
		}
		//root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
		String root=args[0];
		p.find(root);

	}


}

