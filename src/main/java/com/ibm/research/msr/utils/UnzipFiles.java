package com.ibm.research.msr.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.aop.ThrowsAdvice;

public class UnzipFiles {

    public static void main(String[] args) {
        String zipFilePath = "/Users/srikanth/Desktop/hybrid-cloud/workspace/microservice-recommender-api/src/main/output/temp/tst/bias_api.zip";
        
        String destDir = "/Users/srikanth/Desktop/hybrid-cloud/workspace/microservice-recommender-api/src/main/output/temp/tst/";
        
        try {
			unzip(zipFilePath, destDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static void unzip(String zipFilePath, String destDir) throws IOException {
    	byte[] buffer = new byte[1024];
    	
       		
       	//create output directory if not exists
       	File folder = new File(destDir);
       	if(!folder.exists()){
       		folder.mkdir();
       	}
       	ZipInputStream zis = null;
       	FileOutputStream fos = null;
       	
       	try {
	       	//get the zip file content
	       	 zis = new ZipInputStream(new FileInputStream(zipFilePath));
	       	//get the zipped file list entry
	       	ZipEntry ze = zis.getNextEntry();
	       		
	       	while(ze!=null){
	       			
	       	   String fileName = ze.getName();
	              File newFile = new File(destDir + File.separator + fileName);
	                   
	              System.out.println("file unzip : "+ newFile.getAbsoluteFile());
	                   
	               //create all non exists folders
	               //else you will hit FileNotFoundException for compressed folder
	               new File(newFile.getParent()).mkdirs();
	                 
	               fos = new FileOutputStream(newFile);             
	
	               int len;
	               while ((len = zis.read(buffer)) > 0) {
	          		fos.write(buffer, 0, len);
	               }
	           		
	               fos.close();   
	               ze = zis.getNextEntry();
	       	}
       	}catch(Exception e) {
       		System.err.println("Error during file operation. " + e.getMessage());
       	}finally {
       		if (zis != null) {
		        zis.closeEntry();
		       	zis.close();
       		}
       		if (fos != null)
       			fos.close();	  
       	}
       		
       	System.out.println("Done");
       		
      }    

}