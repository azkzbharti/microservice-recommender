
package com.ibm.research.msr.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.ibm.research.msr.binaryextractor.ReferencedClassesExtractor;
import com.ibm.research.msr.utils.ReadJarMap;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;

public class DocumentReader {
	private static List<String> token;
	public List<String> getToken() {
		return token;
	}

	public static void setToken(List<String> token) {
		DocumentReader.token = token;
	}

	public static int getNum_of_tokens() {
		return num_of_tokens;
	}

	public static void setNum_of_tokens(int num_of_tokens) {
		DocumentReader.num_of_tokens = num_of_tokens;
	}

	private static int num_of_tokens;
	private static Map<String, Integer> importCountMap;
	private static String packageName;

	public String getPackageName() {
		return packageName;
	}

	public static Map<String, Integer> getImportCountMap() {
		return importCountMap;
	}

	public static void setImportCountMap(Map<String, Integer> importCountMap) {
		DocumentReader.importCountMap = importCountMap;
	}

	public static void setPackageName(String packageName) {
		DocumentReader.packageName = packageName;
	}

	public DocumentReader(File file) throws IOException {
		importCountMap = new HashMap<String, Integer>();
		
		num_of_tokens = token.size();
	}
	

	public static List<String> processJavaFile(File file) throws IOException {
		List<String> tokens = new ArrayList<String>();
		List<String> imports;
//    	Map<String, Integer> importCountMaps = new HashMap<String, Integer>() ;
		if(file.getName().endsWith("java")) {
			
				
				JavaProjectBuilder builder = new JavaProjectBuilder();
				
		
				try {
					JavaSource src = builder.addSource(file);
					imports = src.getImports();
					JavaPackage pkg = src.getPackage();
					packageName = pkg.getName();
		//		   System.out.println(imports);
				} catch (ParseException e) {
					System.out.println("Parse Error in : " + file.getAbsolutePath());
					return null;
		
				}
		}
		else if(file.getName().endsWith(".class")) {
			HashSet<String> importset=AnalyzeApp.binaryAppImportStatemnts.get(file.getAbsolutePath());
			imports=new ArrayList<String>(importset);
			
		}
		else {
			imports=null;
			System.out.println("No import statementes extracted"+file.getAbsolutePath());
		}
		for (String imp : imports) {

			String cat = ReadJarMap.getLibCatMap().entrySet().stream().filter(entry -> imp.contains(entry.getKey()))
					.map(entry -> entry.getValue()).findFirst().orElse("None");
//			String importRegex = DocumentProcessing.libCatMap.entrySet().stream().filter(entry -> imp.matches(entry.getKey()))
//					.map(entry -> entry.getKey()).findFirst().orElse("None");
			tokens.add(cat);
//			tokens.add(imp);
			if (!importCountMap.containsKey(cat)) {
				importCountMap.put(cat, 1);
			} else {
				importCountMap.put(cat, importCountMap.get(cat) + 1);
			}

		}
//		System.out.println(importCountMap.toString());
//		setImportCountMap(importCountMap);
		return tokens;

	}
	
	public List<String> proceessBinaryFile() {
		List<String> tokens = new ArrayList<String>();
		
		return tokens;
		
	}

	public static List<String> tokenizeDocument(File file) throws Exception {
		token = processJavaFile(file);
		num_of_tokens = token.size();
		return token;

	}

}
