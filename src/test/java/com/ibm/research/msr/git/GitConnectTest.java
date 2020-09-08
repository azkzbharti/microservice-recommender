package com.ibm.research.msr.git;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GitConnect.class, Git.class })
public class GitConnectTest {

	@Test
	public void connectToPublicSSHWithoutBranchTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setTransportConfigCallback(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setURI(Mockito.anyString())).thenReturn(cloneCommand);

		Git git = mock(Git.class);

		when(cloneCommand.call()).thenReturn(git);

		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);

		GitConnect.connectToPublicSSH("dirPath", "url", null);
	}

	@Test
	public void connectToPublicSSHBranchTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setTransportConfigCallback(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setURI(Mockito.anyString())).thenReturn(cloneCommand);
		when(cloneCommand.setBranchesToClone(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setBranch(Mockito.anyString())).thenReturn(cloneCommand);

		Git git = mock(Git.class);

		when(cloneCommand.call()).thenReturn(git);

		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);

		GitConnect.connectToPublicSSH("dirPath", "url", "branch");
	}

	@Test
	public void connectUsingTokenTest() throws IOException, GitAPIException {
		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		
		GitConnect.connectUsingToken("dirPath", "url", "token", null);
	}
	
	@Test
	public void connectUsingTokenWithBranchTest() throws IOException, GitAPIException {
		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		
		when(cloneCommand.setBranchesToClone(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setBranch(Mockito.anyString())).thenReturn(cloneCommand);
		
		GitConnect.connectUsingToken("dirPath", "url", "token", "branch");
	}
	
	@Test
	public void connectToPublicHttpTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setURI(Mockito.anyString())).thenReturn(cloneCommand);

		Git git = mock(Git.class);

		when(cloneCommand.call()).thenReturn(git);

		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);
		
		GitConnect.connectToPublicHttp("dirPath", "url", null);
	}
	
	@Test
	public void connectToPublicHttpWithBranchTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);

		mockStatic(Git.class);

		CloneCommand cloneCommand = mock(CloneCommand.class);
		when(Git.cloneRepository()).thenReturn(cloneCommand);
		when(cloneCommand.setDirectory(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setURI(Mockito.anyString())).thenReturn(cloneCommand);
		when(cloneCommand.setBranchesToClone(Mockito.any())).thenReturn(cloneCommand);
		when(cloneCommand.setBranch(Mockito.anyString())).thenReturn(cloneCommand);

		Git git = mock(Git.class);

		when(cloneCommand.call()).thenReturn(git);

		Repository repository = mock(Repository.class);
		when(git.getRepository()).thenReturn(repository);
		
		GitConnect.connectToPublicHttp("dirPath", "url", "branch");
	}

}
