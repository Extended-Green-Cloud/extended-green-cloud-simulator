#!/bin/bash
echo "Recompiling backend application"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

mvn clean compile package

# COPY CONFIGURATION FILES TO STRATEGY FOLDER
cp -R ./engine/src/main/resources/scenarios ./engine/strategy
cp -R ./engine/src/main/resources/properties ./engine/strategy

echo "Recompilation of the backend application completed"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
