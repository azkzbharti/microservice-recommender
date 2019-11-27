package com.ibm.research.msr.expandcluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.ibm.research.msr.binaryextractor.ReferencedClassesExtractor;

public class InterClassUsageFinder {
	
	class InterClassUsage
	{
		public String name;
		
		public Map<String,Integer> usedClassesToCount=new HashMap<String,Integer>();
		
		public Map<String,Integer> usedByClassesToCount=new HashMap<String,Integer>();

		public String type=null;

		public InterClassUsage(String thisClass) {
			// TODO Auto-generated constructor stub
			name=thisClass;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InterClassUsage other = (InterClassUsage) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private InterClassUsageFinder getOuterType() {
			return InterClassUsageFinder.this;
		}
	}

	// this is the inter class usage matrix, while the interClassUsageMap
	// field below is for source/sink/both type of identification
	Map<ClassPair, Integer> interClassUsageMatrix = new HashMap<ClassPair, Integer>();

	String currentJavaFilePkgName = null;
	
	Set<String> srcRootFoldersSet=null;

	// for use in the .class mode of inter class usage
	// 
	Set<String> fullyQualifiedClassNamesInProject=new HashSet<String>();
	
	// for source/sink/both type of identification as distinct from
	// the interClassUsageMatrix field above
	Map<String,InterClassUsage> interClassUsageMap=
			new HashMap<String, InterClassUsage>();

	
	public  Map<ClassPair,Integer> loader(String jsonPath){
		FileReader fileReader = null;
		Map<ClassPair, Integer> m = new HashMap<ClassPair, Integer>();
		try {
			fileReader = new FileReader(jsonPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONParser jsonParser = new JSONParser();
		try {
			Object obj = jsonParser.parse(fileReader);
			
			JSONObject jsonobj = (JSONObject) obj;
			Set<String> keys = jsonobj.keySet();
			for(String k:keys) {
				
				JSONObject entry = (JSONObject) jsonobj.get(k);
				String name=(String) entry.get("name");
				JSONObject vals = (JSONObject) entry.get("usedClassesToCount");
				if(vals!=null) {
				Set<String> vkeys = vals.keySet();
				for(String v:vkeys) {
					Long q=(Long) vals.get(v);
					m.put(new ClassPair(name, v),q.intValue());
				}
				}
			}		
			interClassUsageMatrix=m;
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return m;
		
	
	}
	public Map<ClassPair, Integer> find(String srcFilesRoot,String outPath) {

		System.out.println("enter interclassusagefinder find "+srcFilesRoot+"-"+outPath);
		srcRootFoldersSet = extractSrcRootFolders(srcFilesRoot);
		
		File fRoot = new File(srcFilesRoot);
		String[] extensions = new String[] { "java" };
//		System.out.println("Getting all .java  in " + fRoot.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
//		System.out.println("files size="+files.size());
		for (File file : files) {
			processOneFile(file, srcFilesRoot);
		}
		
		findTypeAndPrintJson(srcFilesRoot,outPath);
		
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
//		System.out.println("-----SRC BEGIN-----"+file.getName());
//		System.out.println(sb);
//		System.out.println("-----SRC END-----"+file.getName());
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
//		System.out.println("srcPath array="+srcPath.length);
		for (String s: srcPath)
		{
//			System.out.println("\t src path arr elem="+s);
		}
//		String[] srcPath2=new String[]{"C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere\\src\\main"};
		parser.setEnvironment(null, srcPath, null, true);
//		String[] classPathEntries=new String[]{"C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere-manual-dependencies\\lib"};
		//classPathEntries[0]=args[2];

		Hashtable<String, String> options = JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_12);

		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_12);
		parser.setCompilerOptions(options);
		
//		//parser.setEnvironment(classPathEntries, srcPath, null, true);
//		String[] srcPath3 = new String[] {
//				"C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere-manual-dependencies\\src\\",
//				"C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere-manual-dependencies\\src\\main"
////				"C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere-manual-dependencies\\src\\main\\java"
//////				"C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\"
//			};
//		parser.setEnvironment(classPathEntries, srcPath3, null, true);
//		parser.setEnvironment(null, srcPath3, null, true);
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

//		IProblem[] ps = cu.getProblems();
//		System.out.println("\t problems="+ps.length);
//		for (IProblem p:ps)
//		{
//			System.out.println("\t problem="+p.getMessage());
//		}
		
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
							// TODO: examine why this happens
							//System.err.println("thisClassFQName is empty!");
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
						populateInterClassUsageMatrix(localFileInterClassUsageMatrix, usedClassName, thisClassFQName);

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
		populateInterClassUsageMap(localFileInterClassUsageMatrix);
	}
	

	/**
	 * NOTE: localFileInterClassUsageMatrix is an OUT parameter
	 * it could have been a return value but eclipse extract-method refactoring created
	 * it as a parameter and I didn't want to change that
	 *
	 * the other 2 params are proper in params.
	 *  
	 * @param localFileInterClassUsageMatrix
	 * @param usedClassName
	 * @param thisClassFQName
	 */
	private void populateInterClassUsageMatrix(final Map<ClassPair, Integer> localFileInterClassUsageMatrix,
			String usedClassName, String thisClassFQName) {
		ClassPair cp = new ClassPair(thisClassFQName, usedClassName);
//				ClassPair cp=new ClassPair(curClassName,usedClassName);

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

	private void populateInterClassUsageMap(final Map<ClassPair, Integer> localFileInterClassUsageMatrix) {
		for (ClassPair cp : localFileInterClassUsageMatrix.keySet()) {
			//Integer usageCount = interClassUsageMatrix.get(cp);
			Integer usageCount = localFileInterClassUsageMatrix.get(cp);
//			System.out.println("\t"+cp + "->" + usageCount);
			
			String userClass=cp.getThisClass();
			InterClassUsage interClassUsage1 = interClassUsageMap.get(userClass);
			if (interClassUsage1==null)
			{
				interClassUsage1=new InterClassUsage(userClass);
			}
			
			String usedClass=cp.getUsedClass();
			interClassUsage1.usedClassesToCount.put(usedClass,usageCount);
			interClassUsageMap.put(userClass, interClassUsage1);

			// TODO: possible duplication of above, refactor into method
			InterClassUsage interClassUsage2 = interClassUsageMap.get(usedClass);
			if (interClassUsage2==null)
			{
				interClassUsage2=new InterClassUsage(usedClass);
			}
			interClassUsage2.usedByClassesToCount.put(userClass,usageCount);
			interClassUsageMap.put(usedClass, interClassUsage2);
		}
	}

	
	public Map<ClassPair, Integer> findFromBinaryClassFiles(String classFilesRoot,String outPath) {

		System.out.println("enter interclassusagefinder findFromBinaryClassFiles find "+classFilesRoot+"-"+outPath);
		
		File fRoot = new File(classFilesRoot);
		String[] extensions = new String[] { "class" };
//		System.out.println("Getting all .java  in " + fRoot.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
		System.out.println("files size="+files.size());

		ReferencedClassesExtractor r=new ReferencedClassesExtractor(); 
		for (File file : files) {
			String classFileLocationOnDisk=file.getAbsolutePath();
			String thisClassFQName=r.getFullyQualifiedClassName(classFileLocationOnDisk);
			fullyQualifiedClassNamesInProject.add(thisClassFQName);
		}

		
		for (File file : files) {
			System.out.println("processing file="+file.getAbsolutePath());
			processOneFileForBinaryClassFiles(file, classFilesRoot);
		}
		
		findTypeAndPrintJson(classFilesRoot,outPath);
		
		return interClassUsageMatrix;
	}

	
	private void processOneFileForBinaryClassFiles(File file, String classFilesRoot) {
		// TODO Auto-generated method stub
		ReferencedClassesExtractor r=new ReferencedClassesExtractor();

		String classFileLocationOnDisk=file.getAbsolutePath();

		HashSet<String> referencedClasses = r.extractFromClass(classFileLocationOnDisk);

		String thisClassFQName=r.getFullyQualifiedClassName(classFileLocationOnDisk);

		Map<ClassPair, Integer> localFileInterClassUsageMatrix=new HashMap<ClassPair,Integer>();
		for (String usedClassName:referencedClasses)
		{
			// referencedClasses returned by Javassist library containes both
			// source and lib classes, we want only source classes
			if (!fullyQualifiedClassNamesInProject.contains(usedClassName))
			{
				continue;
			}
			
			//javassist returns this class name say InterClassUsageFinder as a used
			// class when the input class is InterClassUsageFinder!. This will upset our source/sink calculation
			if (usedClassName.compareTo(thisClassFQName)==0)
			{
				continue;
			}
			populateInterClassUsageMatrix(localFileInterClassUsageMatrix, usedClassName, thisClassFQName);
		}

		populateInterClassUsageMap(localFileInterClassUsageMatrix);
	}
	/**
	 * 
	 * 
	 * @param args
	 * 
	 * Give the following args to test:
	 * 
	 * C:\Users\GiriprasadSridhara\Documents\acmeair-monolithic-java-master
	 * C:\Users\GiriprasadSridhara\sample.plantsbywebsphere
	 * C:\Users\GiriprasadSridhara\sample.daytrader7
	 * C:\Users\GiriprasadSridhara\Downloads\digdeep-master
	 */
	public static void main(String[] args) {
		
		// test for binary inter-class usage
		//String classFilesRoot="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes\\com\\ff\\sys\\v3\\cellphone\\webservices\\bean\\CellPhoneQualifiedBean.class";
		String classFilesRoot="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes\\";
		InterClassUsageFinder i=new InterClassUsageFinder();
		String outPath="C:\\temp\\inter-class-usage-from-class-files.json";
		i.findFromBinaryClassFiles(classFilesRoot, outPath);
		System.exit(0);
		
		
		Map<ClassPair, Integer> m1=null;
//		loader("/Users/shreya/eclipse-workspace/outputs/daytrader7/temp/inter-class-usage.json");
		for (ClassPair cp: m1.keySet())
		{
			Integer c=m1.get(cp);
			System.out.println(cp.getThisClass()+","+cp.getUsedClass()+","+c);
//			usesOtherClasses.add(cp.getThisClass());
//			usedByOtherClasses.add(cp.getUsedClass());
//			
//			String userClass=cp.getThisClass();
//			String usedClass=cp.getUsedClass();
//			i.addToInterClassUsageMap(interClassUsageMap,userClass,usedClass,"user");
//			i.addToInterClassUsageMap(interClassUsageMap,userClass,usedClass,"used");
		}
		return;
		// TODO Auto-generated method stub

//		int choice = 1;
//		//int choice = 2;
//		InterClassUsageFinder i = new InterClassUsageFinder();
//		if (choice == 1) {
//			if (args.length < 1) {
//				System.err.println("USAGE: <root of source files for project>");
//				System.exit(-1);
//			}
//
//			String srcFilesRoot = args[0];
//			String opJsonFileName=srcFilesRoot + File.separator + "temp"+File.separator+"inter-class-usage.json";
//
//			// InterClassUsageFinder i=new InterClassUsageFinder();
//			Map<ClassPair, Integer> m=i.find(srcFilesRoot,opJsonFileName);
//
//			System.out.println("\nFinalInterClassUsageMatrix:");
////			Set<String> usesOtherClasses=new HashSet<String>();
////			Set<String> usedByOtherClasses=new HashSet<String>();
//			
////			Map<String,InterClassUsage> interClassUsageMap=
////					new HashMap<String, InterClassUsage>();
////			
//			for (ClassPair cp: m.keySet())
//			{
//				Integer c=m.get(cp);
//				System.out.println(cp.getThisClass()+","+cp.getUsedClass()+","+c);
////				usesOtherClasses.add(cp.getThisClass());
////				usedByOtherClasses.add(cp.getUsedClass());
////				
////				String userClass=cp.getThisClass();
////				String usedClass=cp.getUsedClass();
////				i.addToInterClassUsageMap(interClassUsageMap,userClass,usedClass,"user");
////				i.addToInterClassUsageMap(interClassUsageMap,userClass,usedClass,"used");
//			}
////
//
//			
//		} else if (choice == 2) {
//			//String ipf = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\com\\ibm\\research\\digdeep\\preventive\\clusterer\\CarrotClusteringEngineImpl.java";
//			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\com\\ibm\\research\\util\\MapUtil.java";
//			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\com\\ibm\\research\\digdeep\\web\\services\\DynamicRecommendationService.java";
//			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-git-api-web\\src\\com\\ibm\\research\\digdeep\\git\\GitApiResource.java";
//			String ipf="C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere\\src\\main\\java\\com\\ibm\\websphere\\samples\\pbw\\bean\\BackOrderMgr.java";
//			//String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\extra\\recommendations\\"
//			// ipf="C:\\Users\\GiriprasadSridhara\\dependency-migration-asistant\\src\\main\\java\\com\\ibm\\research\\appmod\\dma\\impact\\ChangedAPIUsageSiteFinder.java";
//			File file = new File(ipf);
//			//String srcFilesRoot = "C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\";
//			//String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-web-common\\src\\";
//			//String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\";
//			String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-git-api-web\\src\\";
//			i.processOneFile(file, srcFilesRoot);
//		}
//		else if (choice==3)
//		{
//			//Make a filter that matches files and directories
//		    final IOFileFilter srcFolderFilter = new IOFileFilter() {  
//		        @Override
//		        public boolean accept(File dir, String name) {
//		        	//System.out.println("accept dir " + name);
//		            if (name.compareTo("src")==0)
//		            {
//		            	return true;
//		            }
//		            return false;
//		        }
//
//		        @Override
//		        public boolean accept(File file) {
//		            return false;
//
//		        }
//		    };
//
//		    extractSrcRootFolders("C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\");

//		}
	}
	public void saveTofile(String outfile) {
		Gson gson=new Gson();
		// TODO: check if below is static and add getter
		String strJson=gson.toJson(interClassUsageMatrix);
		String opJsonFileName=outfile;
		try
		{
			PrintWriter pw=new PrintWriter(opJsonFileName);
			pw.println(strJson);
			pw.flush();
			pw.close();
			System.out.println("Wrote InterClassUsage with source/sink identification to "+opJsonFileName);
		}
		catch(Exception e)
		{
			System.out.println("Handled exception.");
			e.printStackTrace();
		}
	}

	private void findTypeAndPrintJson(String srcFilesRoot,String opJsonFileName) {
		System.out.println("interClassUsageMap size="+interClassUsageMap.size());
		for (String c:interClassUsageMap.keySet())
		{
//			System.out.println("class="+c);
			InterClassUsage icu = interClassUsageMap.get(c);
			if ( (icu.usedByClassesToCount.size()>0) &&
					(icu.usedClassesToCount.size()>0))
			{
				//icu.type="UsesOtherClassesAndUsedByOtherClasses";
				icu.type="both";
			}
			else if (icu.usedByClassesToCount.size()>0)
			{
				//icu.type="OnlyUsedByOtherClasses";
				//icu.type="OnlyUsed";
				icu.type="source";
			}
			else if (icu.usedClassesToCount.size()>0)
			{
				//icu.type="OnlyUsesOtherClasses";
				//icu.type="OnlyUses";
				icu.type="sink";
			}

		}
			
		Gson gson=new Gson();
		// TODO: check if below is static and add getter
		String strJson=gson.toJson(interClassUsageMap);
//		System.out.println("\nJSON="+strJson);
//		String opJsonFileName=srcFilesRoot+File.separator+"inter-class-usage.json";
//		String opJsonFileName;
		try
		{
			PrintWriter pw=new PrintWriter(opJsonFileName);
			pw.println(strJson);
			pw.flush();
			pw.close();
			System.out.println("Wrote InterClassUsage with source/sink identification to "+opJsonFileName);
		}
		catch(Exception e)
		{
			System.out.println("Handled exception.");
			e.printStackTrace();
		}
	}

//	public void addToInterClassUsageMap(Map<String,InterClassUsage> interClassUsageMap,
//			String userClass, String usedClass,String type)
//	{
//		InterClassUsage interClassUsage1 = interClassUsageMap.get(userOrUsedByClass);
//		if (interClassUsage1==null)
//		{
//			interClassUsage1=new InterClassUsage(userOrUsedByClass);
//		}
//		
//		Map<String,Integer> usedOrUsedByClassesToCount=null;
//		if (type.equalsIgnoreCase("user"))
//		{
//			usedOrUsedByClassesToCount=interClassUsage1.usedClassesToCount;
//		}
//		else
//		{
//			// "used"
//			usedOrUsedByClassesToCount=interClassUsage1.usedByClassesToCount;
//		}
//		
//		Integer cnt1=usedOrUsedByClassesToCount.get(userOrUsedByClass);
//		if (cnt1==null)
//		{
//			Integer cnt2=new Integer(1);
//			usedOrUsedByClassesToCount.put(userOrUsedByClass,cnt2);
//		}
//		else
//		{
//			Integer cnt2=new Integer(cnt1.intValue()+1);
//			usedOrUsedByClassesToCount.put(userOrUsedByClass,cnt2);
//		}
//		interClassUsageMap.put(key, interClassUsage1);
//	}
	
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

		// for newer pom based projects, where java files
		// are under src/main/java etc instead of src/<package> as in say older digdeep
		
		// TODO: i have not seen such occurrences but if the source folders are not named
		// src or src/main/java but names say as "source", "source/main/java" etc, we need to handle
		// it. In the processOneFile method above, while reading the lines, we can scan 
		// the package declaration line such as com.ibm.research...., find "com/ibm/research/..." in
		// the file path and pick the part before com.ibm.research.... as the source folder root for
		// parsing this java file.
		// But for now, the logic below suffices.
		
		String[] srcPatterns=new String[] {
				File.separator+"src"+File.separator,
				File.separator+"src"+File.separator+"main"+File.separator,
				File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator
		};
		for (File file : files) {
			for (String srcPattern:srcPatterns)
			{
				String srcFolder=extractSourceFoldersHelper(file,srcPattern);
				if (srcFolder!=null)
				{
					srcRootSet.add(srcFolder);
				}
			}
		}
		
//		System.out.println("srcRootSet size="+srcRootSet.size());
//		for (String s: srcRootSet)
//		{
//			System.out.println(s);
//		}
		return srcRootSet;
	}

	private static String extractSourceFoldersHelper(File file,String srcPattern)
	{
		//System.out.println("extractSourceFoldersHelper: "+file.getAbsolutePath());
		//System.out.println("\tsrcPattern: "+srcPattern);
		String fileNameWPath=file.getAbsolutePath();
		int srcIndex=fileNameWPath.indexOf(srcPattern);
		String srcRoot=null;
		if (srcIndex != -1)
		{
			// extract till .../src/ to use as src root
			srcRoot=fileNameWPath.substring(0,srcIndex+srcPattern.length());
			//System.out.println("\t returning "+srcRoot);
			return srcRoot;
		}
		//System.out.println("\t returning null");
		return null;
	}
}
