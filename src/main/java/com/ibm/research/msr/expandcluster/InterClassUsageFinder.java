package com.ibm.research.msr.expandcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class InterClassUsageFinder {

	Map<ClassPair, Integer> interClassUsageMatrix = new HashMap<ClassPair, Integer>();

	String currentJavaFilePkgName = null;

	public Map<ClassPair, Integer> find(String srcFilesRoot) {

		File fRoot = new File(srcFilesRoot);
		String[] extensions = new String[] { "java" };
		System.out.println("Getting all .java  in " + fRoot.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
		for (File file : files) {
			processOneFile(file, srcFilesRoot);
		}
		return interClassUsageMatrix;
	}

	/**
	 * Get the list of class-pairs for a given class. 
	 * @param className
	 * @return
	 */
	public List<ClassPair> getAssociatedClassPairForClass(String className) {
		List<ClassPair> classPairList = new ArrayList<ClassPair>();

		ClassPair cp = null;

		if (!interClassUsageMatrix.isEmpty()) {
			Iterator<ClassPair> itr = interClassUsageMatrix.keySet().iterator();
			while (itr.hasNext()) {
				cp = itr.next();

				if (cp.getThisClass().toLowerCase().equals(className.toLowerCase()))
					classPairList.add(cp);
			}
		}

		return classPairList;

	}

	public void processOneFile(File file, String srcFilesRoot) {

		String fileNameWPath = file.getAbsolutePath();
		System.out.println("file: " + file.getName() + " " + fileNameWPath);

		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(fileNameWPath));
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ASTParser parser = ASTParser.newParser(AST.JLS12);

		parser.setSource(sb.toString().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName(file.getName());
		// parser.setEnvironment(classPathEntries, null, null, true);
		String[] srcPath = new String[1];
		// srcPath[0]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src";
		srcPath[0] = srcFilesRoot;

		parser.setEnvironment(null, srcPath, null, true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		final Stack<TypeDeclaration> stackTD = new Stack<TypeDeclaration>();

		System.out.println("create ast done");
		cu.accept(new ASTVisitor() {

			public boolean visit(PackageDeclaration p) {
				IPackageBinding ipb = p.resolveBinding();
				if (ipb != null) {
					currentJavaFilePkgName = ipb.getName();
				}
				return true;
			}

			public boolean visit(TypeDeclaration td) {
				ITypeBinding itb = td.resolveBinding();
				if (itb != null) {
					// System.out.println("itb qualified name= "
					// + itb.getQualifiedName());
//					IJavaElement ije=itb.getJavaElement();
//					ije.
				} else {
					// System.out.println("itb null");
				}
				// System.out.println("enter visit td " + td.getName().getFullyQualifiedName());
				stackTD.push(td);
				return true;
			}

			public void endVisit(TypeDeclaration td) {
				stackTD.pop();
			}

			public boolean visit(MethodDeclaration m) {
				System.out.println("visit md " + m.getName().getFullyQualifiedName());
				return true;
			}
//			public boolean visit(SingleVariableDeclaration svd)
//			{
//				System.out.println("visit svd "+svd.toString());
//
//				
//				IVariableBinding ivb = svd.resolveBinding();
//				if (ivb!=null)
//				{
//					ITypeBinding itb = ivb.getType();
//					if (itb!=null)
//					{
//						if (itb.isFromSource())
//						{
//							String n = itb.getName();
//							System.out.println("\t\tSVD: Usage of " + n);
//						}
//					}
//				}
//				return true;
//			}

			public boolean visit(VariableDeclarationFragment vdf) {
//				System.out.println("visit vdf "+vdf.toString());

				IVariableBinding ivb = vdf.resolveBinding();
				if (ivb != null) {
					ITypeBinding itb = ivb.getType();
					if (itb != null) {
						if (itb.isFromSource()) {
							String n = itb.getName();
							// System.out.println("\t\tVDF: Usage of " + n);
						}
					}
				}
				return true;
			}

			public boolean visit(VariableDeclaration vd) {
//				System.out.println("visit vd "+vd.toString());
				IVariableBinding ivb = vd.resolveBinding();
				if (ivb != null) {
					ITypeBinding itb = ivb.getType();
					if (itb != null) {
						// if (itb.isFromSource())
						// {
						String n = itb.getName();
						// System.out.println("\t\tVariableDeclaration: Usage of " + n);
						// }
					}
				}

				return true;
			}

			public boolean visit(MethodInvocation mi) {
//				System.out.println("visit MI " +mi.toString());
				Expression e = mi.getExpression();
				if (e == null) {
					return true;
				}
				ITypeBinding itb = e.resolveTypeBinding();
				if (itb != null) {
					if (itb.isFromSource()) {
						String n = itb.getName();
//						System.out.println("\t\tMethodInvocation: Usage of " + n);
					}

				}

				return true;
			}

			public boolean visit(SimpleName e) {
				// to ignore package imports and all
				if (stackTD.isEmpty()) {
					System.out.println("not within a type");
					return true;
				}

				// System.out.println("visit SimpleName " +e.toString());
				ITypeBinding itb = e.resolveTypeBinding();
				if (itb != null) {
					if (itb.isFromSource()) {
						String n = itb.getName();
						IPackageBinding ipb = itb.getPackage();
						String usedClassName = null;
						if (ipb != null) {
							usedClassName = ipb.getName() + "." + n;
						} else {
							usedClassName = n;
						}
						System.out.println("\t\tSimpleName: Usage of " + n);

						String curClassName = null;
						if (!stackTD.isEmpty()) {
							TypeDeclaration curClass = stackTD.peek();
							curClassName = curClass.getName().getFullyQualifiedName();
							System.out.println(curClassName);
						} else {
							curClassName = file.getName();
						}

						String thisClassFQName = null;
						if (currentJavaFilePkgName != null) {
							thisClassFQName = currentJavaFilePkgName + "." + curClassName;
						} else {

							thisClassFQName = curClassName;
						}

						ClassPair cp = new ClassPair(thisClassFQName, usedClassName);
//						ClassPair cp=new ClassPair(curClassName,usedClassName);

						Integer usageCount = interClassUsageMatrix.get(cp);
						if (usageCount == null) {
							usageCount = new Integer(1);
							interClassUsageMatrix.put(cp, usageCount);
						} else {
							Integer uc2 = new Integer(usageCount.intValue() + 1);
							interClassUsageMatrix.put(cp, uc2);
						}

					}

				}

				return true;
			}

		});

		System.out.println("interClassUsageMatrix");
		for (ClassPair cp : interClassUsageMatrix.keySet()) {
			Integer usageCount = interClassUsageMatrix.get(cp);
			System.out.println(cp + "->" + usageCount);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int choice = 2;
		InterClassUsageFinder i = new InterClassUsageFinder();
		if (choice == 1) {
			if (args.length < 1) {
				System.err.println("USAGE: <root of source files for project>");
				System.exit(-1);
			}

			String srcFilesRoot = args[0];
			// InterClassUsageFinder i=new InterClassUsageFinder();
			i.find(srcFilesRoot);
		} else if (choice == 2) {
			String ipf = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\com\\ibm\\research\\digdeep\\preventive\\clusterer\\CarrotClusteringEngineImpl.java";
			;
			// String
			// ipf="C:\\Users\\GiriprasadSridhara\\dependency-migration-asistant\\src\\main\\java\\com\\ibm\\research\\appmod\\dma\\impact\\ChangedAPIUsageSiteFinder.java";
			File file = new File(ipf);
			String srcFilesRoot = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\";
			i.processOneFile(file, srcFilesRoot);
		}
	}

}
