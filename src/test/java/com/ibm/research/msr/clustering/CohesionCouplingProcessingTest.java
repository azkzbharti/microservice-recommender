package com.ibm.research.msr.clustering;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CohesionCouplingProcessing.class, Runtime.class})
public class CohesionCouplingProcessingTest {

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
		
		CohesionCouplingProcessing cohesionCouplingProcessing = new CohesionCouplingProcessing("clusterAllJSON",
				"usageJSON", "outputLocation");
		
		cohesionCouplingProcessing.runClustering();
	}

}
