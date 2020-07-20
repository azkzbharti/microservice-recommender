package com.ibm.research.msr.git;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;


public class GitConnect {
	
    public static void connectToPublicSSH(String dirPath, String url, String branch) throws IOException, GitAPIException {
    	TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();
		Git git;
		if(null == branch) {
		git = Git.cloneRepository()
		        .setDirectory(new File(dirPath))
		        .setTransportConfigCallback(transportConfigCallback)
		        .setURI(url)
		        .call();
		}else {
			git = Git.cloneRepository()
			        .setDirectory(new File(dirPath))
			        .setTransportConfigCallback(transportConfigCallback)
			        .setURI(url)
			        .setBranchesToClone(Arrays.asList(branch))
					  .setBranch(branch)
			        .call();
			
		}
		System.out.println("Having repository: " + git.getRepository().getDirectory());
    }
    
    /**
     * This method enables git connect using token . Reference https://www.codeaffine.com/2014/12/09/jgit-authentication/
     * @param dirPath
     * @param url
     * @param branch
     * @throws IOException
     * @throws GitAPIException
     */
    public static void connectUsingToken(String dirPath, String url, String token, String branch) throws IOException, GitAPIException {
    	
    	CloneCommand command = Git.cloneRepository();
		command.setDirectory(new File(dirPath));
		command.setURI(url);
		command.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "token", token));
		
		//set branch
		if(null != branch) {
				command.setBranchesToClone(Arrays.asList(branch)).setBranch(branch);
		}
		
		try {
			command.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
    }
    
    public static void connectToPublicHttp(String dirPath, String url, String branch) throws IOException, GitAPIException {
		Git git;
		if(null == branch) {
		git = Git.cloneRepository()
		        .setDirectory(new File(dirPath))
		        .setURI(url)
		        .call();
		}else {
			git = Git.cloneRepository()
			        .setDirectory(new File(dirPath))
			        .setURI(url)
			        .setBranchesToClone(Arrays.asList(branch))
					  .setBranch(branch)
			        .call();
			
		}
		System.out.println("Having repository: " + git.getRepository().getDirectory());
    }
    
    

    public static void testGitClone(String dirPath, String url) throws IOException, GitAPIException {
    	TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();
		Git git;
		git = Git.cloneRepository()
		        .setDirectory(new File(dirPath))
		        .setTransportConfigCallback(transportConfigCallback)
		        .setURI(url)
		        .call();
		System.out.println("Having repository: " + git.getRepository().getDirectory());
    }
    
    public static void main(String[] args) {
    	
    	 String REMOTE_URL =  "git@github.ibm.com:app-modernization/daytrader.git";
    	REMOTE_URL = "https://github.com/WASdev/sample.plantsbywebsphere.git";
    	String dirPath = "/Users/srikanth/Desktop/hybrid-cloud/temp/git/";
    	try {
	  		GitConnect.testGitClone(dirPath, REMOTE_URL);
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}	
    		
//		final String REMOTE_URL = "git@github.ibm.com:srikanth-tamilselvam/TreeSentimentAggregation.git";
//	    
//		String APPMOD_HOME = System.getProperty("APPMOD_HOME");
//	  	String dirPath = APPMOD_HOME + File.separator + "test";
//	  	
//	  	String privateSSHFileLoc = "/Users/srikanth/.ssh/id_rsa";
//		String passphrase = "passphrase";
//		
//		
//	  	try {
//	  		GitConnect.connectToPublicSSH(dirPath, REMOTE_URL, null);
//		} catch (IOException | GitAPIException e) {
//			e.printStackTrace();
//		}
    }
	  	

}
