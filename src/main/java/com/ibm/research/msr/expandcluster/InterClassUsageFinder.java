package com.ibm.research.msr.expandcluster;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


public class InterClassUsageFinder {

	public void find(String srcFilesRoot)
	{

		File fRoot = new File(srcFilesRoot);
		String[] extensions = new String[] { "java"};
		System.out.println("Getting all .java  in " + fRoot.getPath()
				+ " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
		for (File file : files) {
			processOneFile(file,srcFilesRoot);
		}

	}
	
	public void processOneFile(File file, String srcFilesRoot)
	{
	
		String fileNameWPath=file.getAbsolutePath();
		System.out.println("file: " + file.getName() + " " + fileNameWPath);
	
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
		//parser.setEnvironment(classPathEntries, null, null, true);
		String[] srcPath=new String[1];
		//srcPath[0]="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src";
		srcPath[0]=srcFilesRoot;
		
		parser.setEnvironment(null, srcPath, null, true);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		System.out.println("create ast done");
		cu.accept(new ASTVisitor() {
			
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

			public boolean visit(VariableDeclarationFragment vdf)
			{
//				System.out.println("visit vdf "+vdf.toString());

				
				IVariableBinding ivb = vdf.resolveBinding();
				if (ivb!=null)
				{
					ITypeBinding itb = ivb.getType();
					if (itb!=null)
					{
						if (itb.isFromSource())
						{
							String n = itb.getName();
							System.out.println("\t\tVDF: Usage of " + n);
						}
					}
				}
				return true;
			}

			public boolean visit(VariableDeclaration vd)
			{
//				System.out.println("visit vd "+vd.toString());
				IVariableBinding ivb = vd.resolveBinding();
				if (ivb!=null)
				{
					ITypeBinding itb = ivb.getType();
					if (itb!=null)
					{
						//if (itb.isFromSource())
						//{
							String n = itb.getName();
							System.out.println("\t\tVariableDeclaration: Usage of " + n);
						//}
					}
				}


				return true;
			}
			
			public boolean visit(MethodInvocation mi)
			{
//				System.out.println("visit MI " +mi.toString());
				Expression e = mi.getExpression();
				if (e==null)
				{
					return true;
				}
				ITypeBinding itb = e.resolveTypeBinding();
				if (itb!=null)
				{
					if (itb.isFromSource())
					{
						String n = itb.getName();
						System.out.println("\t\tMethodInvocation: Usage of " + n);
					}
					
				}

				return true;
			}


		});
	
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length <1)
		{
			System.err.println("USAGE: <root of source files for project>");
			System.exit(-1);
		}
		
		int choice =1;
		InterClassUsageFinder i=new InterClassUsageFinder();
		if (choice==1)
		{
			String srcFilesRoot=args[0];
			//InterClassUsageFinder i=new InterClassUsageFinder();
			i.find(srcFilesRoot);
		}
		else if (choice ==2)
		{
			String ipf="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\com\\ibm\\research\\digdeep\\preventive\\clusterer\\CarrotClusteringEngineImpl.java";;
			//String ipf="C:\\Users\\GiriprasadSridhara\\dependency-migration-asistant\\src\\main\\java\\com\\ibm\\research\\appmod\\dma\\impact\\ChangedAPIUsageSiteFinder.java";
			File file=new File(ipf);
			String srcFilesRoot="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\digdeep-master\\digdeep-tickets-processing\\src\\";
			i.processOneFile(file,srcFilesRoot);
		}
	}

}
