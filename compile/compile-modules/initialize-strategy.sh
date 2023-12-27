#!/bin/bash
echo "Compiling strategy"

# NAVIGATE TO MAIN PROJECT DIR
PROJECT_DIR=$(pwd)
PARENT_DIR=${PROJECT_DIR%/"green-cloud"*}
cd "${PARENT_DIR}/green-cloud" || exit

mvn package
echo "Finished preparing strategy jar. Jar can be found in /engine/target directory"

# NAVIGATE BACK TO COMPILE DIRECTORY
cd ./compile || exit
