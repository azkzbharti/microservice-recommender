package com.ibm.research.msr.clustering;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Affinity.class, Runtime.class})
public class AffinityTest {

	Affinity affinity = new Affinity(new String[]{"word1", "word2"}, "propFile", "outputFile");
	
	@Test
	public void runClusteringTest() throws Exception {
		mockStatic(Runtime.class);
		Runtime runtime = mock(Runtime.class);
		Process process = mock(Process.class);
		
		when(Runtime.getRuntime()).thenReturn(runtime);
		
		when(runtime.exec(Mockito.anyString())).thenReturn(process);
		
		InputStream inputStream = mock(InputStream.class);
		when(process.getErrorStream()).thenReturn(inputStream);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		
		Properties properties = mock(Properties.class);
		whenNew(Properties.class).withAnyArguments().thenReturn(properties);
		
		Set<Object> set = new HashSet<>();
		set.add("key1");
		
		when(properties.keySet()).thenReturn(set);
		when(properties.getProperty(Mockito.anyString())).thenReturn("value");
		
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		affinity.runClustering();
	}

}
