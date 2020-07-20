package com.ibm.research.msr.git;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshTransportConfigCallback implements TransportConfigCallback {
		
	private String privateSSHFileLoc = "/root/.ssh/id_rsa";
	private String passphrase = "passphrase";
	
	public SshTransportConfigCallback() {
		this.privateSSHFileLoc = System.getProperty("SSH_FILE_LOC") != null ? System.getProperty("SSH_FILE_LOC") : privateSSHFileLoc;
		this.passphrase = System.getProperty("PASSPHRASE") != null ? System.getProperty("PASSPHRASE") : passphrase;
		System.out.println(privateSSHFileLoc +" "+passphrase);
	}
	
//	public GitConnect(String loc, String passphrase){
//	if(null != loc && !loc.equals("")) {
//		this.privateSSHFileLoc = loc;
//	}
//	if(null != passphrase && !passphrase.equals("")) {
//		this.passphrase = passphrase;
//	}
//}
	
	  private final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
	        @Override
	        protected void configure(OpenSshConfig.Host hc, Session session) {
	            session.setConfig("StrictHostKeyChecking", "no");
	        }
	        @Override
	        protected JSch createDefaultJSch(FS fs) throws JSchException {
	            JSch jSch = super.createDefaultJSch(fs);
	            jSch.addIdentity(privateSSHFileLoc, passphrase.getBytes());
	            return jSch;
	        }
	    };
	    @Override
	    public void configure(Transport transport) {
	        SshTransport sshTransport = (SshTransport) transport;
	        sshTransport.setSshSessionFactory(sshSessionFactory);
	    }
    
   
    
    public static void main(String[] args) {
		final String REMOTE_URL = "git@github.ibm.com:srikanth-tamilselvam/TreeSentimentAggregation.git";
	    String APPMOD_HOME = System.getProperty("APPMOD_HOME");
	  	String dirPath = APPMOD_HOME + File.separator + "test";
	  	

		TransportConfigCallback transportConfigCallback = new SshTransportConfigCallback();
		Git git;
		try {
			git = Git.cloneRepository()
			        .setDirectory(new File(dirPath))
			        .setTransportConfigCallback(transportConfigCallback)
			        .setURI(REMOTE_URL)
			        .call();
		System.out.println("Having repository: " + git.getRepository().getDirectory());
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 
		
    }
}