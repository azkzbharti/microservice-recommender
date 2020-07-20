package com.ibm.research.msr.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;

import com.ibm.research.msr.clustering.Affinity;
import com.ibm.research.msr.utils.Constants;
import com.ibm.research.msr.utils.Util;




public class APIUtilities {
	private static String getProjectName(String gitURL) {

		String[] gitUrlParts = gitURL.split("/");
		String projectName = gitUrlParts[gitUrlParts.length - 1];

		return projectName;

	}
	
	public static String getCreatingSchemaPythonFile() {
		String MSR_HOME = System.getProperty("MSR_HOME");
		return MSR_HOME + File.separator + "python" + File.separator + "clustering" + File.separator +"create_schema.py ";
	}
	
	public static String getCommunityClusterFile() {
		String MSR_HOME = System.getProperty("MSR_HOME");
		return MSR_HOME + File.separator + "python" + File.separator + "clustering" + File.separator +"community_cluster.py ";
	}
	
	public static String getFilterFile() {
		String MSR_HOME = System.getProperty("MSR_HOME");
		return MSR_HOME + File.separator + "python" + File.separator + "clustering" + File.separator +"filter.txt ";
	}

	public static String getDataAnalysisAPI() {
		 return Util.getProperty("DATA_API_PREFIX");
	}

	public static String sendPost(String url, String req, String contentType) throws Exception {
		String res = null;
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(req));
        post.setHeader("Content-type", "application/json");
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {
           res = EntityUtils.toString(response.getEntity());
        }
        
        return res;
    }
	
	public static String sendAyncPost(String url, String req, String contentType) throws Exception {
		String res = null;
        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		try {
		    // Start the client
		    httpclient.start();
		    final HttpPost post = new HttpPost(url);
		    post.setEntity(new StringEntity(req));
	        post.setHeader("Content-type", contentType);
		    httpclient.execute(post, new FutureCallback<HttpResponse>() {

		        public void completed(final HttpResponse response2) {
		        	System.out.println("After complete");
		            System.out.println(post.getRequestLine() + "->" + response2.getStatusLine());
		        }

		        public void failed(final Exception ex) {
		        	System.out.println("After failed");
		            System.out.println(post.getRequestLine() + "->" + ex);
		        }

		        public void cancelled() {
		        	System.out.println("After cancelled");
		            System.out.println(post.getRequestLine() + " cancelled");
		        }

		    });
		    System.out.println("After execute");
		} finally {
		    httpclient.close();
		}
        return res;
    }
	
	public static void main(String atg[]) throws InterruptedException, ExecutionException, IOException {
		
		String url = "http://appmode.sl.cloud9.ibm.com:8084/api/static/apps";
		String req = "{\"src\":\"/import/cma/account1/daytrader/daytrader-ee7.ear\", \"upi\":\"5e99787879cc77058ee95496\"}";
		String contentType = "application/json";
		
		Runnable r = new Runnable() {
			public void run() {
				System.out.println("start of run");
				try {
					sendPost(url, req, contentType);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
 
		Thread t = new Thread(r);
		// Lets run Thread in background..
		// Sometimes you need to run thread in background for your Timer application..
		t.start(); // starts thread in background..
		// t.run(); // is going to execute the code in the thread's run method on the current thread..
		System.out.println("Main() Program Exited...\n");
		

		
//		CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
//		try {
//		    // Start the client
//		    httpclient.start();
//		    final CountDownLatch latch1 = new CountDownLatch(1);
//		    final HttpPost post = new HttpPost(url);
//		    post.setEntity(new StringEntity(req));
//	        post.setHeader("Content-type", contentType);
//		    httpclient.execute(post, new FutureCallback<HttpResponse>() {
//
//		        public void completed(final HttpResponse response2) {
//		        	latch1.countDown();
//		        	System.out.println("After complete");
//		            System.out.println(post.getRequestLine() + "->" + response2.getStatusLine());
//		        }
//
//		        public void failed(final Exception ex) {
//		        	latch1.countDown();
//		        	System.out.println("After failed");
//		            System.out.println(post.getRequestLine() + "->" + ex);
//		        }
//
//		        public void cancelled() {
//		        	latch1.countDown();
//		        	System.out.println("After cancelled");
//		            System.out.println(post.getRequestLine() + " cancelled");
//		        }
//
//		    });
//		    latch1.await();
//		    System.out.println("After execute");
//		} finally {
//		    httpclient.close();
//		}
		
		System.out.println("Done");
		
	}
	
	public static void runAffinity(String rootPath, String outputJSONFile, String tempFolder, String type) {

		// check for type and support getting this info from binary
		ArrayList<String> javaFiles = getJavaFileNames(rootPath, type);

		Object[] gfg = javaFiles.toArray();
		String[] str = Arrays.copyOf(gfg, gfg.length, String[].class);

		Affinity affinity = new Affinity(str, tempFolder + File.separator + "cluster-affinity.properties",
				outputJSONFile);
		affinity.runClustering();

	}
	
	public static void runTrigger(String rootPath, String originalGraph, String tempFolder, String interclass, String type, String projectPath, String visPath, String serviceFilePath, String userEditGraph) {
		//Calling user_edit.py file
		String seeds_file = projectPath + File.separator + "temp" + File.separator + "seeds.txt";
		
		String editSeedFile = tempFolder + File.separator + "edit_seed.txt";
		String editInfoFile = tempFolder + File.separator + "edit.json" ;
		
		String MSR_HOME = System.getProperty("MSR_HOME");
		String userEditPython = MSR_HOME + File.separator + "python" + File.separator + "clustering" + File.separator +"user_edits.py ";
		
		String cmd = Util.getProperty("PYTHON_HOME") + " " + userEditPython + " --originalFile " + originalGraph + " --editFile " + userEditGraph + " --seedFile "+seeds_file + " --editSeed "+editSeedFile + " --editInfo "+editInfoFile;
		try {
			 System.out.println("Command:"+cmd);
			Runtime rt = Runtime.getRuntime();
			// generate the clusters
			Process proc = rt.exec(cmd);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Calling community_cluster.py file
		String communityClusterFile = APIUtilities.getCommunityClusterFile();
		String filterFile = APIUtilities.getFilterFile();
		String graphInputForSeed = tempFolder + File.separator + "for_seeded";
		
		String cmd2 = Util.getProperty("PYTHON_HOME") + " " + communityClusterFile + " --outPutFilePath " + userEditGraph + " --inPutFilePath " + graphInputForSeed + " --seed_file "+editSeedFile + " --visFilePath "+visPath + " --tempFilePath "+interclass  + " --filterFilePath "+filterFile  + " --serviceEntry "+serviceFilePath + " --editInfo "+editInfoFile;
		
		try {
			 System.out.println(cmd2);
			Runtime rt = Runtime.getRuntime();
			// generate the clusters
			Process proc = rt.exec(cmd2);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Calling edit_enforce.py
		String editedOutput = projectPath + File.separator + "ui" + File.separator + "data" + File.separator + "graph_user_edit.json";
		
		String editEnforcePython = MSR_HOME + File.separator + "python" + File.separator + "clustering" + File.separator +"edit_enforce.py";
		String cmd3 = Util.getProperty("PYTHON_HOME") + " " + editEnforcePython + " --graphFile " + userEditGraph + " --editInfo "+ editInfoFile +  " --outFile "+ editedOutput;
		try {
			 System.out.println("Command:"+cmd3);
			Runtime rt = Runtime.getRuntime();
			// generate the clusters
			Process proc = rt.exec(cmd3);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void runCommunity(String rootPath, String outputJSONFile, String tempFolder, String interclass, String type, String projectPath, String visPath, String transactionFilePath, String serviceFilePath) {
		
		String schemaPythonFile = APIUtilities.getCreatingSchemaPythonFile();
		String filterFile = APIUtilities.getFilterFile();
		String graphInputForSeed = tempFolder + File.separator + "for_seeded";
		
		String cmd = Util.getProperty("PYTHON_HOME") + " " + schemaPythonFile + " --inPutFilePath " + interclass
				+ " --outPutFilePath " + outputJSONFile + " --graphInputForSeed " + graphInputForSeed + " --transactionName " + transactionFilePath  + " --filterFilePath "+filterFile;
		try {
			Runtime rt = Runtime.getRuntime();

			// generate the clusters
			Process proc = rt.exec(cmd);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		String communityClusterFile = APIUtilities.getCommunityClusterFile();
		
		String seeds_file = projectPath + File.separator + "temp" + File.separator + "seeds.txt";
		
		
		String cmd2 = Util.getProperty("PYTHON_HOME") + " " + communityClusterFile + " --outPutFilePath " + outputJSONFile + " --inPutFilePath " + graphInputForSeed + " --seed_file "+seeds_file + " --visFilePath "+visPath + " --tempFilePath "+interclass  + " --filterFilePath "+filterFile  + " --serviceEntry "+serviceFilePath;
		try {
			 System.out.println(cmd2);
			Runtime rt = Runtime.getRuntime();
			// generate the clusters
			Process proc = rt.exec(cmd2);
			InputStream stderr = proc.getErrorStream();
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			System.out.println("<ERROR>");
			while ((line = br.readLine()) != null)
				System.out.println(line);
			System.out.println("</ERROR>");
			int exitVal = proc.waitFor();
			System.out.println("Process exitValue: " + exitVal);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	private static ArrayList<String> getJavaFileNames(String rootPath, String type) {

		Collection<File> javaFiles = null;
		String endsWith = "";
		if (type.equals(Constants.TYPE_SRC)) {
			javaFiles = FileUtils.listFiles(new File(rootPath), new String[] { "java" }, true);
			endsWith = ".java";

		} else {
			javaFiles = FileUtils.listFiles(new File(rootPath), new String[] { "class" }, true);
			endsWith = ".class";
		}

		ArrayList<String> javaFileNames = new ArrayList<String>();

		if (!javaFiles.isEmpty()) {

			for (File f : javaFiles) {
				String name = f.getName();
				if (name.endsWith(endsWith)) {
					name = name.substring(0, name.indexOf("."));
					javaFileNames.add(name);
				}
			}

		}

		return javaFileNames;

	}
	
	public static boolean deleteDirectory(File directoryToBeDeleted, String exceptionFile) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	        	if(!file.getName().equalsIgnoreCase(exceptionFile)) {
	        		deleteDirectory(file, null);
	        	}
	        }
	    }
	    return directoryToBeDeleted.delete();
	}


	

}