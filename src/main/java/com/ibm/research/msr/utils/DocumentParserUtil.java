package com.ibm.research.msr.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.research.msr.binaryextractor.ReferencedClassesExtractor;
import com.ibm.research.msr.extraction.Document;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;

public class DocumentParserUtil {
	private static boolean ignoreNone;

	public static void processFile(Document document) {
		try {
			System.out.println(" Processing file " + document.getFile().getAbsolutePath());
			if (document.getFile().getAbsolutePath().endsWith(".java"))
				processJavaFile(document);

			else if (document.getFile().getAbsolutePath().endsWith(".class"))
				processBinaryFile(document);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void processJavaFile(Document document) throws IOException {
		List<String> tokens = new ArrayList<String>();
		Map<String, Integer> importCountMap = new HashMap<String, Integer>();

		JavaProjectBuilder builder = new JavaProjectBuilder();
		List<String> imports;

		try {
			JavaSource src = builder.addSource(document.getFile());
			imports = src.getImports();
			JavaPackage pkg = src.getPackage();
			// set package name
			document.setPackageName(src.getPackageName());
			document.setImportapis(imports);
		} catch (ParseException e) {
			System.out.println("Parse Error in : " + document.getFile().getAbsolutePath());
			return;
		}
		
		String category="";
		if(ReadJarMap.getLibCatMap()==null) {
			System.out.println("No tokens parsed, as jar-to-packages map doesnt exist..");
			return;
		}
		
		// parse imports and extract data
		for (String importName : imports) {
			 category = ReadJarMap.getLibCatMap().entrySet().stream()
					.filter(entry -> importName.contains(entry.getKey())).map(entry -> entry.getValue()).findFirst()
					.orElse("None");
			tokens.add(category);
			
			if (!importCountMap.containsKey(category)) {
				importCountMap.put(category, 1);
			} else {
				importCountMap.put(category, importCountMap.get(category) + 1);
			}
			
		}

		if(importCountMap.size()>1 && importCountMap.containsKey("None") && importCountMap.get("None")>0 && tokens.size()>1) {
			tokens.remove("None");
			importCountMap.remove("None");
		}
		if (imports.size()==0) {
			 category="None";
			tokens.add(category);
			importCountMap.put(category, 1);
		}
		if (DocumentParserUtil.getIgnoreNone() && category == "None") {
			tokens.remove("None");
			importCountMap.remove("None");
		}		
		
		// set tokens and import Count
		document.setTokens(tokens);
		document.setTokenCountMap(importCountMap);

		
	}

	public static boolean getIgnoreNone() {
		return ignoreNone;
	}

	public static void setIgnoreNone(boolean ignoreNone) {
		DocumentParserUtil.ignoreNone = ignoreNone;
	}

	private static void processBinaryFile(Document document) {

		List<String> tokens = new ArrayList<String>();
		Map<String, Integer> importCountMap = new HashMap<String, Integer>();

		List<String> imports;

		ReferencedClassesExtractor classExtractor = new ReferencedClassesExtractor();
		imports = new ArrayList<String>(classExtractor.extractFromClass(document.getFile().getAbsolutePath()));

		// parse imports and extract data
		for (String importName : imports) {
			String category = ReadJarMap.getLibCatMap().entrySet().stream()
					.filter(entry -> importName.contains(entry.getKey())).map(entry -> entry.getValue()).findFirst()
					.orElse("None");
			tokens.add(category);
			if (!importCountMap.containsKey(category)) {
				importCountMap.put(category, 1);
			} else {
				importCountMap.put(category, importCountMap.get(category) + 1);
			}
			if (DocumentParserUtil.getIgnoreNone() && category == "None") {
				tokens.remove("None");
				importCountMap.remove("None");
				continue;
			}

		}

		// set tokens and import Count
		document.setTokens(tokens);
		document.setTokenCountMap(importCountMap);

		// use for debugging
//		for(Map.Entry<String, Integer> entry : importCountMap.entrySet()) {
//			System.out.println(entry.getKey() + " " + entry.getValue());
//		}

	}

}
