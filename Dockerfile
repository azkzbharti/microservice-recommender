# Build the java projects
FROM maven:3.6-jdk-8 AS build

COPY appmod-common ./appmod-common
COPY microservice-recommender ./microservice-recommender
COPY microservice-recommender-api ./microservice-recommender-api

RUN mvn -f ./appmod-common/pom.xml  clean package install
RUN mvn -f ./microservice-recommender/pom.xml clean package install
RUN mvn -f ./microservice-recommender-api/pom.xml clean package install

FROM python:3.6-slim as python
COPY requirements.txt ./requirements.txt
RUN pip install -r ./requirements.txt

FROM openjdk:8-jre-alpine
COPY --from=python / /

WORKDIR ./app
COPY msrresources/ ./app/msrresources

COPY --from=build ./microservice-recommender-api/target/microservice-recommender-api-0.0.1.jar ./app/microservice-recommender-api-0.0.1.jar
RUN chmod -R 775 /app

# Make the port available
EXPOSE 8081

CMD java -DMSR_HOME=./app/msrresources -jar ./app/microservice-recommender-api-0.0.1.jar


# docker run --name cma-mongo -d -p 27017:27017 -v ~/data:/data/db mongo:3.4-xenial
# docker run --name cma-mongo -d -p 27018:27017 -v ~/data:/data/db mongo:3.4-xenial [open different container port]
# docker run --name cma-mongo -d -p 27017:27017 -v ~/data:/data/db mongo:3.4-xenial && bash -c "echo 'waiting 15 seconds for couchdb to initialize...'; sleep 15 && mongo && use appmoddev1
# mongo [for default port 27017]

# docker run --name cma-mongo -d -p 27017:27017 -v ~/data:/data/db mongo:3.4-xenial
# mongo --port 27017
# use appmoddev
# db.createCollection("m2m_analyses")
# db.createCollection("m2m_overlays")
# db.createCollection("m2m_partitions")
# db.createCollection("m2m_projects")

# use admin
# db.createUser({user: "admin",pwd: "appmodAdmin", roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]})

# db.adminCommand( { shutdown: 1 } )
