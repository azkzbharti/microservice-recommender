package com.ibm.research.msr.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ibm.research.appmod.pa.jarlist.JarApiList;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Util.class, FileUtils.class })
public class UtilTest {

	@BeforeClass
	public static void setUp() {
		System.setProperty("MSR_HOME", "\\msr\\home");
	}

	@Test
	public void getMSRBaseDirTest() {
		String msrBaseDir = Util.getMSRBaseDir();
		assertEquals("\\msr\\home", msrBaseDir);
	}

	@Test
	public void getPropertyTest() {
		System.setProperty("key", "value");
		assertEquals("value", Util.getProperty("key"));
	}

	@Test
	public void getAffinityAlgoPythonFileTest() {
		assertEquals("\\msr\\home\\python\\affinity_algo.py ", Util.getAffinityAlgoPythonFile());
	}

	@Test
	public void getCohesionPythonFileTest() {
		assertEquals("\\msr\\home\\python\\recluster_driver.py ", Util.getCohesionPythonFile());
	}

	@Test
	public void getStopWordsFileTest() {
		assertEquals("\\msr\\home\\stop_words.txt", Util.getStopWordsFile());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void dumpAPIInfoNoFilesTest() {
		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(Collections.EMPTY_LIST);

		assertFalse(Util.dumpAPIInfo("folderPath", "outputPath"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void dumpAPIInfoFilesTest() throws Exception {
		File file = new File("file");
		Collection<File> collection = new ArrayList<>();
		collection.add(file);
		JarApiList jarApiList = mock(JarApiList.class);
		doNothing().when(jarApiList).dumpAPIInfoForJars(Mockito.any(), Mockito.any());
		whenNew(JarApiList.class).withAnyArguments().thenReturn(jarApiList);

		mockStatic(FileUtils.class);
		when(FileUtils.listFiles(Mockito.any(File.class), Mockito.any(String[].class), Mockito.anyBoolean()))
				.thenReturn(collection);

		assertTrue(Util.dumpAPIInfo("folderPath", "outputPath"));
	}
}
