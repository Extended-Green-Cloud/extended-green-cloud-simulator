#!/bin/bash
docker image rm green-cloud/backend
docker image rm green-cloud/frontend
docker image rm green-cloud/socket-server
mvn clean package
