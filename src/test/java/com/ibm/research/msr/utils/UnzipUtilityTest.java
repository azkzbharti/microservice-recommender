package com.ibm.research.msr.utils;

import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UnzipUtility.class})
public class UnzipUtilityTest {
	
	@Test
	public void unzipTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		
		FileInputStream fileInputStream = mock(FileInputStream.class);
		whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);
		
		ZipInputStream zipInputStream = mock(ZipInputStream.class);
		whenNew(ZipInputStream.class).withAnyArguments().thenReturn(zipInputStream);
		
		ZipEntry zipEntry = mock(ZipEntry.class);
		
		when(zipInputStream.getNextEntry()).thenReturn(zipEntry).thenReturn(null);
		when(zipEntry.isDirectory()).thenReturn(true);
		
		when(file.mkdir()).thenReturn(true);
		
		doNothing().when(zipInputStream).closeEntry();
		doNothing().when(zipInputStream).close();
		
		UnzipUtility.unzip("zipFilePath", "destDirectory");
	}
	
	@Test
	public void unzipWithFileExtractTest() throws Exception {
		File file = mock(File.class);
		whenNew(File.class).withAnyArguments().thenReturn(file);
		
		FileInputStream fileInputStream = mock(FileInputStream.class);
		whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);
		
		ZipInputStream zipInputStream = mock(ZipInputStream.class);
		whenNew(ZipInputStream.class).withAnyArguments().thenReturn(zipInputStream);
		
		ZipEntry zipEntry = mock(ZipEntry.class);
		when(zipInputStream.getNextEntry()).thenReturn(zipEntry).thenReturn(null);
		when(zipEntry.isDirectory()).thenReturn(false);
		
		FileOutputStream fileOutputStream = mock(FileOutputStream.class);
		whenNew(FileOutputStream.class).withAnyArguments().thenReturn(fileOutputStream);
		
		BufferedOutputStream bufferedOutputStream = mock(BufferedOutputStream.class);
		whenNew(BufferedOutputStream.class).withAnyArguments().thenReturn(bufferedOutputStream);
		
		when(zipInputStream.read(Mockito.any())).thenReturn(1, -1);
		
		doNothing().when(bufferedOutputStream).write(Mockito.anyInt());
		doNothing().when(bufferedOutputStream).close();
		
		doNothing().when(zipInputStream).closeEntry();
		doNothing().when(zipInputStream).close();
		
		UnzipUtility.unzip("zipFilePath", "destDirectory");
	}
}
