
package com.ibm.research.msr.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ibm.research.msr.utils.DocumentParserUtil;


/// TODO: XXX (1) order getters (2) check unused variables

/**
 * 
 * @author ShreyaKhare
 *
 */
public class Document {

	private String fileName;
	private String packageName;
	// TODO: remove?
	private File file;
	/** contains list of jar files */
	private List<String> tokens;
	/** token to count map*/
	private Map<String, Integer> tokenCountMap;
	
	private List<Double> docVector = new ArrayList<>();
	private List<Double> docVectorTF = new ArrayList<>();
	private List<Double> unitVector = new ArrayList<>();
	

	
	// TODO: change file to file path 
	public Document(File file) throws IOException, Exception {
		this.file = file;
		fileName = file.getName();
		DocumentParserUtil.processJavaFile(this);
	}

	public Document(String filepath, String filename, List<Double> docvector, List<String> tokens) {
		this.file = new File(filepath);
		this.fileName = filename;
		this.docVector = docvector;
		this.tokens = tokens;
		this.packageName = "";
	}

	public List<Double> getDocVector() {
		return docVector;
	}

	public void setDocVector(ArrayList<Double> docVector) {
		this.docVector = docVector;
	}

	public List<Double> getDocVectorTF() {
		return docVectorTF;
	}

	public void setDocVectorTF(ArrayList<Double> docVectorTF) {
		this.docVectorTF = docVectorTF;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<String, Integer> getTokenCountMap() {
		return tokenCountMap;
	}

	public void setTokenCountMap(Map<String, Integer> tokenCountMap) {
		this.tokenCountMap = tokenCountMap;
	}

	public String getName() {
		return fileName;
	}

	public int getNumberOfTokens() {
		return tokens.size();
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public File getFile() {
		return file;
	}
	
	int getTokenOccurenceCount(String word) {
		if(tokenCountMap.containsKey(word)) {
			return tokenCountMap.get(word);
		}
		return 0;
	}

	void addComponentToVector(double tfIdf) {
		docVector.add(tfIdf);
	}

	void addComponentToTfVector(double tf) {
		docVectorTF.add(tf);
	}

	public List<Double> getVector() {
		return docVector;
	}

	void getNormalizedVector() {
		double squaredSum = 0;
		if (!docVector.isEmpty()) {

			for (int d = 0; d < docVector.size(); d++) {
				squaredSum += (docVector.get(d) * docVector.get(d));
			}

			squaredSum = Math.sqrt(squaredSum);

			for (int d = 0; d < docVector.size(); d++) {
				unitVector.add(d, docVector.get(d) / squaredSum);
			}
		}

	}

	public List<Double> getUnitVector() {
		return unitVector;
	}

}
