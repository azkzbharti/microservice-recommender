package com.ibm.research.msr.utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
 
/**
 * This utility extracts files and directories of a standard zip file to
 * a destination directory.
 * @author www.codejava.net
 *
 */
public class UnzipUtility {
    /**
     * Size of the buffer to read/write data
     */
    private static final int BUFFER_SIZE = 4096;
    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     * @param zipFilePath
     * @param destDirectory
     * @throws IOException
     */
    public static void unzip(String zipFilePath, String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = null;
		try {
			zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
		} catch (FileNotFoundException e) {
			System.out.println(" Unable to read zipFile");
			e.printStackTrace();
			return;
		}
        ZipEntry entry;
		try {
			entry = zipIn.getNextEntry();
		} catch (IOException e) {
			System.out.println(" No entry found in zip ");
			e.printStackTrace();
			return;
		}
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                try {
					extractFile(zipIn, filePath);
				} catch (IOException e) {
					System.out.println(" Unable to extract file" + filePath);
					e.printStackTrace();
				}
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            try {
				zipIn.closeEntry();
			} catch (IOException e) {
				e.printStackTrace();
			}
            try {
				entry = zipIn.getNextEntry();
			} catch (IOException e) {
				System.out.println(" No entry found in zip ");
				e.printStackTrace();
				return;
			}
        }
        try {
			zipIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    	System.out.println("Extracting File: " + filePath);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
    
    
}