package com.ibm.research.msr.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ReadJarMap.class})
public class ReadJarMapTest {
	
	@Test
	public void createJARCategoryMapFileNotFoundExceptionTest() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		when(bufferedReader.readLine()).thenThrow(IOException.class);
		doNothing().when(bufferedReader).close();
		
		ReadJarMap readJarMap = new ReadJarMap();
		readJarMap.createJARCategoryMap("csv");
		
		assertTrue(readJarMap.getLibCatMap().isEmpty());
	}
	
	@Test
	public void createJARCategoryMapTest() throws Exception {
		FileReader fileReader = mock(FileReader.class);
		whenNew(FileReader.class).withAnyArguments().thenReturn(fileReader);
		
		BufferedReader bufferedReader = mock(BufferedReader.class);
		whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);
		when(bufferedReader.readLine()).thenReturn("lib/jar,lib/jar").thenReturn("lib/jar,lib/jar")
				.thenReturn("lib/jar,lib/jar").thenReturn(null);
		doNothing().when(bufferedReader).close();
		
		ReadJarMap readJarMap = new ReadJarMap();
		readJarMap.createJARCategoryMap("csv");
		
		assertEquals("jar", readJarMap.getLibCatMap().get("lib/jar"));
	}
}
