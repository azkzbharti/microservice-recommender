/*******************************************************************************
* Licensed Materials - Property of IBM
* (c) Copyright IBM Corporation 2020. All Rights Reserved.
*
* Note to U.S. Government Users Restricted Rights:
* Use, duplication or disclosure restricted by GSA ADP Schedule
* Contract with IBM Corp.
*******************************************************************************/
package com.ibm.research.msr.api;
 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
 
@Configuration
//@EnableSwagger
@EnableSwagger2
public class SwaggerConfig {
	
	
	
	@Bean
	public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.any())              
          .paths(PathSelectors.regex("/msr/.*"))                          
          .build();                                           
    }
	
	  /*
	   private SpringSwaggerConfig springSwaggerConfig;
	    @Autowired
	   public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
	      this.springSwaggerConfig = springSwaggerConfig;
	   }

	   @Bean //Don't forget the @Bean annotation
	   public SwaggerSpringMvcPlugin customImplementation(){
	      return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
	            .apiInfo(apiInfo())
	            .includePatterns("/msr/.*")
	            .apiVersion("1.0");
	   } */
	   
//	   @Bean
//	   public Docket api1() {
//
//	   // here tags is optional, it just adds a description in the UI
//	   // by default description is class name, so if you use same tag using 
//	   // `@Api` on different classes it will pick one of the class name as 
//	   // description, so better define your own description for them
//	   return new Docket(DocumentationType.SWAGGER_2)
//	       
//	       .select()
//	       .apis(RequestHandlerSelectors.basePackage("com.github"))
//	       .build();
//	   }
	   
	   /* private ApiInfo apiInfo() {
	      ApiInfo apiInfo = new ApiInfo(
	              "Microservices Recommender API",
	              "API for MSR capabilities",
	              "https://aichallenges.sl.cloud9.ibm.com/challenges/XXX",
	              "srikanth.tamilselvam@in.ibm.com",
	              "IBM Internal Usage",
	              "https://aichallenges.sl.cloud9.ibm.com/challenges/XXX"
	        );
	      return apiInfo;
	    }*/
	   
	   private ApiInfo apiInfo() {
		    return new ApiInfoBuilder()
		        .title("Candidate Microservices  API")
		        .description("API for CMA capabilities"+
			              "https://aichallenges.sl.cloud9.ibm.com/challenges/XXX")
		        .termsOfServiceUrl("https://aichallenges.sl.cloud9.ibm.com/challenges/XXX")
		        .contact(new Contact("srikanth.tamilselvam@in.ibm.com", "", ""))
		        .license("IBM Internal Usage")
		        .licenseUrl("https://aichallenges.sl.cloud9.ibm.com/challenges/XXX")
		        .version("2.0")
		        .build();
		  }
}
