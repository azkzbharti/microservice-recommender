package com.ibm.research.msr.jarlist;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ JarApiList.class, FileUtils.class })
public class JarApiListTest {

	JarApiList jar = new JarApiList();

	@Test
	public void findTest() throws Exception {

		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		when(file.getName()).thenReturn("jar");
		
		JarFile jarFile = mock(JarFile.class);
		whenNew(JarFile.class).withAnyArguments().thenReturn(jarFile);
		
		Collection<File> collection = new ArrayList<>();
		collection.add(file);

		mockStatic(FileUtils.class);

		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);
		
		JarEntry jarEntry = mock(JarEntry.class);
		
		Enumeration<JarEntry> enumeration = Collections.enumeration(Arrays.asList(jarEntry));
		
		when(jarFile.entries()).thenReturn(enumeration);
		when(jarEntry.getName()).thenReturn(".class");
		
		jar.find("appath");
	}

}
