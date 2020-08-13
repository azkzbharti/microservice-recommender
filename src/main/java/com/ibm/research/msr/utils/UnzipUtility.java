package com.ibm.research.msr.utils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = null;
        try {
	        zipIn 	= new ZipInputStream(new FileInputStream(zipFilePath));
	        ZipEntry entry = zipIn.getNextEntry();
	        // iterates over entries in the zip file
	        while (entry != null) {
	            String filePath = destDirectory + File.separator + entry.getName();
	//            System.out.println(filePath);
	            if (!entry.isDirectory()) {
	                // if the entry is a file, extracts it
	                extractFile(zipIn, filePath);
	            } else {
	                // if the entry is a directory, make the directory
	                File dir = new File(filePath);
	                dir.mkdir();
	            }
	            zipIn.closeEntry();
	            entry = zipIn.getNextEntry();
	        }
        }catch(Exception e) {
        	System.err.println("Error in reading input. " + e.getMessage());
        }finally {
        	if (zipIn != null)
        		zipIn.close();
        }
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    	BufferedOutputStream bos = null;
        try{
        	bos = new BufferedOutputStream(new FileOutputStream(filePath));
	        
	        byte[] bytesIn = new byte[BUFFER_SIZE];
	        int read = 0;
	        while ((read = zipIn.read(bytesIn)) != -1) {
	            bos.write(bytesIn, 0, read);
	        }
	        
        }catch(Exception e) {
        	System.err.println("Error extracting file. " + e.getMessage());
        }finally {
        	if (bos != null)
        		bos.close();
        }
    }
    
    public void checkWarAndExtract(String earChildrenPath) throws IOException {
    	File dir = new File(earChildrenPath);
    	
    	GenericExtFilter filter = new GenericExtFilter(".war");

		
		if(dir.isDirectory()==false){
			System.out.println("Directory does not exists : " + earChildrenPath);
			return;
		}
		
		// list out all the file name and filter by the extension
		String[] list = dir.list(filter);

		for (String file : list) {
			String temp = new StringBuffer(earChildrenPath).append(File.separator)
					.append(file).toString();
			System.out.println("file : " + temp);
			unzip(temp, earChildrenPath);
		}
    }
    
    public static void main(String sa[]) {
    	try {
			new UnzipUtility().checkWarAndExtract("/Users/srikanth/Desktop/hybrid-cloud/workspace/microservice-recommender-api/src/main/output/temp/apps/source/5dde159bff0b8b4150ff2d1a");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
 // inner class, generic extension filter
 	public class GenericExtFilter implements FilenameFilter {

 		private String ext;

 		public GenericExtFilter(String ext) {
 			this.ext = ext;
 		}

 		public boolean accept(File dir, String name) {
 			return (name.endsWith(ext));
 		}
 	}
    
}