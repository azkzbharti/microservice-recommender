package com.ibm.research.msr.binaryextractor;

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

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class ReferencedClassesExtractor {

	public Map<String, HashSet<String>> extract(String jarNameWithFullPath) {
		Map<String, HashSet<String>> classToRefClasses = new HashMap<String, HashSet<String>>();

		try {
			JarFile jarFile = new JarFile(jarNameWithFullPath);

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
					ctc = cp.get(javaClass.getClassName());
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
			jarFile.close();
		} catch (ClassFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classToRefClasses;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReferencedClassesExtractor r = new ReferencedClassesExtractor();
		// test with this very project exported as jar!
		String jarNameWithFullPath = "/Users/senthil/ms-recomm.jar";
		Map<String, HashSet<String>> classToRefClasses = r.extract(jarNameWithFullPath);
		for (String c : classToRefClasses.keySet()) {
			System.out.println("Class = " + c);
			System.out.println("\tReferenced classes(Imports):");
			HashSet<String> rcs = classToRefClasses.get(c);
			for (String rc : rcs) {
				System.out.println("\t " + rc);
			}
		}
	}

}
