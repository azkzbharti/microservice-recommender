# microservice-recommender

MicroService recommender supports the end-to-end pipeline for recommending parititions using API usage information and Artifact Centric approach, both mined using static analysis of the monolith

## Overview

This repository contains the microservice recommender, which needs to be executed on the srouce code of the monolith. It used two approaches. as explained below.

![ Approch using Static Analysis Information](./src/main/resources/ui/img/method.png)

It generates a UI folder as an output. One can run a simple http server to host the UI and see the partitions of the monolith. 

Current version supports  static data extraction on source code of monolith application (.java files). Further, it requires to access the MAVEN repository and hence from wherever it is executed (server / VM ), it needs to access the web. 

## What to run ?

There is a main driver `MSRLauncher` which needs to be executed. It takes in three parameters

* `source type` - to indicate whether we are parsing source code (.java files ) or binary. Accepted values ares `src` and `bin`. Currently we support only source code. 

* `folder path` - under which the source code of the monolith is present. If your source code is any git repository, then clone the repository and pass the absolute path to the repository in the file system

* `output folder path` - folder under which all the meta files will be generated. 

* `alogo to exectue` - alogorithm to exectue. Provide `all` to run both the algorithms. 

## Steps to execute the microservice recommender

### Running in Eclipse

* Clone this repository. `git clone git@github.ibm.com:app-modernization/microservice-recommender.git`

* Import this project in Eclipse using the `File-> Import -> Git -> Projects` From Git. Add your local Git repository and then choose the project

* Once imported, right click on the project and choose the option `Maven -> Update Project`.

* Once the project has been successfully built, run `MSRLauncher` passing the four arguments. For example 
`src /Users/senthil/git/sample.plantsbywebsphere /Users/senthil/output/pbw all`

* Once the programs runs successfully, it should dump the files inside the output folder. 

* Go to the `ui` folder and run a simple httpserver. For example if you have python3 installed you can run this commoand to start the http server - `pythom -m http.server 8081` and then access the generated report at `http://localhost:8081`

### Running from Command Line

* Clone this repository. `git clone git@github.ibm.com:app-modernization/microservice-recommender.git`

* Go to the folder `microservice-recommender`. 

* Use `Maven` to build. `mvn clean package`. 

* Once it is built sucsessfully, you should see a `target` folder created with the jar inside it

* Go to the folder `target`

* Run the command `java -DMSR_HOME=/Users/senthil/eclipse-workspace/microservice-recommender/src/main/resources -cp microservice-recommender-0.0.1-SNAPSHOT.jar com.ibm.research.msr.driver.MSRLauncher src /Users/senthil/git/sample.plantsbywebsphere /Users/senthil/app-mod-test/plantsby-output all`. Please change the paths appropriately. 

* Once the programs runs successfully, it should dump the files inside the output folder. 

* Go to the `ui` folder and run a simple httpserver. For example if you have python3 installed you can run this commoand to start the http server - `pythom -m http.server 8081` and then access the generated report at `http://localhost:8081`

