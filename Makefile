# These can be overidden with env vars.
PROJECT ?= appmod
VERSION := $(shell cat VERSION)
TESTIMAGE ?= msr

.PHONY: build test

all: clean depend build

clean:
	echo Y | docker image prune
	echo Y | docker volume prune
	rm -rf build/*
	rm -rf tmp/volumes/cmaimport
	# Need something to clean any image tarballs that might be there

depend:
	mkdir -p build/msrresources
	# TODO: This is kind of nasty since I need to know about the guts of the other repo.
	# The other repo should have an build target, which should produce the resources and jars my build needs
	cp -r ../microservice-recommender/src/main/resources/*.txt build/msrresources #filter resources
	cp -R ../microservice-recommender/src/main/resources/python build/msrresources #clustering algos
	cp -R ../microservice-recommender/src/main/resources/ui/* build/msrresources #intermediate viz component
	cp -R ../appmod-common build/appmod-common
	cp -R ../microservice-recommender build/microservice-recommender
	docker build -t msr_depend -f docker/Dockerfile.depend  .
	docker run -v ${BASE}/build:/build msr_depend:latest
	# Create the local folder for the shared volume
	mkdir -p tmp/volumes/cmaimport/cma

build:
	docker-compose -p $(PROJECT) build --build-arg VERSION=$(VERSION)

deepclean: clean
	rm -rf target
	
test:
	cd test; ./runtests ${BASE}/test/tmp; cd ..
	# Need to put stuff in here, for example:
	# mvn test
	# nosetests (in the right folder - for Py unit tests)
	# run all the test scripts in tests (e.g. integration tests)

testclean:
	rm -rf test/tmp/*
