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
We have provided you with a docker-compose configuration file. Using this file you can start-up two docker containers. One for Elasticsearch and one for Kibana. It could be that the IP range used to connect both containers is different for you. Please check the used ip address from the elasticsearch container and use that in the compose configuration for Kibana.

```
> docker ps
CONTAINER ID        IMAGE                                                 COMMAND                  CREATED             STATUS              PORTS                                            NAMES
4edb5032a6d0        docker.elastic.co/kibana/kibana:5.3.0                 "/bin/sh -c /usr/l..."   4 minutes ago       Up 4 minutes        0.0.0.0:5601->5601/tcp                           docker_kibana_1
08add2b4317e        docker.elastic.co/elasticsearch/elasticsearch:5.3.0   "/bin/bash bin/es-..."   7 minutes ago       Up 4 minutes        0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp   docker_elasticsearch_1

> docker inspect -f '{{ .NetworkSettings.Networks.docker_esnet.IPAddress }}' 08add2b4317e
172.19.0.2
```

If the final IPAddress is not the same as in the compose file for: ELASTICSEARCH_URL: http://172.19.0.2:9200, change the ELASTICSEARCH_URL to match your IP.

## Note
I would like to provided a compose file that configures the subnet, but I cannot make it work. So if you have an idea. Please let me know. I tried this but it did not work.

```
networks:
  esnet:
    driver: bridge
    ipam:
      driver: default
      config:
        - subnet: 172.20.0.0/16
          gateway: 172.20.0.1
```