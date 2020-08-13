# microservice-recommender-api

This project has dependency with the microservice-evaluator project. The interaction happens over API. Therefore, follow the steps provided below to host microservice-evaluator project before deploying microservice-recommender-api project. 

The port at which microservice-evaluator will be enabled is necessary for microservice-recommender-api project. It will be required to be inserted into the msr.property file covered in step 2



## Set up dependencies
1. Build the Docker image of microservice-evaluator project by following the Docker instructions here:
[https://github.ibm.com/app-modernization/microservices-evaluator/blob/think-demo/README.md]

2. Make sure these repos are checked out in the same workspace as this repo (as peer folders)
```
cd ../
git clone git@github.ibm.com:app-modernization/appmod-common.git
cd appmod-common
git checkout think-demo
cd ../
git clone git@github.ibm.com:app-modernization/microservice-recommender.git
cd microservice-recommender
git checkout think-demo
```

## Building
Make sure you have set up dependencies.
Build all container images
```
export BASE=$PWD
make depend
make build
```
This builds the containers that are listed in the `$BASE/docker-compose.yml`
The relevant Dockerfiles are in `$BASE/docker`.

## Testing

Integration tests are kept in the `test/scripts` folder as executable shell scripts. Here is an example of a test:
This command will run them all:
```
make test
```

To create your own tests, make a copy of `sampletest` and edit the copy as per your needs.


## Running the CMA analysis on an application
Make sure you have built CMA and are in the project root. Then run the following:
```
./scripts/runcma <full_path_to_war_file> <full_path_to_working_dir>
# Example: ./scripts/runcma $PWD/foo.war $PWD/tmp/foo
```

### [TODO: Needs update] Setting up the M2M Application

The following are the sequence of steps to build and start the M2M application. We refer to the current directory as **WORK_DIR**.

1. Begin by checking out the following projects:

```
git clone git@github.ibm.com:app-modernization/appmod-common.git
git checkout think-demo
git clone git@github.ibm.com:app-modernization/microservice-recommender.git
git checkout think-demo
git clone git@github.ibm.com:app-modernization/microservice-recommender-api.git
git checkout think-demo
```

2. Next, generate a copy of the files (python scripts, stop words etc.) and update if needed (no update should be needed typically):

```
mkdir msrresources
cp -r microservice-recommender/src/main/resources/*.txt msrresources #filter resources
cp -R microservice-recommender/src/main/resources/python msrresources #clustering algos
cp -R microservice-recommender/src/main/resources/ui/* msrresources #intermediate viz component
```

#### Docker Setup:

In keeping with [12 Factor](12factor.net) guidelines for cloud native applications, this service uses environment variables to obtain configuration information. Environment variables should be stored in a private file called `.env` which should be placed on the local filesystema and never checked into an SCM like GitHub.  A sample of what should be in this file can be found in `dot-env-example` in this repo.

You should copy this to `.env` and change the values of the environment variables for your deployment:
```sh
cp microservice-recommender-api/dot-env-example .env
vi .env
```

This is what the sample looks like:
```
a) PYTHON_HOME=<Python3 HOME> #ex : python3
b) DATA_API_PREFIX=<HOSTNAME:microservice-evaluator_PORT_NO> #This project has dependency with microservice-evaluator project listed at the very beginning. Use the code2crud container name and port
c) DB_HOST=<mongodb> #Mongo Docker name
d) DB_PORT=<DB Port> #By default its 27017
e) DB_NAME=<DBName> #Default is cmaprod but can be customized
f) DB_USER=<pwd> #Optional authentican can be enabled on mongo server Default appmodAdmin
g) DB_PASSWORD=<pwd> #Optional authentican can be enabled on mongo server Default AppMod123
```
**Note : Update/Verify DATA_API_PREFIX, DB_HOST ** before docker run at step 4.

3. Build the Docker and bring up the application

3a)Create Network bridge [It must have done while building code2rud in step1 itself]
```
cd WORK_DIR

docker network create -d bridge cma-network
```

3b) Download and Run Public Mongo Docker Image
```
docker run --name cma-mongo -d -p 27017:27017 --net=cma-network -v ~/data:/data/db mongo:3.4-xenial
mongo --port 27017
use cmaprod
db.createCollection("m2m_analyses")
db.createCollection("m2m_overlays")
db.createCollection("m2m_partitions")
db.createCollection("m2m_projects")
```
Optionally enable authentication on mongo
```
use admin
db.createUser({user: "appmodAdmin",pwd: "AppMod123", roles: [ { role: "userAdminAnyDatabase", db: "admin" }, {role: "readWriteAnyDatabase", db: "admin"} ]})
```

3c)Build CMA Api Docker
```
mv microservice-recommender-api/Dockerfile ./
mv microservice-recommender-api/requirements.txt ./
docker build --tag m2m .
cp microservice-recommender-api/dot-env-example .env
```

The build process takes a few minutes to complete.

4. Bring up the Docker container once built successfully. Pass the env file during its run

```
docker run --name cma --env-file .env -p 8081:8081 --net=cma-network --detach -t m2m
```

#### Kubernetes Setup:

Prerequisites :
1. Namespace with name igm4c
2. pvc with name igm4c-pvc
3. Make necessary change to the values in deploy folder.

```
deploy-configmaps.yml 
kubectl apply -f deploy 
```

**The application can be accessed from *localhost:8081/swagger/index.html***

Swagger Documentation for Microservices Recommendation API's

## Supported APIs

The tool supports the following set of APIs. It is recommended to call them in the order introduced:

### Registration APIs

A project needs to be registered with the tool before any analyses can be performed. We support 3 ways to register based on the available project artifacts.
We introduced the available APIs and the mandatory arguments below. Only one of these APIs would need to be called under in most scenarios.

#### Registration via Git URL
```
/msr/init/registerGitProject

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
gitURL: The Git URL when the project can be accessed from
```

#### Registering a Local project from the file system
```
/msr/init/registerLocalProject

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
sourceLocation: The location in the local file system where the project is available
```

#### Registration via Project Binaries (.ear, .war, ..)
```
/msr/init/registerSourceProject  

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
sourceZip: The project binary/source in a packaged form
```

Once the project has been registered, a projectId is generated which is used as input to all subsequent APIs.

### Analysis APIs

We perform several forms of static analysis after a project is registered. The APIs to perform these analyses are expected to be called in the order introduced.
The result of executing these APIs is generated in the backend and the only output returned to the user is the status of the call.

#### Collect 3rd party API information
```
/msr/staticanalysis/getJarAPIInfo

projectId: The project ID generated after project registration
```

#### Collect API usage statistics within the project
```
/msr/staticanalysis/getAPIUsageInfo

projectId: The project ID generated after project registration
```

#### Collect Inter-class Usage information
```
/msr/staticanalysis/getInterClassUsage

projectId: The project ID generated after project registration
```

### Partitioning APIs

Finally after collecting all information via Static Analysis, we are ready to generate partitions. We provide different APIs that use different algorithms to generate candidate partitions.
The APIs may be called in any order based on the requirements.

#### Perform Affinity based Partitioning
```
/msr/partition/runAffinityPartitioning

projectId: The project ID generated after project registration
```

#### Perform Community based Partitioning
```
/msr/partition/CommunityDetection

projectId: The project ID generated after project registration
```# microservice-recommender-api

This project has dependency with the microservice-evaluator project. The interaction happens over API. Therefore, follow the steps provided below to host microservice-evaluator project before deploying microservice-recommender-api project. 
### Setup the microservice-evaluator project by following the instructions here:
[https://github.ibm.com/app-modernization/microservices-evaluator/blob/think-demo/README.md]

The port at which microservice-evaluator will be enabled is necessary for microservice-recommender-api project. It will be required to be inserted into the msr.property file covered in step 2



### Setting up the M2M Application

The following are the sequence of steps to build and start the M2M application. We refer to the current directory as **WORK_DIR**.

1. Begin by checking out the following projects:

```
git clone git@github.ibm.com:app-modernization/appmod-common.git
git checkout think-demo
git clone git@github.ibm.com:app-modernization/microservice-recommender.git
git checkout think-demo
git clone git@github.ibm.com:app-modernization/microservice-recommender-api.git
git checkout think-demo
```

2. Next, generate a copy of the files (python scripts, stop words etc.) and update if needed (no update should be needed typically):

```
mkdir msrresources
cp microservice-recommender/src/main/resources/*.txt msrresources #filter resources
cp microservice-recommender/src/main/resources/python/* msrresources #clustering algos
cp microservice-recommender/src/main/resources/ui/* msrresources #intermediate viz component
```

#### Docker Setup:

In keeping with [12 Factor](12factor.net) guidelines for cloud native applications, this service uses environment variables to obtain configuration information. Environment variables should be stored in a private file called `.env` which should be placed on the local filesystema and never checked into an SCM like GitHub.  A sample of what should be in this file can be found in `dot-env-example` in this repo.

You should copy this to `.env` and change the values of the environment variables for your deployment:
```sh
cp microservice-recommender-api/dot-env-example .env
vi .env
```

This is what the sample looks like:
```
a) PYTHON_HOME=<Python3 HOME> #ex : python3
b) DATA_API_PREFIX=<HOSTNAME:microservice-evaluator_PORT_NO> #This project has dependency with microservice-evaluator project listed at the very beginning. Use the code2crud container name and port
c) DB_HOST=<mongodb> #Mongo Docker name
d) DB_PORT=<DB Port> #By default its 27017
e) DB_NAME=<DBName> #Default is cmaprod but can be customized
f) DB_USER=<pwd> #Optional authentican can be enabled on mongo server Default appmodAdmin
g) DB_PASSWORD=<pwd> #Optional authentican can be enabled on mongo server Default AppMod123
```
**Note : Update/Verify DATA_API_PREFIX, DB_HOST ** before docker run at step 4.

3. Build the Docker and bring up the application

3a)Create Network bridge [It must have done while building code2rud in step1 itself]
```
cd WORK_DIR

docker network create -d bridge cma-network
```

3b) Download and Run Public Mongo Docker Image
```
docker run --name cma-mongo -d -p 27017:27017 --net=cma-network -v ~/data:/data/db mongo:3.4-xenial
mongo --port 27017
use cmaprod
db.createCollection("m2m_analyses")
db.createCollection("m2m_overlays")
db.createCollection("m2m_partitions")
db.createCollection("m2m_projects")
```
Optionally enable authentication on mongo
```
use admin
db.createUser({user: "appmodAdmin",pwd: "AppMod123", roles: [ { role: "userAdminAnyDatabase", db: "admin" }, {role: "readWriteAnyDatabase", db: "admin"} ]})
```

3c)Build CMA Api Docker
```
mv microservice-recommender-api/Dockerfile ./
mv microservice-recommender-api/requirements.txt ./
docker build --tag m2m .
cp microservice-recommender-api/dot-env-example .env
```

The build process takes a few minutes to complete.

4. Bring up the Docker container once built successfully. Pass the env file during its run

```
docker run --name cma --env-file .env -p 8081:8081 --net=cma-network --detach -t m2m
```

#### Kubernetes Setup:

Prerequisites :
1. Namespace with name igm4c
2. pvc with name igm4c-pvc
3. Make necessary change to the values in deploy folder.

```
deploy-configmaps.yml 
kubectl apply -f deploy 
```

**The application can be accessed from *localhost:8081/swagger/index.html***

Swagger Documentation for Microservices Recommendation API's

## Supported APIs

The tool supports the following set of APIs. It is recommended to call them in the order introduced:

### Registration APIs

A project needs to be registered with the tool before any analyses can be performed. We support 3 ways to register based on the available project artifacts.
We introduced the available APIs and the mandatory arguments below. Only one of these APIs would need to be called under in most scenarios.

#### Registration via Git URL
```
/msr/init/registerGitProject

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
gitURL: The Git URL when the project can be accessed from
```

#### Registering a Local project from the file system
```
/msr/init/registerLocalProject

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
sourceLocation: The location in the local file system where the project is available
```

#### Registration via Project Binaries (.ear, .war, ..)
```
/msr/init/registerSourceProject  

projectName: A name to identify the project with
projectDescription: A description of the project
sourceLanguage: The primary language of the source code of the project
sourceZip: The project binary/source in a packaged form
```

Once the project has been registered, a projectId is generated which is used as input to all subsequent APIs.

### Analysis APIs

We perform several forms of static analysis after a project is registered. The APIs to perform these analyses are expected to be called in the order introduced.
The result of executing these APIs is generated in the backend and the only output returned to the user is the status of the call.

#### Collect 3rd party API information
```
/msr/staticanalysis/getJarAPIInfo

projectId: The project ID generated after project registration
```

#### Collect API usage statistics within the project
```
/msr/staticanalysis/getAPIUsageInfo

projectId: The project ID generated after project registration
```

#### Collect Inter-class Usage information
```
/msr/staticanalysis/getInterClassUsage

projectId: The project ID generated after project registration
```

### Partitioning APIs

Finally after collecting all information via Static Analysis, we are ready to generate partitions. We provide different APIs that use different algorithms to generate candidate partitions.
The APIs may be called in any order based on the requirements.

#### Perform Affinity based Partitioning
```
/msr/partition/runAffinityPartitioning

projectId: The project ID generated after project registration
```

#### Perform Community based Partitioning
```
/msr/partition/CommunityDetection

projectId: The project ID generated after project registration
```
