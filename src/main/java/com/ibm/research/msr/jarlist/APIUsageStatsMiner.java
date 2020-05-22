package com.ibm.research.msr.jarlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import com.google.gson.Gson;
import com.ibm.research.appmod.pa.binaryextractor.ReferencedClassesExtractor;
import com.ibm.research.appmod.pa.jarlist.APIUsageStats;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class APIUsageStatsMiner {

	String[] classPathEntries=null;
	Map<String,String> fqClassToJar=new HashMap<String,String>();
	Map<String,String> fqMethodNameToJar=new HashMap<String,String>();
	Map<String,HashSet<String>> jarToSetOfUsedAPIs=new HashMap<String,HashSet<String>>();
	Map<String,Integer> jarToAPICount=new HashMap<String,Integer>();

	static int libraryMI=0;

	static int nonLibraryMI=0;

	static int prevLibraryMI=0;

	static int methodDeclarations=0;

	static int methodDeclarationsWithLibCalls=0;

	public void mine(String srcRoot,String jarToPkgsClassesCsv,String opJSONFileNameWithPath)
	{
		PrintWriter pw = null;
		try
		{
			libraryMI=0;
			nonLibraryMI=0;
			prevLibraryMI=0;
			methodDeclarations=0;
			methodDeclarationsWithLibCalls=0;
			Set<String> jarsWithPath = read(jarToPkgsClassesCsv);

			classPathEntries=jarsWithPath.toArray(new String[0]);

			System.out.println("classPathEntries = "+classPathEntries.length);
			for (int i=0;i<classPathEntries.length;i++)
			{
				System.out.println(classPathEntries[i]);
			}

			//System.out.println("fqClassToJar size="+fqClassToJar.size());
			for(String c : fqClassToJar.keySet())
			{
				String j=fqClassToJar.get(c);
				//System.out.println(c+" "+j);
			}

			File fRoot = new File(srcRoot);
			String[] extensions = new String[] { "java"};
			//System.out.println("Getting all .java  in " + fRoot.getPath()
			//		+ " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
			for (File file : files) {
				processOneFile(file,srcRoot);
			}

			//String opFileName=srcRoot+File.separator+"jar-to-used-apis.csv";
			//System.out.println("writing to op file="+opJSONFileNameWithPath);
			pw = new PrintWriter(opJSONFileNameWithPath);
			//pw.println("Jar,DistinctAPIsUsageCount,API");

			List<APIUsageStats> apiUsageStatsList=new ArrayList<APIUsageStats>();

			for (String jar:jarToSetOfUsedAPIs.keySet())
			{
				HashSet<String> usedAPIs = jarToSetOfUsedAPIs.get(jar);
				//System.out.println(usedAPIs.size()+" distinct APIs used in jar="+jar);
				for (String s:usedAPIs)
				{
					//System.out.println("\t" + s);
				}
				Integer iAPICnt=jarToAPICount.get(jar);
				//double percentUse=(usedAPIs.size()*100.0)/iAPICnt.intValue();
				//pw.println(jar+","+usedAPIs.size()+","+s+","+percentUse);
				APIUsageStats a=new APIUsageStats(jar,iAPICnt.intValue(),usedAPIs.size());
				apiUsageStatsList.add(a);
			}

			//System.out.println("apiUsageStatsList size="+apiUsageStatsList.size());

			//			Gson g=new Gson();
			//			String json=g.toJson(apiUsageStatsList);
			//			System.out.println("json=\n"+json);
			//			pw.println(json);
			//			pw.flush();
			//			pw.close();

			//System.out.println("Lib MI="+libraryMI);
			//System.out.println("Non Lib MI="+nonLibraryMI);

			OverallProjectLibUsageStats o=new OverallProjectLibUsageStats(libraryMI, nonLibraryMI, apiUsageStatsList,
					methodDeclarations,methodDeclarationsWithLibCalls);

			Gson g=new Gson();
			String json=g.toJson(o);
			//System.out.println("json=\n"+json);
			pw.println(json);
			pw.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} finally {
			if(pw != null)
				pw.close();
		}
	}

	private Set<String> read(String jarToPkgsClassesCsv) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(jarToPkgsClassesCsv));
		String line=null;
		Set<String> jarsWithPath= new HashSet<String>();

		int lineNum=0;
		while ((line=br.readLine())!=null)
		{
			lineNum++;
			if (lineNum==1)
			{
				continue;//skip header
			}
			String[] arr=line.split(",");
			String jarWithPath=arr[0];
			String fqClassName=arr[2];

			String methodName=null;
			if (arr.length==4)
			{
				methodName=arr[3];
			}

			int li=jarWithPath.lastIndexOf(File.separator);
			String jarWoPath=null;
			if (li!=-1)
			{
				jarWoPath=jarWithPath.substring(li+1);
			}
			else
			{
				jarWoPath=jarWithPath;
			}
			jarsWithPath.add(jarWithPath);
			//fqClassToJar.put(fqClassName, jarWithPath);
			fqClassToJar.put(fqClassName, jarWoPath);
			if (methodName!=null)
			{
				String fqMethName=fqClassName+"."+methodName;
				fqMethodNameToJar.put(fqMethName, jarWoPath);
			}
		}


		for (String fqmn:fqMethodNameToJar.keySet())
		{
			String jar=fqMethodNameToJar.get(fqmn);
			Integer iCnt=jarToAPICount.get(jar);
			if (iCnt==null)
			{
				iCnt=new Integer(1);
				jarToAPICount.put(jar, iCnt);
			}
			else
			{
				Integer iCnt2=new Integer(iCnt.intValue()+1);
				jarToAPICount.put(jar, iCnt2);
			}
		}

		//System.out.println("Total APIs per jar:");
		for (String j:jarToAPICount.keySet())
		{
			//System.out.println(j+"->"+jarToAPICount.get(j));
		}
		return jarsWithPath;
	}

	public void processOneFile(File file, String srcRoot)
	{
		String fileNameWPath=file.getAbsolutePath();
		//System.out.println("file: " + file.getName() + " " + fileNameWPath);

		StringBuffer sb=new StringBuffer();
		String line=null;
		BufferedReader br=null;

		try
		{
			br=new BufferedReader(new FileReader(fileNameWPath));
			while ((line=br.readLine())!=null)
			{
				sb.append(line + "\n");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally {
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
		String[] srcRootArr=new String[1];
		srcRootArr[0]=srcRoot;
		String[] classPathEntries2=new String[] {
				"C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib\\guava-14.0.1.jar",
				"C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\lib\\carrot2-mini-3.6.2.jar"
		};

		ArrayList<String> cp3=new ArrayList<String>();
		for (String c:classPathEntries)
		{
			if (c.contains("digdeep-tickets-processing"))
			{
				cp3.add(c);
			}
		}

		parser.setEnvironment(classPathEntries, srcRootArr, null, true);
		//parser.setEnvironment(cp3.toArray(new String[0]), srcRootArr, null, true);


		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);



		cu.accept(new ASTVisitor() {

			public boolean visit(MethodDeclaration m)
			{
				String mdName=m.getName().getFullyQualifiedName();
				if (mdName.startsWith("get") || mdName.startsWith("set"))
				{
					// ignore as getters and setters will not have method calls at all 
					// in general
				}
				else
				{
					methodDeclarations++;
				}
				return true;
			}

			public void endVisit(MethodDeclaration m)
			{
				String mdName=m.getName().getFullyQualifiedName();
				if (mdName.startsWith("get") || mdName.startsWith("set"))
				{
					// ignore as getters and setters will not have method calls at all 
					// in general
				}
				else
				{
					if (prevLibraryMI<libraryMI)
					{
						methodDeclarationsWithLibCalls++;
					}
				}
				prevLibraryMI=libraryMI;
			}

			public boolean visit(MethodInvocation mi)
			{
				String miName=mi.getName().getFullyQualifiedName();
				IMethodBinding imb = mi.resolveMethodBinding();

				if (imb!=null)
				{
					ITypeBinding dc = imb.getDeclaringClass();
					//					IModuleBinding imob2 = dc.getModule();
					//					IPackageBinding ipb = dc.getPackage();
					//					IModuleBinding imob = ipb.getModule();
					//					
					//					//imob.
					//					System.out.println("IModuleBinding="+imob.getName());
					//					System.out.println("IModuleBinding2="+imob2.getName());
					//					System.out.println("IModuleBinding tostring="+imob.toString());

					String fqMIName=dc.getQualifiedName()+"."+miName;
					//System.out.println("fqMIName = " + fqMIName);
					handleLibOrNonLibCalls(dc.getQualifiedName(), fqMIName);
				}
				else
				{
					//System.out.println("\t method binding null="+mi.toString());
					nonLibraryMI++;
				}
				return true;
			}
		}
				);
	}


	private void handleLibOrNonLibCalls(String className, String fqMIName) {
		String jar=fqClassToJar.get(className);
		if (jar!=null)
		{
			libraryMI++;

			HashSet<String> setOfUsedAPIs = jarToSetOfUsedAPIs.get(jar);
			if (setOfUsedAPIs==null)
			{
				setOfUsedAPIs=new HashSet<String>();
			}
			setOfUsedAPIs.add(fqMIName);
			jarToSetOfUsedAPIs.put(jar, setOfUsedAPIs);
		}
		else
		{
			nonLibraryMI++;
		}
	}

	public void mineFromClassFiles(String classFilesRoot,String jarToPkgsClassesCsv,String opJSONFileNameWithPath)
	{
		PrintWriter pw = null;
		try
		{
			libraryMI=0;
			nonLibraryMI=0;
			prevLibraryMI=0;
			methodDeclarations=0;
			methodDeclarationsWithLibCalls=0;
			Set<String> jarsWithPath = read(jarToPkgsClassesCsv);

			//			classPathEntries=jarsWithPath.toArray(new String[0]);
			//			
			//			System.out.println("classPathEntries = "+classPathEntries.length);
			//			for (int i=0;i<classPathEntries.length;i++)
			//			{
			//				System.out.println(classPathEntries[i]);
			//			}

			//			//System.out.println("fqClassToJar size="+fqClassToJar.size());
			//			for(String c : fqClassToJar.keySet())
			//			{
			//				String j=fqClassToJar.get(c);
			//				//System.out.println(c+" "+j);
			//			}

			File fRoot = new File(classFilesRoot);
			String[] extensions = new String[] { "class"};
			//System.out.println("Getting all .java  in " + fRoot.getPath()
			//		+ " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
			for (File file : files) {
				processOneClassFile(file.getAbsolutePath());
			}

			//String opFileName=srcRoot+File.separator+"jar-to-used-apis.csv";
			//System.out.println("writing to op file="+opJSONFileNameWithPath);
			pw  = new PrintWriter(opJSONFileNameWithPath);
			//pw.println("Jar,DistinctAPIsUsageCount,API");

			List<APIUsageStats> apiUsageStatsList=new ArrayList<APIUsageStats>();

			for (String jar:jarToSetOfUsedAPIs.keySet())
			{
				HashSet<String> usedAPIs = jarToSetOfUsedAPIs.get(jar);
				//System.out.println(usedAPIs.size()+" distinct APIs used in jar="+jar);
				for (String s:usedAPIs)
				{
					//System.out.println("\t" + s);
				}
				Integer iAPICnt=jarToAPICount.get(jar);
				//double percentUse=(usedAPIs.size()*100.0)/iAPICnt.intValue();
				//pw.println(jar+","+usedAPIs.size()+","+s+","+percentUse);
				APIUsageStats a=new APIUsageStats(jar,iAPICnt.intValue(),usedAPIs.size());
				apiUsageStatsList.add(a);
			}

			//System.out.println("apiUsageStatsList size="+apiUsageStatsList.size());

			//			Gson g=new Gson();
			//			String json=g.toJson(apiUsageStatsList);
			//			System.out.println("json=\n"+json);
			//			pw.println(json);
			//			pw.flush();
			//			pw.close();

			//System.out.println("Lib MI="+libraryMI);
			//System.out.println("Non Lib MI="+nonLibraryMI);

			OverallProjectLibUsageStats o=new OverallProjectLibUsageStats(libraryMI, nonLibraryMI, apiUsageStatsList,
					methodDeclarations,methodDeclarationsWithLibCalls);

			Gson g=new Gson();
			String json=g.toJson(o);
			//System.out.println("json=\n"+json);
			pw.println(json);
			pw.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} finally {
			if(pw != null)
				pw.close();
		}
	}


	//public void processOneClassFile(File file, String classFilesRoot)
	public void processOneClassFile(String classFileLocation)
	{
		ReferencedClassesExtractor r=new ReferencedClassesExtractor();
		//String classFileLocation=file.getAbsolutePath();
		String fqcn = r.getFullyQualifiedClassName(classFileLocation);
		String cpToInsert=ReferencedClassesExtractor.getClassPathToInsert(classFileLocation, fqcn);

		ClassPool cp = ClassPool.getDefault();
		CtClass ctc = null;

		try
		{
			cp.insertClassPath(cpToInsert);
			ctc = cp.get(fqcn);

			CtMethod[] methods = ctc.getDeclaredMethods();
			for (CtMethod method:methods)
			{
				String mdName=method.getName();

				if (mdName.startsWith("get") || mdName.startsWith("set"))
				{
					continue;
				}
				methodDeclarations++;

				System.out.println("MD="+method.getName());

				method.instrument(
						new ExprEditor() {
							public void edit(MethodCall m)
									throws CannotCompileException
							{
								System.out.println("\t"+m.getClassName() + "---" + m.getMethodName() + "-----" + m.getSignature());

								String className=m.getClassName();
								String fqMIName=className+"."+m.getMethodName();
								handleLibOrNonLibCalls(className, fqMIName);
							}
						});

				if (prevLibraryMI<libraryMI)
				{
					methodDeclarationsWithLibCalls++;
				}
				prevLibraryMI=libraryMI;
			}
		}
		catch(Exception e )
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		APIUsageStatsMiner a=new APIUsageStatsMiner();

		int ch=2;

		if (ch==1)
		{
			//String srcRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\";
			//		String srcRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master";
			//		String jarToPkgsClassesCsv="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\jar-to-packages-classes.csv";

			String srcRoot="C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere-manual-dependencies\\src";
			String jarToPkgsClassesCsv="C:\\Users\\GiriprasadSridhara\\msr\\microservice-recommender\\src\\main\\resources\\jar-to-packages-classes-public-methods.csv";
			String opJSONFileNameWithPath="C:\\temp\\api-usage-2.json";
			//		String srcRoot="C:\\Users\\GiriprasadSridhara\\Documents\\acmeair-monolithic-java-master\\acmeair-monolithic-java-master\\src";
			//		String jarToPkgsClassesCsv="C:\\Users\\GiriprasadSridhara\\Documents\\acmeair-monolithic-java-master\\acmeair-monolithic-java-master\\acme-jar-to-apis.csv";
			//		String srcRoot="C:\\Users\\GiriprasadSridhara\\sample.daytrader7\\daytrader-ee7-ejb\\";
			//		String jarToPkgsClassesCsv="C:\\Users\\GiriprasadSridhara\\sample.daytrader7\\daytrader-ee7-ejb\\lib-dma\\daytrader-ee7-ejb-jar-to-apis.csv";

			//		a.mine(srcRoot, jarToPkgsClassesCsv,opJSONFileNameWithPath);
		}
		else if (ch==2)
		{
			String classFileLocation ="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes\\com\\ff\\sys\\v3\\cellphone\\webservices\\bean\\CellPhoneQualifiedBean.class";;

			String classFilesRoot="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes";
			String jarToPkgsClassesCsv="C:\\temp\\mobile-ear-1.0.23-output\\temp\\jar-to-packages.csv";
			String opJSONFileNameWithPath="C:\\temp\\api-usage-3.json";

			a.mineFromClassFiles(classFilesRoot, jarToPkgsClassesCsv, opJSONFileNameWithPath);
			//a.processOneClassFile(classFileLocation);
		}
		else if (ch==3)
		{
			String classFileLocation ="C:\\temp\\mobile-ear-1.0.23-output\\temp\\unzip\\WEB-INF\\classes\\com\\ff\\sys\\v3\\cellphone\\webservices\\bean\\CellPhoneQualifiedBean.class";;
			a.processOneClassFile(classFileLocation);
		}
	}

}

