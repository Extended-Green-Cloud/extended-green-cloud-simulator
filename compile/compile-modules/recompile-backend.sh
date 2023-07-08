#!/bin/bash
echo "Recompiling backend application"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

mvn clean compile package
echo "Recompilation of the backend application completed"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
