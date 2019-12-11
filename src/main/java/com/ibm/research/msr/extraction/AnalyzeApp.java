/**
 * 
 */
package com.ibm.research.msr.extraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.DocumentParserUtil;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

/**
 * @author ShreyaKhare
 *
 */
public class AnalyzeApp {

	private String appPath;
	private String outputPath;
	
	public String getOutputPath() {
		return outputPath;
	}


	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	/**
	 * @return the libCatMap
	 */
	public Map<String, String> getLibCatMap() {
		return libCatMap;
	}


	/**
	 * @param libCatMap the libCatMap to set
	 */
	public void setLibCatMap(Map<String, String> libCatMap) {
		this.libCatMap = libCatMap;
	}

	private String appType;
	private List<File> files;
	private List<Document> listOfDocuments = new ArrayList<Document>();
	private Set<String> vocab = new LinkedHashSet<String>();
	private Map<String, String> libCatMap;

	/**
	 * 
	 * @param map 
	 * @param appPath: test use-> /Users/shreya/git/digdeep or
	 *        src/main/resources/tf_idf-analy.csv
	 * @throws IOException
	 * @throws Exception
	 */
	public AnalyzeApp(String appPath, String appType, String outputPath, Map<String, String> map) throws IOException, Exception {
		this.appPath = appPath;
		this.appType = appType;
		this.outputPath=outputPath;
		this.libCatMap=map;
		DocumentParserUtil docUtil= new DocumentParserUtil(false, map);

		File dir = new File(appPath);
		if (dir.isDirectory()) {
			String[] extensions = null;
			if (this.appType.equals(Constants.SRC))
				extensions = new String[] { "java" };
			else
				extensions = new String[] { "class" };

			List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);
			for (File file : files) {
				Document document = new Document(file,docUtil);
				System.out.println(file);
				System.out.println(document.getTokens());
				if (document.getNumberOfTokens() > 0) {
					listOfDocuments.add(document);
				}
			}
			computeMeasure();
			// TODO: add as an input
					} else {
			
//			System.out.println("Reading from CSV file");
//			read_measure_tf_file(appPath);
//			

		}
	}
	public AnalyzeApp(String documentFile,String measureFile) {
		readDocuments(documentFile);
		try {
			read_measure_tf_file(measureFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void savetoFile(String measureFile,String documentFile) throws IOException {
		serializeDocuments(documentFile);
		saveMeasure(measureFile);
	}
	public void serializeDocuments(String filename) {
    	Gson objGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		String mapToJson = objGson.toJson(listOfDocuments);
		 try {
             Files.write(Paths.get(filename), mapToJson.getBytes(), StandardOpenOption.CREATE);
     } catch (IOException e) {
             e.printStackTrace();
     }
	}
	public void readDocuments(String filename) {
    	Gson objGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
		try {
			this.listOfDocuments = objGson.fromJson(new FileReader(filename), new TypeToken<ArrayList<Document>>(){}.getType());
			System.out.println(listOfDocuments.size());
		} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void saveMeasure(String measurePath) throws IOException {
		CSVWriter csvWriter = new CSVWriter(new FileWriter(measurePath));

		// set header with vocab name
		List<String> header = new ArrayList<>(vocab);
		header.add(0, "docsName");
		header.add(1, "ClassName");
		header.add(2, "PackageName");
		System.out.println(vocab.size());
		System.out.println(vocab.toString());
		csvWriter.writeNext(header.toArray(new String[0]));

		for (Document document : listOfDocuments) {
			String[] line = new String[vocab.size() + 2];
			line[0] = document.getFile().getAbsolutePath();
			line[1] = document.getName();
			line[2] = document.getPackageName();
			for (int i = 3; i < vocab.size() + 2; i++) {
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
		this.listOfDocuments = new ArrayList<>();
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
			String packageName = line[2];
			List<Double> docVector = new ArrayList<Double>();
			List<String> documentTokens = new ArrayList<String>();
			for (int i = 3; i < line.length; i++) {
				if (line[i] != " ") {
					Double val = Double.parseDouble(line[i]);
					docVector.add(val);
					if (val > 0) {
						documentTokens.add(headers.get(i - 2));
					}
				}
			}
			Document doc = new Document(filepath, filename, docVector, documentTokens,packageName);
			listOfDocuments.add(doc);
			
		}
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
