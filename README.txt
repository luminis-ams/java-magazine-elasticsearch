This repository accompanies an article I have written for Java Magazine. It shows some basic usage of interacting with elasticsearch from a standard Java Application. You can so a query combined with a filter and ask for some aggregations.

The repos contains a reader for the java magazines from the nljug website. It scrapes the content and puts it into an elasticsearch index.

# Building the sample
The project makes use of maven to build an executable jar.

# Running the sample
There are two classes that you can run:
JavaMagazineReader: Imports all articles into a new index.
JavaMagazineRunner: Starts a very basic command line client that connects to your cluster.

If you want to change the name of the cluster, change the Constants class.

# Running the unit/integration tests
Use maven to run the unit / integration tests.

# Docker
We have provided you with a docker-compose configuration file. Using this file you can start-up two docker containers. One for Elasticsearch and one for Kibana.

If you have docker-compose installed you can change to the docker directory and just run:

$ docker-compose up