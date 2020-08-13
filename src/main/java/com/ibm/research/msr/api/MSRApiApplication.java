/*******************************************************************************
 * Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;

import java.util.Collections;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;

@SpringBootApplication
public class MSRApiApplication {
	
	public static void main(String[] args) {
		System.out.println("Inside MSRApiApplication -- "+args);
		SpringApplication app = new SpringApplication(MSRApiApplication.class);

		if (null != args && args.length > 0) {
			System.out.println(
					"Started with provided port :"+args[0]);
			app.setDefaultProperties(Collections.singletonMap("server.port", args[0]));
			app.run(args);

		} else {

			app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
			app.run(args);
			System.out.println(
					"Usage: One argument is expected. pass the port number.Started with default port 8081");
		}

	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
	  System.out.println("Inside MSRApiApplication.multipartConfigElement -- ");
	  MultipartConfigFactory factory = new MultipartConfigFactory();
	  /** changed for springboot Upgradde **/
	  DataSize ds = DataSize.ofMegabytes(1000);
	  factory.setMaxFileSize(ds);
	  factory.setMaxRequestSize(ds);
	  MultipartConfigElement mce  = factory.createMultipartConfig();
	  return mce;
	}
	 
}