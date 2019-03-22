#!/bin/bash
# https://github.com/oracle/docker-images/blob/master/OracleDatabase/SingleInstance/README.md#running-oracle-database-enterprise-and-standard-edition-2-in-a-docker-container
docker run --name jdbc-oracle \
 -p 1521:1521 -p 5500:5500 \
 --shm-size=1g \
 -v $(pwd)/oracle:/docker-entrypoint-initdb.d/setup \
 -d oracle/database:18.3.0-se2
