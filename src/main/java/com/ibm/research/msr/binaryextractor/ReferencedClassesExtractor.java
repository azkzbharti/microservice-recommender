package com.ibm.research.msr.binaryextractor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;

public class ReferencedClassesExtractor {
	
	public HashSet<String> extractFromClass(String classPath) {
		
		//System.out.println("extract from class-classpath="+classPath);
		HashSet<String> refClasses = new HashSet<String>();
		ClassParser parser = new ClassParser(classPath);
		
		JavaClass javaClass = null;

		try {
			javaClass = parser.parse();
			//System.out.println("apache bcel parse class success="+javaClass.getClassName()+"-"+javaClass.getPackageName());
		} catch (ClassFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//System.out.println(javaClass.getClassName());

		ClassPool cp = ClassPool.getDefault();

		CtClass ctc = null;

		try {
			
//			System.out.println("calling class for name");
//			Class.forName(
//					  javaClass.getClassName(),
//					  true,
//					  ClassLoader.getSystemClassLoader());
//			System.out.println("after calling class for name");
//			cp.insertClassPath(new ClassClassPath(javaClass.getClass()));
//works	cp.insertClassPath("C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes");

			// GS NOTE: The input to this method ie classPath is something like
			// C:\temp\mobile-ear-1.0.23-output\temp\\unzip\WEB-INF\classes\com\ff\sys\v3\cellphone\webservices\bean\CellPhoneQualifiedBean.class
			// Now we want to extract the part from where the fully qualifie class name begins
			// ie C:\temp\mobile-ear-1.0.23-output\temp\\unzip\WEB-INF\classes\
			// hence the method below. If we don't insert this classpath
			// we will get a class not found exception
			String fullyQualifiedClassName=javaClass.getClassName();
			String cpToInsert=getClassPathToInsert(classPath, fullyQualifiedClassName);
			cp.insertClassPath(cpToInsert);
			ctc = cp.get(javaClass.getClassName());
		} catch (NotFoundException n) {
			n.printStackTrace();
		} catch (Exception cn) {
			cn.printStackTrace();
		}
		if (ctc != null) {
			
			try {
				Collection<String> rclasses = ctc.getRefClasses();
				for (String rc : rclasses) {
					//System.out.println("\t referenced class =" + rc);
					refClasses.add(rc);
				}

				//classToRefClasses.put(javaClass.getClassName(), refClasses);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("ctc null for " + javaClass.getClassName());
		}
		
		return refClasses;
		
		
	}

	public String getFullyQualifiedClassName(String classFileLocationOnDisk)
	{
		ClassParser parser = new ClassParser(classFileLocationOnDisk);
		
		JavaClass javaClass = null;
		String fqClassName=null;
		try {
			javaClass = parser.parse();
			//System.out.println("apache bcel parse class success="+javaClass.getClassName()+"-"+javaClass.getPackageName());
			fqClassName=javaClass.getClassName();
		} catch (ClassFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return fqClassName;
	}
	
	public Map<String, HashSet<String>> extractFromJAR(String jarNameWithFullPath) {
		Map<String, HashSet<String>> classToRefClasses = new HashMap<String, HashSet<String>>();

		JarFile jarFile = null;
		try {
			jarFile = new JarFile(jarNameWithFullPath);

			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				// System.out.println("jar entry name="+entry.getName());

				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				ClassParser parser = new ClassParser(jarNameWithFullPath, entry.getName());
				JavaClass javaClass = null;

				javaClass = parser.parse();
				//System.out.println(javaClass.getClassName());

				ClassPool cp = ClassPool.getDefault();

				CtClass ctc = null;

				try {
					cp.insertClassPath(jarNameWithFullPath);
					
					ctc = cp.get(javaClass.getClassName());
					
//					CtMethod[] ms = ctc.getDeclaredMethods();
//					for (CtMethod ctm : ms) {
//						try {
//							ctm.instrument(new ExprEditor() {
//								
//							});
//						} catch (CannotCompileException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
				} catch (NotFoundException n) {
					n.printStackTrace();
				}
				if (ctc != null) {
					HashSet<String> refClasses = new HashSet<String>();
					try {
						Collection<String> rclasses = ctc.getRefClasses();
						for (String rc : rclasses) {
							//System.out.println("\t referenced class =" + rc);
							refClasses.add(rc);
						}

						classToRefClasses.put(javaClass.getClassName(), refClasses);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("ctc null for " + javaClass.getClassName());
				}
			}
		} catch (ClassFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (jarFile != null)
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return classToRefClasses;
	}

	public static String getClassPathToInsert(String docAbsolutePath, String fullyQualifiedClassName)
	{
		StringBuffer sbDotReplacedWithSlash=new StringBuffer();
		for (int i=0;i<fullyQualifiedClassName.length();i++)
		{
			char curChar=fullyQualifiedClassName.charAt(i);
			if (curChar=='.')
			{
				sbDotReplacedWithSlash.append(File.separator);
			}
			else
			{
				sbDotReplacedWithSlash.append(fullyQualifiedClassName.charAt(i));
			}
		}
		
		//System.out.println("sDotReplacedWithSlash="+sbDotReplacedWithSlash.toString());
		int i1=docAbsolutePath.indexOf(sbDotReplacedWithSlash.toString());
		String classPathToInsert=docAbsolutePath.substring(0,i1);
		//System.out.println("cp="+classPathToInsert);
		return classPathToInsert;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int ch=2;
		
		if (ch==1)
		{
			ReferencedClassesExtractor r = new ReferencedClassesExtractor();
			// test with this very project exported as jar!
			String jarNameWithFullPath = "/Users/senthil/ms-recomm.jar";
			Map<String, HashSet<String>> classToRefClasses = r.extractFromJAR(jarNameWithFullPath);
			for (String c : classToRefClasses.keySet()) {
				//System.out.println("Class = " + c);
				//System.out.println("\tReferenced classes(Imports):");
				HashSet<String> rcs = classToRefClasses.get(c);
				for (String rc : rcs) {
					//System.out.println("\t " + rc);
				}
			}
		}
		else if (ch==2)
		{
			String s1="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes\\com\\ff\\sys\\v3\\cellphone\\webservices\\bean\\CellPhoneQualifiedBean.class";
			String s2="com.ff.sys.v3.cellphone.webservices.bean.CellPhoneQualifiedBean";
//			String repl=File.separator;
//			System.out.println(repl);
//			String sDotReplacedWithSlash=s2.replaceAll("\\Q.\\E",File.separator);//File.separator);
			
			StringBuffer sbDotReplacedWithSlash=new StringBuffer();
			for (int i=0;i<s2.length();i++)
			{
				if (s2.charAt(i)=='.')
				{
					sbDotReplacedWithSlash.append(File.separator);
				}
				else
				{
					sbDotReplacedWithSlash.append(s2.charAt(i));
				}
			}
			
			System.out.println("sDotReplacedWithSlash="+sbDotReplacedWithSlash.toString());
			int i1=s1.indexOf(sbDotReplacedWithSlash.toString());
			String classPathToInsert=s1.substring(0,i1);
			System.out.println("cp="+classPathToInsert);
		}
	}

}
