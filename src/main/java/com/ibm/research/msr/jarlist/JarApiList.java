package com.ibm.research.msr.jarlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.Path;

/*
 * Given the app path, it will extract the apiList for the .jar 
 * and save it in "src/main/resources/jar-to-packges.csv"
 */
public class JarApiList {
	
	String outputFileName=null;
	
	public void setOutputFileName(String o)
	{
		outputFileName=o;
	}

	public static void createJARFile(String appath) {
		// functionality here
		// write as "src/main/resources/jar-to-packges.csv"
		JarApiList j = new JarApiList();
		j.find(appath);

	}

	/**
	 * Function used by MSRLauncher
	 * 
	 * @param jarFiles
	 * @param tempFolder
	 */
	public void dumpAPIInfoForJars(ArrayList<String> jarFiles, String tempFolder) {

		Iterator<String> itr = jarFiles.iterator();
		Set<String> processedJars = new HashSet<String>();

		String csvFile = tempFolder + Path.SEPARATOR + "jar-to-packages.csv";
		PrintWriter pw = null;

		String jarName = null;
		JarFile jarFile = null;

		try {
			pw = new PrintWriter(csvFile);
			pw.println("jarName,packageName,className");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while (itr.hasNext()) {
			jarName = itr.next();
			if (processedJars.contains(jarName)) {
				continue;
			}
			try {
				jarFile = new JarFile(jarName);
				collectJavaPackagesAndClasses(jarName, jarFile, pw);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			processedJars.add(jarName);

		}
		
		if (pw != null) {
			pw.flush();
			pw.close();
		}

	}

	public void find(String root) {
		try {

			// String
			// libFolder="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib";
			// String root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
			// PrintWriter pw=new
			// PrintWriter(root+File.separator+"jar-to-packages-classes.csv");
			//String csvFile = "src/main/resources/jar-to-packages.csv";
			String csvFile = null;
			
			if (outputFileName==null)
			{
				csvFile="src/main/resources/jar-to-packages-classes-public-methods.csv";
			}
			else
			{
				csvFile=outputFileName;
			}
			// PrintWriter pw=new
			// PrintWriter("src/main/output/jar-to-packages-classes.csv");
			PrintWriter pw = new PrintWriter(csvFile);
			System.out.println("writing to op file="+csvFile);
			//pw.println("jarName,packageName,className");
			pw.println("jarName,packageName,className,publicMethodName");
			// File flf=new File(libFolder);

			// File[] files=flf.listFiles();

			File fRoot = new File(root);
			String[] extensions = new String[] { "jar" };
//			System.out.println("Getting all .jar  in " + fRoot.getPath()
//					+ " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
			Set<String> processedJars = new HashSet<String>();
			for (File f : files) {
				String fn = f.getName();
				if (!fn.endsWith("jar")) {
					continue;
				}
				if (processedJars.contains(fn)) {
					continue;
				}
				processedJars.add(fn);
				// String
				// jarName="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib\\guava-14.0.1.jar";
				String jarName = f.getAbsolutePath();

//	        	System.out.println(jarName);

				JarFile jarFile = new JarFile(jarName);

				// File file = new File(jarName);

				// Set<String> pkgs;
				// pkgs =
				collectJavaPackagesAndClasses(jarName, jarFile, pw);

				// System.out.println(f.getAbsolutePath());
				// System.out.println("jar name=" + fn);
				// System.out.println("Packages=");
//					for (String p:pkgs)
//					{
//						//System.out.println(fn+","+p);
//						pw.println(fn+","+p);
//					}
				pw.flush();
			}
			pw.close();
//			System.out.println("processedJars ="+processedJars.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void collectJavaPackagesAndClasses(String jarName, JarFile jarFile, PrintWriter pw)
			throws ClassFormatException, IOException {
		Set<String> packages = new HashSet<String>();
		//System.out.println("collectJavaPackagesAndClasses for jar="+jarName);
		
//		        Map<String, JavaClass> javaClasses =
//		            new LinkedHashMap<String, JavaClass>();
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			//System.out.println("\t jar entry name="+entry.getName());
			try {
				JarEntry entry = entries.nextElement();
				// System.out.println("jar entry name="+entry.getName());
				if (!entry.getName().endsWith(".class")) {
					continue;
				}
				// for module-info.class, the parse method throws an
				// Invalid byte tag in constant pool: 19 exception, so skipping it.
				if (entry.getName().compareTo("module-info.class") == 0) {
					continue;
				}
				ClassParser parser = new ClassParser(jarName, entry.getName());
				JavaClass javaClass = parser.parse();
				String cName = javaClass.getClassName();
				String pName = javaClass.getPackageName();
				// System.out.println(javaClass.getPackageName());
				// packages.add(javaClass.getPackageName());
				// System.out.println("\t super class name = " + javaClass.getSuperclassName());
				// javaClasses.put(javaClass.getClassName(), javaClass);
				Method[] javaClassMethods = javaClass.getMethods();
				for (Method m : javaClassMethods) {
					if (m.isPublic()) {
						pw.println(jarName + "," + pName + "," + cName + "," + m.getName());
					}
				} 
			} catch (Exception e) {
				// TODO: handle exception
				System.err.println("handled exception.");
				e.printStackTrace();
			}
			
			//pw.println(jarName + "," + pName + "," + cName);
		}
		// return packages;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		JarApiList p = new JarApiList();
		if (args.length < 1) {
			System.err.println("USAGE: <root folder under which jars are located (recursively searched) <optional output file name>");
			System.exit(-1);
		}
		// root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
		String root = args[0];
		
		if (args.length==2)
		{
			String opFileName=args[1];
			p.setOutputFileName(opFileName);
		}
		p.find(root);

	}

}
