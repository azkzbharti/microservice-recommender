package com.ibm.research.msr.tests;

import java.io.File;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/***
 * Test suite around registration.
 * @author srikanth
 *
 */
public class RegistrationTest {

	public static void main(String[] args) {
		testRegistrationUsingToken();
		// TODO Auto-generated method stub

	}
	
	public static void testRegistrationUsingToken() {
		String dirPath = System.getProperty("user.dir");
		dirPath = dirPath + "/" +"tests-output";
        System.out.println("Working Directory = " + dirPath);
        
		String gitURL = "https://github.ibm.com/app-modernization/daytrader.git";
		String token = "";
		
		CloneCommand command = Git.cloneRepository();
		command.setDirectory(new File(dirPath));
		command.setURI(gitURL);
		command.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "token", token));
		try {
			command.call();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
