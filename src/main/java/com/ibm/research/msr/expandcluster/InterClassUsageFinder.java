package com.ibm.research.msr.expandcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
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
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class InterClassUsageFinder {

	Map<ClassPair, Integer> interClassUsageMatrix = new HashMap<ClassPair, Integer>();

	String currentJavaFilePkgName = null;
	
	Set<String> srcRootFoldersSet=null;

	public Map<ClassPair, Integer> find(String srcFilesRoot) {

		srcRootFoldersSet = extractSrcRootFolders(srcFilesRoot);
		
		File fRoot = new File(srcFilesRoot);
		String[] extensions = new String[] { "java" };
//		System.out.println("Getting all .java  in " + fRoot.getPath() + " including those in subdirectories");
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

				if (cp.getUsedClass().toLowerCase().equals(className.toLowerCase()))
					classPairList.add(cp);
			}
		}

		return classPairList;

	}

	public void processOneFile(File file, String srcFilesRoot) {

		String fileNameWPath = file.getAbsolutePath();
//		System.out.println("file: " + file.getName() + " " + fileNameWPath);

		// iff file is under /src/
		if (!fileNameWPath.contains(File.separator+"src"+File.separator))
		{
//			System.out.println("Ignoring java file not under src folder");
			return;
		}

		// NOTE: 
		// srcFilesRoot is something like C:\digdeep-master ie root under which 
		// all *java files must be processed.
		// If we give the above to ASTParser as source root, then it WILL NOT work properly
		// what ASTParser needs is the "src" path relative to the project such as
		// C:\digdeep-master\digdeep-web-common\src\
		// the variable srcRoot holds the above.
		
		int srcIndex=fileNameWPath.indexOf(File.separator+"src"+File.separator);
		String srcRoot=null;
		if (srcIndex != -1)
		{
			// extract till .../src/ to use as src root
			srcRoot=fileNameWPath.substring(0,srcIndex+5);	
		}
		
		//System.out.println("\t src root="+srcRoot);
		
		
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
		//String[] srcPath = new String[1];
		//String[] srcPath = new String[2];
		//String[] srcPath = new String[3];
		String[] srcRootFoldersSetArr = srcRootFoldersSet.toArray(new String[1]);
		String[] srcPath=new String[srcRootFoldersSetArr.length+1];
		// srcPath[0]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src";
		//srcPath[0] = srcFilesRoot;
		if (srcRoot != null)
		{
			srcPath[0]=srcRoot;
//			srcPath[1]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-common\\src\\";
//			srcPath[2]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-git-common\\src\\";
//			//srcPath[1]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\";
			for (int i=0;i<srcRootFoldersSetArr.length;i++)
			{
				srcPath[i+1]=srcRootFoldersSetArr[i];
			}
		}
		else
		{
			srcPath[0] = srcFilesRoot;
		}
		parser.setEnvironment(null, srcPath, null, true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		final Stack<TypeDeclaration> stackTD = new Stack<TypeDeclaration>();

		final Map<ClassPair, Integer> localFileInterClassUsageMatrix = new HashMap<ClassPair, Integer>();
		
		//System.out.println("create ast done");
		cu.accept(new ASTVisitor() {

			public boolean visit(PackageDeclaration p) {
				//System.out.println("\t visit PackageDeclaration " + p.getName().getFullyQualifiedName());
				IPackageBinding ipb = p.resolveBinding();
				if (ipb != null) {
					currentJavaFilePkgName = ipb.getName();
				}
				return true;
			}

			public boolean visit(TypeDeclaration td) {
				//System.out.println("\t visit TypeDeclaration " + td.getName().getFullyQualifiedName());
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
				//System.out.println("\tvisit md " + m.getName().getFullyQualifiedName());
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
					//System.out.println("not within a type");
					return true;
				}

				//System.out.println("\t\t visit SimpleName " +e.toString());
				ITypeBinding itb = e.resolveTypeBinding();
				if (itb != null) {
					if (itb.isFromSource()) {
						String plainTypeName = itb.getName();
						IPackageBinding ipb = itb.getPackage();
						String usedClassName = null;
						if (ipb != null) {
							usedClassName = ipb.getName() + "." + plainTypeName;
						} else {
							usedClassName = plainTypeName;
						}
						//System.out.println("\t\tSimpleName: Usage of " + n);

						String curClassName = null;
						if (!stackTD.isEmpty()) {
							TypeDeclaration curClass = stackTD.peek();
							curClassName = curClass.getName().getFullyQualifiedName();
							//System.out.println(curClassName);
						} else {
							curClassName = file.getName();
						}
						
						// TODO: length of 1 is to avoid type parameter names as K V E T 
						// appearing in the usage (the other way to do this is expensive
						// i.e., we have to do 2 pass over all the src files, with the 
						// 1st pass gathering all declared class names in this project and
						// then checking in 2nd pass if the simple name is not actually a declared
						// class name found in the 1st pass but a typical type parameter name 
						// such as K V E T.
						
						if (plainTypeName.length()==1)
						{
//							System.out.println("ignoring plainTypeName as length is 1. type name=" + plainTypeName);
							return true;
						}

						String thisClassFQName = null;
						if (currentJavaFilePkgName != null) {
							thisClassFQName = currentJavaFilePkgName + "." + curClassName;
						} else {

							thisClassFQName = curClassName;
						}

						if (thisClassFQName.isEmpty())
						{
							System.err.println("thisClassFQName is empty!");
							return true;
						}
						
						if (usedClassName.isEmpty())
						{
							System.err.println("usedClassName is empty!");
							return true;
						}
						
						if (thisClassFQName.compareTo(usedClassName)==0)
						{
							// same class usage (ie in type name declatation ie declatation like Class A {
							return true;
						}
						
						
						ClassPair cp = new ClassPair(thisClassFQName, usedClassName);
//						ClassPair cp=new ClassPair(curClassName,usedClassName);

						Integer usageCount = interClassUsageMatrix.get(cp);
						if (usageCount == null) {
							usageCount = new Integer(1);
							interClassUsageMatrix.put(cp, usageCount);
							localFileInterClassUsageMatrix.put(cp, usageCount);
						} else {
							Integer uc2 = new Integer(usageCount.intValue() + 1);
							interClassUsageMatrix.put(cp, uc2);
							localFileInterClassUsageMatrix.put(cp, uc2);
						}

					}

				}
				else
				{
					//System.out.println("\t type binding null");
				}

				return true;
			}

			public boolean visit(TypeParameter e)
			{
				//System.out.println("visit type parameter " +e.getName().getFullyQualifiedName());
				return true;
			}
		});

//		System.out.println("\tLocalFileInterClassUsageMatrix");
		//for (ClassPair cp : interClassUsageMatrix.keySet()) {
		for (ClassPair cp : localFileInterClassUsageMatrix.keySet()) {
			//Integer usageCount = interClassUsageMatrix.get(cp);
			Integer usageCount = localFileInterClassUsageMatrix.get(cp);
//			System.out.println("\t"+cp + "->" + usageCount);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int choice = 1;
		//int choice = 2;
		InterClassUsageFinder i = new InterClassUsageFinder();
		if (choice == 1) {
			if (args.length < 1) {
				System.err.println("USAGE: <root of source files for project>");
				System.exit(-1);
			}

			String srcFilesRoot = args[0];
			// InterClassUsageFinder i=new InterClassUsageFinder();
			Map<ClassPair, Integer> m=i.find(srcFilesRoot);

//			System.out.println("\nFinalInterClassUsageMatrix:");
			for (ClassPair cp: m.keySet())
			{
				Integer c=m.get(cp);
				System.out.println(cp.getThisClass()+"-"+cp.getUsedClass()+"="+c);
			}
		
		} else if (choice == 2) {
			//String ipf = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\com\\ibm\\research\\digdeep\\preventive\\clusterer\\CarrotClusteringEngineImpl.java";
			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\com\\ibm\\research\\util\\MapUtil.java";
			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\com\\ibm\\research\\digdeep\\web\\services\\DynamicRecommendationService.java";
			String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-git-api-web\\src\\com\\ibm\\research\\digdeep\\git\\GitApiResource.java";
			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\extra\\recommendations\\"
			// ipf="C:\\Users\\GiriprasadSridhara\\dependency-migration-asistant\\src\\main\\java\\com\\ibm\\research\\appmod\\dma\\impact\\ChangedAPIUsageSiteFinder.java";
			File file = new File(ipf);
			//String srcFilesRoot = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\";
			//String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\";
			//String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\";
			String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-git-api-web\\src\\";
			i.processOneFile(file, srcFilesRoot);
		}
		else if (choice==3)
		{
			//Make a filter that matches files and directories
		    final IOFileFilter srcFolderFilter = new IOFileFilter() {  
		        @Override
		        public boolean accept(File dir, String name) {
		        	System.out.println("accept dir " + name);
		            if (name.compareTo("src")==0)
		            {
		            	return true;
		            }
		            return false;
		        }

		        @Override
		        public boolean accept(File file) {
		            return false;

		        }
		    };

		    extractSrcRootFolders("C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\");

		}
	}

	public static Set<String> extractSrcRootFolders(String sRoot) {
		//File root=new File("C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\");
		File root=new File(sRoot);
//			//List files and folders in that directory
//		    Collection<File> srcDirs = FileUtils.listFilesAndDirs(root, null, srcFolderFilter);
//		    for(File s:srcDirs)
//		    {
//		    	System.out.println(s.getAbsolutePath());
//		    }
		String[] extensions = new String[] { "java" };
		//File fRoot=new File(root);
		//			System.out.println("Getting all .java  in " + fRoot.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(root, extensions, true);
		Set<String> srcRootSet=new HashSet<String>();
		for (File file : files) {
			
			String fileNameWPath=file.getAbsolutePath();
			int srcIndex=fileNameWPath.indexOf(File.separator+"src"+File.separator);
			String srcRoot=null;
			if (srcIndex != -1)
			{
				// extract till .../src/ to use as src root
				srcRoot=fileNameWPath.substring(0,srcIndex+5);
				srcRootSet.add(srcRoot);
			}

		}
		
//		System.out.println("srcRootSet size="+srcRootSet.size());
//		for (String s: srcRootSet)
//		{
//			System.out.println(s);
//		}
		return srcRootSet;
	}

}
