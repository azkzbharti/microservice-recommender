package com.ibm.research.msr.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.research.msr.extraction.Document;
import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.parser.ParseException;

public class DocumentParserUtil {

	public static void processJavaFile(Document document) throws IOException {
		List<String> tokens = new ArrayList<String>();
		Map<String, Integer> importCountMap = new HashMap<String, Integer>() ;

		JavaProjectBuilder builder = new JavaProjectBuilder();
		List<String> imports;

		try {
			JavaSource src = builder.addSource(document.getFile());
			imports = src.getImports();
			JavaPackage pkg = src.getPackage();
			// set package name
			document.setPackageName(pkg.getName());
		} catch (ParseException e) {
			System.out.println("Parse Error in : " + document.getFile().getAbsolutePath());
			return;
		}
		
		// parse imports and extract data
		for (String importName : imports) {
			String category = ReadJarMap.getLibCatMap()
					.entrySet()
					.stream()
					.filter(entry -> importName.contains(entry.getKey()))
					.map(entry -> entry.getValue()).findFirst().orElse("None");
			tokens.add(category);
			
			if (!importCountMap.containsKey(category)) {
				importCountMap.put(category, 1);
			} else {
				importCountMap.put(category, importCountMap.get(category) + 1);
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
