package driver;

import java.io.IOException;

import com.ibm.research.msr.clustering.Clustering;
import com.ibm.research.msr.clustering.DBSCAN;
import com.ibm.research.msr.clustering.KMeans;
import com.ibm.research.msr.clustering.Naive;
import com.ibm.research.msr.clustering.NaiveTFIDF;
import com.ibm.research.msr.extraction.AnalyzeApp;
import com.ibm.research.msr.utils.ReadJarMap;

import weka.clusterers.DBScan;
import weka.gui.simplecli.Exit;

/**
 * 
 *
 */
public class App {
	
	public static void main(String[] args) throws IOException, Exception {
		try {
		ReadJarMap.createJARCategoryMap();
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error while creating jar map");
		}
		
//		String appPath="/Users/shreya/git/digdeep";

		String appPath = args[0];
		String algorithm = args[1];//"KMeans";
		AnalyzeApp analyzer = new AnalyzeApp(appPath);
		
//		analyzer.computeMeasure();
//		analyzer.saveMeasure(null);

		Clustering oc = null;
		System.out.println(algorithm);
		switch(algorithm) {
			case "kMeans": {
				int k = Integer.parseInt(args[2]);
				oc = new KMeans(analyzer.getListOfDocuments(),analyzer.getMeasurePath(), k);	
				break;
			}
			case "DBSCAN":{
				double  epsilon = Double.parseDouble(args[2]);//0.0003 ;
				int neighbours = Integer.parseInt(args[3]);//args[3];
				oc =  new DBSCAN(analyzer.getListOfDocuments(),analyzer.getMeasurePath(),epsilon,neighbours);
				break;
			}
			case "NAIVETFIDF":{
				 String meaureType= args[2];  //"cosine";//args[2]; 
				 oc = new NaiveTFIDF(analyzer.getListOfDocuments(),meaureType);
//				 oc = new NaiveTFIDF(analyzer.getListOfDocuments(),"cosine");
				 algorithm=algorithm+meaureType;
				 break;
			}
			case "NAIVE":{
				 oc = new Naive(analyzer.getListOfDocuments());
				 break;
			}
			default:{
				System.out.println("No algorithm, exiting");
				System.exit(0);
			}
			
		}
		
		oc.runClustering(); 
		oc.getClusters();
		
		String filename="src/main/resources/cluster.html";    // TODO : Make argument
		filename=filename.replaceAll(".html", algorithm+".html");	
		oc.savecLusterJSON(filename);

	}
}
