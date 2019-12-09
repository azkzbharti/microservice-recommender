
package com.ibm.research.msr.extraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.ibm.research.msr.utils.DocumentParserUtil;


/// TODO: XXX (1) order getters (2) check unused variables

/**
 * 
 * @author ShreyaKhare
 *
 */
public class Document {

	@Expose private String fileName;
	@Expose private String packageName;
	@Expose private List<String> tokens;
	// TODO: remove?
	private File file;
	
	/**
	 * @return the importapis
	 */
	public List<String> getImportapis() {
		return importapis;
	}

	/**
	 * @param importapis the importapis to set
	 */
	public void setImportapis(List<String> importapis) {
		this.importapis = importapis;
	}


	/** contains list of jar files */

	@Expose private List<String> importapis;
	/** token to count map*/
	private Map<String, Integer> tokenCountMap = new HashMap<String, Integer>();
	
	private List<Double> docVector = new ArrayList<>();
	private List<Double> docVectorTF = new ArrayList<>();
	private List<Double> unitVector = new ArrayList<>();
	

	
	// TODO: change file to file path 
	public Document(File file,DocumentParserUtil dpu) throws IOException, Exception {
		this.file = file;
		fileName = file.getName();
		dpu.processFile(this);
	}
	
	public Document(String filepath, String filename, List<Double> docvector, List<String> tokens,String packageName) {
		this.file = new File(filepath);
		this.fileName = filename;
		this.docVector = docvector;
		this.tokens = tokens;
		Map<String, Integer> importCountMap = new HashMap<String, Integer>();
		for (String tok:this.tokens) {
		if (!importCountMap.containsKey(tok)) {
			importCountMap.put(tok, 1);
		} else {
			importCountMap.put(tok, importCountMap.get(tok) + 1);
		}
		this.tokenCountMap=importCountMap;
		this.packageName = packageName;
	}
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
	
	public String getUniqueName() {
		if(this.fileName.endsWith(".java")){
			return this.packageName+"."+this.fileName.replace(".java", "");
		}
		else 
			return this.packageName+"."+this.fileName.replace(".class", "");
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
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
		Document other = (Document) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.getAbsolutePath().equals(other.file.getAbsolutePath()))
			return false;
		return true;
	}

	
	public List<Double> getUnitVector() {
		return unitVector;
	}
	
	

	

}
