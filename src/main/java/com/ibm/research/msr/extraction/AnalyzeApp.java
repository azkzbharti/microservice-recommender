/**
 * 
 */
package com.ibm.research.msr.extraction;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * @author ShreyaKhare
 *
 */
public class AnalyzeApp {

	

	private String appPath;
	private List<File> files;
	private List<Document> listOfDocuments = new ArrayList<Document>();
	private Set<String> vocab = new LinkedHashSet<String>();
	private String measurePath="src/main/resources/measure.csv";

	/**
	 * 
	 * @param appPath: test use-> /Users/shreya/git/digdeep or src/main/resources/tf_idf-analy.csv  
	 * @throws IOException
	 * @throws Exception
	 */
	public AnalyzeApp(String appPath) throws IOException, Exception {
		this.appPath = appPath;
		File dir = new File(appPath);
		if(dir.isDirectory()) {
			System.out.println();
			String[] extensions = new String[] { "java"};
			List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
			for (File file : files) {
					Document document = new Document(file);
					if (document.getNumberOfTokens() > 0) {
						listOfDocuments.add(document);
				}
			}
			computeMeasure();
			// TODO: add as an input
			saveMeasure();
		}
		else {
//			"src/main/resources/tf_idf-analy.csv"
			System.out.println("Reading from CSV file");
			read_measure_tf_file(appPath);
			
		}
	}
	
	public void computeMeasure() throws IOException, Exception {
		calculateTotalWords();
		documentToVector();
	}

	/**
	 * calculate total number of words -> vocab size
	 */
	private void calculateTotalWords() {
		listOfDocuments.forEach(d -> vocab.addAll(d.getTokens()));
	}

	// TODO: XXX rename?
	private void documentToVector() throws IOException, Exception {
		int totalDocuments = listOfDocuments.size();
		for (Document document : listOfDocuments) {
			int numberOfWords = document.getNumberOfTokens();
			for (String token : vocab) {
				double tokenFrequency = (double) document.getTokenOccurenceCount(token) / (double) numberOfWords;
				double idf = (double) Math.log(totalDocuments / getNumDocsWithWord(token) + 1);
				double tfIdf = tokenFrequency * idf;
				document.addComponentToVector(tfIdf);
				document.addComponentToTfVector(tokenFrequency);
			}
			document.getNormalizedVector();
		}
	}

	/**
	 * 
	 * @param word
	 * @return number of documents where this word (actually jar name occurs)
	 */
	private int getNumDocsWithWord(String word) {
		int count = 0;
		for (Document document : listOfDocuments) {
			if (document.getTokens().contains(word)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * TF-IDF in a csvfiles
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	public void saveMeasure() throws IOException {
		CSVWriter csvWriter = new CSVWriter(new FileWriter(this.measurePath));
		
		// set header with vocab name
		List<String> header = new ArrayList<>(vocab);
		header.add(0, "docsName");
		header.add(1, "ClassName");
		System.out.println(vocab.size());
		System.out.println(vocab.toString());
		csvWriter.writeNext(header.toArray(new String[0]));
		
		for(Document document : listOfDocuments) {
			String[] line = new String[vocab.size() + 2];
			line[0] = document.getFile().getAbsolutePath();
			line[1] = document.getName();
			for (int i = 2; i < vocab.size() + 2; i++) {
				List<Double> docVector = document.getDocVector();
				double termScore = docVector.get(i - 2);
				String res = Double.toString(termScore);
				line[i] = res;
			}
			csvWriter.writeNext(line);
		}
		csvWriter.close();
	}

	public void read_measure_tf_file(String appPath2) throws IOException {
		// reads from csv file and creates a list of documents
		this.listOfDocuments= new ArrayList<>();
		
		CSVReader reader = new CSVReader(new FileReader(appPath2));
		String[] header = reader.readNext();
		List<String> headers = new ArrayList<>();
		for (int i = 2; i < header.length; i++) {
			headers.add(header[i]);
		}
		String[] line = null;
		while ((line = reader.readNext()) != null) {

			String filepath = line[0];
			String filename = line[1];
	
			List<Double> docVector = new ArrayList<Double>();
			List<String> documentTokens = new ArrayList<String>();
			for (int i = 2; i < line.length; i++) {
				if (line[i] != " ") {
					Double val = Double.parseDouble(line[i]);
					docVector.add(val);
					if (val > 0) {
						documentTokens.add(headers.get(i - 2));
					}
				}
			}
			Document doc = new Document(filepath, filename, docVector, documentTokens);
//			System.out.println(doc.getDocVector().size());
			listOfDocuments.add(doc);
//			System.out.println(listOfDocuments.get(0).getDocVector().size());
//			System.out.println(listOfDocuments.get(0).getName());

		}
	}
	


	public String getMeasurePath() {
		return measurePath;
	}

	public void setMeasurePath(String measurePath) {
		this.measurePath = measurePath;
	}
	public String getAppath() {
		return appPath;
	}

	public void setAppath(String appath) {
		this.appPath = appath;
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public List<Document> getListOfDocuments() {
		return listOfDocuments;
	}

	public void setListOfDocuments(List<Document> listOfDocuments) {
		this.listOfDocuments = listOfDocuments;
	}
	
	

}
